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
public class HttpRouterTest {
    
    public HttpRouterTest() {
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
     * Test of CONNECT method, of class HttpRouter.
     */
    @Test
    public void testCONNECT() {
        System.out.println("CONNECT");
        String path = "";
        Object target = null;
        HttpRouter instance = new HttpRouter();
        HttpRouter expResult = null;
        HttpRouter result = instance.CONNECT(path, target);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of DELETE method, of class HttpRouter.
     */
    @Test
    public void testDELETE() {
        System.out.println("DELETE");
        String path = "";
        Object target = null;
        HttpRouter instance = new HttpRouter();
        HttpRouter expResult = null;
        HttpRouter result = instance.DELETE(path, target);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of GET method, of class HttpRouter.
     */
    @Test
    public void testGET() {
        System.out.println("GET");
        String path = "";
        Object target = null;
        HttpRouter instance = new HttpRouter();
        HttpRouter expResult = null;
        HttpRouter result = instance.GET(path, target);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of HEAD method, of class HttpRouter.
     */
    @Test
    public void testHEAD() {
        System.out.println("HEAD");
        String path = "";
        Object target = null;
        HttpRouter instance = new HttpRouter();
        HttpRouter expResult = null;
        HttpRouter result = instance.HEAD(path, target);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of OPTIONS method, of class HttpRouter.
     */
    @Test
    public void testOPTIONS() {
        System.out.println("OPTIONS");
        String path = "";
        Object target = null;
        HttpRouter instance = new HttpRouter();
        HttpRouter expResult = null;
        HttpRouter result = instance.OPTIONS(path, target);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of PATCH method, of class HttpRouter.
     */
    @Test
    public void testPATCH() {
        System.out.println("PATCH");
        String path = "";
        Object target = null;
        HttpRouter instance = new HttpRouter();
        HttpRouter expResult = null;
        HttpRouter result = instance.PATCH(path, target);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of POST method, of class HttpRouter.
     */
    @Test
    public void testPOST() {
        System.out.println("POST");
        String path = "";
        Object target = null;
        HttpRouter instance = new HttpRouter();
        HttpRouter expResult = null;
        HttpRouter result = instance.POST(path, target);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of PUT method, of class HttpRouter.
     */
    @Test
    public void testPUT() {
        System.out.println("PUT");
        String path = "";
        Object target = null;
        HttpRouter instance = new HttpRouter();
        HttpRouter expResult = null;
        HttpRouter result = instance.PUT(path, target);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of TRACE method, of class HttpRouter.
     */
    @Test
    public void testTRACE() {
        System.out.println("TRACE");
        String path = "";
        Object target = null;
        HttpRouter instance = new HttpRouter();
        HttpRouter expResult = null;
        HttpRouter result = instance.TRACE(path, target);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of route method, of class HttpRouter.
     */
    @Test
    public void testRoute() {
        System.out.println("route");
        HttpMethod method = null;
        String path = "";
        HttpRouter instance = new HttpRouter();
        Routed expResult = null;
        Routed result = instance.route(method, path);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
