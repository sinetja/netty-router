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

/**
 *
 * @author Richard Lea <chigix@zoho.com>
 */
public class UnsupportedMethodException extends BadRequestException {

    public UnsupportedMethodException(HttpRequest requestMsg) {
        super("UnsupportedMethodException#method: " + requestMsg.method(), requestMsg);
    }

}
