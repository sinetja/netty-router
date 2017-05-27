Netty-Router is a tiny Java library intended for use with Netty 4.1, to route HTTP
requests to your Netty handlers.

Javadoc:

* `Netty-Router <http://sinetja.github.io/netty-router>`_
* `Netty <http://netty.io/4.1/api/index.html>`_

For usage instructions, see the Javadoc above of class ``Router`` and
`the example <https://github.com/sinetja/netty-router/tree/master/src/test/java/io/netty/example/http/router>`_.

Use with Maven
~~~~~~~~~~~~~~

::

  <dependency>
    <groupId>tv.cntt</groupId>
    <artifactId>netty-router</artifactId>
    <version>2.2.0</version>
  </dependency>

Tip:
To boost Netty speed, you should also add
`Javassist <http://www.csg.ci.i.u-tokyo.ac.jp/~chiba/javassist/>`_

::

  <dependency>
    <groupId>org.javassist</groupId>
    <artifactId>javassist</artifactId>
    <version>3.21.0-GA</version>
  </dependency>
