package io.netty.handler.codec.http.router;

import io.netty.handler.codec.http.HttpRequest;

import java.util.List;
import java.util.Map;

public class Routed extends MethodRouted<Object> {
  public Routed(
      Object                    target,
      boolean                   notFound,
      HttpRequest               request,
      String                    path,
      Map<String, String>       pathParams,
      Map<String, List<String>> queryParams
  ) {
    super(target, notFound, request, path, pathParams, queryParams);
  }

  /**
   * When target is a class, this method calls "newInstance" on the class.
   * Otherwise it returns the target as is.
   *
   * @return null if target is null
   */
  public static Object instanceFromTarget(Object target) throws InstantiationException, IllegalAccessException {
    return jauter.Routed.instanceFromTarget(target);
  }

  /**
   * When target is a class, this method calls "newInstance" on the class.
   * Otherwise it returns the target as is.
   *
   * @return null if target is null
   */
  public Object instanceFromTarget() throws InstantiationException, IllegalAccessException {
    return jauter.Routed.instanceFromTarget(target());
  }
}
