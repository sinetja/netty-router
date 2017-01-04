/*
 * This file is part of the netty-router package.
 * 
 * (c) Richard Lea <chigix@zoho.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package io.netty.handler.routing;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.text.MessageFormat;

/**
 *
 * @author Richard Lea <chigix@zoho.com>
 */
public class RoutingExceptionHandler extends ChannelHandlerAdapter {

    private static final InternalLogger LOG = InternalLoggerFactory.getInstance(RoutingException.class);

    @Override
    public final void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof UnableRoutingMessageException) {
            handleUnableRouting(ctx, (UnableRoutingMessageException) msg);
            ReferenceCountUtil.release(msg);
            return;
        }
        if (msg instanceof RoutingException) {
            handleRoutingException(ctx, (RoutingException) msg);
            ReferenceCountUtil.release(msg);
            return;
        }
        super.channelRead(ctx, msg);
    }

    public void handleRoutingException(ChannelHandlerContext ctx, RoutingException exc) {
        LOG.error(exc);
    }

    public void handleUnableRouting(ChannelHandlerContext ctx, UnableRoutingMessageException exc) {
        LOG.warn(MessageFormat.format("One http message is trying to be put in a closed channel: {0}", ctx.channel().id()));
    }

}
