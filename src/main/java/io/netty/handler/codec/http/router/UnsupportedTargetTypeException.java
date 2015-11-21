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
public class UnsupportedTargetTypeException extends Exception {

    private final Class expectedClass;
    private final Object actualObject;

    public UnsupportedTargetTypeException(Class expectedClass, Object actualObject, Class scope) {
        super(MessageFormat.format("[{0}]: {2} is not allowed, here is expecting {1}", scope, expectedClass, actualObject.getClass()));
        this.expectedClass = expectedClass;
        this.actualObject = actualObject;
    }

    public Class getExpectedClass() {
        return expectedClass;
    }

    public Object getActualObject() {
        return actualObject;
    }

}
