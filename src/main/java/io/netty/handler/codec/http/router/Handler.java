package io.netty.handler.codec.http.router;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.util.concurrent.EventExecutorGroup;

/**
 * Inbound handler that converts HttpRequest to Routed and passes Routed to the
 * matched handler.
 */
public class Handler extends AbstractHandler<Object, Router> {
  public static final String ROUTER_HANDLER_NAME = Handler.class.getName() + "_ROUTER_HANDLER";
  public static final String ROUTED_HANDLER_NAME = Handler.class.getName() + "_ROUTED_HANDLER";

  //----------------------------------------------------------------------------

  protected EventExecutorGroup group;

  public Handler(Router router) {
    super(router);
  }

  public Handler group(EventExecutorGroup group) {
    this.group = group;
    return this;
  }

  public EventExecutorGroup group() {
    return group;
  }

  /**
   * Should be used to add the router to pipeline:
   * channel.pipeline().addLast(router.name(), router)
   */
  public String name() {
    return ROUTER_HANDLER_NAME;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void routed(ChannelHandlerContext ctx, MethodRouted<Object> routed) throws InstantiationException, IllegalAccessException {
    // Set handler
    ChannelInboundHandler handler = null;
    Object target = routed.target();
    if (target instanceof ChannelInboundHandler) {
      // Get handler from route target
      handler = (ChannelInboundHandler) target;
    } else {
      // Create handler from class
      Class<? extends ChannelInboundHandler> klass = (Class<? extends ChannelInboundHandler>) target;
      handler = klass.newInstance();
    }

    // The handler may have been added (keep alive)
    ChannelPipeline pipeline     = ctx.pipeline();
    ChannelHandler  addedHandler = pipeline.get(ROUTED_HANDLER_NAME);
    if (handler != addedHandler) {
      if (addedHandler == null) {
        if (group == null)
          pipeline.addAfter(ROUTER_HANDLER_NAME, ROUTED_HANDLER_NAME, handler);
        else
          pipeline.addAfter(group, ROUTER_HANDLER_NAME, ROUTED_HANDLER_NAME, handler);
      } else {
        pipeline.replace(addedHandler, ROUTED_HANDLER_NAME, handler);
      }
    }

    // Pass to the routed handler
    routed.retain();
    ctx.fireChannelRead(new Routed(routed.target(), routed.notFound(), routed.request(), routed.path(), routed.pathParams(), routed.queryParams()));
  }
}
