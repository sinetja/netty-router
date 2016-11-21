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
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.router.HttpException;
import io.netty.handler.codec.http.router.Routing;

/**
 *
 * @author Richard Lea <chigix@zoho.com>
 */
public class BadRequestException extends HttpException {

    private final HttpRequest requestMsg;

    public BadRequestException(String message, HttpRequest requestMsg) {
        super(message);
        this.requestMsg = requestMsg;
    }

    @Override
    public final HttpResponseStatus getResponseCode() {
        return HttpResponseStatus.BAD_REQUEST;
    }

    @Override
    public final HttpRequest getHttpRequest() {
        return this.requestMsg;
    }

    @Override
    public final Routing getMatchedRouting() {
        throw new UnsupportedOperationException("Not supported in BadRequest.");
    }

}
