/*
 * This file is part of the netty-router package.
 * 
 * (c) Richard Lea <chigix@zoho.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package io.netty.handler.codec.http.router.testutil;

import java.io.OutputStream;
import java.io.PrintWriter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.WriterAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

/**
 *
 * @author Richard Lea <chigix@zoho.com>
 */
public class Log4jUtil {

    /**
     * Try to get all messages through Log4j via adding a one-time used
     * appender.
     * http://logging.apache.org/log4j/2.x/manual/customconfig.html#AppendingToWritersAndOutputStreams
     *
     * @param out
     * @param level
     */
    public static void catchLogMessages(OutputStream out, Level level) {
        LoggerContext ctx = LoggerContext.getContext(false);
        Configuration conf = ctx.getConfiguration();
        PatternLayout layout = PatternLayout.createDefaultLayout();
        Appender appender = WriterAppender.createAppender(layout, null, new PrintWriter(out), "LogCatcher", false, true);
        appender.start();
        conf.addAppender(appender);
        for (LoggerConfig loggerConf : conf.getLoggers().values()) {
            loggerConf.addAppender(appender, level, null);
        }
        conf.getRootLogger().addAppender(appender, level, null);
    }

}
