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
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderUtil;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.CharsetUtil;
import java.text.MessageFormat;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author Richard Lea <chigix@zoho.com>
 */
@ChannelHandler.Sharable
public class RouterHandler extends SimpleChannelInboundHandler<HttpRequest> {

    private final HttpRouter router;

    private final ChannelHandler exceptionHandler;

    private final String routerRegisteredName;

    private final String routedRegisteredName;

    private final ConcurrentMap<Channel, HttpRequest> activeRequests = new ConcurrentHashMap<Channel, HttpRequest>();

    private Logging logger = new Logging(this);

    public RouterHandler(HttpRouter router, ChannelHandler exceptionHandler) {
        super();
        this.router = router;
        this.exceptionHandler = exceptionHandler;
        this.routerRegisteredName = UUID.randomUUID().toString();
        this.routedRegisteredName = UUID.randomUUID().toString();
    }

    public RouterHandler(HttpRouter router) {
        this(router, new DefaultHttpExceptionHandler());
    }

    /**
     * Get Handler Name
     *
     * @return
     */
    public String getName() {
        return this.routerRegisteredName;
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, HttpRequest msg) throws Exception {
        this.activeRequests.put(ctx.channel(), msg);
        // @TODO To discuss if need to support "Expect: 100- continue" or not
        if (HttpHeaderUtil.is100ContinueExpected(msg)) {
            ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE));
            return;
        }
        // Route
        final QueryStringDecoder qsd = new QueryStringDecoder(msg.uri());
        final Routed<ChannelHandler> routed = this.router.route(msg.method(), qsd.path());
        if (routed == null) {
            throw new NotFoundException(qsd.path());
        }
        this.channelForward(ctx, routed.getTarget(), new HttpHandlerRouted(routed, msg));
    }

    private void channelForward(ChannelHandlerContext ctx, ChannelHandler nextHandler, Object msg) {
        final ChannelHandler addedHandler = ctx.pipeline().get(this.routedRegisteredName);
        if (addedHandler == null) {
            ctx.pipeline().addAfter(this.routerRegisteredName, this.routedRegisteredName, nextHandler);
        } else {
            ctx.pipeline().replace(addedHandler, this.routedRegisteredName, nextHandler);
        }
        ctx.fireChannelRead(msg);
    }

    /**
     * Suggested to register this to pipeline rather than manually add this to
     * pipeline.
     *
     * @param pipeline
     * @return
     */
    public RouterHandler takePipeline(ChannelPipeline pipeline) {
        pipeline.addLast(this.routerRegisteredName, this);
        return this;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof WrappedException) {
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, Unpooled.copiedBuffer("Failure: " + HttpResponseStatus.INTERNAL_SERVER_ERROR.toString() + "\r\n", CharsetUtil.UTF_8));
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            this.logger.error(MessageFormat.format("Unprocessed WrappedException occured through {0}, with the request-message:{1}", ((WrappedException) msg).getSource().getClass(), this.activeRequests.get(ctx.channel())), (Throwable) msg);
            return;
        } else if (msg instanceof Exception) {
            this.channelForward(ctx, this.exceptionHandler, new WrappedException(this.activeRequests.get(ctx.channel()), ctx.pipeline().get(this.routedRegisteredName), (Throwable) msg));
            return;
        }
        super.write(ctx, msg, promise);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        this.activeRequests.remove(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        this.channelForward(ctx, this.exceptionHandler, new WrappedException(this.activeRequests.get(ctx.channel()), this, cause));
    }

    public class WrappedException extends Exception {

        private final ChannelHandler source;

        private final HttpRequest requestMsg;

        public WrappedException(HttpRequest requestMsg, ChannelHandler source, Throwable cause) {
            super(cause);
            this.source = source;
            this.requestMsg = requestMsg;
        }

        public WrappedException(HttpRequest requestMsg, ChannelHandler source, String message, Throwable cause) {
            super(message, cause);
            this.source = source;
            this.requestMsg = requestMsg;
        }

        public ChannelHandler getSource() {
            return source;
        }

        public HttpRequest getRequestMsg() {
            return requestMsg;
        }
    }

    /**
     * Customize User Logger
     *
     * @param logger
     */
    public void setLogger(Logging logger) {
        this.logger = logger;
    }

}
