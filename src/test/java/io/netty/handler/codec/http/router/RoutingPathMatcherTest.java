/*
 * This file is part of the netty-router package.
 * 
 * (c) Richard Lea <chigix@zoho.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package io.netty.handler.codec.http.router;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.router.testutil.Log4jUtil;
import io.netty.util.CharsetUtil;
import java.io.ByteArrayOutputStream;
import junit.framework.Assert;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Richard Lea <chigix@zoho.com>
 */
public class RoutingPathMatcherTest {

    public RoutingPathMatcherTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of add method, of class RoutingPathMatcher.
     */
    @Test
    public void testAdd() {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        Log4jUtil.catchLogMessages(result, Level.ALL);
        System.out.println("add");
        RoutingPathMatcher matcher = new RoutingPathMatcher();
        Routing routing_before_delete = new Routing(new RoutingConfig.SimplePathGet("BEFORE_DELETE", "/before/delete"), HttpMethod.GET);
        Routing routing_tobe_delete = new Routing(new RoutingConfig.SimplePathGet("BEFORE_DELETE", "/tobe/delete"), HttpMethod.GET);
        Routing routing_after_delete = new Routing(new RoutingConfig.SimplePathGet("AFTER_DELETE", "/after/delete"), HttpMethod.GET);
        matcher.add(routing_before_delete).add(routing_tobe_delete).add(routing_after_delete);
        Assert.assertEquals("There is Routing Override occured in same name: BEFORE_DELETE", new String(result.toByteArray(), CharsetUtil.UTF_8).trim());
    }

    /**
     * Test of remove method, of class RoutingPathMatcher.
     */
    @Test
    public void testRemove() {
        System.out.println("remove");
        RoutingPathMatcher matcher = new RoutingPathMatcher();
        Routing routing_before_delete = new Routing(new RoutingConfig.SimplePathGet("BEFORE_DELETE", "/before/delete"), HttpMethod.GET);
        Routing routing_tobe_delete = new Routing(new RoutingConfig.SimplePathGet("TOBE_DELETE", "/tobe/delete"), HttpMethod.GET);
        Routing routing_after_delete = new Routing(new RoutingConfig.SimplePathGet("AFTER_DELETE", "/after/delete"), HttpMethod.GET);
        matcher.add(routing_before_delete).add(routing_tobe_delete).add(routing_after_delete);
        assertSame(routing_tobe_delete, matcher.match("/tobe/delete").getRouting());
        matcher.remove("/tobe/delete");
        assertNull(matcher.match("/tobe/delete"));
    }

    /**
     * Test of match method, of class RoutingPathMatcher.
     */
    @Test
    public void testMatch() {
        System.out.println("match");
        RoutingPathMatcher matcher = new RoutingPathMatcher();
        Routing plain_path_routing_1 = new Routing(new RoutingConfig.SimplePathGet("plain_path_routing_1", "/tester/plain/get"), HttpMethod.GET);
        Routing single_var_routing_1 = new Routing(new RoutingConfig.SimplePathGet("single_var_routing_1", "/tester/var/:var1"), HttpMethod.GET);
        Routing dual_var_routing_1 = new Routing(new RoutingConfig.SimplePathGet("dual_var_routing_1", "/tester/var/:var1/var/:var2"), HttpMethod.GET);
        matcher.add(plain_path_routing_1).add(single_var_routing_1).add(dual_var_routing_1);
        assertEquals(plain_path_routing_1.getIdentity(), matcher.match("/tester/plain/get").getRouting().getIdentity());
        assertEquals(single_var_routing_1.getIdentity(), matcher.match("/tester/var/bankai").getRouting().getIdentity());
        assertEquals(single_var_routing_1.getIdentity(), matcher.match("/tester/var/var").getRouting().getIdentity());
        assertEquals(dual_var_routing_1.getIdentity(), matcher.match("/tester/var/bankai/var/jikai").getRouting().getIdentity());
    }

    /**
     * Test of generatePath method, of class RoutingPathMatcher.
     */
    @Test
    public void testGeneratePath() {
        System.out.println("generatePath");
        String name = "";
        Object[] params = null;
        RoutingPathMatcher instance = new RoutingPathMatcher();
        String expResult = "";
        String result = instance.generatePath(name, params);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
