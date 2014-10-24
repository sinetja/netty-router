package io.netty.handler.codec.http;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public abstract class AbstractRouter<T> extends SimpleChannelInboundHandler<HttpRequest> {
  protected final MethodRouter<T> router;

  public AbstractRouter(MethodRouter<T> router) {
    this.router = router;
  }

  //----------------------------------------------------------------------------

  /** @param routed Will automatically be released. Please call routed.retain() if you want. */
  protected abstract void routed(Routed<T> routed);

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, HttpRequest req) {
    if (HttpHeaders.is100ContinueExpected(req)) {
      ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE));
      return;
    }

    // Route
    HttpMethod         method  = req.getMethod();
    QueryStringDecoder qsd     = new QueryStringDecoder(req.getUri());
    jauter.Routed<T>   jrouted = router.route(method, qsd.path());

    if (jrouted == null) {
      routed(null);
      return;
    }

    Routed<T> routed = new Routed<T>(jrouted.target(), jrouted.notFound(), req, qsd.path(), jrouted.params(), qsd.parameters());
    routed(routed);
  }
}
