package io.netty.handler.codec.http;

public class MethodRouter<T> extends jauter.Router<HttpMethod, T> {
  protected HttpMethod CONNECT() { return HttpMethod.CONNECT; }
  protected HttpMethod DELETE()  { return HttpMethod.DELETE ; }
  protected HttpMethod GET()     { return HttpMethod.GET    ; }
  protected HttpMethod HEAD()    { return HttpMethod.HEAD   ; }
  protected HttpMethod OPTIONS() { return HttpMethod.OPTIONS; }
  protected HttpMethod PATCH()   { return HttpMethod.PATCH  ; }
  protected HttpMethod POST()    { return HttpMethod.POST   ; }
  protected HttpMethod PUT()     { return HttpMethod.PUT    ; }
  protected HttpMethod TRACE()   { return HttpMethod.TRACE  ; }
}
