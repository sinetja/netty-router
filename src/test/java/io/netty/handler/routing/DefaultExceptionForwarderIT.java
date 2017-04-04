/*
 * This file is part of the netty-router package.
 * 
 * (c) Richard Lea <chigix@zoho.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package io.netty.handler.routing;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.router.HttpRouted;
import io.netty.handler.codec.http.router.RoutingConfig;
import io.netty.handler.codec.http.router.testutil.CodecUtil;
import io.netty.handler.codec.http.router.testutil.SnapshotHttpException;
import io.netty.handler.codec.http.router.testutils.builder.DefaultHttpRequestFactory;
import io.netty.handler.codec.http.router.testutils.builder.HttpMessageFactory;
import io.netty.handler.codec.http.router.testutils.builder.HttpRequestBuilder;
import io.netty.util.CharsetUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard Lea <chigix@zoho.com>
 */
public class DefaultExceptionForwarderIT {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultExceptionForwarderIT.class.getName());

    @Test
    public void testExceptionCatch() {
        final Exception toThrow = new Exception("BANKAI");
        HttpRequestBuilder builder = new HttpRequestBuilder(CharsetUtil.UTF_8);
        builder.header(HttpHeaderNames.HOST, "example.com");
        builder.header(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        builder.header(HttpHeaderNames.CONTENT_LENGTH, "500");
        builder.uri("/test/exception");
        HttpMessageFactory factory = new DefaultHttpRequestFactory(HttpVersion.HTTP_1_1, HttpMethod.POST, RandomStringUtils.randomAlphanumeric(500).getBytes());
        List<SnapshotHttpException> exceptions = new ArrayList<SnapshotHttpException>();
        EmbeddedChannel channel = CodecUtil.createTestableChannel(null, exceptions, new RoutingConfig() {
            @Override
            public String configureRoutingName() {
                return "EXCEPTION THROW TEST";
            }

            @Override
            public String configurePath() {
                return "/test/exception";
            }

            @Override
            public HttpMethod[] configureMethods() {
                return new HttpMethod[]{HttpMethod.POST};
            }

            @Override
            public void configurePipeline(ChannelPipeline pipeline) {
                pipeline.addLast(new SimpleChannelInboundHandler<HttpRouted>() {
                    @Override
                    protected void messageReceived(ChannelHandlerContext ctx, HttpRouted msg) throws Exception {
                        throw toThrow;
                    }
                }).addLast(new DefaultExceptionForwarder());
            }
        });
        channel.pipeline().addFirst(new HttpRequestDecoder());
        channel.writeInbound(CodecUtil.encodeHttpRequest(builder.getResult(factory)));
        Assert.assertTrue(exceptions.size() > 0);
        Assert.assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR, exceptions.get(0).getSnapshoted().getResponseCode());
        System.out.println(exceptions.get(0).getSnapshoted().getResponseCode());
        Assert.assertSame(toThrow, ((RoutingException) exceptions.get(0).getCause()).unwrapException());
        Assert.assertEquals("POST:EXCEPTION THROW TEST", ((RoutingException) exceptions.get(0).getCause()).getRoutingNameTrace());
    }

    @Test
    public void testNotAddedInParent() {
        final Exception toThrow = new Exception("BANKAI");
        HttpRequestBuilder builder = new HttpRequestBuilder(CharsetUtil.UTF_8);
        builder.header(HttpHeaderNames.HOST, "example.com");
        builder.header(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        builder.header(HttpHeaderNames.CONTENT_LENGTH, "500");
        builder.uri("/test/exception");
        HttpMessageFactory factory = new DefaultHttpRequestFactory(HttpVersion.HTTP_1_1, HttpMethod.POST, RandomStringUtils.randomAlphanumeric(500).getBytes());
        EmbeddedChannel channel = new EmbeddedChannel(new HttpRequestDecoder(), new SimpleChannelInboundHandler<HttpRequest>() {
            @Override
            protected void messageReceived(ChannelHandlerContext ctx, HttpRequest msg) throws Exception {
                throw toThrow;
            }
        }, new DefaultExceptionForwarder() {
            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                try {
                    super.exceptionCaught(ctx, cause);
                } catch (Exception exception) {
                    Assert.assertSame(toThrow, cause);
                    Assert.assertEquals("Please check this exception forwarder is added at Routing Pipeline. "
                            + "Null Pipeline was not allowed to forward.", exception.getMessage());
                }
            }

        });
        channel.writeInbound(CodecUtil.encodeHttpRequest(builder.getResult(factory)));
    }

    @Test
    public void testSubRouterExceptionCatch() {
        final Exception exceptionInRouter = new Exception("COMMON");
        final Exception exceptionInSubRouter = new Exception("BANKAI");
        final Exception exceptionInSubRouting = new Exception("JIKAI");
        HttpRequestBuilder builder = new HttpRequestBuilder(CharsetUtil.UTF_8);
        builder.header(HttpHeaderNames.HOST, "example.com");
        builder.header(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        builder.header(HttpHeaderNames.CONTENT_LENGTH, "500");
        builder.uri("/test/exception");
        HttpMessageFactory factory = new DefaultHttpRequestFactory(HttpVersion.HTTP_1_1, HttpMethod.POST, RandomStringUtils.randomAlphanumeric(500).getBytes());
        List<SnapshotHttpException> exceptions = new ArrayList<SnapshotHttpException>();
        EmbeddedChannel channel = CodecUtil.createTestableChannel(null, exceptions, new RoutingConfig() {
            @Override
            public String configureRoutingName() {
                return "OUTER_ROUTING";
            }

            @Override
            public String configurePath() {
                return "/test/exception";
            }

            @Override
            public HttpMethod[] configureMethods() {
                return new HttpMethod[]{HttpMethod.POST};
            }

            @Override
            public void configurePipeline(ChannelPipeline pipeline) {
                final DefaultExceptionForwarder ex_fwd = new DefaultExceptionForwarder();
                pipeline.addLast(new ChannelHandlerAdapter() {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        if (msg instanceof HttpRouted) {
                            throw exceptionInRouter;
                        }
                        super.channelRead(ctx, msg);
                    }

                },
                        new Router(true, "Router_Bankai") {
                    @Override
                    protected void route(ChannelHandlerContext ctx, Object msg, Map<String, ChannelPipeline> routingPipelines) throws Exception {
                        pipelineForward(routingPipelines.get("ROUTING_JIKAI"), msg);
                        throw exceptionInSubRouter;
                    }

                    @Override
                    protected void initRouter(ChannelHandlerContext ctx) throws Exception {
                        this.newRouting(ctx, "ROUTING_JIKAI").addLast(new ChannelHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                throw exceptionInSubRouting;
                            }

                        }).addLast(new DefaultExceptionForwarder());
                    }

                    @Override
                    protected void initExceptionRouting(ChannelPipeline pipeline) {
                        pipeline.addLast(new ChannelHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ex_fwd.exceptionCaught(ctx, (Throwable) msg);
                            }

                        });
                    }

                }, ex_fwd);
            }
        });
        channel.pipeline().addFirst(new HttpRequestDecoder());
        channel.writeInbound(CodecUtil.encodeHttpRequest(builder.getResult(factory)));
        Assert.assertEquals(3, exceptions.size());
        Assert.assertEquals("POST:OUTER_ROUTING", ((RoutingException) exceptions.get(0).getCause()).getRoutingNameTrace());
        Assert.assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR, exceptions.get(0).getSnapshoted().getResponseCode());
        Assert.assertSame(exceptionInRouter, ((RoutingException) exceptions.get(0).getCause()).unwrapException());
        Assert.assertEquals("POST:OUTER_ROUTING -> ROUTING_JIKAI", ((RoutingException) exceptions.get(1).getCause()).getRoutingNameTrace());
        Assert.assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR, exceptions.get(1).getSnapshoted().getResponseCode());
        Assert.assertSame(exceptionInSubRouting, ((RoutingException) exceptions.get(1).getCause()).unwrapException());
        Assert.assertEquals("POST:OUTER_ROUTING", ((RoutingException) exceptions.get(2).getCause()).getRoutingNameTrace());
        Assert.assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR, exceptions.get(2).getSnapshoted().getResponseCode());
        Assert.assertSame(exceptionInSubRouter, ((RoutingException) exceptions.get(2).getCause()).unwrapException());
    }

}
