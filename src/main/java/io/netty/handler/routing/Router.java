/*
 * This file is part of the netty-router package.
 * 
 * (c) Richard Lea <chigix@zoho.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package io.netty.handler.routing;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandlerInvokerUtil;
import io.netty.channel.ChannelPipeline;
import io.netty.util.IllegalReferenceCountException;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Richard Lea <chigix@zoho.com>
 */
public abstract class Router extends ChannelHandlerAdapter {

    private static final InternalLogger LOG = InternalLoggerFactory.getInstance(Router.class);

    private final AtomicInteger nullNameRoutingCount = new AtomicInteger();

    private final Map<String, ChannelPipeline> routingPipelines = new ConcurrentHashMap<String, ChannelPipeline>();

    private final ConcurrentMap<Channel, RoutingPipeline> activePipeline = new ConcurrentHashMap<Channel, RoutingPipeline>();

    private RoutingPipeline exceptionPipeline;

    private final boolean autoRelease;

    private final String routerTypeName;

    /**
     * It should be set to false if this router is added after a decoder
     * handler, or {@link IllegalReferenceCountException} would be raised.
     *
     * @param autoRelease
     */
    public Router(boolean autoRelease) {
        this.autoRelease = autoRelease;
        this.routerTypeName = this.getClass().getName();
    }

    public Router(boolean autoRelease, String routerTypeName) {
        this.autoRelease = autoRelease;
        this.routerTypeName = routerTypeName;
    }

    public Router() {
        this(true);
    }

    @Override
    public final void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            this.route(ctx, msg, routingPipelines);
        } finally {
            if (autoRelease) {
                try {
                    ReferenceCountUtil.release(msg);
                } catch (IllegalReferenceCountException e) {
                    LOG.warn(MessageFormat.format("Message {0} seems deallocated previously, please be sure Router[{1}] doesn't follow any decoder.", msg.getClass(), this.routerTypeName));
                }
            }
        }
    }

    @Override
    public final void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.exceptionPipeline = (RoutingPipeline) this.newRouting(ctx, "EXCEPTION_PIPELINE");
        this.initExceptionRouting(exceptionPipeline);
        this.initRouter(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        this.pipelineForward(this.exceptionPipeline, cause);
    }

    /**
     * The util for create {@link RoutingPipeline} in router. If the router
     * previously contained a routing for the name, the old routing is replaced
     * by the new created routing.
     *
     * @param ctx
     * @param name The name of the routing to create. {@code null} to let the
     * name auto-generated.
     * @return
     */
    protected final ChannelPipeline newRouting(ChannelHandlerContext ctx, String name) {
        if (name == null) {
            name = "Routing-" + this.nullNameRoutingCount.incrementAndGet();
        }
        RoutingPipeline pipeline = new RoutingPipeline(ctx, name, this);
        ctx.pipeline().addLast(pipeline.getStart().getAnchorName(), pipeline.getStart());
        ctx.pipeline().addAfter(pipeline.getStart().getAnchorName(), pipeline.getEnd().getAnchorName(), pipeline.getEnd());
        this.routingPipelines.put(name, pipeline);
        return pipeline;
    }

    protected final ChannelPipeline newRouting(ChannelHandlerContext ctx) {
        return this.newRouting(ctx, null);
    }

    /**
     * Forward a message to a specified routing pipeline directly.
     *
     * @param pipeline The pipeline to be the where forwarding.
     * @param msg The message to be forwarded.
     */
    protected final void pipelineForward(ChannelPipeline pipeline, Object msg) {
        try {
            ChannelHandlerInvokerUtil.invokeChannelReadNow(((RoutingPipeline) pipeline).getStart().getContext(), msg);
        } catch (NullPointerException e) {
            if (pipeline == null) {
                throw new NullPointerException("Null Pipeline was not allowed to forward.");
            } else {
                throw e;
            }
        }
    }

    protected final void exceptionForward(Throwable exc) {
        this.pipelineForward(this.exceptionPipeline, exc);
    }

    /**
     * Replace the current acitve pipeline. The getter for activePipeline is not
     * provided from this {@link Router} for suggesting users maintainance on
     * old activepipeline.
     *
     * @param ctx
     * @param old The Old activePipeline.
     * @param neww The new Pipeline to set as current active pipeline.
     * @return
     */
    protected final boolean replaceActivePipeline(ChannelHandlerContext ctx, ChannelPipeline old, ChannelPipeline neww) {
        try {
            if (this.activePipeline.replace(ctx.channel(), (RoutingPipeline) old, (RoutingPipeline) neww)) {
                return true;
            }
        } catch (NullPointerException e) {
            if (old != null && neww != null) {
                throw e;
            } else if (ctx == null) {
                throw e;
            }
        }
        if (old == null && neww != null) {
            return this.activePipeline.putIfAbsent(ctx.channel(), (RoutingPipeline) neww) == null;
        }
        if (old != null && neww == null) {
            return this.activePipeline.remove(ctx.channel(), old);
        }
        if (old == null && neww == null) {
            return this.activePipeline.containsKey(ctx.channel());
        }
        return false;
    }

    protected final String getRouterTypeName() {
        return this.routerTypeName;
    }

    /**
     * Route this msg to an routingPipeline.
     *
     * @param ctx The {@link ChannelHandlerContext} which this {@link Router}
     * belongs to.
     * @param msg The message to be routed to another sub pipeline.
     * @param routingPipelines
     * @throws Exception
     */
    protected abstract void route(ChannelHandlerContext ctx, Object msg, Map<String, ChannelPipeline> routingPipelines) throws Exception;

    protected void initExceptionRouting(ChannelPipeline pipeline) {
        pipeline.addLast(new RoutingExceptionHandler());
    }

    /**
     * The Handler Added is not allowed to be used in Router, and initRouter is
     * the interface instead.
     *
     * @param ctx
     * @throws Exception
     */
    protected void initRouter(ChannelHandlerContext ctx) throws Exception {
    }

}
