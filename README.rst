This tiny Java library is intended for use with Netty, to route HTTP requests to
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

  import io.netty.handler.codec.http.Router;
  Router router = new Router();

Add rules
~~~~~~~~~

::

  import io.netty.handler.codec.http.HttpMethod;

  router.pattern(HttpMethod.GET,  "/articles",     IndexHandler.class);
  router.pattern(HttpMethod.GET,  "/articles/:id", ShowHandler.class);
  router.pattern(HttpMethod.POST, "/articles",     CreateHandler.class);

Instead of using handler class, you can use handler instance:

::

  DeleteHandler deleteHandler = new DeleteHandler();
  router.pattern(HttpMethod.DELETE, "/articles/:id", deleteHandler);

Processing logic
~~~~~~~~~~~~~~~~

Add ``router`` to your Netty inbound pipeline, after the HTTP request decoder.
When a path is matched:

* ``router`` will create a new instance of the matched handler class, and add it
  to the pipeline, right after ``router`` itself.
* If you use handler instance as ``deleteHandler`` above, ``router`` doesn't have
  to create a new instance.
* ``router`` then passes the current HTTP request to your handler.

Netty pipeline example
~~~~~~~~~~~~~~~~~~~~~~

::

  TODO

Create reverse route
~~~~~~~~~~~~~~~~~~~~

TODO
