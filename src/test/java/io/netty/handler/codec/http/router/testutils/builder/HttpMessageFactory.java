/*
 * This file is part of the netty-router package.
 * 
 * (c) Richard Lea <chigix@zoho.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package io.netty.handler.codec.http.router.testutils.builder;

import io.netty.handler.codec.http.FullHttpMessage;

/**
 *
 * @author Richard Lea <chigix@zoho.com>
 * @param <M>
 */
public interface HttpMessageFactory<M extends FullHttpMessage> {

    M create();

}
