/*
 * This file is part of the netty-router package.
 * 
 * (c) Richard Lea <chigix@zoho.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package io.netty.handler.codec.http.router.testutils.builder;

/**
 *
 * @author Richard Lea <chigix@zoho.com>
 */
public enum ContentTypes {
    XML() {
        @Override
        public String getValue() {
            return "'application/xml";
        }
    },
    JSON() {
        @Override
        public String getValue() {
            return "application/json";
        }
    },
    HAL() {
        @Override
        public String getValue() {
            return "application/hal+json";
        }
    },
    RSS() {
        @Override
        public String getValue() {
            return "application/rss+xml";
        }
    },
    YML() {
        @Override
        public String getValue() {
            return "application/yml";
        }
    },
    CSV() {
        @Override
        public String getValue() {
            return "text/csv";
        }
    },
    CSS() {
        @Override
        public String getValue() {
            return "text/css";
        }
    },
    HTML() {
        @Override
        public String getValue() {
            return "text/html";
        }
    },
    TXT() {
        @Override
        public String getValue() {
            return "text/plain";
        }
    },
    BIN() {
        @Override
        public String getValue() {
            return "application/octet-stream";
        }
    },
    JPG() {
        @Override
        public String getValue() {
            return "image/jpeg";
        }
    },
    GIF() {
        @Override
        public String getValue() {
            return "image/gif";
        }
    },
    PNG() {
        @Override
        public String getValue() {
            return "image/png";
        }
    },
    SVG() {
        @Override
        public String getValue() {
            return "image/svg+xml";
        }
    };

    public abstract String getValue();
}
