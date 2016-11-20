/*
 * This file is part of the netty-router package.
 * 
 * (c) Richard Lea <chigix@zoho.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package io.netty.handler.codec.http.router;

import java.text.MessageFormat;
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
public class RouterUtilTest {

    public RouterUtilTest() {
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
     * Test of normalizePath method, of class RouterUtil.
     */
    @Test
    public void testNormalizePath() {
        System.out.println("normalizePath");
        assertEquals("/bankai/", RouterUtil.normalizePath("/bankai/"));
        assertEquals("/bankai/", RouterUtil.normalizePath("//bankai/"));
        assertEquals("/bankai/", RouterUtil.normalizePath("/bankai/////"));
        try {
            assertNotSame("bankai", RouterUtil.normalizePath("bankai"));
            fail(MessageFormat.format("{0} is not thrown", InvalidPathException.class));
        } catch (InvalidPathException ex) {
            assertSame(InvalidPathException.InvalidReason.NOT_BEGIN_WITH_SLASH, ex.getReason());
        }
    }

}
