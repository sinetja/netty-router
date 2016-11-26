/*
 * This file is part of the netty-router package.
 * 
 * (c) Richard Lea <chigix@zoho.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package io.netty.handler.codec.http.router.testutils.builder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

/**
 *
 * @author Richard Lea <chigix@zoho.com>
 */
public class DefaultHttpRequestFactory implements HttpMessageFactory<FullHttpRequest> {

    private final HttpVersion httpVersion;

    private final HttpMethod httpMethod;

    private final ByteBuf content;

    public DefaultHttpRequestFactory(HttpVersion httpVersion, HttpMethod httpMethod, ByteBuf content) {
        this.httpVersion = httpVersion;
        this.httpMethod = httpMethod;
        this.content = content;
    }

    public DefaultHttpRequestFactory(HttpVersion httpVersion, HttpMethod httpMethod, byte[] content) {
        this(httpVersion, httpMethod, Unpooled.copiedBuffer(content));
    }

    public DefaultHttpRequestFactory() {
        this(HttpVersion.HTTP_1_1, HttpMethod.GET, Unpooled.buffer());
    }

    public DefaultHttpRequestFactory(HttpVersion httpVersion, HttpMethod httpMethod) {
        this(httpVersion, httpMethod, Unpooled.buffer());
    }

    public DefaultHttpRequestFactory(HttpVersion httpVersion) {
        this(httpVersion, HttpMethod.GET, Unpooled.buffer());
    }

    @Override
    public FullHttpRequest create() {
        if (this.content == null) {
            return new DefaultFullHttpRequest(httpVersion, httpMethod, "/");
        } else {
            return new DefaultFullHttpRequest(httpVersion, httpMethod, "/", content.copy());
        }
    }

}
