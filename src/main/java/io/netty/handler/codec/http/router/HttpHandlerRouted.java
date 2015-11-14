/*
 * This file is part of the netty-router package.
 * 
 * (c) Richard Lea <chigix@zoho.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package io.netty.handler.codec.http.router;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.HttpRequest;
import java.util.Map;

/**
 *
 * @author Richard Lea <chigix@zoho.com>
 */
public class HttpHandlerRouted implements Routed<ChannelHandler> {

    private final Routed<ChannelHandler> routed;

    private final HttpRequest requestMsg;

    public HttpHandlerRouted(Routed<ChannelHandler> routed, HttpRequest request) {
        this.routed = routed;
        this.requestMsg = request;
    }

    @Override
    public ChannelHandler getTarget() {
        return this.routed.getTarget();
    }

    @Override
    public Map<String, Object> decodedParams() {
        return this.routed.decodedParams();
    }

    public HttpRequest getRequestMsg() {
        return requestMsg;
    }

}
