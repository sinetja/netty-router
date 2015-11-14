/*
 * This file is part of the netty-router package.
 * 
 * (c) Richard Lea <chigix@zoho.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package io.netty.handler.codec.http.router;

/**
 * The pattern defination for the path.
 * <br>
 * Tokens are small bits of text that can be placed into the whole url path via
 * simple placeholders defined in this pattern.
 *
 * @author Richard Lea <chigix@zoho.com>
 */
public class Pattern<T> {

    /**
     * The path trimed the slashes.
     */
    private final String path;

    /**
     * The substrins between each slash in the path.
     */
    private final String[] tokens;

    /**
     * The handler as the destination to be routed to.
     */
    private final T target;

    /**
     *
     * @param path
     * @param target
     * @throws io.netty.handler.codec.http.router.InvalidPathException
     */
    public Pattern(String path, T target) throws InvalidPathException {
        this.path = RouterUtil.normalizePath(path);
        this.tokens = this.path.split("/");
        this.target = target;
    }

    public String getPath() {
        return path;
    }

    public String[] getTokens() {
        return tokens;
    }

    public T getTarget() {
        return target;
    }

}
