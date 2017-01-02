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
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.router.exceptions.BadRequestException;
import io.netty.handler.codec.http.router.exceptions.NotFoundException;
import io.netty.handler.codec.http.router.exceptions.UnsupportedMethodException;
import io.netty.handler.routing.RoutingException;
import io.netty.handler.routing.SimpleCycleRouter;
import io.netty.handler.routing.UnableRoutingMessageException;
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

    private final RoutingIndex methodMatcher = new RoutingIndex();

    /**
     * @TODO test the remove time schedule when channel inactived.
     */
    private final Map<Channel, HttpRouted> activeRouted = new HashMap<Channel, HttpRouted>();

    private final ConcurrentMap<Channel, ConcurrentMap<String, Routing>> configuredPipeline = new ConcurrentHashMap<Channel, ConcurrentMap<String, Routing>>();

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
            LOG.error((Throwable) msg);
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
        if (cause instanceof HttpException) {
            super.exceptionForward(cause);
            return;
        } else if (cause instanceof DecoderException && !ctx.channel().isOpen()) {
            return;
        } else if (cause instanceof UnableRoutingMessageException && !ctx.channel().isOpen()) {
            UnableRoutingMessageException exc = (UnableRoutingMessageException) cause;
            if (!(exc.getRoutingMessage() instanceof HttpObject)) {
                LOG.warn(MessageFormat.format("Unexpected message[{1}] is trying to be put in a closed channel: {0}", ctx.channel().id(), exc.getRoutingMessage().getClass().getName()));
                return;
            }
            LOG.warn(MessageFormat.format("One http message is trying to be put in a closed channel: {0}", ctx.channel().id()));
            return;
        } else if (!(cause instanceof RoutingException)) {
            LOG.error("Bomb!!!!--Unwrapped exception was throwed:", cause);
            super.exceptionForward(new HttpException(cause) {
                @Override
                public HttpRequest getHttpRequest() {
                    return activeRouted.get(ctx.channel()).getRequestMsg();
                }

                @Override
                public Routing getMatchedRouting() {
                    throw new UnsupportedOperationException("Not supported for uncached exception.");
                }
            });
            return;
        }
        final RoutingException exc = (RoutingException) cause;
        if (exc.unwrapException() instanceof HttpException) {
            super.exceptionForward(exc.unwrapException());
        } else {
            super.exceptionForward(new HttpException(exc.unwrapException()) {

                @Override
                public HttpRequest getHttpRequest() {
                    return activeRouted.get(ctx.channel()).getRequestMsg();
                }

                @Override
                public Routing getMatchedRouting() {
                    return methodMatcher.PATH_ROUTING_IDENTITIES.get(exc.getRoutingName());
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
            if ((matcher = this.methodMatcher.PATH_MATCHERS.get(configureMethod.toString())) == null) {
                // @TODO is it necessary to throw unsupport method exception?
            } else {
                Routing routing = new Routing(routingConf, configureMethod);
                this.newRouting(ctx, routing.getIdentity());
                matcher.add(routing);
                this.methodMatcher.PATH_ROUTING_IDENTITIES.put(routing.getIdentity(), routing);
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
        pipeline.addLast(new SimpleHttpExceptionHandler());
    }

    @Override
    protected final ChannelPipeline routeBegin(ChannelHandlerContext ctx, HttpRequest msg, Map<String, ChannelPipeline> routingPipelines) throws Exception {
        if (!msg.decoderResult().isSuccess()) {
            throw new BadRequestException("Request Decoded Failure", msg);
        }
        // Route
        final QueryStringDecoder qsd = new QueryStringDecoder(msg.uri());
        final RoutingPathMatcher matcher;
        final RoutingPathMatched matched_info;
        // PATH_MATCHER is only featured with comparing path, 
        // so method support have to be checked here.
        if ((matcher = this.methodMatcher.PATH_MATCHERS.get(msg.method().toString())) == null) {
            throw new UnsupportedMethodException(msg);
        } else {
            matched_info = matcher.match(qsd.path());
        }
        if (matched_info == null) {
            throw new NotFoundException(qsd.path(), msg);
        }
        if (null == this.configuredPipeline.get(ctx.channel()).putIfAbsent(matched_info.getRouting().getIdentity(), matched_info.getRouting())) {
            // Add Handlers
            matched_info.getRouting().unwrap().configurePipeline(routingPipelines.get(matched_info.getRouting().getIdentity()));
        }
        this.activeRouted.put(ctx.channel(), new HttpRouted(matched_info, msg, ctx));
        return routingPipelines.get(matched_info.getRouting().getIdentity());
    }

    @Override
    protected final boolean routeEnd(ChannelHandlerContext ctx, LastHttpContent msg) throws Exception {
        return true;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, Object in, List<Object> out) throws Exception {
        HttpRouted routed = activeRouted.get(ctx.channel());
        if (this.activeRouted.get(ctx.channel()) == null) {
            throw new Exception("EMERGENCY: HttpRouted object could not be found in HttpRouter decoding. Please fix it firstly.");
        }
        if (!(in instanceof HttpRequest)) {
            if (!routed.isDenied) {
                out.add(in);
            }
            return;
        }
        if (in == routed.getRequestMsg()) {
            out.add(routed);
            return;
        }
        throw new Exception("EMERGENCY: Previous HttpRouted info is being used, however next HttpRequest message have been received.");
    }

    /**
     * A aggregator with HttpRequest message, ChannelHandlerContext and Routing
     * info.
     */
    private class HttpRouted extends io.netty.handler.codec.http.router.HttpRouted {

        private final RoutingPathMatched matched;

        private final ChannelHandlerContext ctx;

        private boolean isDeniedLocked = false;

        private boolean isDenied = false;

        public HttpRouted(RoutingPathMatched matched, HttpRequest request, ChannelHandlerContext ctx) {
            super(request);
            this.matched = matched;
            this.ctx = ctx;
        }

        @Override
        public Map<String, Object> decodedParams() {
            return matched.decodedParams();
        }

        @Override
        public RoutingConfig unwrapRoutingConf() {
            return matched.getRouting().unwrap();
        }

        @Override
        public String getPatternName() {
            return matched.getRouting().getName();
        }

        @Override
        public void allow() {
            this.isDeniedLocked = true;
            this.isDenied = false;
            super.allow();
        }

        @Override
        public void deny() {
            this.isDeniedLocked = true;
            this.isDenied = true;
            super.deny();
        }

        @Override
        public ChannelHandlerContext getChannelHandlerContext() {
            return this.ctx;
        }

    }

    private class RoutingIndex {

        public final Map<String, RoutingPathMatcher> PATH_MATCHERS = new HashMap<String, RoutingPathMatcher>();

        public final Map<String, Routing> PATH_ROUTING_IDENTITIES = new HashMap<String, Routing>();

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

}
