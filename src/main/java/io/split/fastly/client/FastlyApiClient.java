package io.split.fastly.client;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.FluentCaseInsensitiveStringsMap;
import com.ning.http.client.FluentStringsMap;
import com.ning.http.client.Response;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import static io.split.fastly.client.FastlyApiClient.Method.POST;
import static io.split.fastly.client.FastlyApiClient.Method.PURGE;
import static io.split.fastly.client.FastlyApiClient.Method.PUT;
import static java.util.stream.Collectors.toList;



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
    private final Joiner SURROGATE_KEY_JOINER = Joiner.on(" ");


    public FastlyApiClient(final String apiKey, final String serviceId) {
        this(apiKey, serviceId, null);
    }

    public FastlyApiClient(final String apiKey, final String serviceId, AsyncHttpClientConfig config) {

        _commonHeaders = ImmutableMap.of(
                "Fastly-Key", apiKey,
                "Accept", "application/json",
                "User-Agent", "fastly-api-java-v"+ VersionResolver.instance().getVersion());
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
                ImmutableMap.<String, String> builder()
                        .putAll(_commonHeaders)
                        .put("Content-Type", "application/x-www-form-urlencoded")
                        .build(),
                ImmutableMap.<String, String> builder().put("content", vcl).put("name", name).put("id", id).build());
    }

    public List<Future<Response>> vclUpdate(int version, Map<String, String> vcl) {
        List<Future<Response>> responses = vcl.entrySet().stream().map( e -> {
            String apiUrl = String.format("%s/service/%s/version/$d/vcl/%s", FASTLY_URL, _serviceId, version, e.getKey());
            return _asyncHttpExecutor.execute(
                    apiUrl,
                    PUT,
                    ImmutableMap.<String, String> builder()
                            .putAll(_commonHeaders)
                            .put("Content-Type", "application/x-www-form-urlencoded")
                            .build(),
                    ImmutableMap.<String, String> builder().put("content", e.getValue()).put("name", e.getKey()).build());

            }).collect(toList());
        return responses;
    }

    public Future<Response> purgeUrl(final String url) {
        return purgeUrl(url, Collections.EMPTY_MAP);
    }

    public Future<Response> softPurgeUrl(final String url) {
        return softPurgeUrl(url, Collections.EMPTY_MAP);
    }

    public Future<Response> softPurgeUrl(final String url, Map<String, String> extraHeaders) {
        return purgeUrl(url, buildHeaderForSoftPurge(extraHeaders));
    }

    public Future<Response> purgeUrl(final String url, Map<String, String> extraHeaders) {

        return _asyncHttpExecutor.execute(
                url,
                PURGE,
                ImmutableMap.<String, String>builder().putAll(_commonHeaders).putAll(extraHeaders).build(),
                Collections.EMPTY_MAP);
    }

    public Future<Response> purgeKey(String key, Map<String, String> extraHeaders) {
        String apiUrl = String.format("%s/service/%s/purge/%s", FASTLY_URL, _serviceId, key);
        return _asyncHttpExecutor.execute(
                apiUrl,
                POST,
                ImmutableMap.<String, String> builder()
                        .putAll(_commonHeaders)
                        .putAll(extraHeaders)
                        .build(),
                Collections.EMPTY_MAP);
    }

    public Future<Response> purgeKey(String key) {
        return purgeKey(key, Collections.EMPTY_MAP);
    }

    public Future<Response> softPurgeKey(String key, Map<String, String> extraHeaders) {
        return purgeKey(key, buildHeaderForSoftPurge(extraHeaders));
    }

    public Future<Response> softPurgeKeys(List<String> keys) {
        Preconditions.checkNotNull(keys, "keys cannot be null!");
        Preconditions.checkArgument(keys.size() <= 256, "Fastly can't purge batches of more than 256 keys");

        String apiUrl = String.format("%s/service/%s/purge", FASTLY_URL, _serviceId);
        return _asyncHttpExecutor.execute(
                apiUrl,
                POST,
                ImmutableMap.<String, String> builder()
                        .putAll(_commonHeaders)
                        .put("Surrogate-Key", SURROGATE_KEY_JOINER.join(keys))
                        .build(),
                Collections.EMPTY_MAP);
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
     * Supports PURGE operations.
     */
    static class ExtendedAsyncHttpClient extends AsyncHttpClient {

        public ExtendedAsyncHttpClient(AsyncHttpClientConfig config) {
            super(config);
        }

        /**
         * Prepare an HTTP client PURGE request.
         *
         * @param url A well formed URL.
         * @return {@link BoundRequestBuilder}
         */
        public BoundRequestBuilder preparePurge(String url) {
            return requestBuilder("PURGE", url);
        }
    }

    /**
     * Entity Responsible for executing the requesting against the remote endpoint.
     */
    private class AsyncHttpExecutor {

        private ExtendedAsyncHttpClient client;

        public AsyncHttpExecutor() {
            client = _config != null ? new ExtendedAsyncHttpClient(_config) : new ExtendedAsyncHttpClient(defaultConfig);
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
                        Method method,
                        Map<String, String> headers,
                        Map<String, String> parameters) {

            AsyncHttpClient.BoundRequestBuilder request = getRequestForMethod(apiUrl, method);

            build(request, headers, parameters);

           // proxyServer.map(request.setProxyServer)

            return request.execute();

        }

        private AsyncHttpClient.BoundRequestBuilder getRequestForMethod(String apiURL, Method method) {

            if (method == Method.PURGE) {
                return client.preparePurge(apiURL);
            }

            if (method == Method.POST) {
                return client.preparePost(apiURL);
            }

            if (method == Method.PUT) {
                return client.preparePut(apiURL);
            }

            if (method == Method.DELETE) {
                return client.prepareDelete(apiURL);
            }

            if (method == Method.GET) {
                return client.prepareGet(apiURL);
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

    static enum Method {

        POST,
        PURGE,
        PUT,
        GET,
        DELETE;

    }

}


