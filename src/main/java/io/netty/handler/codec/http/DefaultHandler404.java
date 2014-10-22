package io.netty.handler.codec.http;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class DefaultHandler404 extends SimpleChannelInboundHandler<Routed> {
  private static final byte[] CONTENT_404 = "404 Not Found".getBytes();

  @Override
  public void channelRead0(ChannelHandlerContext ctx, Routed routed) {
    HttpResponse res = new DefaultFullHttpResponse(
      HttpVersion.HTTP_1_1,
      HttpResponseStatus.NOT_FOUND,
      Unpooled.wrappedBuffer(CONTENT_404)
    );

    HttpHeaders headers = res.headers();
    headers.set(HttpHeaders.Names.CONTENT_TYPE,   "text/plain");
    headers.set(HttpHeaders.Names.CONTENT_LENGTH, CONTENT_404.length);

    if (!HttpHeaders.isKeepAlive(routed.request())) {
      ctx.writeAndFlush(res).addListener(ChannelFutureListener.CLOSE);
    } else {
      headers.set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
      ctx.writeAndFlush(res);
    }
  }
}
