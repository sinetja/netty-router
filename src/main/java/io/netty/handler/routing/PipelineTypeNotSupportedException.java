/*
 * This file is part of the netty-router package.
 * 
 * (c) Richard Lea <chigix@zoho.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package io.netty.handler.routing;

import io.netty.channel.ChannelPipeline;

/**
 *
 * @author Richard Lea <chigix@zoho.com>
 */
public class PipelineTypeNotSupportedException extends Exception {

    private ChannelPipeline pipeline;

    public PipelineTypeNotSupportedException(ChannelPipeline pipeline) {
        super("Unsupported Pipeline Type for Router: " + pipeline.getClass().getName());
        this.pipeline = pipeline;
    }

    public ChannelPipeline getPipeline() {
        return pipeline;
    }

}
