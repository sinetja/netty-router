/*
 * This file is part of the netty-router package.
 * 
 * (c) Richard Lea <chigix@zoho.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package io.netty.handler.codec.http.router;

import io.netty.handler.codec.http.HttpMethod;

/**
 * The wrapper for the pattern defination including some calculated information
 * such as tokens from the defined path.
 * <br>
 * This class is not allowed to be used outside of this package.<br>
 * Tokens are small bits of text that can be into the whole url path via simple
 * placeholders behind in this pattern.
 *
 * @author Richard Lea <chigix@zoho.com>
 */
public class Routing {

    private final RoutingConfig conf;

    private final String[] tokens;

    private final String name;

    /**
     * This path in this class should be normalized.
     */
    private final String path;

    private final String identity;

    public Routing(RoutingConfig conf, HttpMethod methods) {
        this.conf = conf;
        this.tokens = conf.configurePath().split("/");
        this.name = conf.configureRoutingName();
        this.path = RouterUtil.normalizePath(conf.configurePath());
        this.identity = methods.toString() + ":" + this.name;
    }

    public RoutingConfig unwrap() {
        return this.conf;
    }

    public String[] getTokens() {
        return tokens;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getIdentity() {
        return identity;
    }

}
