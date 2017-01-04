/*
 * This file is part of the netty-router package.
 * 
 * (c) Richard Lea <chigix@zoho.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package io.netty.handler.codec.http.router.testutil;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.router.HttpException;
import io.netty.handler.codec.http.router.HttpRouted;

/**
 *
 * @author Richard Lea <chigix@zoho.com>
 */
public abstract class SnapshotHttpException extends HttpException {

    private final HttpException originalException;

    public SnapshotHttpException(Throwable cause, HttpException origin_http_exception) {
        super(cause);
        originalException = origin_http_exception;
    }

    @Override
    public final HttpRequest getHttpRequest() {
        return originalException.getHttpRequest();
    }

    @Override
    public final HttpRouted getHttpRouted() {
        return originalException.getHttpRouted();
    }

    public final HttpException getSnapshoted() {
        return originalException;
    }

    public abstract boolean isChannelClosed();

}
