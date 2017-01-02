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
import io.netty.handler.codec.http.router.HttpRouted;

/**
 *
 * @author Richard Lea <chigix@zoho.com>
 */
public class ForbiddenException extends HttpException {

    private final HttpRouted routed;

    public ForbiddenException(String forbiddenReason, HttpRouted routed) {
        super(forbiddenReason);
        this.routed = routed;
    }

    @Override
    public HttpResponseStatus getResponseCode() {
        return HttpResponseStatus.FORBIDDEN;
    }

    @Override
    public HttpRequest getHttpRequest() {
        return routed.getRequestMsg();
    }

    @Override
    public HttpRouted getHttpRouted() {
        return this.routed;
    }

}
