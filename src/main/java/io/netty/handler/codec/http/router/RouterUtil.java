/*
 * This file is part of the netty-router package.
 * 
 * (c) Richard Lea <chigix@zoho.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package io.netty.handler.codec.http.router;

/**
 *
 * @author Richard Lea <chigix@zoho.com>
 */
class RouterUtil {

    public static String normalizePath(String path) throws InvalidPathException {
        if (path.isEmpty()) {
            throw new NullPointerException();
        }
        int beginIndex = 0;
        while (beginIndex < path.length() && path.charAt(beginIndex) == '/') {
            beginIndex++;
        }
        if (beginIndex == path.length()) {
            // This case is like "//////////////////"
            return "/";
        }
        if (beginIndex > 0) {
            beginIndex--;
        } else {
            throw new InvalidPathException(path, InvalidPathException.InvalidReason.NOT_BEGIN_WITH_SLASH);
        }
        // Url like `/.+` would be processed by the following.
        int endIndex = path.length() - 1;
        while (endIndex > beginIndex && path.charAt(endIndex) == '/') {
            endIndex--;
        }
        if (endIndex < path.length() - 1) {
            endIndex += 2;
        } else {
            endIndex++;
        }
        return path.substring(beginIndex, endIndex);
    }
}
