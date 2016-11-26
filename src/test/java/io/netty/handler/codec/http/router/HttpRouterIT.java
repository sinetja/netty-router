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
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.router.testutil.CodecUtil;
import io.netty.handler.codec.http.router.testutil.CheckableRoutingConfig;
import io.netty.handler.codec.http.router.testutils.builder.DefaultHttpRequestFactory;
import io.netty.handler.codec.http.router.testutils.builder.HttpMessageFactory;
import io.netty.handler.codec.http.router.testutils.builder.HttpRequestBuilder;
import io.netty.util.CharsetUtil;
import java.text.MessageFormat;
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
        final AtomicReference<Boolean> channelActive = new AtomicReference<Boolean>();
        EmbeddedChannel channel = new EmbeddedChannel(new HttpRequestDecoder(), new HttpRouter() {

            @Override
            protected void initRoutings(ChannelHandlerContext ctx, HttpRouter router) {
                this.newRouting(ctx, CheckableRoutingConfig.PLAIN_ROUTING.setChecker(CodecUtil.createHandlerAsRouteChecker(routed)));
                this.newRouting(ctx, CheckableRoutingConfig.SINGLE_VAR_ROUTING.setChecker(CodecUtil.createHandlerAsRouteChecker(routed)));
                this.newRouting(ctx, CheckableRoutingConfig.DUAL_VAR_ROUTING.setChecker(CodecUtil.createHandlerAsRouteChecker(routed)));
            }

            @Override
            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                super.channelActive(ctx);
                LOG.debug(MessageFormat.format("Channel [{0}] is actived.", ctx.channel().id()));
                channelActive.set(Boolean.TRUE);
            }

            @Override
            public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                super.channelInactive(ctx);
                LOG.debug(MessageFormat.format("Channel [{0}] is inactived and removed in HttpRouter.", ctx.channel().id()));
                channelActive.set(Boolean.FALSE);
            }

        });
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
        LOG.info("PASS [/plain/tester//path]");
        routed.set(null);
        Assert.assertSame(Boolean.FALSE, channelActive.get());
    }

}
