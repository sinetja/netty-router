/*
 * This file is part of the netty-router package.
 * 
 * (c) Richard Lea <chigix@zoho.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package io.netty.handler.codec.http.router.testutils.builder;

import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.HttpHeaderNames;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract class for building HttpMessage. This class' design is partially
 * based on the builder in grails/gorm-rest-client:
 * https://github.com/grails/gorm-rest-client/blob/ea20081a96c5c7189dfdcf502f466f96dabc66d2/grails-rx-http-client/src/main/groovy/grails/http/client/builder/HttpMessageBuilder.groovy
 *
 * @author Richard Lea <chigix@zoho.com>
 * @param <T>
 * @param <M>
 */
public class HttpMessageBuilder<T extends HttpMessageBuilder, M extends FullHttpMessage> {

    private final Charset charset;

    private final OutputStream output;

    private final Writer writer;

    private final Map<CharSequence, CharSequence> settingHeaders;

    public HttpMessageBuilder(Charset charset) {
        this.charset = charset;
        output = new ByteArrayOutputStream();
        writer = new OutputStreamWriter(output);
        this.settingHeaders = new HashMap<CharSequence, CharSequence>();
    }

    /**
     * Sets the content type for the request.
     *
     * @param contentType
     * @return
     */
    public T contentType(CharSequence contentType) {
        this.settingHeaders.put(HttpHeaderNames.CONTENT_TYPE, contentType);
        return (T) this;
    }

    /**
     * Clone a HttpMessage from this builder upon history of calling building
     * methods as prototype. Defaultly this is not turned on in
     * HttpMessageBuilder. It is needed to instantiate prototype method in
     * builder class when prototype is required.
     *
     * @param factory
     * @return
     */
    public M getResult(HttpMessageFactory<M> factory) {
        M message = factory.create();
        for (Map.Entry<CharSequence, CharSequence> entry : settingHeaders.entrySet()) {
            CharSequence key = entry.getKey();
            CharSequence value = entry.getValue();
            message.headers().add(key, value);
        }
        return message;
    }

    /**
     * Sets a request header.
     *
     * @param name
     * @param value
     * @return
     */
    public T header(CharSequence name, CharSequence value) {
        this.settingHeaders.put(name, value);
        return (T) this;
    }

    public T removeHeader(CharSequence name) {
        this.settingHeaders.remove(name);
        return (T) this;
    }

    /**
     * Adds JSON to the body of the request.
     *
     * @param json
     * @return
     */
    public T json(String json) {
        defaultContentType(ContentTypes.JSON.getValue());
        try {
            this.writer.write(json);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return (T) this;
    }

    /**
     * Sets the body of the request to the XML.
     *
     * @param xml
     * @return
     */
    public T xml(String xml) {
        defaultContentType(ContentTypes.XML.getValue());
        try {
            this.writer.write(xml);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return (T) this;
    }

    /**
     * Sets the body to a given string, mostly be used for customized
     * encoder/decoder.
     *
     * @param text
     * @return
     */
    public T text(String text) {
        defaultContentType(ContentTypes.TXT.getValue());
        try {
            this.writer.write(text);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return (T) this;
    }

    /**
     * Sets the body to a given bytes content. Mostly be used for customized
     * encoder/decoder.
     *
     * @param b
     * @return
     */
    public T bytes(byte[] b) {
        try {
            this.output.write(b);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return (T) this;
    }

    protected void defaultContentType(String contentType) {
        if (!this.settingHeaders.containsKey(HttpHeaderNames.CONTENT_TYPE)) {
            contentType(contentType);
        }
    }

}
