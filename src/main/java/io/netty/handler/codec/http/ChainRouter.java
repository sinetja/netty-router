package io.netty.handler.codec.http;

import java.util.HashMap;
import java.util.Map;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.EventExecutorGroup;

// http://stackoverflow.com/questions/1069528/method-chaining-inheritance-don-t-play-well-together-java

/**
 * Inbound handler that converts HttpRequest to Routed and passes Routed to the
 * matched handler.
 */
@ChannelHandler.Sharable
@SuppressWarnings("unchecked")
public class ChainRouter<T extends ChainRouter<T>> extends SimpleChannelInboundHandler<HttpRequest> {
  public static final String ROUTER_HANDLER_NAME = ChainRouter.class.getName() + "_ROUTER_HANDLER";
  public static final String ROUTED_HANDLER_NAME = ChainRouter.class.getName() + "_ROUTED_HANDLER";

  //----------------------------------------------------------------------------

  protected final Map<HttpMethod, jauter.Router<Object>> routers =
      new HashMap<HttpMethod, jauter.Router<Object>>();

  protected final jauter.Router<Object> anyMethodRouter =
      new jauter.Router<Object>();

  //----------------------------------------------------------------------------

  protected EventExecutorGroup group;

  /** Fallback priority: handlerInstance404, handlerClass404, DefaultHandler404.INSTANCE */
  protected ChannelInboundHandler handlerInstance404;

  /** Fallback priority: handlerInstance404, handlerClass404, DefaultHandler404.INSTANCE */
  protected Class<? extends ChannelInboundHandler> handlerClass404;

  //----------------------------------------------------------------------------

  /**
   * Should be used to add the router to pipeline:
   * channel.pipeline().addLast(router.name(), router)
   */
  public String name() {
    return ROUTER_HANDLER_NAME;
  }

  public EventExecutorGroup group() {
    return group;
  }

  public ChannelInboundHandler handlerInstance404() {
    return handlerInstance404;
  }

  public Class<? extends ChannelInboundHandler> handlerClass404() {
    return handlerClass404;
  }

  //----------------------------------------------------------------------------


  public T group(EventExecutorGroup group) {
    this.group = group;
    return (T) this;
  }

  public T handler404(ChannelInboundHandler handlerInstance404) {
    this.handlerInstance404 = handlerInstance404;
    return (T) this;
  }

  public T handler404(Class<? extends ChannelInboundHandler> handlerClass404) {
    this.handlerClass404 = handlerClass404;
    return (T) this;
  }

  //----------------------------------------------------------------------------

  public T pattern(HttpMethod method, String path, ChannelInboundHandler handlerInstance) {
    getRouter(method).pattern(path, handlerInstance);
    return (T) this;
  }

  public T pattern(HttpMethod method, String path, Class<? extends ChannelInboundHandler> handlerClass) {
    getRouter(method).pattern(path, handlerClass);
    return (T) this;
  }

  public T patternFirst(HttpMethod method, String path, ChannelInboundHandler handlerInstance) {
    getRouter(method).patternFirst(path, handlerInstance);
    return (T) this;
  }

  public T patternFirst(HttpMethod method, String path, Class<? extends ChannelInboundHandler> handlerClass) {
    getRouter(method).patternFirst(path, handlerClass);
    return (T) this;
  }

  public T patternLast(HttpMethod method, String path, ChannelInboundHandler handlerInstance) {
    getRouter(method).patternLast(path, handlerInstance);
    return (T) this;
  }

  public T patternLast(HttpMethod method, String path, Class<? extends ChannelInboundHandler> handlerClass) {
    getRouter(method).patternLast(path, handlerClass);
    return (T) this;
  }

  private jauter.Router<Object> getRouter(HttpMethod method) {
    if (method == null) return anyMethodRouter;

    jauter.Router<Object> jr = routers.get(method);
    if (jr == null) {
      jr = new jauter.Router<Object>();
      routers.put(method, jr);
    }
    return jr;
  }

  //----------------------------------------------------------------------------

  public T CONNECT(String path, ChannelInboundHandler handlerInstance) {
    return pattern(HttpMethod.CONNECT, path, handlerInstance);
  }

  public T DELETE(String path, ChannelInboundHandler handlerInstance) {
    return pattern(HttpMethod.DELETE, path, handlerInstance);
  }

