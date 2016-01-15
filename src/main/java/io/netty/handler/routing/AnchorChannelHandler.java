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
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.text.MessageFormat;
import java.util.UUID;

/**
 *
 * @author Richard Lea <chigix@zoho.com>
 */
class AnchorChannelHandler extends ChannelHandlerAdapter {

    private static final InternalLogger LOG = InternalLoggerFactory.getInstance(AnchorChannelHandler.class);

    private final String anchorName;

    private ChannelHandlerContext ctx;

    private final RoutingPipeline parentPipeline;

    public AnchorChannelHandler(RoutingPipeline parent) {
        this.ctx = null;
        this.anchorName = UUID.randomUUID().toString();
        this.parentPipeline = parent;
    }

    public ChannelHandlerContext getContext() {
        return ctx;
    }

    public String getAnchorName() {
        return anchorName;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        //LOG.debug(MessageFormat.format("HANDLER ADDED:[AnchorChannelHandler#{0}#{1}]", this.getAnchorName(), this.parentPipeline.getPipelineName()));
    }

}
