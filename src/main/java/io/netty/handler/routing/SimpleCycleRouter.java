/*
 * This file is part of the netty-router package.
 * 
 * (c) Richard Lea <chigix@zoho.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package io.netty.handler.routing;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.util.internal.RecyclableArrayList;
import io.netty.util.internal.TypeParameterMatcher;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Simply separate packages in pipeline through Classes with special names
 * given.
 *
 * Exactly, I have to say that this Router Class is specially designed for
 * HttpRouter. Seldomly routing messages consist so complicated message anchors
 * like HTTP protocol.
 *
 * @author Richard Lea <chigix@zoho.com>
 * @param <BEGIN> The type of the message to start one new routing.
 * @param <END> The type of the message to end current routing.
 */
public abstract class SimpleCycleRouter<BEGIN, END> extends Router {

    private static final InternalLogger LOG = InternalLoggerFactory.getInstance(SimpleCycleRouter.class);

    private final TypeParameterMatcher matcherBegin = TypeParameterMatcher.find(this, SimpleCycleRouter.class, "BEGIN");
    private final TypeParameterMatcher matcherEnd = TypeParameterMatcher.find(this, SimpleCycleRouter.class, "END");

    private final AtomicReference<RoutingPipeline> activePipeline = new AtomicReference<RoutingPipeline>(null);

    public SimpleCycleRouter() {
        super();
    }

    public SimpleCycleRouter(boolean autoRelease, String routerTypeName) {
        super(autoRelease, routerTypeName);
    }

    public SimpleCycleRouter(boolean autoRelease) {
        super(autoRelease);
    }

    @Override
    protected void route(ChannelHandlerContext ctx, Object msg, Map<String, ChannelPipeline> routingPipelines) throws Exception {
        if (!ctx.channel().isOpen()) {
            // Assume this occurance should not exists because closed channel should have also closed socket input.
            LOG.warn(MessageFormat.format("One message is trying to be put in a closed channel: {0}", ctx.channel().id()));
            return;
        }
        RoutingPipeline pipeline;
        if (this.matcherBegin.match(msg)) {
            pipeline = (RoutingPipeline) this.routeBegin(ctx, (BEGIN) msg, routingPipelines);
            if (pipeline == null) {
                LOG.warn(MessageFormat.format("Null pipeline was returned in channel [{0}], it is suggested to throw exception before set a null as pipeline in channel.", ctx.channel().id()));
                return;
            }
            if (this.replaceActivePipeline(ctx, this.activePipeline.get(), pipeline)) {
                LOG.debug(MessageFormat.format("Channel [{0}] is switched to pipeline [{1}].", ctx.channel().id(), pipeline.getPipelineName()));
                this.activePipeline.set(pipeline);
                RecyclableArrayList forward_list = RecyclableArrayList.newInstance();
                this.decode(ctx, (BEGIN) msg, forward_list);
                for (Object forward_out : forward_list) {
                    this.pipelineForward(pipeline, forward_out);
                }
                forward_list.recycle();
                return;
            } else {
                LOG.warn(MessageFormat.format("Message Begin [{1}] Occured in occupied Pipeline [{0}]", this.activePipeline.get().getPipelineName(), msg.toString()));
            }
        } else if (this.matcherEnd.match(msg) && this.routeEnd(ctx, (END) msg)) {
            pipeline = (RoutingPipeline) this.activePipeline.get();
            if (this.replaceActivePipeline(ctx, pipeline, null)) {
                this.activePipeline.set(null);
                this.pipelineForward(pipeline, msg);
                LOG.debug(MessageFormat.format("Channel [{0}] is emptied by previous pipeline [{1}] finished.", ctx.channel().id(), pipeline.getPipelineName()));
                return;
            } else {
                throw new CycleEndException(pipeline, msg.toString());
            }
        }
        if ((pipeline = (RoutingPipeline) this.activePipeline.get()) != null) {
            this.pipelineForward(pipeline, msg);
        } else {
            LOG.error(MessageFormat.format("One message occured in an empty routing channel: {0}. "
                    + "Please Check Type Matching required by SimpleCycleRouter in class: {2}",
                    ctx.channel().id(), msg.toString(), this.getClass().getName()));
        }
    }

    /**
     * Decode the message object from {@code in} to {@code out}
     *
     * @param ctx the {@link ChannelHandlerContext} which this Router Belongs
     * to.
     * @param in the message object to be decoded
     * @param out the {@link List} to which decoded messages should be added.
     * @throws Exception is thrown if an error occurs.
     */
    protected void decode(ChannelHandlerContext ctx, BEGIN in, List<Object> out) throws Exception {
        out.add(in);
    }

    protected abstract ChannelPipeline routeBegin(ChannelHandlerContext ctx, BEGIN msg, Map<String, ChannelPipeline> routingPipelines) throws Exception;

    protected abstract boolean routeEnd(ChannelHandlerContext ctx, END msg) throws Exception;

}
