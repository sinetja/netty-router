/*
 * This file is part of the netty-router package.
 * 
 * (c) Richard Lea <chigix@zoho.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package io.netty.handler.codec.http.router;

import io.netty.handler.codec.http.HttpRequest;
import java.text.MessageFormat;
import java.util.Map;

/**
 *
 * @author Richard Lea <chigix@zoho.com>
 */
public abstract class HttpRouted {

    private final HttpRequest requestMsg;

    public HttpRouted(HttpRequest request) {
        this.requestMsg = request;
    }

    public abstract Map<String, Object> decodedParams();

    public HttpRequest getRequestMsg() {
        return requestMsg;
    }

    public abstract RoutingConfig unwrapRoutingConf();

    public abstract String getPatternName();

    @Override
    public String toString() {
        return MessageFormat.format("HttpRouted({0})", this.getPatternName());
    }

}
