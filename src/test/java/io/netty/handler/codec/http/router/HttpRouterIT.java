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
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.router.exceptions.BadRequestException;
import io.netty.handler.codec.http.router.exceptions.LastNotFoundException;
import io.netty.handler.codec.http.router.exceptions.NotFoundException;
import io.netty.handler.codec.http.router.exceptions.UnsupportedMethodException;
import io.netty.handler.codec.http.router.testutil.CodecUtil;
import io.netty.handler.codec.http.router.testutil.CheckableRoutingConfig;
import io.netty.handler.codec.http.router.testutil.SnapshotHttpException;
import io.netty.handler.codec.http.router.testutils.builder.ContentTypes;
import io.netty.handler.codec.http.router.testutils.builder.DefaultHttpRequestFactory;
import io.netty.handler.codec.http.router.testutils.builder.HttpMessageFactory;
import io.netty.handler.codec.http.router.testutils.builder.HttpRequestBuilder;
import io.netty.handler.routing.UnableRoutingMessageException;
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
        List<SnapshotHttpException> exceptions = new ArrayList<SnapshotHttpException>();
        EmbeddedChannel channel = CodecUtil.createTestableChannel(null, exceptions, new RoutingConfig() {
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
                pipeline.addLast(new SimpleChannelInboundHandler<HttpRouted>() {
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
        for (SnapshotHttpException exception : exceptions) {
            Assert.fail(MessageFormat.format("There should be no exception, but {0} was thrown.", exception));
            if (exception.isChannelClosed()) {
                Assert.fail("Channel Should not be closed, but closed before Exception thrown: " + exception);
            }
        }
    }

    @Test
    /**
     * Put an array of Http Requests into one opened channel, testing for
     * comprehensibility.
     */
    public void testRoutingSwitcher() {
        AtomicReference<HttpRouted> routed = new AtomicReference<HttpRouted>();
        List<SnapshotHttpException> exceptions = new ArrayList<SnapshotHttpException>();
        EmbeddedChannel channel = CodecUtil.createTestableChannel(null, exceptions,
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
        Assert.assertEquals("Unexpected exception thrown for a correct routing.", 0, exceptions.size());
        LOG.info("PASS [/plain/tester/path]");
        routed.set(null);
        exceptions.clear();
        channel.writeInbound(CodecUtil.encodeHttpRequest(builder.getResult(factory).setUri("//plain/tester/path")));
        Assert.assertSame(CheckableRoutingConfig.PLAIN_ROUTING, routed.get().unwrapRoutingConf());
        Assert.assertEquals("Unexpected exception thrown for a correct routing.", 0, exceptions.size());
        LOG.info("PASS [//plain/tester/path]");
        routed.set(null);
        exceptions.clear();
        channel.writeInbound(CodecUtil.encodeHttpRequest(builder.getResult(factory).setUri("/single/var/BANKAI/suffix")));
        Assert.assertSame(CheckableRoutingConfig.SINGLE_VAR_ROUTING, routed.get().unwrapRoutingConf());
        Assert.assertEquals("Unexpected exception thrown for a correct routing.", 0, exceptions.size());
        LOG.info("PASS [/single/var/BANKAI/suffix]");
        routed.set(null);
        exceptions.clear();
        channel.writeInbound(CodecUtil.encodeHttpRequest(builder.getResult(factory).setUri("/plain/tester//path")));
        Assert.assertNull(routed.get());
        Assert.assertTrue("", exceptions.size() > 0);
        Assert.assertTrue(exceptions.get(0).getCause() instanceof NotFoundException);
        Assert.assertFalse(exceptions.get(0).isChannelClosed());
        Assert.assertTrue(exceptions.get(1).getCause() instanceof LastNotFoundException);
        Assert.assertFalse(exceptions.get(1).isChannelClosed());
        LOG.info("Channel Outbound Stream: " + CodecUtil.readOutboundString(channel));
        LOG.info("PASS [/plain/tester//path]");
        routed.set(null);
    }

    @Test
    public void testOverChunksizedRequestWithoutContentLength() {
        final ArrayList<HttpResponse> outputs = new ArrayList<HttpResponse>();
        List<SnapshotHttpException> exceptions = new ArrayList<SnapshotHttpException>();
        List<String> chunks = new ArrayList<String>();
        EmbeddedChannel channel = CodecUtil.createTestableChannel(chunks, exceptions,
                CheckableRoutingConfig.PLAIN_ROUTING.setChecker(CodecUtil.createHandlerAsRouteChecker(null)),
                CheckableRoutingConfig.SINGLE_VAR_ROUTING.setChecker(CodecUtil.createHandlerAsRouteChecker(null)),
                CheckableRoutingConfig.DUAL_VAR_ROUTING.setChecker(CodecUtil.createHandlerAsRouteChecker(null)));
        channel.pipeline().addFirst(new HttpRequestDecoder(4096, 8192, 8192), new ChannelHandlerAdapter() {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                outputs.add((HttpResponse) msg);
                super.write(ctx, msg, promise);
            }

        }, new FullResponseLengthFixer());
        System.out.println("EmbeddedChannel: " + channel.id());
        HttpRequestBuilder builder = new HttpRequestBuilder(CharsetUtil.UTF_8);
        builder.header(HttpHeaderNames.HOST, "example.com");
        builder.header(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        builder.header(HttpHeaderNames.CONTENT_LENGTH, "50000");
        builder.uri("/plain/tester/path");
        HttpMessageFactory factory = new DefaultHttpRequestFactory(HttpVersion.HTTP_1_1, HttpMethod.POST, RandomStringUtils.randomAlphanumeric(50000).getBytes());
        System.out.println("WRITE INBOUND - 1st");
        exceptions.clear();
        channel.writeInbound(CodecUtil.encodeHttpRequest(builder.getResult(factory)));
        Assert.assertEquals(8192, chunks.get(chunks.size() - 2).length());
        Assert.assertEquals("Unexpected exception thrown for a correct routing.", 0, exceptions.size());
        chunks.clear();
        builder.removeHeader(HttpHeaderNames.CONTENT_LENGTH);
        LOG.info("[PASS] valid request with content-length in headers.");
        System.out.println("WRITE INBOUND - 2nd");
        exceptions.clear();
        channel.writeInbound(CodecUtil.encodeHttpRequest(builder.getResult(factory)));
        Assert.assertEquals(2, chunks.size());
        Assert.assertTrue(chunks.get(0).split("decodeResult: success", 2)[0].length() > 10);
        Assert.assertTrue(chunks.get(1).split("decodeResult: failure", 2)[0].length() > 10);
        // The second chunk, whose length is bigger than 4096, is recognized as 
        // a new HTTP request. Apparently, it is impossible to parse 
        // a HTTP request sized as 4096 correctly.
        Assert.assertTrue(exceptions.size() > 1);
        // Wrapped in HttpRouter#routeBegin
        Assert.assertTrue(exceptions.get(0).getSnapshoted() instanceof BadRequestException);
        Assert.assertFalse(exceptions.get(0).isChannelClosed());
        Assert.assertTrue(exceptions.get(1).getCause() instanceof DecoderException);
        Assert.assertFalse(exceptions.get(1).isChannelClosed());
        chunks.clear();
    }

    @Test
    public void testSmallSizeRequestWithoutContentLength() {
        List<SnapshotHttpException> exceptions = new ArrayList<SnapshotHttpException>();
        List<String> chunks = new ArrayList<String>();
        EmbeddedChannel channel = CodecUtil.createTestableChannel(chunks, exceptions,
                CheckableRoutingConfig.PLAIN_ROUTING.setChecker(CodecUtil.createHandlerAsRouteChecker(null)),
                CheckableRoutingConfig.SINGLE_VAR_ROUTING.setChecker(CodecUtil.createHandlerAsRouteChecker(null)),
                CheckableRoutingConfig.DUAL_VAR_ROUTING.setChecker(CodecUtil.createHandlerAsRouteChecker(null)));
        channel.pipeline().addFirst(new HttpRequestDecoder(4096, 8192, 8192));
        System.out.println("EmbeddedChannel: " + channel.id());
        HttpRequestBuilder builder = new HttpRequestBuilder(CharsetUtil.UTF_8);
        builder.header(HttpHeaderNames.HOST, "example.com");
        builder.header(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        builder.header(HttpHeaderNames.CONTENT_LENGTH, "500");
        builder.uri("/plain/tester/path");
        HttpMessageFactory factory = new DefaultHttpRequestFactory(HttpVersion.HTTP_1_1, HttpMethod.POST, RandomStringUtils.randomAlphanumeric(500).getBytes());
        System.out.println("WRITE INBOUND - 1st");
        exceptions.clear();
        channel.writeInbound(CodecUtil.encodeHttpRequest(builder.getResult(factory)));
        Assert.assertEquals(2, chunks.size());
        Assert.assertEquals("Unexpected exception thrown for a correct routing.", 0, exceptions.size());
        chunks.clear();
        builder.removeHeader(HttpHeaderNames.CONTENT_LENGTH);
        LOG.info("[PASS] valid request with content-length in headers.");
        System.out.println("WRITE INBOUND - 2nd");
        exceptions.clear();
        channel.writeInbound(CodecUtil.encodeHttpRequest(builder.getResult(factory)));
        Assert.assertEquals(1, chunks.size());
        Assert.assertEquals("Unexpected exception thrown for a correct routing.", 0, exceptions.size());
        chunks.clear();
        // The second chunk is a string without mark of new line, so this chunk is still waiting to be received.
        System.out.println("WRITE INBOUND - 3rd");
        exceptions.clear();
        Assert.assertNull(channel.readOutbound());
        channel.writeInbound(CodecUtil.encodeHttpRequest(builder.getResult(factory)));
        Assert.assertEquals(1, chunks.size());
        Assert.assertTrue(exceptions.size() > 0);
        Assert.assertTrue(exceptions.get(0).getSnapshoted() instanceof BadRequestException);
        Assert.assertTrue(exceptions.get(0).getCause() instanceof UnsupportedMethodException);
        Assert.assertFalse("Channel [#" + channel.id() + "] is unexpectedly closed.", exceptions.get(0).isChannelClosed());
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
        List<SnapshotHttpException> exceptions = new ArrayList<SnapshotHttpException>();
        EmbeddedChannel channel = CodecUtil.createTestableChannel(null, exceptions, new RoutingConfig() {
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
        channel.pipeline().addFirst(new HttpRequestDecoder(), new ChannelHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                if (msg instanceof HttpContent) {
                    ctx.channel().close();
                }
                super.channelRead(ctx, msg);
            }

        });
        channel.writeInbound(buffer);
        Assert.assertTrue(exceptions.size() > 1);
        Assert.assertTrue(exceptions.get(0).getSnapshoted() instanceof NotFoundException);
        for (int i = 1; i < exceptions.size(); i++) {
            SnapshotHttpException get = exceptions.get(i);
            Assert.assertTrue(get.getCause() instanceof UnableRoutingMessageException);
        }
    }

    @Test
    public void testUnsupportMethodException() {
        ByteBuf buffer;
        try {
            buffer = Unpooled.copiedBuffer(FileUtils.readFileToByteArray(new File(this.getClass().getResource("/requests/UnsupportedMethodRequest").getFile())));
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
            return;
        }
        List<SnapshotHttpException> exceptions = new ArrayList<SnapshotHttpException>();
        List<String> chunks = new ArrayList<String>();
        EmbeddedChannel channel = CodecUtil.createTestableChannel(chunks, exceptions,
                CheckableRoutingConfig.PLAIN_ROUTING.setChecker(CodecUtil.createHandlerAsRouteChecker(null)));
        channel.pipeline().addFirst(new HttpRequestDecoder(4096, 8192, 8192));
        System.out.println("EmbeddedChannel: " + channel.id());
        exceptions.clear();
        channel.writeInbound(buffer);
        Assert.assertTrue(exceptions.size() > 1);
        Assert.assertTrue(exceptions.get(0).getSnapshoted() instanceof BadRequestException);
        Assert.assertTrue(exceptions.get(0).getCause() instanceof UnsupportedMethodException);
        Assert.assertTrue(exceptions.get(1).getSnapshoted() instanceof LastNotFoundException);
        Assert.assertTrue(exceptions.get(2).getSnapshoted() instanceof BadRequestException);
        Assert.assertTrue(exceptions.get(2).getSnapshoted().getHttpRequest().decoderResult().isFailure());
    }

    @Test
    public void test100ContinueGetRequest() {
        List<SnapshotHttpException> exceptions = new ArrayList<SnapshotHttpException>();
        System.out.println("test100ContinueGetRequest");
        EmbeddedChannel channel = CodecUtil.createTestableChannel(null, exceptions, new RoutingConfig.GET() {
            @Override
            public String configureRoutingName() {
                return "TESTING_GET";
            }

            @Override
            public String configurePath() {
                return "/";
            }

            @Override
            public void configurePipeline(ChannelPipeline pipeline) {
                pipeline.addLast(new ChannelHandlerAdapter() {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        System.out.println(msg);
                    }

                });
            }
        });
        channel.pipeline().addFirst(new HttpRequestDecoder());
        ByteBuf buffer;
        try {
            buffer = Unpooled.copiedBuffer(FileUtils.readFileToByteArray(new File(this.getClass().getResource("/requests/100ContinueGetRequest").getFile())));
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
            return;
        }
        channel.writeInbound(buffer);
        Assert.assertEquals(0, exceptions.size());
    }

}
