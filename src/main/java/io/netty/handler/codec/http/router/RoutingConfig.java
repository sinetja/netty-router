/*
 * This file is part of the netty-router package.
 * 
 * (c) Richard Lea <chigix@zoho.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package io.netty.handler.codec.http.router;

import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpMethod;

/**
 * The pattern defination for a routing, regarding the path defined, HTTP
 * methods specified and configure the pipeline for this routing.
 *
 * @author Richard Lea <chigix@zoho.com>
 */
public interface RoutingConfig {

    String configureRoutingName();

    String configurePath();

    HttpMethod[] configureMethods();

    void configurePipeline(ChannelPipeline pipeline);

    public static abstract class CONNECT implements RoutingConfig {

        @Override
        public final HttpMethod[] configureMethods() {
            return new HttpMethod[]{HttpMethod.CONNECT};
        }

    }

    public static abstract class DELETE implements RoutingConfig {

        @Override
        public final HttpMethod[] configureMethods() {
            return new HttpMethod[]{HttpMethod.DELETE};
        }

    }

    public static abstract class GET implements RoutingConfig {

        @Override
        public final HttpMethod[] configureMethods() {
            return new HttpMethod[]{HttpMethod.GET};
        }

    }

    public static abstract class HEAD implements RoutingConfig {

        @Override
        public final HttpMethod[] configureMethods() {
            return new HttpMethod[]{HttpMethod.HEAD};
        }

    }

    public static abstract class OPTIONS implements RoutingConfig {

        @Override
        public final HttpMethod[] configureMethods() {
            return new HttpMethod[]{HttpMethod.OPTIONS};
        }

    }

    public static abstract class PATCH implements RoutingConfig {

        @Override
        public final HttpMethod[] configureMethods() {
            return new HttpMethod[]{HttpMethod.PATCH};
        }

    }

    public static abstract class POST implements RoutingConfig {

        @Override
        public final HttpMethod[] configureMethods() {
            return new HttpMethod[]{HttpMethod.POST};
        }

    }

    public static abstract class PUT implements RoutingConfig {

        @Override
        public final HttpMethod[] configureMethods() {
            return new HttpMethod[]{HttpMethod.PUT};
        }

    }

    public static abstract class TRACE implements RoutingConfig {

        @Override
        public final HttpMethod[] configureMethods() {
            return new HttpMethod[]{HttpMethod.TRACE};
        }

    }

}
