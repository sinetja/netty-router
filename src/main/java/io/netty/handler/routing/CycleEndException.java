/*
 * This file is part of the netty-router package.
 * 
 * (c) Richard Lea <chigix@zoho.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package io.netty.handler.routing;

import java.text.MessageFormat;

/**
 *
 * @author Richard Lea <chigix@zoho.com>
 */
public class CycleEndException extends Exception {

    private final RoutingPipeline pipelineToEnd;

    private final String endMessage;

    public CycleEndException(RoutingPipeline pipelineToEnd, String endMessage) {
        super(MessageFormat.format("Closing Pipeline [{0}] Failed with message [{1}]", pipelineToEnd.getPipelineName(), endMessage));
        this.pipelineToEnd = pipelineToEnd;
        this.endMessage = endMessage;
    }

}
