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
import java.text.MessageFormat;

/**
 *
 * @author Richard Lea <chigix@zoho.com>
 */
public class NotFoundException extends HttpException {

    @Override
    public HttpResponseStatus getResponseCode() {
        return HttpResponseStatus.NOT_FOUND;
    }

    private final String resourceNameToFound;

    private final HttpRequest requestMsg;

    public NotFoundException(String resourceNameToFound, HttpRequest requestmsg) {
        super(MessageFormat.format("The resource not found: {0}", resourceNameToFound));
        this.resourceNameToFound = resourceNameToFound;
        this.requestMsg = requestmsg;
    }

    @Override
    public HttpRequest getHttpRequest() {
        return this.requestMsg;
    }

    @Override
    public Routing getMatchedRouting() {
        return null;
    }

}
