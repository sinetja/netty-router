/*
 * This file is part of the netty-router package.
 * 
 * (c) Richard Lea <chigix@zoho.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package io.netty.handler.codec.http.router;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.TypeParameterMatcher;

/**
 * HttpExceptionHandler which allows only handle specific type of
 * {@code EXCEPTION}
 *
 * @author Richard Lea <chigix@zoho.com>
 * @param <EXCEPTION>
 */
public abstract class HttpExceptionInboundHandler<EXCEPTION> extends ChannelHandlerAdapter {

    private final TypeParameterMatcher typeMatcher = TypeParameterMatcher.find(this, HttpExceptionInboundHandler.class, "EXCEPTION");

    @Override
    public final void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof HttpException)) {
            super.channelRead(ctx, msg);
            return;
        }
        HttpException http_exception = (HttpException) msg;
        if (typeMatcher.match(http_exception.getCause())) {
            handleException(ctx, http_exception);
        } else {
            super.channelRead(ctx, msg);
        }
    }

    protected abstract void handleException(ChannelHandlerContext ctx, HttpException exc);

}
