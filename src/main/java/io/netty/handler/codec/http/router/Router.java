package io.netty.handler.codec.http.router;

/**
 * Targets of routes must be ChannelInboundHandler classes or instances.
 * In case of instances, the classes of the instances must be annotated with
 * io.netty.channel.ChannelHandler.Sharable.
 */
public class Router extends MethodRouter<Object, Router> {
  @Override protected Router getThis() { return this; }
}
