/*
 * Copyright 2015 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.codec.http.router;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import io.netty.handler.codec.http.HttpMethod;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

public class RoutingTest {
    private Router<String> router;

    @Before
    public void setUp() {
        router = StringRouter.create();
    }

    @Test
    public void testIgnoreSlashesAtBothEnds() {
        assertEquals("index", router.route(GET, "articles").target());
        assertEquals("index", router.route(GET, "/articles").target());
        assertEquals("index", router.route(GET, "//articles").target());
        assertEquals("index", router.route(GET, "articles/").target());
        assertEquals("index", router.route(GET, "articles//").target());
        assertEquals("index", router.route(GET, "/articles/").target());
        assertEquals("index", router.route(GET, "//articles//").target());
    }

    @Test
    public void testHandleEmptyParams() {
        RouteResult<String> routed = router.route(GET, "/articles");
        assertEquals("index", routed.target());
        assertEquals(0,       routed.pathParams().size());
    }

    @Test
    public void testHandleParams() {
        RouteResult<String> routed = router.route(GET, "/articles/123");
        assertEquals("show", routed.target());
        assertEquals(1,      routed.pathParams().size());
        assertEquals("123",  routed.pathParams().get("id"));
    }

    @Test
    public void testEncodedSlash() {
        RouteResult<String> routed = router.route(GET, "/articles/123%2F456");
        assertEquals("show",    routed.target());
        assertEquals(1,         routed.pathParams().size());
        assertEquals("123/456", routed.pathParams().get("id"));

        assertEquals("/articles/123%2F456", routed.uri());
        assertEquals("/articles/123/456",   routed.decodedPath());
    }

    @Test
    public void testHandleNone() {
        RouteResult<String> routed = router.route(GET, "/noexist");
        assertEquals("404", routed.target());
    }

    @Test
    public void testHandleSplatWildcard() {
        RouteResult<String> routed = router.route(GET, "/download/foo/bar.png");
        assertEquals("download",    routed.target());
        assertEquals(1,             routed.pathParams().size());
        assertEquals("foo/bar.png", routed.pathParams().get("*"));
    }

    @Test
    public void testHandleOrder() {
        RouteResult<String> routed1 = router.route(GET, "/articles/new");
        assertEquals("new", routed1.target());
        assertEquals(0,     routed1.pathParams().size());

        RouteResult<String> routed2 = router.route(GET, "/articles/123");
        assertEquals("show", routed2.target());
        assertEquals(1,      routed2.pathParams().size());
        assertEquals("123",  routed2.pathParams().get("id"));

        RouteResult<String> routed3 = router.route(GET, "/notfound");
        assertEquals("404", routed3.target());
        assertEquals(0,     routed3.pathParams().size());
    }

    @Test
    public void testHandleAnyMethod() {
        RouteResult<String> routed1 = router.route(GET, "/anyMethod");
        assertEquals("anyMethod", routed1.target());
        assertEquals(0,           routed1.pathParams().size());

        RouteResult<String> routed2 = router.route(POST, "/anyMethod");
        assertEquals("anyMethod", routed2.target());
        assertEquals(0,           routed2.pathParams().size());
    }

    @Test
    public void testHandleRemoveByTarget() {
        router.removeTarget("index");
        RouteResult<String> routed = router.route(GET, "/articles");
        assertEquals("404", routed.target());
    }

    @Test
    public void testHandleRemoveByPathPattern() {
        router.removePathPattern("/articles");
        RouteResult<String> routed = router.route(GET, "/articles");
        assertEquals("404", routed.target());
    }

    @Test
    public void testAllowedMethods() {
        assertEquals(9, router.allAllowedMethods().size());

        Set<HttpMethod> methods = router.allowedMethods("/articles");
        assertEquals(2, methods.size());
        assertTrue(methods.contains(GET));
        assertTrue(methods.contains(POST));
    }

    @Test
    public void testHandleSubclasses() {
        Router<Class<? extends Action>> router = new Router<Class<? extends Action>>()
                .addRoute(GET, "/articles",     Index.class)
                .addRoute(GET, "/articles/:id", Show.class);

        RouteResult<Class<? extends Action>> routed1 = router.route(GET, "/articles");
        RouteResult<Class<? extends Action>> routed2 = router.route(GET, "/articles/123");
        assertNotNull(routed1);
        assertNotNull(routed2);
        assertEquals(Index.class, routed1.target());
        assertEquals(Show.class,  routed2.target());
    }
}
