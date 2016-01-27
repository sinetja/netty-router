/*
 * This file is part of the netty-router package.
 * 
 * (c) Richard Lea <chigix@zoho.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package io.netty.handler.codec.http.router.testutils;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.router.RoutingConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Special RoutingConfig for embedding testing codes.
 *
 * @author Richard Lea <chigix@zoho.com>
 */
public abstract class CheckableRoutingConfig implements RoutingConfig {

    private static final Logger LOG = LoggerFactory.getLogger(CheckableRoutingConfig.class.getName());

    public static final CheckableRoutingConfig PLAIN_ROUTING = new CheckableRoutingConfig() {

        @Override
        public String configureRoutingName() {
            return "PLAIN_ROUTING_FOR_TESTER";
        }

        @Override
        public String configurePath() {
            return "/plain/tester/path";
        }

        @Override
        public HttpMethod[] configureMethods() {
            return new HttpMethod[]{HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE};
        }

    };
    public static final CheckableRoutingConfig SINGLE_VAR_ROUTING = new CheckableRoutingConfig() {

        @Override
        public String configureRoutingName() {
            return "SINGLE_VAR_ROUTING_FOR_TESTER";
        }

        @Override
        public String configurePath() {
            return "/single/var/:var1/suffix";
        }

        @Override
        public HttpMethod[] configureMethods() {
            return new HttpMethod[]{HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE};
        }

    };
    public static final CheckableRoutingConfig DUAL_VAR_ROUTING = new CheckableRoutingConfig() {

        @Override
        public String configureRoutingName() {
            return "DUAL_VAR_ROUTING_FOR_TESTER";
        }

        @Override
        public String configurePath() {
            return "/dual/var/:var1/gap/input/:var2/suffix";
        }

        @Override
        public HttpMethod[] configureMethods() {
            return new HttpMethod[]{HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE};
        }

    };

    private ChannelHandler checker = null;

    public CheckableRoutingConfig setChecker(ChannelHandler checker) {
        this.checker = checker;
        return this;
    }

    @Override
    public final void configurePipeline(ChannelPipeline pipeline) {
        pipeline.addLast(new ChannelHandlerAdapter() {

            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                LOG.info("MessageForward: {}", msg);
                ctx.fireChannelRead(msg);
            }

        }).addLast(checker);
    }

}
