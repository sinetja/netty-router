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
import io.netty.handler.codec.http.router.RoutingConfig;
import io.netty.handler.routing.RoutingTraceable;
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

    private static final Logger LOG = LoggerFactory.getLogger(CodecUtil.class.getName());

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

    public static EmbeddedChannel createTestableChannel(final List<String> receivedChunks, final List<SnapshotHttpException> exceptStack, final RoutingConfig... routings) {
        final List<SnapshotHttpException> excepts;
        if (exceptStack == null) {
            excepts = new ArrayList<SnapshotHttpException>();
        } else {
            excepts = exceptStack;
        }
        return new EmbeddedChannel(new SimpleChannelInboundHandler<DefaultHttpRequest>() {
            @Override
            protected void messageReceived(ChannelHandlerContext ctx, DefaultHttpRequest msg) throws Exception {
                if (receivedChunks != null) {
                    receivedChunks.add(msg.toString());
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
                if (receivedChunks != null) {
                    receivedChunks.add(new String(bytes, CharsetUtil.UTF_8));
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
                pipeline.addLast(new UnwrappedExceptionHandler() {
                    @Override
                    protected void reportException(ChannelHandlerContext ctx, final HttpException httpexc) {
                        final boolean isPreviouslyClosed = !ctx.channel().isOpen();
                        excepts.add(new SnapshotHttpException(httpexc.getCause(), httpexc) {
                            @Override
                            public boolean isChannelClosed() {
                                return isPreviouslyClosed;
                            }

                            @Override
                            public String getRoutingNameTrace() {
                                if (httpexc.getCause() instanceof RoutingTraceable) {
                                    return ((RoutingTraceable) httpexc.getCause()).getRoutingNameTrace();
                                } else if (httpexc instanceof RoutingTraceable) {
                                    return ((RoutingTraceable) httpexc).getRoutingNameTrace();
                                }
                                return null;
                            }
                        });
                    }
                }).addLast(new SimpleChannelInboundHandler<HttpException>() {
                    @Override
                    protected void messageReceived(ChannelHandlerContext ctx, final HttpException msg) throws Exception {
                        final boolean isPreviouslyClosed = !ctx.channel().isOpen();
                        excepts.add(new SnapshotHttpException(msg, msg) {
                            @Override
                            public boolean isChannelClosed() {
                                return isPreviouslyClosed;
                            }

                            @Override
                            public String getRoutingNameTrace() {
                                if (msg.getCause() instanceof RoutingTraceable) {
                                    return ((RoutingTraceable) msg).getRoutingNameTrace();
                                } else if (msg instanceof RoutingTraceable) {
                                    return ((RoutingTraceable) msg).getRoutingNameTrace();
                                }
                                return null;
                            }
                        });
                    }
                });
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

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                LOG.debug("EXCEPTIONCAUGHT: channel[{}] -- [{}] {}", ctx.channel().id(), cause.getClass().toString(), cause.getMessage());
                super.exceptionCaught(ctx, cause);
            }
        });
    }

    public static final String readOutboundString(EmbeddedChannel channel) {
        StringBuffer sb = null;
        while (true) {
            ByteBuf bytebuf = channel.readOutbound();
            if (bytebuf == null) {
                break;
            }
            if (sb == null) {
                sb = new StringBuffer();
            }
            sb.append(bytebuf.toString(CharsetUtil.UTF_8));
        }
        if (sb == null) {
            return null;
        } else {
            return sb.toString();
        }
    }

    private static abstract class UnwrappedExceptionHandler extends io.netty.handler.codec.http.router.UnwrappedExceptionHandler {

        protected abstract void reportException(ChannelHandlerContext ctx, HttpException httpexc);

        @Override
        protected void handleDecoderException(ChannelHandlerContext ctx, HttpException httpexc) {
            reportException(ctx, httpexc);
        }

        @Override
        protected void handleUnableRouting(ChannelHandlerContext ctx, HttpException httpexc) {
            reportException(ctx, httpexc);
        }

        @Override
        protected void handleUnwrappedException(ChannelHandlerContext ctx, HttpException httpexc) {
            reportException(ctx, httpexc);
        }
    }

}
