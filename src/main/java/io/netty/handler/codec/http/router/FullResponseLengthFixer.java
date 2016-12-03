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
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;

/**
 *
 * @author Richard Lea <chigix@zoho.com>
 */
public class FullResponseLengthFixer extends ChannelHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof FullHttpResponse) {
            FullHttpResponse resp = (FullHttpResponse) msg;
            resp.headers().set(HttpHeaderNames.CONTENT_LENGTH, resp.content().writerIndex() + "");
        }
        super.write(ctx, msg, promise);
    }

}
