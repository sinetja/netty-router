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
public class UnableRoutingMessageException extends Exception {

    private final Object message;

    public UnableRoutingMessageException() {
        message = null;
    }

    public UnableRoutingMessageException(String msg) {
        super(msg);
        message = null;
    }

    public UnableRoutingMessageException(String message, Object msg) {
        super(message);
        this.message = msg;
    }

    public Object getRoutingMessage() {
        return message;
    }

}
