/*
 * This file is part of the netty-router package.
 * 
 * (c) Richard Lea <chigix@zoho.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package io.netty.handler.codec.http.router;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderUtil;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import java.text.MessageFormat;
import java.util.Map;

/**
 * HttpRouted info object could be used as a unit for processing one http
 * request, rather than implementing a self record in channel.
 *
 * @author Richard Lea <chigix@zoho.com>
 */
public abstract class HttpRouted {

    private final HttpRequest requestMsg;

    public HttpRouted(HttpRequest request) {
        this.requestMsg = request;
    }

    public abstract Map<String, Object> decodedParams();

    public HttpRequest getRequestMsg() {
        return requestMsg;
    }

    public abstract RoutingConfig unwrapRoutingConf();

    public abstract String getPatternName();

    @Override
    public String toString() {
        return MessageFormat.format("HttpRouted({0})", this.getPatternName());
    }

    public abstract ChannelHandlerContext getChannelHandlerContext();

    /**
     * Allow access for the request.
     */
    public void allow() {
        if (HttpHeaderUtil.is100ContinueExpected(requestMsg)) {
            this.getChannelHandlerContext().writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE));
        }
    }

    /**
     * Deny access for the request. This method could help keep channel open for
     * still use, unlike throwing {@link HttpException} to return error code as
     * respond as well as closing the channel.
     */
    public void deny() {
        this.getChannelHandlerContext().writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN));
    }

}
