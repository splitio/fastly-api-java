package io.split.fastly.client;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FastlyApiClientTest {

    public static final String API_KEY = "someApikey";
    public static final String SERVICE_ID = "someServiceId";
    public static final List<String> SURROGATE_KEYS = Arrays.asList("key1", "key2");
    public static final String CUSTOM_FASTLY_URL = "http://custom_fastly_url.com";

    @Mock
    private FastlyApiClient.AsyncHttpExecutor executor;

    @Captor
    private ArgumentCaptor<Map<String, String>> headersCaptor;

    @Captor
    private ArgumentCaptor<Map<String, String>> parametersCaptor;

    @Captor
    private ArgumentCaptor<String> urlCaptor;

    @Captor
    private ArgumentCaptor<FastlyApiClient.Method> methodCaptor;

    private FastlyApiClient fastlyApiClient;

    @Before
    public void init() {
        fastlyApiClient = new FastlyApiClient(API_KEY, SERVICE_ID, null, executor);
        when(executor.execute(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(null);
    }

    @Test
    public void testPurgeKeys() {
        fastlyApiClient.purgeKeys(SURROGATE_KEYS);

        Mockito.verify(executor).execute(urlCaptor.capture(), methodCaptor.capture(), headersCaptor.capture(),
                parametersCaptor.capture());

        assertThat(urlCaptor.getValue(), is(String.format("%s/service/%s/purge", FastlyApiClient.FASTLY_URL, SERVICE_ID)));
        assertThat(methodCaptor.getValue(), is(FastlyApiClient.Method.POST));

        Map<String, String> headers = headersCaptor.getValue();
        assertThat(headers.get("Fastly-Key"), is(API_KEY));
        assertThat(headers.get("Accept"), is("application/json"));
        assertThat(headers.get("User-Agent"), is("fastly-api-java-v" + VersionResolver.instance().getVersion()));
        assertThat(headers.get("Surrogate-Key"), is(FastlyApiClient.SURROGATE_KEY_JOINER.join(SURROGATE_KEYS)));
        assertThat(headers.get("Fastly-Soft-Purge"), is(nullValue()));
    }

    @Test
    public void testSoftPurgeKeys() {
        fastlyApiClient.softPurgeKeys(SURROGATE_KEYS);

        Mockito.verify(executor).execute(urlCaptor.capture(), methodCaptor.capture(), headersCaptor.capture(),
                parametersCaptor.capture());

        assertThat(urlCaptor.getValue(), is(String.format("%s/service/%s/purge", FastlyApiClient.FASTLY_URL, SERVICE_ID)));
        assertThat(methodCaptor.getValue(), is(FastlyApiClient.Method.POST));

        Map<String, String> headers = headersCaptor.getValue();
        assertThat(headers.get("Fastly-Key"), is(API_KEY));
        assertThat(headers.get("Accept"), is("application/json"));
        assertThat(headers.get("User-Agent"), is("fastly-api-java-v" + VersionResolver.instance().getVersion()));
        assertThat(headers.get("Surrogate-Key"), is(FastlyApiClient.SURROGATE_KEY_JOINER.join(SURROGATE_KEYS)));
        assertThat(headers.get("Fastly-Soft-Purge"), is("1"));
    }

    @Test
    public void testPurgeKeysWithCustomFastlyUrl() {
        fastlyApiClient.purgeKeys(SURROGATE_KEYS, CUSTOM_FASTLY_URL);

        Mockito.verify(executor).execute(urlCaptor.capture(), methodCaptor.capture(), headersCaptor.capture(),
                parametersCaptor.capture());

        assertThat(urlCaptor.getValue(), is(String.format("%s/service/%s/purge", CUSTOM_FASTLY_URL, SERVICE_ID)));
        assertThat(methodCaptor.getValue(), is(FastlyApiClient.Method.POST));

        Map<String, String> headers = headersCaptor.getValue();
        assertThat(headers.get("Fastly-Key"), is(API_KEY));
        assertThat(headers.get("Accept"), is("application/json"));
        assertThat(headers.get("User-Agent"), is("fastly-api-java-v" + VersionResolver.instance().getVersion()));
        assertThat(headers.get("Surrogate-Key"), is(FastlyApiClient.SURROGATE_KEY_JOINER.join(SURROGATE_KEYS)));
        assertThat(headers.get("Fastly-Soft-Purge"), is(nullValue()));
    }

    @Test
    public void testSoftPurgeKeysWithCustomFastlyUrl() {
        fastlyApiClient.softPurgeKeys(SURROGATE_KEYS, CUSTOM_FASTLY_URL);

        Mockito.verify(executor).execute(urlCaptor.capture(), methodCaptor.capture(), headersCaptor.capture(),
                parametersCaptor.capture());

        assertThat(urlCaptor.getValue(), is(String.format("%s/service/%s/purge", CUSTOM_FASTLY_URL, SERVICE_ID)));
        assertThat(methodCaptor.getValue(), is(FastlyApiClient.Method.POST));

        Map<String, String> headers = headersCaptor.getValue();
        assertThat(headers.get("Fastly-Key"), is(API_KEY));
        assertThat(headers.get("Accept"), is("application/json"));
        assertThat(headers.get("User-Agent"), is("fastly-api-java-v" + VersionResolver.instance().getVersion()));
        assertThat(headers.get("Surrogate-Key"), is(FastlyApiClient.SURROGATE_KEY_JOINER.join(SURROGATE_KEYS)));
        assertThat(headers.get("Fastly-Soft-Purge"), is("1"));
    }
}
