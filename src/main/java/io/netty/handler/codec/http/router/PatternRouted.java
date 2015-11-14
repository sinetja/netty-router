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
 * @param <T>
 */
public class PatternRouted<T> implements Routed<T> {

    private final Pattern<T> pattern;
    private final Map params;

    public PatternRouted(Pattern<T> pattern, Map params) {
        this.pattern = pattern;
        this.params = params;
    }

    @Override
    public T getTarget() {
        return this.pattern.getTarget();
    }

    @Override
    public Map<String, Object> decodedParams() {
        return this.params;
    }

    public Pattern<T> getPattern() {
        return pattern;
    }

}
