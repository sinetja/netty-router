Netty-Router 3

# Overview

Netty-Router 3 is a tiny Java Library intended for using in Netty 5.0 focusing on the practices of Common Routing upon Http request message, error exception forward and internal ChannelHandler forward.

For detailed usage of each class provided, see the test cases in this repository.

# Getting Started

The main classes provided to be used are `RouterHandler` and `HttpRouter`.

Although there is no special restrictions on the usage of `HttpRouter` and route targets can be any type, it is greatly suggested to input `ChannelHandler` object into routing, because of common development upon Netty.

When designing these classes, of course, the plan on these central routing classes is could be used individually in any JAVA project without dependency on Netty, however it is just in the plan and current roadmap of this version is focusing on the developer in NETTY.

This router library is as a netty handler, so it could be very sensible for netty users to put this router into practice via creating a new instance of `RouterHandler`, and then insert it into the pipeline. That's all direct instrction of `RouterHandler`.

What's more and more important, the `RouterHandler` needs users to input a list of mapping with the detailed informations of path and target handler, which is called the routing table. And a new instantiation of  `HttpRouter` is for this task:

```java
HttpRouter<ChannelHandler> router = new HttpRouter<ChannelHandler>()
    .GET    ("/bankai", new BankaiHandler())
    .POST   ("/article", new ArticleHandler())
    .PUT    ("/user/*", new UserHandler())
    .DELETE ("/user/:username", new UserHandler());

RouterHandler routerHandler = new RouterHandler(router);
routerHandler.takePipeline(channel.pipeline());
```

# Pattern definition

In netty-router, A PATTERN is descibing both of path information and target ChannelHandler to execute.

The path should begin with a slash as a form of absolute path. If there was any invalid check encountered, `InvalidPathException` would be raised immediately.

# Usage of HttpRouter

Basically, the routing info--regarding the pattern of HttpMethod, path definition and target ChannelHandler to run--is provided from this class.

In addition, you can also remove routes by target or by path:

```java
router.removeTarget(articleHandler);
router.removeTarget("/articles");
```

Defaultly, some sever log information would appear, which is upon the default Logging offered by JDK. If you are using SLF4j(Recommened) or Log4j, and so on, you could also be very easy to override the default logging program:

```java
routerHandler.setLogger(new Logging(){
    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(RouterHandler.class);
    @override
    public void error(Throwable cause){
        LOG.error(null, cause);
    }
});
```

# Common Error Processing

In netty-router 3, the `exceptionHandler` is provided for process every error occured during the Netty Server working, commonly the exceptions thrown after routed.

In this library, a preset `DefaultHttpExceptionHandler` could also be accessed and override directly for customizing user exception processing codes.

# TODO

* Router list Print support.
* Router Switcher of Pipeline support for multiple handler chaining routed.