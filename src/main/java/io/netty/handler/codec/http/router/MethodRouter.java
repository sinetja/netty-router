package io.netty.handler.codec.http.router;

import io.netty.handler.codec.http.HttpMethod;

// http://stackoverflow.com/questions/9655335/method-chaining-how-to-use-getthis-trick-in-case-of-multi-level-inheritance
public abstract class MethodRouter<T, RouteLike extends MethodRouter<T, RouteLike>> extends jauter.Router<HttpMethod, T, RouteLike> {
  @Override protected HttpMethod CONNECT() { return HttpMethod.CONNECT; }
  @Override protected HttpMethod DELETE()  { return HttpMethod.DELETE ; }
  @Override protected HttpMethod GET()     { return HttpMethod.GET    ; }
  @Override protected HttpMethod HEAD()    { return HttpMethod.HEAD   ; }
  @Override protected HttpMethod OPTIONS() { return HttpMethod.OPTIONS; }
  @Override protected HttpMethod PATCH()   { return HttpMethod.PATCH  ; }
  @Override protected HttpMethod POST()    { return HttpMethod.POST   ; }
  @Override protected HttpMethod PUT()     { return HttpMethod.PUT    ; }
  @Override protected HttpMethod TRACE()   { return HttpMethod.TRACE  ; }
}
