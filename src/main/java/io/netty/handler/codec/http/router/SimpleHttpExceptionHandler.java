/*
 * This file is part of the netty-router package.
 * 
 * (c) Richard Lea <chigix@zoho.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package io.netty.handler.codec.http.router;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.router.exceptions.BadRequestException;
import io.netty.handler.codec.http.router.exceptions.NotFoundException;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.nio.charset.Charset;

/**
 * Must be put at the end of pipelines because using
 * SimpleChannelInboundHandler. In HttpRouter in this package, this handler
 * would be automatically added to the pipeline in the end at the time when
 * HttpRouter was added in Pipeline and actived.
 *
 * @author Richard Lea <chigix@zoho.com>
 */
public class SimpleHttpExceptionHandler extends SimpleChannelInboundHandler<HttpException> {

    private static final InternalLogger LOG = InternalLoggerFactory.getInstance(SimpleHttpExceptionHandler.class);

    protected void appendContent(ByteBuf content, String append) {
        content.writeBytes(append.getBytes(Charset.forName("UTF-8")));
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, HttpException msg) throws Exception {
        //this.error(MessageFormat.format("EXCEPTIONCAUGHT: [{1}] {0}", exc.getCause().getMessage(), exc.getSource().getClass().getName()), exc.getCause());
        ByteBuf content = Unpooled.buffer();
        FullHttpResponse response = new DefaultFullHttpResponse(msg.getHttpRequest().protocolVersion(), msg.getResponseCode(), content);
        if (msg instanceof NotFoundException) {
            this.exceptNotFound((NotFoundException) msg, response.headers(), content);
        } else if (msg instanceof BadRequestException) {
            this.exceptBadRequest((BadRequestException) msg, response.headers(), content);
        } else {
            this.exceptInternalServerError(msg, response.headers(), content);
        }
        ctx.close();
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    protected void exceptInternalServerError(HttpException inputMessage, HttpHeaders outputHeaders, ByteBuf outputContent) {
        outputHeaders.set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        appendContent(outputContent, "<html>\n");
        appendContent(outputContent, "<head><title>500 Internal Server Error</title></head>\n");
        appendContent(outputContent, "<body bgcolor=\"white\">\n");
        appendContent(outputContent, "<center><h1>500 Internal Server Error</h1></center>\n");
        appendContent(outputContent, "<hr><center>Netty Server</center>\n");
        appendContent(outputContent, "</body></html>\n");
    }

    protected void exceptNotFound(NotFoundException inputMessage, HttpHeaders outputHeaders, ByteBuf outputContent) {
        outputHeaders.set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        appendContent(outputContent, "<html>\n");
        appendContent(outputContent, "<head><title>404 Not Found</title></head>\n");
        appendContent(outputContent, "<body bgcolor=\"white\">\n");
        appendContent(outputContent, "<center><h1>404 Not Found</h1></center>\n");
        appendContent(outputContent, "<hr><center>Netty Server</center>\n");
        appendContent(outputContent, "</body></html>\n");
    }

    protected void exceptBadRequest(BadRequestException inputMessage, HttpHeaders outputHeaders, ByteBuf outputContent) {
        outputHeaders.set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        appendContent(outputContent, "<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\n");
        appendContent(outputContent, "<html>\n");
        appendContent(outputContent, "<head><title>400 Bad Request</title></head>\n");
        appendContent(outputContent, "<body bgcolor=\"white\">\n");
        appendContent(outputContent, "<h1>404 Not Found</h1>\n");
        appendContent(outputContent, "<p>Your browser sent a request that this server could not understand.</p>\n");
        appendContent(outputContent, "<p>Sorry for the inconvenience.</p>\n");
        appendContent(outputContent, "<hr><center>Netty Server</center>\n");
        appendContent(outputContent, "</body></html>\n");
    }

}
