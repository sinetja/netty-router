/*
 * This file is part of the netty-router package.
 * 
 * (c) Richard Lea <chigix@zoho.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package io.netty.handler.codec.http.router.exceptions;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.router.HttpRouted;
import io.netty.handler.routing.SimpleCycleRouter;

/**
 * This class is designed for throwning in {@link SimpleCycleRouter#matcherEnd}.
 * It should not be used by users.
 *
 * @author Richard Lea <chigix@zoho.com>
 */
public class LastNotFoundException extends NotFoundException {

    public LastNotFoundException() {
        super(null, null);
    }

    @Override
    public String getMessage() {
        return "This class should not be used by users. Please skip this exception, and use the previous NotFoundException instead.";
    }

    @Override
    public HttpRequest getHttpRequest() {
        throw new UnsupportedOperationException("Please use previous NotFoundException");
    }

    @Override
    public HttpRouted getHttpRouted() {
        throw new UnsupportedOperationException("Please use previous NotFoundException");
    }

}
