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
 *
 * @author Richard Lea <chigix@zoho.com>
 * @param <T>
 */
public class HttpRouter<T> {

    public HttpRouter CONNECT(String path, T target) throws InvalidPathException {
        this.connectRouter.addLast(path, target);
        return this;
    }

    public HttpRouter DELETE(String path, T target) throws InvalidPathException {
        this.deleteRouter.addLast(path, target);
        return this;
    }

    public HttpRouter GET(String path, T target) throws InvalidPathException {
        this.getRouter.addLast(path, target);
        return this;
    }

    public HttpRouter HEAD(String path, T target) throws InvalidPathException {
        this.headRouter.addLast(path, target);
        return this;
    }

    public HttpRouter OPTIONS(String path, T target) throws InvalidPathException {
        this.optionsRouter.addLast(path, target);
        return this;
    }

    public HttpRouter PATCH(String path, T target) throws InvalidPathException {
        this.patchRouter.addLast(path, target);
        return this;
    }

    public HttpRouter POST(String path, T target) throws InvalidPathException {
        this.postRouter.addLast(path, target);
        return this;
    }

    public HttpRouter PUT(String path, T target) throws InvalidPathException {
        this.putRouter.addLast(path, target);
        return this;
    }

    public HttpRouter TRACE(String path, T target) throws InvalidPathException {
        this.traceRouter.addLast(path, target);
        return this;
    }

    private final PatternRoutingMatcher connectRouter = new PatternRoutingMatcher();
    private final PatternRoutingMatcher deleteRouter = new PatternRoutingMatcher();
    private final PatternRoutingMatcher getRouter = new PatternRoutingMatcher();
    private final PatternRoutingMatcher headRouter = new PatternRoutingMatcher();
    private final PatternRoutingMatcher optionsRouter = new PatternRoutingMatcher();
    private final PatternRoutingMatcher patchRouter = new PatternRoutingMatcher();
    private final PatternRoutingMatcher postRouter = new PatternRoutingMatcher();
    private final PatternRoutingMatcher putRouter = new PatternRoutingMatcher();
    private final PatternRoutingMatcher traceRouter = new PatternRoutingMatcher();

    /**
     *
     * @param method
     * @param path
     * @return If {@code null} is returned, it means that no pattern was matched
     * with the given path. As expactation, the routed object would be returned
     * regarding the {@link Pattern} and extracted params appropriately.
     */
    public Routed route(HttpMethod method, String path) {
        if (method.equals(HttpMethod.CONNECT)) {
            return this.connectRouter.match(path);
        } else if (method.equals(HttpMethod.DELETE)) {
            return this.deleteRouter.match(path);
        } else if (method.equals(HttpMethod.GET)) {
            return this.getRouter.match(path);
        } else if (method.equals(HttpMethod.HEAD)) {
            return this.headRouter.match(path);
        } else if (method.equals(HttpMethod.OPTIONS)) {
            return this.optionsRouter.match(path);
        } else if (method.equals(HttpMethod.PATCH)) {
            return this.patchRouter.match(path);
        } else if (method.equals(HttpMethod.POST)) {
            return this.postRouter.match(path);
        } else if (method.equals(HttpMethod.PUT)) {
            return this.putRouter.match(path);
        } else if (method.equals(HttpMethod.TRACE)) {
            return this.traceRouter.match(path);
        }
        return null;
    }

}
