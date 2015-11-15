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

/**
 *
 * @author Richard Lea <chigix@zoho.com>
 */
public class NotFoundException extends Exception {

    private final String errorPath;

    public NotFoundException(String path) {
        super(MessageFormat.format("Unknown path: {0}", path));
        this.errorPath = path;
    }

    public String getErrorPath() {
        return errorPath;
    }

}
