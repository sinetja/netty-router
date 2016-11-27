/*
 * This file is part of the netty-router package.
 * 
 * (c) Richard Lea <chigix@zoho.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package io.netty.handler.codec.http.router.testutil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.router.HttpException;
import io.netty.handler.codec.http.router.HttpRouted;
import io.netty.handler.codec.http.router.HttpRouter;
import io.netty.handler.codec.http.router.HttpRouterIT;
import io.netty.handler.codec.http.router.RoutingConfig;
import io.netty.handler.codec.http.router.SimpleHttpExceptionHandler;
import io.netty.util.CharsetUtil;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard Lea <chigix@zoho.com>
 */
public class CodecUtil {

    private static final Logger LOG = LoggerFactory.getLogger(HttpRouterIT.class.getName());

    /**
     * Convert a HttpRequest Object in Netty Codec to Netty standard ByteBuf for
     * unit testing.
     *
     * @param request
     * @return
     */
    public static final Object[] encodeHttpRequest(HttpRequest request) {
        EmbeddedChannel channel = new EmbeddedChannel(new HttpRequestEncoder());
        Assert.assertTrue(channel.writeOutbound(request));
        Assert.assertTrue(channel.finish());
        List<ByteBuf> bytebuffers = new ArrayList<ByteBuf>();
        ByteBuf outbound;
        while ((outbound = channel.readOutbound()) != null) {
            bytebuffers.add(outbound);
        }
        ByteBuf[] result = new ByteBuf[bytebuffers.size()];
        return bytebuffers.toArray(result);
    }

    public static ChannelHandler createHandlerAsRouteChecker(final AtomicReference<HttpRouted> routedResult) {
        return new SimpleChannelInboundHandler<HttpRouted>() {

            @Override
            protected void messageReceived(ChannelHandlerContext ctx, HttpRouted msg) throws Exception {
                System.out.println("checkerReceived:" + msg.getRequestMsg());
                if (routedResult != null) {
                    routedResult.set(msg);
                }
            }
        };
    }

    public static EmbeddedChannel createTestableChannel(final List<String> chunks, final AtomicReference<Exception> except, final AtomicReference<Boolean> previouslyClosed, final RoutingConfig... routings) {
        return new EmbeddedChannel(new SimpleChannelInboundHandler<DefaultHttpRequest>() {
            @Override
            protected void messageReceived(ChannelHandlerContext ctx, DefaultHttpRequest msg) throws Exception {
                if (chunks != null) {
                    chunks.add(msg.toString());
                }
                ctx.fireChannelRead(msg);
            }
        }, new SimpleChannelInboundHandler<DefaultHttpContent>() {
            @Override
            protected void messageReceived(ChannelHandlerContext ctx, DefaultHttpContent msg) throws Exception {
                ByteBuf data = msg.content().copy();
                byte[] bytes = new byte[data.writerIndex()];
                for (int i = 0; i < bytes.length; i++) {
                    bytes[i] = data.readByte();
                }
                if (chunks != null) {
                    chunks.add(new String(bytes, CharsetUtil.UTF_8));
                }
                ctx.fireChannelRead(msg.copy());
            }
        }, new HttpRouter() {
            @Override
            protected void initRoutings(ChannelHandlerContext ctx, HttpRouter router) {
                for (RoutingConfig routing : routings) {
                    this.newRouting(ctx, routing);
                }
            }

            @Override
            protected void initExceptionRouting(ChannelPipeline pipeline) {
                pipeline.addLast(generateExceptionChecker(except, previouslyClosed));
            }

            @Override
            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                super.channelActive(ctx);
                LOG.debug(MessageFormat.format("Channel [{0}] is actived.", ctx.channel().id()));
            }

            @Override
            public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                super.channelInactive(ctx);
                LOG.debug(MessageFormat.format("Channel [{0}] is inactived and removed in HttpRouter.", ctx.channel().id()));
            }

        });
    }

    private static ChannelHandler generateExceptionChecker(AtomicReference<Exception> paramExcept, AtomicReference<Boolean> paramPreviouslyClosed) {
        final AtomicReference<Exception> except;
        if (paramExcept == null) {
            except = new AtomicReference<Exception>();
        } else {
            except = paramExcept;
        }
        final AtomicReference<Boolean> previouslyClosed;
        if (paramPreviouslyClosed == null) {
            previouslyClosed = new AtomicReference<Boolean>();
        } else {
            previouslyClosed = paramPreviouslyClosed;
        }
        return new SimpleHttpExceptionHandler() {

            @Override
            protected void messageReceived(ChannelHandlerContext ctx, HttpException msg) throws Exception {
                super.messageReceived(ctx, msg);
                except.set(msg);
                LOG.debug("EXCEPTIONCAUGHT: channel[" + ctx.channel().id() + "] -- " + msg.toString());
                if (ctx.channel().isOpen()) {
                    previouslyClosed.set(Boolean.FALSE);
                    LOG.warn("Channel [" + ctx.channel().id() + "] haven't been closed.");
                } else {
                    previouslyClosed.set(Boolean.TRUE);
                    LOG.debug("Channel [" + ctx.channel().id() + "] has been closed before exception caught.");
                }
            }
        };
    }

}