  public T GET(String path, ChannelInboundHandler handlerInstance) {
    return pattern(HttpMethod.GET, path, handlerInstance);
  }

  public T HEAD(String path, ChannelInboundHandler handlerInstance) {
    return pattern(HttpMethod.HEAD, path, handlerInstance);
  }

  public T OPTIONS(String path, ChannelInboundHandler handlerInstance) {
    return pattern(HttpMethod.OPTIONS, path, handlerInstance);
  }

  public T PATCH(String path, ChannelInboundHandler handlerInstance) {
    return pattern(HttpMethod.PATCH, path, handlerInstance);
  }

  public T POST(String path, ChannelInboundHandler handlerInstance) {
    return pattern(HttpMethod.POST, path, handlerInstance);
  }

  public T PUT(String path, ChannelInboundHandler handlerInstance) {
    return pattern(HttpMethod.PUT, path, handlerInstance);
  }

  public T TRACE(String path, ChannelInboundHandler handlerInstance) {
    return pattern(HttpMethod.TRACE, path, handlerInstance);
  }

  public T ANY(String path, ChannelInboundHandler handlerInstance) {
    return pattern(null, path, handlerInstance);
  }

  //----------------------------------------------------------------------------

  public T CONNECT(String path, Class<? extends ChannelInboundHandler> handlerClass) {
    return pattern(HttpMethod.CONNECT, path, handlerClass);
  }

  public T DELETE(String path, Class<? extends ChannelInboundHandler> handlerClass) {
    return pattern(HttpMethod.DELETE, path, handlerClass);
  }

  public T GET(String path, Class<? extends ChannelInboundHandler> handlerClass) {
    return pattern(HttpMethod.GET, path, handlerClass);
  }

  public T HEAD(String path, Class<? extends ChannelInboundHandler> handlerClass) {
    return pattern(HttpMethod.HEAD, path, handlerClass);
  }

  public T OPTIONS(String path, Class<? extends ChannelInboundHandler> handlerClass) {
    return pattern(HttpMethod.OPTIONS, path, handlerClass);
  }

  public T PATCH(String path, Class<? extends ChannelInboundHandler> handlerClass) {
    return pattern(HttpMethod.PATCH, path, handlerClass);
  }

  public T POST(String path, Class<? extends ChannelInboundHandler> handlerClass) {
    return pattern(HttpMethod.POST, path, handlerClass);
  }

  public T PUT(String path, Class<? extends ChannelInboundHandler> handlerClass) {
    return pattern(HttpMethod.PUT, path, handlerClass);
  }

  public T TRACE(String path, Class<? extends ChannelInboundHandler> handlerClass) {
    return pattern(HttpMethod.TRACE, path, handlerClass);
  }

  public T ANY(String path, Class<? extends ChannelInboundHandler> handlerClass) {
    return pattern(null, path, handlerClass);
  }

  //----------------------------------------------------------------------------

  public T CONNECT_FIRST(String path, ChannelInboundHandler handlerInstance) {
    return patternFirst(HttpMethod.CONNECT, path, handlerInstance);
  }

  public T DELETE_FIRST(String path, ChannelInboundHandler handlerInstance) {
    return patternFirst(HttpMethod.DELETE, path, handlerInstance);
  }

  public T GET_FIRST(String path, ChannelInboundHandler handlerInstance) {
    return patternFirst(HttpMethod.GET, path, handlerInstance);
  }

  public T HEAD_FIRST(String path, ChannelInboundHandler handlerInstance) {
    return patternFirst(HttpMethod.HEAD, path, handlerInstance);
  }

  public T OPTIONS_FIRST(String path, ChannelInboundHandler handlerInstance) {
    return patternFirst(HttpMethod.OPTIONS, path, handlerInstance);
  }

  public T PATCH_FIRST(String path, ChannelInboundHandler handlerInstance) {
    return patternFirst(HttpMethod.PATCH, path, handlerInstance);
  }

  public T POST_FIRST(String path, ChannelInboundHandler handlerInstance) {
    return patternFirst(HttpMethod.POST, path, handlerInstance);
  }

  public T PUT_FIRST(String path, ChannelInboundHandler handlerInstance) {
    return patternFirst(HttpMethod.PUT, path, handlerInstance);
  }

