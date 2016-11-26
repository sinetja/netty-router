/*
 * This file is part of the netty-router package.
 * 
 * (c) Richard Lea <chigix@zoho.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package io.netty.handler.codec.http.router.testutils.builder;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;
import io.netty.handler.codec.http.multipart.MemoryFileUpload;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Richard Lea <chigix@zoho.com>
 */
public class HttpRequestBuilder extends HttpMessageBuilder<HttpRequestBuilder, FullHttpRequest> {

    private final Charset charset;

    private static final ContentTypes[] TEXT_TYPES = new ContentTypes[]{
        ContentTypes.XML,
        ContentTypes.JSON,
        ContentTypes.TXT,
        ContentTypes.YML,
        ContentTypes.CSV,
        ContentTypes.HTML,
        ContentTypes.RSS,
        ContentTypes.HAL,
        ContentTypes.SVG};

    private String uri = null;

    private HttpMethod method = null;

    private CharSequence[] accept = null;

    private Map<String, String> form = null;

    private Map<String, Object> multipartFile = null;

    private Map<String, Object> multipartBytes = null;

    public HttpRequestBuilder(Charset charset) {
        super(charset);
        this.charset = charset;
    }

    public HttpRequestBuilder uri(String uri) {
        this.uri = uri;
        return this;
    }

    /**
     * Sets the method of the request.
     *
     * @param method The HTTP method.
     * @return
     */
    public HttpRequestBuilder method(CharSequence method) {
        return this.method(HttpMethod.valueOf(method.toString()));
    }

    /**
     * Sets the method of the request.
     *
     * @param method The HTTP method.
     * @return
     */
    public HttpRequestBuilder method(HttpMethod method) {
        this.method = method;
        return this;
    }

    /**
     * Used to configure BASIC authentication.
     *
     * @param user
     * @param password
     * @return
     */
    public HttpRequestBuilder auth(String user, String password) {
        String authString = user + ":" + password;
        header(HttpHeaderNames.AUTHORIZATION, Base64.encode(Unpooled.wrappedBuffer(authString.getBytes(charset))).toString());
        return this;
    }

    /**
     * Sets the Authorization HTTP header to the given value. Used typically to
     * pass OAuth access token.
     *
     * @param accessToken
     * @return
     */
    public HttpRequestBuilder auth(String accessToken) {
        header(HttpHeaderNames.AUTHORIZATION, accessToken);
        return this;
    }

    /**
     * Sets the accept HTTP header to the given value.
     *
     * @param contentTypes
     * @return
     */
    public HttpRequestBuilder accept(CharSequence... contentTypes) {
        this.accept = contentTypes;
        return this;
    }

    /**
     * Builds a form
     *
     * @param form
     * @return
     * @throws
     * io.netty.handler.codec.http.multipart.HttpPostRequestEncoder.ErrorDataEncoderException
     * if the request is not a POST.
     */
    public HttpRequestBuilder form(Map<String, String> form) throws HttpPostRequestEncoder.ErrorDataEncoderException {
        this.form = form;
        return this;
    }

    public HttpRequestBuilder multipart(String name, File file) {
        this.multipartFile = new HashMap<String, Object>();
        multipartFile.put("name", name);
        multipartFile.put("file", file);
        return this;
    }

    public HttpRequestBuilder multipart(String name, String filename, InputStream content, int length) {
        return this.multipart(name, filename, content, length, guessMultipartContentType(filename, true));
    }

    public HttpRequestBuilder multipart(String name, String filename, InputStream content, int length, ContentTypes contentType) {
        this.multipartBytes = new HashMap<String, Object>();
        this.multipartBytes.put("name", name);
        this.multipartBytes.put("filename", filename);
        this.multipartBytes.put("content", content);
        this.multipartBytes.put("length", length);
        this.multipartBytes.put("contentType", contentType);
        return this;
    }

    private ContentTypes guessMultipartContentType(String filename) {
        return this.guessMultipartContentType(filename, true);
    }

    private ContentTypes guessMultipartContentType(String filename, boolean binary) {
        String guess = filename.substring(filename.lastIndexOf("."));
        try {
            return ContentTypes.valueOf(guess.toUpperCase());
        } catch (IllegalArgumentException e) {
            return binary ? ContentTypes.BIN : ContentTypes.TXT;
        }
    }

    @Override
    public FullHttpRequest getResult(HttpMessageFactory<FullHttpRequest> factory) {
        FullHttpRequest httpRequest = super.getResult(factory);
        if (uri != null) {
            httpRequest.setUri(uri);
        }
        if (method != null) {
            httpRequest.setMethod(method);
        }
        if (accept != null) {
            StringBuilder joined = new StringBuilder();
            String[] previousAdded = new String[]{};
            if (httpRequest.headers().contains(HttpHeaderNames.ACCEPT)) {
                previousAdded = httpRequest.headers().get(HttpHeaderNames.ACCEPT).toString().split(",");
            }
            if (previousAdded.length > 0) {
                for (int i = 0; i < previousAdded.length; i++) {
                    if (i > 0) {
                        joined.append(",");
                    }
                    joined.append(previousAdded[i]);
                }
            }
            if (previousAdded.length > 0 && accept.length > 0) {
                joined.append(",");
            }
            if (accept.length > 0) {
                for (int i = 0; i < accept.length; i++) {
                    if (i > 0) {
                        joined.append(",");
                    }
                    joined.append(accept[i]);
                }
            }
            header(HttpHeaderNames.ACCEPT, joined);
        }
        if (form != null) {
            try {
                HttpPostRequestEncoder encoder = new HttpPostRequestEncoder(httpRequest, false);
                for (Map.Entry<String, String> entry : form.entrySet()) {
                    encoder.addBodyAttribute(entry.getKey(), entry.getValue());
                }
            } catch (HttpPostRequestEncoder.ErrorDataEncoderException ex) {
                throw new RuntimeException(ex);
            }
        }
        if (multipartFile != null) {
            ContentTypes guessType = guessMultipartContentType(((File) multipartFile.get("file")).getName());
            HttpPostRequestEncoder encoder;
            try {
                encoder = new HttpPostRequestEncoder(httpRequest, false);
                encoder.addBodyFileUpload((String) multipartFile.get("name"), (File) multipartFile.get("file"), guessType.getValue(), Arrays.binarySearch(TEXT_TYPES, guessType) > -1);
            } catch (HttpPostRequestEncoder.ErrorDataEncoderException ex) {
                throw new RuntimeException(ex);
            }
        }
        if (multipartBytes != null) {
            String name = (String) this.multipartBytes.get("name");
            String filename = (String) this.multipartBytes.get("filename");
            InputStream content = (InputStream) this.multipartBytes.get("content");
            Integer length = (Integer) this.multipartBytes.get("length");
            ContentTypes contentType = (ContentTypes) this.multipartBytes.get("contentType");
            MemoryFileUpload upload;
            if (Arrays.binarySearch(TEXT_TYPES, contentType) > -1) {
                upload = new MemoryFileUpload(name, filename, contentType.getValue(), null, this.charset, length);
            } else {
                upload = new MemoryFileUpload(name, filename, contentType.getValue(), "binary", null, length);
            }
            try {
                upload.setContent(content);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            HttpPostRequestEncoder encoder;
            try {
                encoder = new HttpPostRequestEncoder(httpRequest, false);
                encoder.addBodyHttpData(upload);
            } catch (HttpPostRequestEncoder.ErrorDataEncoderException ex) {
                throw new RuntimeException(ex);
            }
        }
        return httpRequest;
    }

}
