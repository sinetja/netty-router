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
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

/**
 *
 * @author Richard Lea <chigix@zoho.com>
 */
public class DefaultHttpExceptionHandler extends SimpleChannelInboundHandler<HttpException> {

    private static final InternalLogger LOG = InternalLoggerFactory.getInstance(DefaultHttpExceptionHandler.class);

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, HttpException msg) throws Exception {
        //this.error(MessageFormat.format("EXCEPTIONCAUGHT: [{1}] {0}", exc.getCause().getMessage(), exc.getSource().getClass().getName()), exc.getCause());
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, Unpooled.copiedBuffer("Failure: " + HttpResponseStatus.INTERNAL_SERVER_ERROR.toString() + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

}
