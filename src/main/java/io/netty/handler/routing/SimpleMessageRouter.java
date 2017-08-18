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
import java.util.List;
import java.util.Map;

/**
 *
 * @author Richard Lea <chigix@zoho.com>
 * @param <T> The class of the message needed to be routed.
 */
public abstract class SimpleMessageRouter<T> extends Router {

    private static final InternalLogger LOG = InternalLoggerFactory.getInstance(SimpleMessageRouter.class);

    private final TypeParameterMatcher matcher = TypeParameterMatcher.find(this, SimpleMessageRouter.class, "T");

    public SimpleMessageRouter(boolean autoRelease) {
        super(autoRelease);
    }

    public SimpleMessageRouter(boolean autoRelease, String routerTypeName) {
        super(autoRelease, routerTypeName);
    }

    public SimpleMessageRouter() {
        super();
    }

    @Override
    protected final void route(ChannelHandlerContext ctx, Object msg, Map<String, ChannelPipeline> routings) throws Exception {
        if (!ctx.channel().isOpen()) {
            // Assume this occurance should not exists because closed channel should have also closed socket input.
            // And Unexpectedly closed channel should have related error thrown previously.
            throw new UnableRoutingMessageException("Message is trying to be put in a closed channel", msg);
        }
        final ChannelPipeline dispatched_routing;
        if (matcher.match(msg)) {
            dispatched_routing = dispatch(ctx, (T) msg, routings);
        } else {
            throw new UnableRoutingMessageException("The Type of message readed is not supported.", msg);
        }
        if (dispatched_routing == null) {
            LOG.warn("Returning NULL as dispatched Routing is not suggested, and insteadly an exception should be thrown.");
            return;
        }
        RecyclableArrayList forward_list = RecyclableArrayList.newInstance();
        try {
            this.decode(ctx, (T) msg, forward_list);
            for (Object forward_out : forward_list) {
                this.pipelineForward(dispatched_routing, forward_out);
            }
        } finally {
            forward_list.recycle();
        }
    }

    protected abstract ChannelPipeline dispatch(ChannelHandlerContext ctx, T msg, Map<String, ChannelPipeline> routings) throws Exception;

    /**
     * Decode the message object from {@code in} to {@code out}
     *
     * @param ctx the {@link ChannelHandlerContext} which this Router Belongs
     * to.
     * @param in the message object to be decoded
     * @param out the {@link List} to which decoded messages should be added.
     * @throws Exception is thrown if an error occurs.
     */
    protected void decode(ChannelHandlerContext ctx, T in, List<Object> out) throws Exception {
        out.add(in);
    }

}
