/*
 * This file is part of the netty-router package.
 * 
 * (c) Richard Lea <chigix@zoho.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package io.netty.handler.codec.http.router;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Richard Lea <chigix@zoho.com>
 */
class RoutingPathMatcher {

    private static final InternalLogger LOG = InternalLoggerFactory.getInstance(RoutingPathMatcher.class);

    /**
     * The list of all the patterns managed in this router. Mainly used to be
     * scan all the pattern to find a match result with a given generatePath.
     */
    protected final Map<String, Routing> patterns = new LinkedHashMap<String, Routing>();

    //----------------------------------------------------
    /**
     * Inserts a {@link Pattern} at the last position of this router.
     *
     * @param pattern
     * @return
     */
    public RoutingPathMatcher add(Routing pattern) {
        if (this.patterns.put(pattern.getName(), pattern) != null) {
            LOG.warn("There is Routing Override occured in same name: " + pattern.getName());
        }
        return this;
    }

    //----------------------------------------------------
    /**
     * Remove registered patterns configured by the specified routing name.
     *
     * @param path
     * @return
     */
    public RoutingPathMatcher remove(String routeName) {
        this.patterns.remove(routeName);
        return this;
    }

    /**
     * Remove all registered patterns with the specified path in param given.
     *
     * @param path
     * @return
     */
    public RoutingPathMatcher removePathPattern(String path) {
        List<String> tobeRemovedPatterns = new ArrayList<String>();
        for (Map.Entry<String, Routing> entrySet : this.patterns.entrySet()) {
            if (entrySet.getValue().getPath().equals(RouterUtil.normalizePath(path))) {
                tobeRemovedPatterns.add(entrySet.getKey());
            }
        }
        for (String routingName : tobeRemovedPatterns) {
            this.patterns.remove(routingName);
        }
        return this;
    }

    //----------------------------------------------------
    /**
     * Returns the mapping routed information object with target object for the
     * given generatePath. This is THE MAIN match support funciton for this
     * RoutingPathMatcher.
     *
     * @param path
     * @return The {@code null} return means "NOT FOUND", which is usually
     * discribed as 404 in response.
     * @TODO add path mapping cache acceleration.
     */
    public RoutingPathMatched match(String path) {
        final String[] tokens_from_path;
        try {
            tokens_from_path = RouterUtil.normalizePath(path).split("/");
        } catch (InvalidPathException ex) {
            return null;
        }
        // The map of parameter defined in the pattern with the form of param-name:param-value,
        // which is correspondingly form pattern defined and given generatePath.
        for (Routing pattern : this.patterns.values()) {
            final Map<String, String> params = new HashMap<String, String>();
            boolean matched;
            String[] iteration_pattern_tokens = pattern.getTokens();
            // ===================initialization for this iteration
            if (tokens_from_path.length == 0 && iteration_pattern_tokens.length == 0) {
                matched = true;
            } else if (tokens_from_path.length == iteration_pattern_tokens.length) {
                matched = false;
                for (int i = 0; i < iteration_pattern_tokens.length; i++) {
                    String token_from_path = tokens_from_path[i];
                    String token_definedin_pattern = iteration_pattern_tokens[i];
                    // Detect the [Path Segment Variable Expansion](http://tools.ietf.org/html/rfc6570#section-3.2.6)
                    if (token_definedin_pattern.equals(token_from_path)) {
                        matched = true;
                    } else if (token_definedin_pattern.length() > 0 && token_definedin_pattern.charAt(0) == ':') {
                        // Put this mapping as key-value into params.
                        // The token begining with colon to be as key
                        // The token at the same position in the path to be as value
                        params.put(token_definedin_pattern.substring(1), token_from_path);
                        matched = true;
                    } else {
                        // break the testing iteration on this pattern
                        matched = false;
                        break;
                    }
                }
            } else if (iteration_pattern_tokens.length > 0
                    && iteration_pattern_tokens[iteration_pattern_tokens.length - 1].equals(":*")
                    && tokens_from_path.length >= iteration_pattern_tokens.length) {
                // For the special case that the pattern ends with `:*`
                // of course there would be more tokens in the given generatePath than the matched pattern.
                matched = false;
                for (int i = 0; i < iteration_pattern_tokens.length - 1; i++) {
                    String token_from_path = tokens_from_path[i];
                    String token_definedin_pattern = iteration_pattern_tokens[i];
                    // Detect the [Path Segment Variable Expansion](http://tools.ietf.org/html/rfc6570#section-3.2.6)
                    if (token_definedin_pattern.equals(token_from_path)) {
                        matched = true;
                    } else if (token_definedin_pattern.length() > 0 && token_definedin_pattern.charAt(0) == ':') {
                        // Put this mapping as key-value into params.
                        // The token begining with colon to be as key
                        // The token at the same position in the path to be as value
                        params.put(token_definedin_pattern.substring(1), token_from_path);
                        matched = true;
                    } else {
                        // break the testing iteration on this pattern
                        matched = false;
                        break;
                    }
                }
                if (matched) {
                    // Extract the part matched with the `*` mark 
                    // and put it into params array with the key valued `*`
                    StringBuilder sb = new StringBuilder(tokens_from_path[iteration_pattern_tokens.length - 1]);
                    for (int i = iteration_pattern_tokens.length; i < tokens_from_path.length; i++) {
                        sb.append("/").append(tokens_from_path[i]);
                    }
                    params.put("*", sb.toString());
                }
            } else {
                matched = false;
            }
            if (matched) {
                return new RoutingPathMatched(pattern, params);
            }
        }
        return null;
    }

