package io.netty.handler.codec.http.router;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * This handler should be put at the last position of the inbound pipeline to
 * catch all exceptions caused by bad client (closed connection, malformed request etc.)
 * and server processing.
 *
 * By default exceptions are logged to stderr. You may need to override
 * onUnknownMessage, onBadClient, and onBadServer to log to more suitable places.
 */
@Sharable
public class BadClientSilencer extends SimpleChannelInboundHandler<Object> {
  protected void onUnknownMessage(Object msg) {
    if (msg != io.netty.handler.codec.http.LastHttpContent.EMPTY_LAST_CONTENT)
      System.err.println("Unknown msg: " + msg);
  }

  protected void onBadClient(Throwable e) {
    System.err.println("Caught exception (maybe client is bad): " + e);
  }

  protected void onBadServer(Throwable e) {
    System.err.println("Caught exception (maybe server is bad): " + e);
  }

  //----------------------------------------------------------------------------

  @Override
  public void channelRead0(ChannelHandlerContext ctx, Object msg) {
    // This handler is the last inbound handler.
    // This means msg has not been handled by any previous handler.
    ctx.channel().close();
    onUnknownMessage(msg);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
    ctx.channel().close();

    if (e instanceof java.io.IOException                            ||  // Connection reset by peer, Broken pipe
        e instanceof java.nio.channels.ClosedChannelException       ||
        e instanceof io.netty.handler.codec.DecoderException        ||
        e instanceof io.netty.handler.codec.CorruptedFrameException ||  // Bad WebSocket frame
        e instanceof java.lang.IllegalArgumentException             ||  // Use https://... URL to connect to HTTP server
        e instanceof javax.net.ssl.SSLException                     ||  // Use http://... URL to connect to HTTPS server
        e instanceof io.netty.handler.ssl.NotSslRecordException)
      onBadClient(e);  // Maybe client is bad
    else
      onBadServer(e);  // Maybe server is bad
  }
}
