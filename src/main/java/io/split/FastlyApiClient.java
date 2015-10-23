package io.split;

import com.google.common.collect.ImmutableMap;
import com.ning.http.client.*;
import org.jboss.netty.handler.codec.http.HttpMethod;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import static java.util.stream.Collectors.toList;
import static org.jboss.netty.handler.codec.http.HttpMethod.POST;
import static org.jboss.netty.handler.codec.http.HttpMethod.PUT;
import static org.jboss.netty.handler.codec.http.HttpMethod.DELETE;
import static org.jboss.netty.handler.codec.http.HttpMethod.GET;

/**
 * Wrapper client for http://docs.fastly.com/api
 *
 * Created by patricioe on 10/12/15.
 */
public class FastlyApiClient {

    private final static String FASTLY_URL = "https://api.fastly.com";
    private final Map<String, String> _commonHeaders;
    private final AsyncHttpClientConfig _config;
    private final AsyncHttpExecutor _asyncHttpExecutor;
    private final String _serviceId;
    private final String _apiKey;

    public FastlyApiClient(final String apiKey, final String serviceId,
                           AsyncHttpClientConfig config /*, proxyServer:Option[ProxyServer]=None*/) {

        _commonHeaders = ImmutableMap.of("X-Fastly-Key", apiKey, "Accept", "application/json");
        _config = config;
        _apiKey = apiKey;
        _serviceId = serviceId;
        _asyncHttpExecutor = new AsyncHttpExecutor();
    }

    public Future<Response> vclUpload(int version, String vcl, String id, String name) {
        String apiUrl = String.format("%s/service/%s/version/%d/vcl", FASTLY_URL, _serviceId, version);
        return _asyncHttpExecutor.execute(
                apiUrl,
                POST,
                ImmutableMap.<String, String> builder().putAll(_commonHeaders).put("Content-Type", "application/x-www-form-urlencoded").build(),
                ImmutableMap.<String, String> builder().put("content", vcl).put("name", name).put("id", id).build());
    }

    public List<Future<Response>> vclUpdate(int version, Map<String, String> vcl) {
        List<Future<Response>> responses = vcl.entrySet().stream().map( e -> {
            String apiUrl = String.format("%s/service/%s/version/$d/vcl/%s", FASTLY_URL, _serviceId, version, e.getKey());
            return _asyncHttpExecutor.execute(
                    apiUrl,
                    PUT,
                    ImmutableMap.<String, String> builder().putAll(_commonHeaders).put("Content-Type", "application/x-www-form-urlencoded").build(),
                    ImmutableMap.<String, String> builder().put("content", e.getValue()).put("name", e.getKey()).build());

            }).collect(toList());
        return responses;
    }

    public Future<Response> purge(final String url, Map<String, String> extraHeaders) {
        String urlWithoutPrefix = url.replaceFirst("^(http://|https://)","");
        String apiUrl = String.format("%s/purge/%s", FASTLY_URL, urlWithoutPrefix);
        return _asyncHttpExecutor.execute(
                apiUrl,
                POST,
                ImmutableMap.<String, String> builder().putAll(_commonHeaders).putAll(extraHeaders).build(),
                Collections.EMPTY_MAP);
    }

    public Future<Response> purge(final String url) {
        return purge(url, Collections.EMPTY_MAP);
    }

    public Future<Response> softPurge(final String url, Map<String, String> extraHeaders) {
        return purge(url, buildHeaderForSoftPurge(extraHeaders));
    }

    public Future<Response> softPurge(final String url) {
        return softPurge(url, Collections.EMPTY_MAP);
    }

    public Future<Response> purgeKey(String key, Map<String, String> extraHeaders) {
        String apiUrl = String.format("%s/service/%s/purge/%s", FASTLY_URL, _serviceId, key);
        return _asyncHttpExecutor.execute(
                apiUrl,
                POST,
                ImmutableMap.<String, String> builder().put("X-Fastly-Key", _apiKey).putAll(extraHeaders).build(),
                Collections.EMPTY_MAP);
    }

    public Future<Response> purgeKey(String key) {
        return purgeKey(key, Collections.EMPTY_MAP);
    }

    public Future<Response> softPurgeKey(String key, Map<String, String> extraHeaders) {
        return purgeKey(key, buildHeaderForSoftPurge(extraHeaders));
    }

    public Future<Response> softPurgeKey(String key) {
        return softPurgeKey(key, Collections.EMPTY_MAP);
    }

    public Future<Response>  purgeAll() {
        String apiUrl = String.format("%s/service/%s/purge_all", FASTLY_URL, _serviceId);
        return _asyncHttpExecutor.execute(
                apiUrl,
                POST,
                _commonHeaders,
                Collections.emptyMap());
    }


    private Map<String, String> buildHeaderForSoftPurge(Map<String, String> extraHeaders) {
        return ImmutableMap.<String, String> builder().put("Fastly-Soft-Purge", "1").putAll(extraHeaders).build();
    }

    public void closeConnectionPool() {
        _asyncHttpExecutor.close();
    }

    /**
     * Entity Responsible for executing the requesting against the remote endpoint.
     */
    private class AsyncHttpExecutor {

        private AsyncHttpClient client;

        public AsyncHttpExecutor() {
            client = _config != null ? new AsyncHttpClient(_config) : new AsyncHttpClient(defaultConfig);
        }

        private AsyncHttpClientConfig defaultConfig = new AsyncHttpClientConfig.Builder()
                .setAllowPoolingConnections(true)
                .setMaxConnections(50)
                .setMaxRequestRetry(3)
                .setMaxConnections(20000)
                .build();

        public void close() {
            client.close();
        }

        public Future<Response> execute( String apiUrl,
                        HttpMethod method,
                        Map<String, String> headers,
                        Map<String, String> parameters) {

            AsyncHttpClient.BoundRequestBuilder request = getRequestForMethod(apiUrl, method);

            build(request, headers, parameters);

           // proxyServer.map(request.setProxyServer)

            return request.execute();

        }

        private AsyncHttpClient.BoundRequestBuilder getRequestForMethod(String apiURL, HttpMethod method) {
            if (method.equals(POST)) {
                return client.preparePost(apiURL);
            }

            if (method.equals(PUT)) {
                return client.preparePost(apiURL);
            }

            if (method.equals(DELETE)) {
                return client.preparePost(apiURL);
            }

            if (method.equals(GET)) {
                return client.preparePost(apiURL);
            }
            return null;
        }

        private void build(AsyncHttpClient.BoundRequestBuilder request, Map<String, String> headers, Map<String, String> parameters) {

            FluentCaseInsensitiveStringsMap fluentCaseInsensitiveStringsMap = new FluentCaseInsensitiveStringsMap();
            headers.forEach( (k, v) -> fluentCaseInsensitiveStringsMap.add(k, v));

            request.setHeaders(fluentCaseInsensitiveStringsMap);

            FluentStringsMap fluentStringsMap = new FluentStringsMap();
            parameters.forEach((k, v) -> fluentStringsMap.add(k, v));

            if (request.build().getMethod().equals("GET")) {
                request.setQueryParams(fluentStringsMap);
            } else {
                request.setFormParams(fluentStringsMap);
            }

            String host = headers.get("Host");
            if (host != null) {
                request.setVirtualHost(host);
            }
        }

    }

}


