package io.netty.handler.codec.http.router;

import io.netty.channel.ChannelHandlerContext;

/**
 * Inbound handler that uses router whose targets can be classes or instances of
 * the classes.
 */
public abstract class DualAbstractHandler extends AbstractHandler<Object, Router> {
  protected abstract void routed(ChannelHandlerContext ctx, MethodRouted<Object> routed, Object target) throws Exception;

  public DualAbstractHandler(Router router) {
    super(router);
  }

  @Override
  protected void routed(ChannelHandlerContext ctx, MethodRouted<Object> routed) throws Exception {
    Object target = null;
    Object objectOrClass = routed.target();
    if (objectOrClass instanceof Class) {
      // Create handler from class
      Class<?> klass = (Class<?>) objectOrClass;
      target = klass.newInstance();
    } else {
      // Get handler from route target
      target = objectOrClass;
    }

    routed(ctx, routed, target);
  }
}
