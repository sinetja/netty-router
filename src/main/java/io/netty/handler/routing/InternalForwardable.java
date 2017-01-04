/*
 * This file is part of the netty-router package.
 * 
 * (c) Richard Lea <chigix@zoho.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package io.netty.handler.routing;

/**
 *
 * @author Richard Lea <chigix@zoho.com>
 */
public interface InternalForwardable {

    public void setForwarder(Forwarder forwarder);

}
