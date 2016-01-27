/*
 * This file is part of the netty-router package.
 * 
 * (c) Richard Lea <chigix@zoho.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package io.netty.handler.codec.http.router;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderUtil;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.router.exceptions.NotFoundException;
import io.netty.handler.routing.RoutingException;
import io.netty.handler.routing.SimpleCycleRouter;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author Richard Lea <chigix@zoho.com>
 */
public class HttpRouter extends SimpleCycleRouter<HttpRequest, LastHttpContent> {

    private static final InternalLogger LOG = InternalLoggerFactory.getInstance(HttpRouter.class);

    private final RoutingIndex matcherIndex = new RoutingIndex();

    /**
     * @TODO test the remove time schedule.
     */
    private final Map<Channel, ActiveRoutedEntry> activeRouted = new HashMap<Channel, ActiveRoutedEntry>();

    private final ConcurrentMap<Channel, ConcurrentMap<String, Routing>> configuredPipeline = new ConcurrentHashMap<Channel, ConcurrentMap<String, Routing>>();

    private final ConcurrentMap<HttpRequest, HttpRouted> routedCollection = new ConcurrentHashMap<HttpRequest, HttpRouted>();

    public HttpRouter() {
        super(false, "HttpRouter");
    }

    @Override
    public void write(final ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof HttpException) {
            HttpException httpexc = (HttpException) msg;
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, httpexc.getResponseCode(), Unpooled.copiedBuffer("Failure: " + httpexc.getMessage() + "\r\n", CharsetUtil.UTF_8));
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            LOG.error(MessageFormat.format("Unprocessed WrappedException occured through {0}, with the request-message:{1}", ((HttpException) msg).getMatchedRouting().getName(), this.activeRouted.get(ctx.channel())), (Throwable) msg);
            return;
        } else if (msg instanceof Exception) {
            this.exceptionForward(new HttpException((Throwable) msg) {

                @Override
                public HttpRequest getHttpRequest() {
                    return activeRouted.get(ctx.channel()).getMessage();
                }

                @Override
                public Routing getMatchedRouting() {
                    return activeRouted.get(ctx.channel()).getRouting();
                }

            });
            LOG.debug("EXCEPTION WRITE:[RouterHandler]");
            return;
        }
        super.write(ctx, msg, promise);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        this.activeRouted.remove(ctx.channel());
        this.configuredPipeline.remove(ctx.channel());
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //this.pipelineForward(this.exceptionPipeline, new WrappedException(this.activeRouted.get(ctx.channel()).getMessage(), this.activeRouted.get(ctx.channel()).getRouting(), cause));
        //this.activeRouted.remove(ctx.channel());
        LOG.debug("EXCEPTION CAUGHT:[RouterHandler]");
        final RoutingException exc = (RoutingException) cause;
        if (exc.unwrapException() instanceof HttpException) {
            super.exceptionForward(exc.unwrapException());
        } else {
            super.exceptionForward(new HttpException(exc.unwrapException()) {

                @Override
                public HttpRequest getHttpRequest() {
                    return activeRouted.get(ctx.channel()).getMessage();
                }

                @Override
                public Routing getMatchedRouting() {
                    return matcherIndex.ROUTING_IDENTITIES.get(exc.getRoutingName());
                }
            });
        }
    }

    @Override
    protected final void initRouter(ChannelHandlerContext ctx) throws Exception {
        this.configuredPipeline.put(ctx.channel(), new ConcurrentHashMap<String, Routing>());
        this.initRoutings(ctx, this);
    }

    protected void initRoutings(ChannelHandlerContext ctx, HttpRouter router) {
    }

    /**
     * The only suggestion to define new HTTP routing.
     *
     * @param ctx
     * @param routingConf
     */
    protected void newRouting(ChannelHandlerContext ctx, RoutingConfig routingConf) {
        for (HttpMethod configureMethod : routingConf.configureMethods()) {
            RoutingPathMatcher matcher;
            if ((matcher = this.matcherIndex.PATH_MATCHERS.get(configureMethod.toString())) == null) {
                // @TODO is it necessary to throw unsupport method exception?
            } else {
                Routing routing = new Routing(routingConf, configureMethod);
                this.newRouting(ctx, routing.getIdentity());
                matcher.add(routing);
                this.matcherIndex.ROUTING_IDENTITIES.put(routing.getIdentity(), routing);
            }
        }
    }

    protected void newCONNECT(ChannelHandlerContext ctx, RoutingConfig.CONNECT connectConfig) {
        this.newRouting(ctx, connectConfig);
    }

    protected void newDELETE(ChannelHandlerContext ctx, RoutingConfig.DELETE connectConfig) {
        this.newRouting(ctx, connectConfig);
    }

    protected void newGET(ChannelHandlerContext ctx, RoutingConfig.GET connectConfig) {
        this.newRouting(ctx, connectConfig);
    }

    protected void newHEAD(ChannelHandlerContext ctx, RoutingConfig.HEAD connectConfig) {
        this.newRouting(ctx, connectConfig);
    }

    protected void newOPTIONS(ChannelHandlerContext ctx, RoutingConfig.OPTIONS connectConfig) {
        this.newRouting(ctx, connectConfig);
    }

    protected void newPATCH(ChannelHandlerContext ctx, RoutingConfig.PATCH connectConfig) {
        this.newRouting(ctx, connectConfig);
    }

    protected void newPOST(ChannelHandlerContext ctx, RoutingConfig.POST connectConfig) {
        this.newRouting(ctx, connectConfig);
    }

    protected void newPUT(ChannelHandlerContext ctx, RoutingConfig.PUT connectConfig) {
        this.newRouting(ctx, connectConfig);
    }

    protected void newTRACE(ChannelHandlerContext ctx, RoutingConfig.TRACE connectConfig) {
        this.newRouting(ctx, connectConfig);
    }

    @Override
    protected void initExceptionRouting(ChannelPipeline pipeline) {
        pipeline.addLast(new DefaultHttpExceptionHandler());
    }

    @Override
    protected void route(ChannelHandlerContext ctx, Object msg, Map<String, ChannelPipeline> routingPipelines) throws Exception {
        // @TODO To discuss if need to support "Expect: 100- continue" or not
        if (msg instanceof HttpMessage && HttpHeaderUtil.is100ContinueExpected((HttpMessage) msg)) {
            ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE));
            return;
        }
        super.route(ctx, msg, routingPipelines);
    }

    @Override
    protected final ChannelPipeline routeBegin(ChannelHandlerContext ctx, HttpRequest msg, Map<String, ChannelPipeline> routingPipelines) throws Exception {
        this.activeRouted.put(ctx.channel(), new ActiveRoutedEntry(null, msg));
        // Route
        final QueryStringDecoder qsd = new QueryStringDecoder(msg.uri());
        final RoutingPathMatcher matcher;
        final RoutingPathMatched routed;
        if ((matcher = this.matcherIndex.PATH_MATCHERS.get(msg.method().toString())) == null) {
            // @TODO Method not support and write this error back.
            this.exceptionForward(new NotFoundException(qsd.path(), msg));
            return null;
        } else {
            routed = matcher.match(qsd.path());
        }
        if (routed == null) {
            this.exceptionForward(new NotFoundException(qsd.path(), msg));
            return null;
        }
        this.activeRouted.put(ctx.channel(), new ActiveRoutedEntry(routed.getRouting(), msg));
        if (null == this.configuredPipeline.get(ctx.channel()).putIfAbsent(routed.getRouting().getIdentity(), routed.getRouting())) {
            // Add Handlers
            routed.getRouting().unwrap().configurePipeline(routingPipelines.get(routed.getRouting().getIdentity()));
        }
        this.routedCollection.put(msg, new HttpRouted(routed, msg));
        return routingPipelines.get(routed.getRouting().getIdentity());
    }

    @Override
    protected final boolean routeEnd(ChannelHandlerContext ctx, LastHttpContent msg) throws Exception {
        return true;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, HttpRequest in, List<Object> out) throws Exception {
        HttpRouted routed;
        if ((routed = this.routedCollection.get(in)) instanceof HttpRouted) {
            this.routedCollection.remove(in);
            out.add(routed);
        } else {
            out.add(in);
        }
    }

    private class RoutingIndex {

        public final Map<String, RoutingPathMatcher> PATH_MATCHERS = new HashMap<String, RoutingPathMatcher>();

        public final Map<String, Routing> ROUTING_IDENTITIES = new HashMap<String, Routing>();

        public RoutingIndex() {
            PATH_MATCHERS.put(HttpMethod.CONNECT.toString(), new RoutingPathMatcher());
            PATH_MATCHERS.put(HttpMethod.DELETE.toString(), new RoutingPathMatcher());
            PATH_MATCHERS.put(HttpMethod.GET.toString(), new RoutingPathMatcher());
            PATH_MATCHERS.put(HttpMethod.HEAD.toString(), new RoutingPathMatcher());
            PATH_MATCHERS.put(HttpMethod.OPTIONS.toString(), new RoutingPathMatcher());
            PATH_MATCHERS.put(HttpMethod.PATCH.toString(), new RoutingPathMatcher());
            PATH_MATCHERS.put(HttpMethod.POST.toString(), new RoutingPathMatcher());
            PATH_MATCHERS.put(HttpMethod.PUT.toString(), new RoutingPathMatcher());
            PATH_MATCHERS.put(HttpMethod.TRACE.toString(), new RoutingPathMatcher());
        }

    }

    private class ActiveRoutedEntry {

        private final Routing pattern;
        private final HttpRequest message;

        public ActiveRoutedEntry(Routing pattern, HttpRequest message) {
            this.pattern = pattern;
            this.message = message;
        }

        public Routing getRouting() {
            return pattern;
        }

        public HttpRequest getMessage() {
            return message;
        }

    }

}
