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

import java.util.HashMap;
import java.util.Map;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpMethod.PUT;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ReverseRoutingTest {
    @Test
    public void testHandleMethod() {
        assertEquals("/articles", StringRouter.router.path(GET, "index"));

        assertEquals("/articles/123", StringRouter.router.path(GET, "show", "id", "123"));

        assertEquals("/anyMethod", StringRouter.router.path(GET, "anyMethod"));
        assertEquals("/anyMethod", StringRouter.router.path(POST, "anyMethod"));
        assertEquals("/anyMethod", StringRouter.router.path(PUT, "anyMethod"));
    }

    @Test
    public void testHandleEmptyParams() {
        assertEquals("/articles", StringRouter.router.path("index"));
    }

    @Test
    public void testHandleMapParams() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", 123);
        assertEquals("/articles/123", StringRouter.router.path("show", map));
    }

    @Test
    public void testHandleVarargs() {
        assertEquals("/download/foo/bar.png", StringRouter.router.path("download", "*", "foo/bar.png"));
    }

    @Test
    public void testReturnPathWithMinimumNumberOfParams() {
        Map<String, Object> map1 = new HashMap<String, Object>();
        map1.put("id",     123);
        map1.put("format", "json");
        assertEquals("/articles/123/json", StringRouter.router.path("show", map1));

        Map<String, Object> map2 = new HashMap<String, Object>();
        map2.put("id",     123);
        map2.put("format", "json");
        map2.put("x",      1);
        map2.put("y",      2);
        String path = StringRouter.router.path("show", map2);
        boolean matched1 = path.equals("/articles/123/json?x=1&y=2");
        boolean matched2 = path.equals("/articles/123/json?y=2&x=1");
        assertEquals(true, matched1 || matched2);
    }
}
