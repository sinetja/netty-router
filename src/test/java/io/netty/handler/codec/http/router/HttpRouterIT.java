/*
 * This file is part of the netty-router package.
 * 
 * (c) Richard Lea <chigix@zoho.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package io.netty.handler.codec.http.router;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.router.testutils.CodecUtil;
import io.netty.handler.codec.http.router.testutils.CheckableRoutingConfig;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.AfterClass;
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
    public void testRoutingSwitcher() {
        final AtomicReference<HttpRouted> routed = new AtomicReference<HttpRouted>();
        EmbeddedChannel channel = new EmbeddedChannel(new HttpRequestDecoder(), new HttpRouter() {

            @Override
            protected void initRoutings(ChannelHandlerContext ctx, HttpRouter router) {
                this.newRouting(ctx, CheckableRoutingConfig.PLAIN_ROUTING.setChecker(generateRouteChecker(routed)));
                this.newRouting(ctx, CheckableRoutingConfig.SINGLE_VAR_ROUTING.setChecker(generateRouteChecker(routed)));
                this.newRouting(ctx, CheckableRoutingConfig.DUAL_VAR_ROUTING.setChecker(generateRouteChecker(routed)));
            }

        });
        HttpRequest http_req;
        http_req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/plain/tester/path", Unpooled.copiedBuffer(RandomStringUtils.randomAlphanumeric(500).getBytes()));
        http_req.headers().set(HttpHeaderNames.HOST, "example.com");
        http_req.headers().set(HttpHeaderNames.CONTENT_LENGTH, "500");
        http_req.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        //http_req.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
        //http_req.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED);
        channel.writeInbound(CodecUtil.encodeHttpRequest(http_req));
        LOG.info("routed result: {}", routed.getAndSet(null));
        http_req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/plain/tester/path", Unpooled.copiedBuffer(RandomStringUtils.randomAlphanumeric(500).getBytes()));
        http_req.headers().set(HttpHeaderNames.HOST, "example.com");
        http_req.headers().set(HttpHeaderNames.CONTENT_LENGTH, "500");
        http_req.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        channel.writeInbound(CodecUtil.encodeHttpRequest(http_req));
        LOG.info("routed result: {}", routed.getAndSet(null));
        http_req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/plain/tester/path", Unpooled.copiedBuffer(RandomStringUtils.randomAlphanumeric(500).getBytes()));
        http_req.headers().set(HttpHeaderNames.HOST, "example.com");
        http_req.headers().set(HttpHeaderNames.CONTENT_LENGTH, "500");
        http_req.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        channel.writeInbound(CodecUtil.encodeHttpRequest(http_req));
        LOG.info("routed result: {}", routed.getAndSet(null));
        http_req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/single/var/BANKAI/suffix", Unpooled.copiedBuffer(RandomStringUtils.randomAlphanumeric(500).getBytes()));
        http_req.headers().set(HttpHeaderNames.HOST, "example.com");
        http_req.headers().set(HttpHeaderNames.CONTENT_LENGTH, "500");
        http_req.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        channel.writeInbound(CodecUtil.encodeHttpRequest(http_req));
        LOG.info("routed result: {}", routed.getAndSet(null));
    }

    private ChannelHandler generateRouteChecker(final AtomicReference<HttpRouted> routed) {
        return new SimpleChannelInboundHandler<HttpRouted>() {

            @Override
            protected void messageReceived(ChannelHandlerContext ctx, HttpRouted msg) throws Exception {
                System.out.println(msg.getRequestMsg());
                routed.set(msg);
                LOG.info("=============ROUTE END==============");
            }
        };
    }

}
