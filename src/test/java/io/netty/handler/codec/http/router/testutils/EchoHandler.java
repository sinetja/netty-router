/*
 * This file is part of the netty-router package.
 * 
 * (c) Richard Lea <chigix@zoho.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package io.netty.handler.codec.http.router.testutils;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.router.HttpRouted;

/**
 *
 * @author Richard Lea <chigix@zoho.com>
 */
public class EchoHandler extends SimpleChannelInboundHandler<HttpRouted> {

    public static EchoHandler newInstance() {
        return new EchoHandler();
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, HttpRouted msg) throws Exception {
        System.out.println(msg.toString());
    }

}
