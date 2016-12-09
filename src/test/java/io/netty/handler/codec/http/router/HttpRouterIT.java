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
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.router.exceptions.BadRequestException;
import io.netty.handler.codec.http.router.exceptions.NotFoundException;
import io.netty.handler.codec.http.router.exceptions.UnsupportedMethodException;
import io.netty.handler.codec.http.router.testutil.CodecUtil;
import io.netty.handler.codec.http.router.testutil.CheckableRoutingConfig;
import io.netty.handler.codec.http.router.testutils.builder.ContentTypes;
import io.netty.handler.codec.http.router.testutils.builder.DefaultHttpRequestFactory;
import io.netty.handler.codec.http.router.testutils.builder.HttpMessageFactory;
import io.netty.handler.codec.http.router.testutils.builder.HttpRequestBuilder;
import io.netty.util.CharsetUtil;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 *
 * @author Richard Lea <chigix@zoho.com>
 */
public class HttpRouterIT {

    private static final Logger LOG = LoggerFactory.getLogger(HttpRouterIT.class.getName());

    public HttpRouterIT() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testSuccessREST() {
        final Map<String, String> handlerResultChecker = new HashMap<String, String>();
        AtomicReference<Exception> except = new AtomicReference<Exception>();
        AtomicReference<Boolean> previouslyClosed = new AtomicReference<Boolean>();
        previouslyClosed.set(Boolean.FALSE);
        EmbeddedChannel channel = CodecUtil.createTestableChannel(null, except, previouslyClosed, new RoutingConfig() {
            @Override
            public String configureRoutingName() {
                return "TARGET_ROUTING";
            }

            @Override
            public String configurePath() {
                return "/name/:name/testing_api";
            }

            @Override
            public HttpMethod[] configureMethods() {
                return new HttpMethod[]{HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE};
            }

            @Override
            public void configurePipeline(ChannelPipeline pipeline) {
                pipeline.addLast(new ChannelHandlerAdapter() {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        System.out.println(msg);
                        super.channelRead(ctx, msg);
                    }

                }).addLast(new SimpleChannelInboundHandler<HttpRouted>() {
                    @Override
                    protected void messageReceived(ChannelHandlerContext ctx, HttpRouted msg) throws Exception {
                        String name = (String) msg.decodedParams().get("name");
                        handlerResultChecker.put("name", name);
                        handlerResultChecker.put("METHOD", msg.getRequestMsg().method().toString());
                        if (msg.getRequestMsg().method() == HttpMethod.GET) {
                            HttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(name.getBytes(CharsetUtil.UTF_8)));
                            resp.headers().add(HttpHeaderNames.CONTENT_TYPE, ContentTypes.HTML.getValue());
                            ctx.writeAndFlush(resp);
                        }
                        System.out.println(msg.getRequestMsg());
                    }
                });
            }
        });
        channel.pipeline().addFirst(new FullResponseLengthFixer());
        HttpRequestBuilder builder = new HttpRequestBuilder(CharsetUtil.UTF_8);
        builder.uri("/name/Richard/testing_api");
        builder.accept(ContentTypes.HTML.getValue());
        LOG.info("[TESTING] === GET /name/Richard/testing_api");
        handlerResultChecker.clear();
        channel.writeInbound(builder.getResult(new DefaultHttpRequestFactory(HttpVersion.HTTP_1_1, HttpMethod.GET)));
        Assert.assertEquals("Richard", handlerResultChecker.get("name"));
        Assert.assertEquals("GET", handlerResultChecker.get("METHOD"));
        FullHttpResponse resp = (FullHttpResponse) channel.readOutbound();
        Assert.assertEquals("Richard".length() + "", resp.headers().get(HttpHeaderNames.CONTENT_LENGTH));
        Assert.assertEquals("Richard", resp.content().toString(CharsetUtil.UTF_8));
        Assert.assertNull(except.get());
        Assert.assertFalse(previouslyClosed.get());
    }

    @Test
    /**
     * Put an array of Http Requests into one opened channel, testing for
     * comprehensibility.
     */
    public void testRoutingSwitcher() {
        final AtomicReference<HttpRouted> routed = new AtomicReference<HttpRouted>();
        EmbeddedChannel channel = CodecUtil.createTestableChannel(null, null, null,
                CheckableRoutingConfig.PLAIN_ROUTING.setChecker(CodecUtil.createHandlerAsRouteChecker(routed)),
                CheckableRoutingConfig.SINGLE_VAR_ROUTING.setChecker(CodecUtil.createHandlerAsRouteChecker(routed)),
                CheckableRoutingConfig.DUAL_VAR_ROUTING.setChecker(CodecUtil.createHandlerAsRouteChecker(routed)));
        channel.pipeline().addFirst(new HttpRequestDecoder(), new HttpResponseEncoder(), new FullResponseLengthFixer());
        HttpRequestBuilder builder = new HttpRequestBuilder(CharsetUtil.UTF_8);
        builder.header(HttpHeaderNames.HOST, "example.com");
        builder.header(HttpHeaderNames.CONTENT_LENGTH, "500");
        builder.header(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        // builder.header(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
        // builder.contentType(HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED);
        HttpMessageFactory factory = new DefaultHttpRequestFactory(HttpVersion.HTTP_1_1, HttpMethod.POST, RandomStringUtils.randomAlphanumeric(500).getBytes());
        channel.writeInbound(CodecUtil.encodeHttpRequest(builder.getResult(factory).setUri("/plain/tester/path")));
        Assert.assertSame(CheckableRoutingConfig.PLAIN_ROUTING, routed.get().unwrapRoutingConf());
        LOG.info("PASS [/plain/tester/path]");
        routed.set(null);
        channel.writeInbound(CodecUtil.encodeHttpRequest(builder.getResult(factory).setUri("//plain/tester/path")));
        Assert.assertSame(CheckableRoutingConfig.PLAIN_ROUTING, routed.get().unwrapRoutingConf());
        LOG.info("PASS [//plain/tester/path]");
        routed.set(null);
        channel.writeInbound(CodecUtil.encodeHttpRequest(builder.getResult(factory).setUri("/single/var/BANKAI/suffix")));
        Assert.assertSame(CheckableRoutingConfig.SINGLE_VAR_ROUTING, routed.get().unwrapRoutingConf());
        LOG.info("PASS [/single/var/BANKAI/suffix]");
        routed.set(null);
        channel.writeInbound(CodecUtil.encodeHttpRequest(builder.getResult(factory).setUri("/plain/tester//path")));
        Assert.assertNull(routed.get());
        if (channel.isOpen()) {
            Assert.fail(MessageFormat.format("Channel#[{0}] is not closed.", channel.id()));
        }
        LOG.info("Channel Outbound Stream: " + CodecUtil.readOutboundString(channel));
        LOG.info("PASS [/plain/tester//path]");
        routed.set(null);
        Assert.assertFalse(channel.isOpen());
    }

    @Test
    public void testOverChunksizedRequestWithoutContentLength() {
        AtomicReference<Exception> except = new AtomicReference<Exception>();
        AtomicReference<Boolean> previouslyClosedChecker = new AtomicReference<Boolean>();
        List<String> chunks = new ArrayList<String>();
        EmbeddedChannel channel = CodecUtil.createTestableChannel(chunks, except, previouslyClosedChecker,
                CheckableRoutingConfig.PLAIN_ROUTING.setChecker(CodecUtil.createHandlerAsRouteChecker(null)),
                CheckableRoutingConfig.SINGLE_VAR_ROUTING.setChecker(CodecUtil.createHandlerAsRouteChecker(null)),
                CheckableRoutingConfig.DUAL_VAR_ROUTING.setChecker(CodecUtil.createHandlerAsRouteChecker(null)));
        channel.pipeline().addFirst(new HttpRequestDecoder(4096, 8192, 8192), new HttpResponseEncoder(), new FullResponseLengthFixer());
        System.out.println("EmbeddedChannel: " + channel.id());
        HttpRequestBuilder builder = new HttpRequestBuilder(CharsetUtil.UTF_8);
        builder.header(HttpHeaderNames.HOST, "example.com");
        builder.header(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        builder.header(HttpHeaderNames.CONTENT_LENGTH, "50000");
        builder.uri("/plain/tester/path");
        HttpMessageFactory factory = new DefaultHttpRequestFactory(HttpVersion.HTTP_1_1, HttpMethod.POST, RandomStringUtils.randomAlphanumeric(50000).getBytes());
        System.out.println("WRITE INBOUND - 1st");
        except.set(null);
        previouslyClosedChecker.set(Boolean.FALSE);
        channel.writeInbound(CodecUtil.encodeHttpRequest(builder.getResult(factory)));
        Assert.assertEquals(8192, chunks.get(chunks.size() - 2).length());
        Assert.assertNull(except.get());
        Assert.assertFalse(previouslyClosedChecker.get());
        chunks.clear();
        builder.removeHeader(HttpHeaderNames.CONTENT_LENGTH);
        LOG.info("[PASS] valid request with content-length in headers.");
        System.out.println("WRITE INBOUND - 2nd");
        except.set(null);
        previouslyClosedChecker.set(Boolean.FALSE);
        channel.writeInbound(CodecUtil.encodeHttpRequest(builder.getResult(factory)));
        for (int i = 0; i < chunks.size(); i++) {
            String get = chunks.get(i);
            LOG.info("chunk[" + i + "]: " + get);
        }
        // The second chunk, whose length is bigger than 4096, is recognized as 
        // a new HTTP request. Apparently, it is impossible to parse 
        // a HTTP request sized as 4096 correctly.
        Assert.assertEquals(2, chunks.size());
        Assert.assertTrue(except.get() instanceof BadRequestException);
        Assert.assertTrue(previouslyClosedChecker.get());
        LOG.info("Channel Outbound Stream: " + CodecUtil.readOutboundString(channel));
        chunks.clear();
    }

    @Test
    public void testSmallSizeRequestWithoutContentLength() {
        AtomicReference<Exception> except = new AtomicReference<Exception>();
        AtomicReference<Boolean> previouslyClosedChecker = new AtomicReference<Boolean>();
        List<String> chunks = new ArrayList<String>();
        EmbeddedChannel channel = CodecUtil.createTestableChannel(chunks, except, previouslyClosedChecker,
                CheckableRoutingConfig.PLAIN_ROUTING.setChecker(CodecUtil.createHandlerAsRouteChecker(null)),
                CheckableRoutingConfig.SINGLE_VAR_ROUTING.setChecker(CodecUtil.createHandlerAsRouteChecker(null)),
                CheckableRoutingConfig.DUAL_VAR_ROUTING.setChecker(CodecUtil.createHandlerAsRouteChecker(null)));
        channel.pipeline().addFirst(new HttpRequestDecoder(4096, 8192, 8192), new HttpResponseEncoder());
        System.out.println("EmbeddedChannel: " + channel.id());
        HttpRequestBuilder builder = new HttpRequestBuilder(CharsetUtil.UTF_8);
        builder.header(HttpHeaderNames.HOST, "example.com");
        builder.header(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        builder.header(HttpHeaderNames.CONTENT_LENGTH, "500");
        builder.uri("/plain/tester/path");
        HttpMessageFactory factory = new DefaultHttpRequestFactory(HttpVersion.HTTP_1_1, HttpMethod.POST, RandomStringUtils.randomAlphanumeric(500).getBytes());
        System.out.println("WRITE INBOUND - 1st");
        except.set(null);
        previouslyClosedChecker.set(Boolean.FALSE);
        channel.writeInbound(CodecUtil.encodeHttpRequest(builder.getResult(factory)));
        LOG.info("Channel Outbound Stream: " + CodecUtil.readOutboundString(channel));
        Assert.assertEquals(2, chunks.size());
        Assert.assertNull(except.get());
        Assert.assertFalse(previouslyClosedChecker.get());
        chunks.clear();
        builder.removeHeader(HttpHeaderNames.CONTENT_LENGTH);
        LOG.info("[PASS] valid request with content-length in headers.");
        System.out.println("WRITE INBOUND - 2nd");
        except.set(null);
        previouslyClosedChecker.set(Boolean.FALSE);
        channel.writeInbound(CodecUtil.encodeHttpRequest(builder.getResult(factory)));
        LOG.info("Channel Outbound Stream: " + CodecUtil.readOutboundString(channel));
        Assert.assertEquals(1, chunks.size());
        Assert.assertNull(except.get());
        Assert.assertFalse(previouslyClosedChecker.get());
        chunks.clear();
        // The second chunk is a string without mark of new line, so this chunk is still waiting to be received.
        System.out.println("WRITE INBOUND - 3rd");
        except.set(null);
        previouslyClosedChecker.set(Boolean.FALSE);
        channel.writeInbound(CodecUtil.encodeHttpRequest(builder.getResult(factory)));
        for (int i = 0; i < chunks.size(); i++) {
            String get = chunks.get(i);
            LOG.info("chunk[" + i + "]: " + get.length());
        }
        LOG.info("Channel Outbound Stream: " + CodecUtil.readOutboundString(channel));
        Assert.assertTrue(except.get() instanceof UnsupportedMethodException);
        Assert.assertTrue(previouslyClosedChecker.get());
        chunks.clear();
    }

    @Test
    public void testClosedChannelCase() {
        ByteBuf buffer;
        try {
            buffer = Unpooled.copiedBuffer(FileUtils.readFileToByteArray(new File(this.getClass().getResource("/requests/OverPackageRequest").getFile())));
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
            return;
        }
        AtomicReference<Exception> except = new AtomicReference<Exception>();
        AtomicReference<Boolean> previouslyClosed = new AtomicReference<Boolean>();
        previouslyClosed.set(Boolean.FALSE);
        EmbeddedChannel channel = CodecUtil.createTestableChannel(null, except, previouslyClosed, new RoutingConfig() {
            @Override
            public String configureRoutingName() {
                return "OVERPACKAGE_TEST";
            }

            @Override
            public String configurePath() {
                return "/test/over/package";
            }

            @Override
            public HttpMethod[] configureMethods() {
                return new HttpMethod[]{HttpMethod.GET};
            }

            @Override
            public void configurePipeline(ChannelPipeline pipeline) {
                pipeline.addLast(new ChannelHandlerAdapter() {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        System.out.println(msg);
                        super.channelRead(ctx, msg);
                    }

                });
            }
        });
        channel.pipeline().addFirst(new HttpRequestDecoder());
        channel.writeInbound(buffer);
        Assert.assertTrue(except.get() instanceof NotFoundException);
    }
}
