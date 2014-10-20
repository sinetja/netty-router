package io.netty.handler.codec.http

import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.Unpooled

import io.netty.channel.{
  ChannelHandlerContext,
  ChannelInitializer,
  ChannelFutureListener,
  ChannelOption,
  SimpleChannelInboundHandler
}

import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel

object Example {
  def main(args: Array[String]) {
    Server.start(8000)
  }
}

object Server {
  def start(port: Int) {
    val bossGroup   = new NioEventLoopGroup(1)
    val workerGroup = new NioEventLoopGroup

    val b = new ServerBootstrap
    b.group(bossGroup, workerGroup)
     .childOption(ChannelOption.TCP_NODELAY,  java.lang.Boolean.TRUE)
     .childOption(ChannelOption.SO_KEEPALIVE, java.lang.Boolean.TRUE)
     .channel(classOf[NioServerSocketChannel])
     .childHandler(ExampleInitializer)

    val ch = b.bind(port).sync().channel

    println("Server started: http://127.0.0.1:" + port + '/')
    ch.closeFuture.sync()

    bossGroup.shutdownGracefully()
    workerGroup.shutdownGracefully()
  }
}

object ExampleInitializer extends ChannelInitializer[SocketChannel] {
  private val router = (new Router)
    .pattern(HttpMethod.GET, "/",             new ExampleHandler)
    .pattern(HttpMethod.GET, "/articles/:id", classOf[ExampleHandler])

  def initChannel(ch: SocketChannel) {
    val p = ch.pipeline
    p.addLast(new HttpServerCodec)
    p.addLast(router.name, router)
  }
}

class ExampleHandler extends SimpleChannelInboundHandler[HttpRequest] {
  override def channelRead0(ctx: ChannelHandlerContext, req: HttpRequest) {
    val pathParams  = Router.pathParams(req)
    val queryParams = Router.queryParams(req)

    val res = new DefaultFullHttpResponse(
      HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
      Unpooled.copiedBuffer(s"pathParams: $pathParams, queryParams: $queryParams".getBytes)
    )
    res.headers.set(HttpHeaders.Names.CONTENT_TYPE,   "text/plain")
    res.headers.set(HttpHeaders.Names.CONTENT_LENGTH, res.content.readableBytes)

    Router.keepAliveWriteAndFlush(ctx, req, res)
  }
}
