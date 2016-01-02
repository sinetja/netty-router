/*
 * This file is part of the netty-router package.
 * 
 * (c) Richard Lea <chigix@zoho.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package io.netty.handler.codec.http.router;

import io.netty.channel.ChannelHandler;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static io.netty.handler.codec.http.router.Assert.*;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Richard Lea <chigix@zoho.com>
 */
public class MethodlessRouterTest {

    private RoutingPathMatcher routerInstance;

    /**
     * /
     */
    private final UnitCase mockRootPath = new UnitCase("/");
    /**
     * /standard/bankai/path
     */
    private final UnitCase mockStandardBankaiPath = new UnitCase("/standard/bankai/path");
    /**
     * /standard/bankai/path/:*
     */
    private final UnitCase mockOvermatchBankaiPath = new UnitCase("/standard/bankai/path/:*");
    /**
     * /standard/:p1/bankai/path
     */
    private final UnitCase mockOneParamBankaiPath = new UnitCase("/standard/:p1/bankai/path");
    /**
     * /standard/:p1/bankai/:p2/path
     */
    private final UnitCase mockTwoParamBankaiPath = new UnitCase("/standard/:p1/bankai/:p2/path");

    public MethodlessRouterTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        this.routerInstance = new RoutingPathMatcher();
        this.routerInstance.addLast(this.mockStandardBankaiPath.getPatternPath(), this.mockStandardBankaiPath);
        this.routerInstance.addLast(this.mockOvermatchBankaiPath.getPatternPath(), this.mockOvermatchBankaiPath);
        this.routerInstance.addLast(this.mockOneParamBankaiPath.getPatternPath(), this.mockOneParamBankaiPath);
        this.routerInstance.addLast(this.mockTwoParamBankaiPath.getPatternPath(), this.mockTwoParamBankaiPath);
        this.routerInstance.addLast(this.mockRootPath.getPatternPath(), this.mockRootPath);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of addFirst method, of class RoutingPathMatcher.
     */
    @Test
    public void testAddFirst_Pattern() {
        System.out.println("addFirst");
        RoutingConfig pattern = null;
        RoutingPathMatcher instance = new RoutingPathMatcher();
        RoutingPathMatcher expResult = null;
        RoutingPathMatcher result = instance.addFirst(pattern);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addFirst method, of class RoutingPathMatcher.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testAddFirst_String_ChannelHandler() throws Exception {
        System.out.println("addFirst");
        String path = "";
        ChannelHandler target = null;
        RoutingPathMatcher instance = new RoutingPathMatcher();
        RoutingPathMatcher expResult = null;
        RoutingPathMatcher result = instance.addFirst(path, target);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of add method, of class RoutingPathMatcher.
     */
    @Test
    public void testAddLast_Pattern() {
        System.out.println("addLast");
        RoutingConfig pattern = null;
        RoutingPathMatcher instance = new RoutingPathMatcher();
        RoutingPathMatcher expResult = null;
        RoutingPathMatcher result = instance.add(pattern);
        assertEquals(expResult, result);
        Assert.assertException();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of add method, of class RoutingPathMatcher.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testAddLast_String_ChannelHandler() throws Exception {
        System.out.println("addLast");
        String path = "";
        ChannelHandler target = null;
        RoutingPathMatcher instance = new RoutingPathMatcher();
        RoutingPathMatcher expResult = null;
        RoutingPathMatcher result = instance.addLast(path, target);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of remove method, of class RoutingPathMatcher.
     */
    @Test
    public void testRemove_ChannelHandler() {
        System.out.println("remove");
        ChannelHandler target = null;
        RoutingPathMatcher instance = new RoutingPathMatcher();
        RoutingPathMatcher expResult = null;
        RoutingPathMatcher result = instance.remove(target);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of remove method, of class RoutingPathMatcher.
     */
    @Test
    public void testRemove_String() {
        System.out.println("remove");
        String path = "";
        RoutingPathMatcher instance = new RoutingPathMatcher();
        RoutingPathMatcher expResult = null;
        RoutingPathMatcher result = instance.remove(path);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of match method, of class RoutingPathMatcher.
     */
    @Test
    public void testMatch() {
        System.out.println("match");
        // EXPECT TRUE============================
        assertSame(this.mockRootPath, this.routerInstance.match("/").targetPipeline());
        assertSame(this.mockStandardBankaiPath, this.routerInstance.match("/standard/bankai/path").targetPipeline());
        assertSame(this.mockOvermatchBankaiPath, this.routerInstance.match("/standard/bankai/path/over/match").targetPipeline());
        assertSame(this.mockOneParamBankaiPath, this.routerInstance.match("/standard/value1/bankai/path").targetPipeline());
        final Routed result_two_param_bankai_path = this.routerInstance.match("/standard/value1/bankai/value2/path");
        assertSame(this.mockTwoParamBankaiPath, result_two_param_bankai_path.targetPipeline());
        assertEquals("value1", result_two_param_bankai_path.decodedParams().get("p1"));
        assertEquals("value2", result_two_param_bankai_path.decodedParams().get("p2"));
        assertNull(this.routerInstance.match("/standard/value1/bankai/value2/path/notFound"));
    }

    /**
     * Test of generatePath method, of class RoutingPathMatcher.
     */
    @Test
    public void testGeneratePath() {
        System.out.println("generatePath");
        Map<String, String> params = new HashMap<String, String>();
        params.put("p1", "value1");
        params.put("p2", "value2");
        assertEquals("/standard/bankai/path?p2=value2&p1=value1", this.routerInstance.generatePath(this.mockStandardBankaiPath, params));
        assertEquals(null, this.routerInstance.generatePath(this.mockOvermatchBankaiPath, params));
    }

    private class UnitCase {

        private final String patternPath;

        public UnitCase(String patternPath) {
            this.patternPath = patternPath;
        }

        public String getPatternPath() {
            return patternPath;
        }

    }

}