  public T TRACE_FIRST(String path, ChannelInboundHandler handlerInstance) {
    return patternFirst(HttpMethod.TRACE, path, handlerInstance);
  }

  public T ANY_FIRST(String path, ChannelInboundHandler handlerInstance) {
    return patternFirst(null, path, handlerInstance);
  }

  //----------------------------------------------------------------------------

  public T CONNECT_FIRST(String path, Class<? extends ChannelInboundHandler> handlerClass) {
    return patternFirst(HttpMethod.CONNECT, path, handlerClass);
  }

  public T DELETE_FIRST(String path, Class<? extends ChannelInboundHandler> handlerClass) {
    return patternFirst(HttpMethod.DELETE, path, handlerClass);
  }

  public T GET_FIRST(String path, Class<? extends ChannelInboundHandler> handlerClass) {
    return patternFirst(HttpMethod.GET, path, handlerClass);
  }

  public T HEAD_FIRST(String path, Class<? extends ChannelInboundHandler> handlerClass) {
    return patternFirst(HttpMethod.HEAD, path, handlerClass);
  }

  public T OPTIONS_FIRST(String path, Class<? extends ChannelInboundHandler> handlerClass) {
    return patternFirst(HttpMethod.OPTIONS, path, handlerClass);
  }

  public T PATCH_FIRST(String path, Class<? extends ChannelInboundHandler> handlerClass) {
    return patternFirst(HttpMethod.PATCH, path, handlerClass);
  }

  public T POST_FIRST(String path, Class<? extends ChannelInboundHandler> handlerClass) {
    return patternFirst(HttpMethod.POST, path, handlerClass);
  }

  public T PUT_FIRST(String path, Class<? extends ChannelInboundHandler> handlerClass) {
    return patternFirst(HttpMethod.PUT, path, handlerClass);
  }

  public T TRACE_FIRST(String path, Class<? extends ChannelInboundHandler> handlerClass) {
    return patternFirst(HttpMethod.TRACE, path, handlerClass);
  }

  public T ANY_FIRST(String path, Class<? extends ChannelInboundHandler> handlerClass) {
    return patternFirst(null, path, handlerClass);
  }

  //----------------------------------------------------------------------------

  public T CONNECT_LAST(String path, ChannelInboundHandler handlerInstance) {
    return patternLast(HttpMethod.CONNECT, path, handlerInstance);
  }

  public T DELETE_LAST(String path, ChannelInboundHandler handlerInstance) {
    return patternLast(HttpMethod.DELETE, path, handlerInstance);
  }

  public T GET_LAST(String path, ChannelInboundHandler handlerInstance) {
    return patternLast(HttpMethod.GET, path, handlerInstance);
  }

  public T HEAD_LAST(String path, ChannelInboundHandler handlerInstance) {
    return patternLast(HttpMethod.HEAD, path, handlerInstance);
  }

  public T OPTIONS_LAST(String path, ChannelInboundHandler handlerInstance) {
    return patternLast(HttpMethod.OPTIONS, path, handlerInstance);
  }

  public T PATCH_LAST(String path, ChannelInboundHandler handlerInstance) {
    return patternLast(HttpMethod.PATCH, path, handlerInstance);
  }

  public T POST_LAST(String path, ChannelInboundHandler handlerInstance) {
    return patternLast(HttpMethod.POST, path, handlerInstance);
  }

  public T PUT_LAST(String path, ChannelInboundHandler handlerInstance) {
    return patternLast(HttpMethod.PUT, path, handlerInstance);
  }

  public T TRACE_LAST(String path, ChannelInboundHandler handlerInstance) {
    return patternLast(HttpMethod.TRACE, path, handlerInstance);
  }

  public T ANY_LAST(String path, ChannelInboundHandler handlerInstance) {
    return patternLast(null, path, handlerInstance);
  }

  //----------------------------------------------------------------------------

  public T CONNECT_LAST(String path, Class<? extends ChannelInboundHandler> handlerClass) {
    return patternLast(HttpMethod.CONNECT, path, handlerClass);
  }

  public T DELETE_LAST(String path, Class<? extends ChannelInboundHandler> handlerClass) {
    return patternLast(HttpMethod.DELETE, path, handlerClass);
  }

