/*
 * This file is part of the netty-router package.
 * 
 * (c) Richard Lea <chigix@zoho.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package io.netty.handler.codec.http.router;

import java.util.logging.Level;
import java.util.logging.Logger;
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
public class PatternTest {

    private Pattern instance;

    private String path = "/bankai";

    private Object target = new Object();

    public PatternTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        try {
            instance = new Pattern(this.path, this.target);
        } catch (InvalidPathException ex) {
            Logger.getLogger(PatternTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getPath method, of class Pattern.
     */
    @Test
    public void testGetPath() {
        System.out.println("getPath");
        assertEquals(this.path, instance.getPath());
    }

    /**
     * Test of getTokens method, of class Pattern.
     */
    @Test
    public void testGetTokens() {
        System.out.println("getTokens");
        assertArrayEquals(new String[]{"", "bankai"}, this.instance.getTokens());
    }

    /**
     * Test of getTarget method, of class Pattern.
     */
    @Test
    public void testGetTarget() {
        System.out.println("getTarget");
        assertSame(this.target, this.instance.getTarget());
    }

}
