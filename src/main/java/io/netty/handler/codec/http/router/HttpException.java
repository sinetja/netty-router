/*
 * This file is part of the netty-router package.
 * 
 * (c) Richard Lea <chigix@zoho.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package io.netty.handler.codec.http.router;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * To be used to extending plain exception object with HTTP information.
 *
 * @author Richard Lea <chigix@zoho.com>
 */
public abstract class HttpException extends Exception {

    public HttpException(String message) {
        super(message);
    }

    public HttpException(Throwable cause) {
        super(cause);
    }

    public HttpResponseStatus getResponseCode() {
        return HttpResponseStatus.INTERNAL_SERVER_ERROR;
    }

    @Override
    public String getMessage() {
        String excMessage = super.getMessage();
        if (excMessage == null || excMessage.trim().equals("")) {
            return this.getResponseCode().toString();
        } else {
            return excMessage;
        }
    }

    public abstract HttpRequest getHttpRequest();

    public abstract HttpRouted getHttpRouted();

}