  public T GET_LAST(String path, Class<? extends ChannelInboundHandler> handlerClass) {
    return patternLast(HttpMethod.GET, path, handlerClass);
  }

  public T HEAD_LAST(String path, Class<? extends ChannelInboundHandler> handlerClass) {
    return patternLast(HttpMethod.HEAD, path, handlerClass);
  }

  public T OPTIONS_LAST(String path, Class<? extends ChannelInboundHandler> handlerClass) {
    return patternLast(HttpMethod.OPTIONS, path, handlerClass);
  }

  public T PATCH_LAST(String path, Class<? extends ChannelInboundHandler> handlerClass) {
    return patternLast(HttpMethod.PATCH, path, handlerClass);
  }

  public T POST_LAST(String path, Class<? extends ChannelInboundHandler> handlerClass) {
    return patternLast(HttpMethod.POST, path, handlerClass);
  }

  public T PUT_LAST(String path, Class<? extends ChannelInboundHandler> handlerClass) {
    return patternLast(HttpMethod.PUT, path, handlerClass);
  }

  public T TRACE_LAST(String path, Class<? extends ChannelInboundHandler> handlerClass) {
    return patternLast(HttpMethod.TRACE, path, handlerClass);
  }

  public T ANY_LAST(String path, Class<? extends ChannelInboundHandler> handlerClass) {
    return patternLast(null, path, handlerClass);
  }

  //----------------------------------------------------------------------------

  public void removeTarget(Object target) {
    for (jauter.Router<Object> jr : routers.values()) jr.removeTarget(target);
    anyMethodRouter.removeTarget(target);
  }

  public void removePath(String path) {
    for (jauter.Router<Object> jr : routers.values()) jr.removePath(path);
    anyMethodRouter.removePath(path);
  }

  //----------------------------------------------------------------------------

  @Override
  public void channelRead0(ChannelHandlerContext ctx, HttpRequest req) throws InstantiationException, IllegalAccessException {
    if (HttpHeaders.is100ContinueExpected(req)) {
      ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE));
      return;
    }

    // Get router, using anyMethodRouter as fallback
    HttpMethod            method  = req.getMethod();
    jauter.Router<Object> jrouter = routers.get(method);
    if (jrouter == null)  jrouter = anyMethodRouter;

    // Route
    QueryStringDecoder    qsd     = new QueryStringDecoder(req.getUri());
    jauter.Routed<Object> jrouted = jrouter.route(qsd.path());

    // Route again, using anyMethodRouter as fallback
    if (jrouted == null && jrouter != anyMethodRouter) {
      jrouter = anyMethodRouter;
      jrouted = jrouter.route(qsd.path());
    }

    // Set handler and pathParams
    ChannelInboundHandler handler = null;
    Map<String, String>   pathParams;
    if (jrouted != null) {
      Object target = jrouted.target();
      if (target instanceof ChannelInboundHandler) {
        // Get handler from route target
        handler = (ChannelInboundHandler) target;
      } else {
        // Create handler from class
        Class<? extends ChannelInboundHandler> klass = (Class<? extends ChannelInboundHandler>) target;
        handler = klass.newInstance();
      }

      pathParams = jrouted.params();
    } else {
      pathParams = new HashMap<String, String>();
    }

    // If handler is not set, use fallback: (default: DefaultHandler404.INSTANCE)
    if (handler == null) {
      if (handlerInstance404 != null)
        handler = handlerInstance404;
      else if (handlerClass404 != null)
        handler = handlerClass404.newInstance();
      else
        handler = DefaultHandler404.INSTANCE;
    }

    ReferenceCountUtil.retain(req);
    Routed routed = new Routed(req, qsd.path(), pathParams, qsd.parameters());

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
    ctx.fireChannelRead(routed);
  }

  //----------------------------------------------------------------------------
  // Reverse routing.

  public String path(HttpMethod method, ChannelInboundHandler handlerInstance, Object... params) {
    return _path(method, handlerInstance, params);
  }

  public String path(HttpMethod method, Class<? extends ChannelInboundHandler> handlerClass, Object... params) {
    return _path(method, handlerClass, params);
  }

  private String _path(HttpMethod method, Object target, Object... params) {
    jauter.Router<Object> router = (method == null)? anyMethodRouter : routers.get(method);
    return (router == null)? null : router.path(target);
  }
}
