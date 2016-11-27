/*
 * This file is part of the netty-router package.
 * 
 * (c) Richard Lea <chigix@zoho.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package io.netty.handler.codec.http.router;

import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.router.exceptions.BadRequestException;
import io.netty.handler.codec.http.router.exceptions.UnsupportedMethodException;
import io.netty.handler.codec.http.router.testutil.CodecUtil;
import io.netty.handler.codec.http.router.testutil.CheckableRoutingConfig;
import io.netty.handler.codec.http.router.testutils.builder.DefaultHttpRequestFactory;
import io.netty.handler.codec.http.router.testutils.builder.HttpMessageFactory;
import io.netty.handler.codec.http.router.testutils.builder.HttpRequestBuilder;
import io.netty.util.CharsetUtil;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
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
        channel.pipeline().addFirst(new HttpRequestDecoder(), new HttpResponseEncoder());
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
        channel.pipeline().addFirst(new HttpRequestDecoder(4096, 8192, 8192), new HttpResponseEncoder());
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

}