    /**
     * Generate the routable path string best matching the pattern with the
     * given params correspondingly.
     *
     * @param name
     * @param params The count of params should be nice and even to be devided
     * by 2, otherwise the {@link IllegalArgumentException} would be raised.
     * @return Returns {@code null} if there is no {@link Pattern} matched in
     * this router.In addtion, the pattern with ":*" would also not be matched
     * in any cases.
     * @throws IllegalArgumentException
     */
    public String generatePath(String name, Object... params) {
        if (params.length == 0) {
            return this.generatePath(name, Collections.emptyMap());
        }
        if (params.length == 1 && params[0] instanceof Map) {
            return this.pathMap(name, (Map<Object, Object>) params[0]);
        }
        if (params.length % 2 == 1) {
            throw new IllegalArgumentException(MessageFormat.format("Missing value for params: {0}", params[params.length - 1]));
        }
        final Map map = new HashMap();
        for (int i = 0; i < params.length; i += 2) {
            final String key = params[i].toString();
            final String value = params[i + 1].toString();
            map.put(key, value);
        }
        return this.pathMap(name, map);
    }

    /**
     * A PRIVATE util method only be allowed calling from {@link #generatePath(io.netty.channel.ChannelHandler, java.lang.Object...)
     * }. Generate the routable path string with the params value given best
     * matching the pattern linked to the given target.
     *
     * @param name
     * @param params A name/value pair where the name is of equivalence to the
     * key defined in the pattern.
     * @return Returns {@code null} if there is no {@link Pattern} matched in
     * this router. In addtion, the pattern with ":*" would also not be matched
     * in any cases.
     * @TODO Improve the imploding using string replacement rather than string
     * builder.
     */
    private String pathMap(String name, Map<Object, Object> params) {
        final Routing pattern_mapped_target = this.patterns.get(name);
        // The best one is the one with minimum number of params in the query.
        String bestCandidate = null;
        boolean matched;
        final Set<String> usedKeys = new HashSet<String>();
        matched = true;
        usedKeys.clear();
        final StringBuilder path_string_builder = new StringBuilder();
        for (String token_definedin_pattern : pattern_mapped_target.getTokens()) {
            path_string_builder.append("/");
            if (token_definedin_pattern.length() > 0 && token_definedin_pattern.charAt(0) == ':') {
                // This token is a placeholer.
                final String key = token_definedin_pattern.substring(1);
                if (params.get(key) == null) {
                    matched = false;
                    break;
                }
                usedKeys.add(key);
                path_string_builder.append(params.get(key).toString());
            } else {
                // This token is a plain text
                path_string_builder.append(token_definedin_pattern);
            }
        }
        final StringBuilder sb;
        sb = new StringBuilder(RouterUtil.normalizePath(path_string_builder.toString()));
        if (matched) {
            final int numQueryParams = params.size() - usedKeys.size();
            if (numQueryParams > 0) {
                boolean firstQueryParam = true;
                for (Map.Entry<Object, Object> entry : params.entrySet()) {
                    final String key = entry.getKey().toString();
                    if (!usedKeys.contains(key)) {
                        if (firstQueryParam) {
                            sb.append("?");
                            firstQueryParam = false;
                        } else {
                            sb.append("&");
                        }
                        final String value = entry.getValue().toString();
                        try {
                            sb.append(URLEncoder.encode(key, "UTF-8"));
                            sb.append("=").append(URLEncoder.encode(value, "UTF-8"));
                        } catch (UnsupportedEncodingException ex) {
                            throw new UnsupportedOperationException(ex);
                        }
                    }
                }
            }
            bestCandidate = sb.toString();
        }
        return bestCandidate;
    }

}
