Netty-Router 3

# Overview

Netty-Router 3 is a tiny Java Library intended for using in Netty 5.0 focusing on the practices of Common Routing upon Http request message, error exception forward and internal ChannelHandler forward.

For detailed usage of each class provided, see the test cases in this repository.

# Getting Started

The main class provided in this repository to quickly begin a HTTP router is *io.netty.handler.codec.http.router.HttpRouter*.

Just as the developing in Netty writing some classes extending `ChannelHandlerAdapter`, these classes could directly be reused with `HttpRouter`. Originally, `HttpRouter` is designed upon a *Pipeline Router*, the core of the routing implementation provided in this repository. 

`RoutingConfig` is also provided to help users to simply create routing rules in JAVA classes directly, without any other framework or library support. `RoutingConfig` could also be extended to include some customized routing rules.

A simple map of router config could be written like something bellow:

```java
new HttpRouter() {
    @Override
    protected void initRoutings(ChannelHandlerContext ctx, HttpRouter router) {
        for (RoutingConfig routing : routings) {
            this.newGET(ctx, new HomePage());
            this.newPOST(ctx, new LoginRouting());
            this.newRouting(ctx, new UserRest());
        }
    }

    @Override
    protected void initExceptionRouting(ChannelPipeline pipeline) {
        pipeline.addLast(generateExceptionChecker(except, previouslyClosed));
    }

});
```

# Routing Configuration

A route is a map from a URL to a Pipeline with handlers. Hense, this configuration is to define a pipeline attached with a rule for routing system, upon HTTP protocol.

```java
new RoutingConfig() {
    @Override
    public String configureRoutingName() {
        return "TARGET_ROUTING";
    }

    @Override
    public String configurePath() {
        return "/name/:name/testing_api";
    }

    @Override
    public HttpMethod[] configureMethods() {
        return new HttpMethod[]{HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE};
    }

    @Override
    public void configurePipeline(ChannelPipeline pipeline) {
        pipeline.addLast(new ChannelHandlerAdapter()).addLast(new ChannelHandlerAdapter());
    }
}
```

# Common Error Processing

In a `Router`, there is another special pipeline for handling exception, allowing users to add exception handlers, still the Netty ChannelHandler. So any catched exception could directly throw again and it would be forwarded into this special pipeline, which could make users develop main logical handlers without stress of exception in code.

# TODO

* API of Getting matched Routing is unreasonable in HTTPException.
* Shareable Router Handler.
* Router list Print support.