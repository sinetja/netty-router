/*
 * This file is part of the netty-router package.
 * 
 * (c) Richard Lea <chigix@zoho.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package io.netty.handler.codec.http.router.testutils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestEncoder;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;

/**
 *
 * @author Richard Lea <chigix@zoho.com>
 */
public class CodecUtil {

    /**
     * Convert a HttpRequest Object in Netty Codec to Netty standard ByteBuf for
     * unit testing.
     *
     * @param request
     * @return
     */
    public static final Object[] encodeHttpRequest(HttpRequest request) {
        EmbeddedChannel channel = new EmbeddedChannel(new HttpRequestEncoder());
        Assert.assertTrue(channel.writeOutbound(request));
        Assert.assertTrue(channel.finish());
        List<ByteBuf> bytebuffers = new ArrayList<ByteBuf>();
        ByteBuf outbound;
        while ((outbound = channel.readOutbound()) != null) {
            bytebuffers.add(outbound);
        }
        ByteBuf[] result = new ByteBuf[bytebuffers.size()];
        return bytebuffers.toArray(result);
    }
}
