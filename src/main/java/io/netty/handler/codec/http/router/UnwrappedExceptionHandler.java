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
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.routing.Forwarder;
import io.netty.handler.routing.InternalForwardable;
import io.netty.handler.routing.RoutingException;
import io.netty.handler.routing.UnableRoutingMessageException;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.text.MessageFormat;

/**
 *
 * @author Richard Lea <chigix@zoho.com>
 */
public class UnwrappedExceptionHandler extends HttpExceptionInboundHandler<Exception> implements InternalForwardable {

    private Forwarder forwardToNextHandler;

    private static final InternalLogger LOG = InternalLoggerFactory.getInstance(UnwrappedExceptionHandler.class);

    @Override
    protected final void handleException(ChannelHandlerContext ctx, HttpException exc) {
        if (!(exc instanceof WrappedByRouter)) {
            forwardToNextHandler.forward(ctx, exc);
            return;
        }
        if (exc.getCause() instanceof RoutingException) {
            handleRoutingException(ctx, exc);
        } else if (exc.getCause() instanceof UnableRoutingMessageException) {
            handleUnableRouting(ctx, exc);
        } else if (exc.getCause() instanceof DecoderException) {
            handleDecoderException(ctx, exc);
        } else {
            handleUnwrappedException(ctx, exc);
        }
    }

    protected void handleRoutingException(ChannelHandlerContext ctx, final HttpException httpexc) {
        final RoutingException exc = (RoutingException) httpexc.getCause();
        if (exc.unwrapException() instanceof HttpException) {
            forwardToNextHandler.forward(ctx, exc.unwrapException());
        } else {
            forwardToNextHandler.forward(ctx, new HttpException(exc.unwrapException()) {

                @Override
                public HttpRequest getHttpRequest() {
                    return httpexc.getHttpRequest();
                }

                @Override
                public io.netty.handler.codec.http.router.HttpRouted getHttpRouted() {
                    return httpexc.getHttpRouted();
                }
            });
        }
    }

    protected void handleUnableRouting(ChannelHandlerContext ctx, HttpException httpexc) {
        if (ctx.channel().isOpen()) {
            handleUnwrappedException(ctx, httpexc);
            return;
        }
        UnableRoutingMessageException origin_exc = (UnableRoutingMessageException) httpexc.getCause();
        if (!(origin_exc.getRoutingMessage() instanceof HttpObject)) {
            LOG.warn(MessageFormat.format("Unexpected message[{1}] is trying to be put in a closed channel: {0}", ctx.channel().id(), origin_exc.getRoutingMessage().getClass().getName()));
            return;
        }
        LOG.warn(MessageFormat.format("One http message is trying to be put in a closed channel: {0}", ctx.channel().id()));
    }

    protected void handleDecoderException(ChannelHandlerContext ctx, HttpException httpexc) {
        if (ctx.channel().isOpen()) {
            handleUnwrappedException(ctx, httpexc);
        }
    }

    protected void handleUnwrappedException(ChannelHandlerContext ctx, final HttpException httpexc) {
        LOG.error("Bomb!!!!--Unwrapped exception was throwed:", httpexc.getCause());
        forwardToNextHandler.forward(ctx, new HttpException(httpexc.getCause()) {
            @Override
            public HttpRequest getHttpRequest() {
                return httpexc.getHttpRequest();
            }

            @Override
            public io.netty.handler.codec.http.router.HttpRouted getHttpRouted() {
                return httpexc.getHttpRouted();
            }
        });
    }

    @Override
    public void setForwarder(Forwarder forwarder) {
        forwardToNextHandler = forwarder;
    }

}
