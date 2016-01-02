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
public abstract class RoutingException extends Exception {

    public abstract String getRoutingName();

    public abstract ChannelPipeline getRoutingPipeline();

    public abstract Throwable unwrapException();
}
