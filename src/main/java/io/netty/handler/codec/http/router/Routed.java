package io.netty.handler.codec.http.router;

import io.netty.handler.codec.http.HttpRequest;

import java.util.List;
import java.util.Map;

public class Routed extends MethodRouted<Object> {
  /**
   * When target is a class, this method calls "newInstance" on the class.
   * Otherwise it returns the target as is.
   */
  public static Object instanceFromTarget(Object target) throws InstantiationException, IllegalAccessException {
    if (target == null) return null;

    if (target instanceof Class) {
      // Create handler from class
      Class<?> klass = (Class<?>) target;
      return klass.newInstance();
    } else {
      return target;
    }
  }

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
   */
  public Object instanceFromTarget() throws InstantiationException, IllegalAccessException {
    return Routed.instanceFromTarget(target());
  }
}
