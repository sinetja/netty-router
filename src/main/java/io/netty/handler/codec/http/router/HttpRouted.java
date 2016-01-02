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
public class HttpRouted {

    private final RoutingPathMatched pathMatched;

    private final HttpRequest requestMsg;

    public HttpRouted(RoutingPathMatched matched, HttpRequest request) {
        this.pathMatched = matched;
        this.requestMsg = request;
    }

    public Map<String, Object> decodedParams() {
        return this.pathMatched.decodedParams();
    }

    public HttpRequest getRequestMsg() {
        return requestMsg;
    }

    public RoutingConfig unwrapRoutingConf() {
        return this.pathMatched.getRouting().unwrap();
    }

    public String getPatternName() {
        return this.pathMatched.getRouting().getName();
    }

}
