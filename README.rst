This tiny Java library is intended for use with Netty 4, to route HTTP requests to
your Netty handlers. It is based on
`Jauter <https://github.com/xitrum-framework/jauter>`_.

Use with Maven
~~~~~~~~~~~~~~

::

  <dependency>
    <groupId>tv.cntt</groupId>
    <artifactId>netty-router</artifactId>
    <version>1.0</version>
  </dependency>

Tip: You should also add `Javassist <http://javassist.org/>`_, it boosts Netty 4+ speed.

::

  <dependency>
    <groupId>org.javassist</groupId>
    <artifactId>javassist</artifactId>
    <version>3.18.2-GA</version>
  </dependency>

Create router
~~~~~~~~~~~~~

::

  import io.netty.handler.codec.http.HttpMethod;
  import io.netty.handler.codec.http.Router;

  Router router = new Router()
    .pattern(HttpMethod.GET,  "/articles",     IndexHandler.class)
    .pattern(HttpMethod.GET,  "/articles/:id", ShowHandler.class)
    .pattern(HttpMethod.POST, "/articles",     CreateHandler.class);

Instead of using handler class, you can use handler instance:

::

  DeleteHandler deleteHandler = new DeleteHandler();
  router.pattern(HttpMethod.DELETE, "/articles/:id", deleteHandler);

Add router to pipeline
~~~~~~~~~~~~~~~~~~~~~~

Add ``router`` to your Netty inbound pipeline, after the HTTP request decoder.
When a path is matched:

* ``router`` will create a new instance of the matched handler class, and add it
  to the pipeline, right after ``router`` itself. If you use handler instance as
  ``deleteHandler`` above, ``router`` doesn't have to create a new instance.
* ``router`` will add path params and query params to the request as headers.
* ``router`` will passes the current HTTP request to your handler.

::

  public class ExampleInitializer extends ChannelInitializer<SocketChannel> {
    private static final router = new Router()
      .pattern(HttpMethod.GET, "/",             new ExampleHandler())
      .pattern(HttpMethod.GET, "/articles/:id", ExampleHandler.class)

    public void initChannel(SocketChannel ch) {
      ChannelPipeline p = ch.pipeline();
      p.addLast(new HttpServerCodec);
      p.addLast(router.name(), router);  // Must use router.name()
    }
  }

Extract params from request
~~~~~~~~~~~~~~~~~~~~~~~~~~~

When the request is routed to your handler by ``router``, you can extract path
params and query params from it:

::

  Map<String, String>       Router.pathParams(req)
  Map<String, List<String>> Router.queryParams(req)

  // Use path params first, then fall back to query params
  String       Router.param(req)
  List<String> Router.params(req)

See `test <https://github.com/xitrum-framework/netty-router/tree/master/src/test/scala/io/netty/handler/codec/http>`_
for example.

Create reverse route
~~~~~~~~~~~~~~~~~~~~

TODO
