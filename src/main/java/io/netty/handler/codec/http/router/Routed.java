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
public interface Routed<T> {

    /**
     * Get the target object including the executed business logic.
     *
     * @return
     */
    public T getTarget();

    /**
     * Get the params decoded as defined in the Pattern from the given URL.
     *
     * @return
     */
    public Map<String, Object> decodedParams();
}
