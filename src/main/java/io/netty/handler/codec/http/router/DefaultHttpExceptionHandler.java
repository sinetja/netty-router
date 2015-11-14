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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Richard Lea <chigix@zoho.com>
 */
public class DefaultHttpExceptionHandler extends SimpleChannelInboundHandler<RouterHandler.WrappedException> {

    private static final Logger LOG = Logger.getLogger(DefaultHttpExceptionHandler.class.getName());

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, RouterHandler.WrappedException exc) throws Exception {
        this.error(exc);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, Unpooled.copiedBuffer("Failure: " + HttpResponseStatus.INTERNAL_SERVER_ERROR.toString() + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    protected void info(String msg) {
        LOG.info(msg);
    }

    protected void warn(String msg) {
        LOG.warning(msg);
    }

    protected void error(Throwable cause) {
        LOG.log(Level.SEVERE, null, cause);
    }

    protected void error(String msg, Throwable cause) {
        LOG.log(Level.SEVERE, msg, cause);
    }

}
