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

/**
 *
 * @author Richard Lea <chigix@zoho.com>
 */
public class Logging {

    private final Logger LOG;

    public Logging(Object source) {
        this.LOG = Logger.getLogger(source.getClass().toString());
    }

    public void info(String msg) {
        LOG.info(msg);
    }

    public void error(Throwable cause) {
        LOG.log(Level.SEVERE, null, cause);
    }

    public void error(String msg, Throwable cause) {
        LOG.log(Level.SEVERE, msg, cause);
    }

    public void warn(String msg) {
        LOG.warning(msg);
    }

}
