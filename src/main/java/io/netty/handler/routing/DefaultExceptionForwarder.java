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
import io.netty.channel.ChannelPipeline;

/**
 * Special handler for catch exceptions and forward to the parent router's
 * exception pipeline.
 *
 * @author Richard Lea <chigix@zoho.com>
 */
public class DefaultExceptionForwarder extends ChannelHandlerAdapter implements PackageDependencyAware {

    private RoutingPipeline parentPipeline;

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (this.parentPipeline == null) {
            throw new Exception("Please check this exception forwarder is added at Routing Pipeline. "
                    + "Null Pipeline was not allowed to forward.");
        }
        this.parentPipeline.getParentRouter().exceptionCaught(ctx, cause);
    }

    @Override
    public void setParentRoutingPipeline(ChannelPipeline pipeline) {
        this.parentPipeline = (RoutingPipeline) pipeline;
    }

}
