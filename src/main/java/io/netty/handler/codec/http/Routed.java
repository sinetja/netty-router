package io.netty.handler.codec.http;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Routed {
  protected final HttpRequest               request;
  protected final String                    path;
  protected final Map<String, String>       pathParams;
  protected final Map<String, List<String>> queryParams;

  //----------------------------------------------------------------------------

  public Routed(HttpRequest request, String path, Map<String, String> pathParams, Map<String, List<String>> queryParams) {
    this.request     = request;
    this.path        = path;
    this.pathParams  = pathParams;
    this.queryParams = queryParams;
  }

  public HttpRequest request() {
    return request;
  }

  public String path() {
    return path;
  }

  public Map<String, String> pathParams() {
    return pathParams;
  }

  public Map<String, List<String>> queryParams() {
    return queryParams;
  }

  //----------------------------------------------------------------------------
  // Utilities to get params.

  /**
   * @return The first query param, or null
   */
  public String queryParam(String name) {
    List<String> values = queryParams.get(name);
    return (values == null)? null : values.get(0);
  }

  /** @return Uses path param first, then falls back to the first query param, or null */
  public String param(String name) {
    String pathValue = pathParams.get(name);
    return (pathValue == null)? queryParam(name) : pathValue;
  }

  /**
   * Both path param and query params are returned.
   * Empty list is returned if there are no such params.
   */
  public List<String> params(String name) {
    List<String> values = queryParams.get(name);
    if (values == null) values = new ArrayList<String>();

    String value = pathParams.get(name);
    if (value != null) values.add(value);

    return values;
  }
}
