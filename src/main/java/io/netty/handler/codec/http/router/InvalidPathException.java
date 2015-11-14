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
public class InvalidPathException extends Exception {

    private final String path;

    private final InvalidReason reason;

    public InvalidPathException(String path, InvalidReason reason) {
        super(MessageFormat.format("Invalid Path: [{0}], {1}", path, reason.getReasonString()));
        this.path = path;
        this.reason = reason;
    }

    public InvalidReason getReason() {
        return reason;
    }

    public enum InvalidReason {

        NOT_BEGIN_WITH_SLASH("The Path must be beginning with a slash.");

        private final String message;

        private InvalidReason(String reason) {
            this.message = reason;
        }

        public String getReasonString() {
            return message;
        }

    }

}
