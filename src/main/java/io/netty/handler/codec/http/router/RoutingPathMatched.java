/*
 * This file is part of the netty-router package.
 * 
 * (c) Richard Lea <chigix@zoho.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package io.netty.handler.codec.http.router;

import java.util.Map;

/**
 *
 * @author Richard Lea <chigix@zoho.com>
 */
class RoutingPathMatched {

    private final Routing routing;
    private final Map params;

    public RoutingPathMatched(Routing routing, Map params) {
        this.routing = routing;
        this.params = params;
    }

    public Routing getRouting() {
        return routing;
    }

    public Map<String, Object> decodedParams() {
        return this.params;
    }

}
