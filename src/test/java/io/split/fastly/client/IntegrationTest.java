package io.split.fastly.client;

import io.split.fastly.client.FastlyApiClient;
import io.split.fastly.client.VersionResolver;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.*;

import static org.mockito.Mockito.when;

public class IntegrationTest {

    @Test
    public void testPurgeKeys() {
        final String apikey = "someApikey";
        final String serviceId = "someServiceId";

        FastlyApiClient.AsyncHttpExecutor executor = Mockito.mock(FastlyApiClient.AsyncHttpExecutor.class);
        when(executor.execute(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(null);
        FastlyApiClient client = new FastlyApiClient(apikey, serviceId, null, executor);

        List<String> keys = Arrays.asList("key1", "key2");
        client.purgeKeys(keys, Collections.EMPTY_MAP);
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<FastlyApiClient.Method> methodCaptor = ArgumentCaptor.forClass(FastlyApiClient.Method.class);
        ArgumentCaptor<Map> headersCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map> parametersCaptor = ArgumentCaptor.forClass(Map.class);
        Mockito.verify(executor).execute(urlCaptor.capture(), methodCaptor.capture(), headersCaptor.capture(),
                parametersCaptor.capture());

        assert (urlCaptor.getValue().equals(String.format("%s/service/%s/purge", FastlyApiClient.FASTLY_URL, serviceId)));
        assert (methodCaptor.getValue().equals(FastlyApiClient.Method.POST));

        Map<String, String> headers = headersCaptor.getValue();
        assert (headers.get("Fastly-Key").equals(apikey));
        assert (headers.get("Accept").equals("application/json"));
        assert (headers.get("User-Agent").equals("fastly-api-java-v"+ VersionResolver.instance().getVersion()));
        assert (headers.get("Surrogate-Key").equals(FastlyApiClient.SURROGATE_KEY_JOINER.join(keys)));
        assert (Objects.isNull(headers.get("Fastly-Soft-Purge")));
    }


    @Test
    public void testSoftPurgeKeys() {
        final String apikey = "someApikey";
        final String serviceId = "someServiceId";

        FastlyApiClient.AsyncHttpExecutor executor = Mockito.mock(FastlyApiClient.AsyncHttpExecutor.class);
        when(executor.execute(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(null);
        FastlyApiClient client = new FastlyApiClient(apikey, serviceId, null, executor);

        List<String> keys = Arrays.asList("key1", "key2");
        client.softPurgeKeys(keys);
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<FastlyApiClient.Method> methodCaptor = ArgumentCaptor.forClass(FastlyApiClient.Method.class);
        ArgumentCaptor<Map> headersCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map> parametersCaptor = ArgumentCaptor.forClass(Map.class);
        Mockito.verify(executor).execute(urlCaptor.capture(), methodCaptor.capture(), headersCaptor.capture(),
                parametersCaptor.capture());

        assert (urlCaptor.getValue().equals(String.format("%s/service/%s/purge", FastlyApiClient.FASTLY_URL, serviceId)));
        assert (methodCaptor.getValue().equals(FastlyApiClient.Method.POST));

        Map<String, String> headers = headersCaptor.getValue();
        assert (headers.get("Fastly-Key").equals(apikey));
        assert (headers.get("Accept").equals("application/json"));
        assert (headers.get("User-Agent").equals("fastly-api-java-v"+ VersionResolver.instance().getVersion()));
        assert (headers.get("Surrogate-Key").equals(FastlyApiClient.SURROGATE_KEY_JOINER.join(keys)));
        assert (headers.get("Fastly-Soft-Purge").equals("1"));
    }

}
