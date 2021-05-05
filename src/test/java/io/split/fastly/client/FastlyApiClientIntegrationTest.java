package io.split.fastly.client;

import com.google.common.collect.Lists;
import com.ning.http.client.Response;
import io.split.BaseFastlyTest;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

// Remove Ignore if you are setting the keys.properties to try this against Fastly. Not including api keys since this is a public repo.
@Ignore
public class FastlyApiClientIntegrationTest extends BaseFastlyTest {

    @Test
    public void testPurgeAll() throws ExecutionException, InterruptedException, IOException {
        FastlyApiClient client = new FastlyApiClient(_fastly_api_key, _fastly_service_id, null);

        purgeAll(client);

        Future<Response> future = client.softPurgeKey("1446076111485");
        Response res = future.get();

        printResult(res);
    }

    private void purgeAll(FastlyApiClient client) throws InterruptedException, ExecutionException, IOException {
        Future<Response> future;
        Response res;
        future = client.purgeAll();
        res = future.get();
        printResult(res);
    }

    private void printResult(Response res) throws IOException {
        System.out.println(res.getStatusCode());
        System.out.println(res.getStatusText());
        System.out.println(res.getResponseBody());
    }

    @Test
    public void testPurgeMultipleKeys() throws ExecutionException, InterruptedException, IOException {
        FastlyApiClient client = new FastlyApiClient(_fastly_api_key, _fastly_service_id, null);

        Future<Response> future = client.softPurgeKeys(Lists.newArrayList("a", "b", "c", "d"));
        Response res = future.get();

        printResult(res);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPurgeMoreThan256Keys() throws ExecutionException, InterruptedException, IOException {
        List<String> keys = new Random().ints(257,0,10000)
                .boxed()
                .map(i -> Integer.toString(i))
                .collect(Collectors.toList());

        FastlyApiClient client = new FastlyApiClient(_fastly_api_key, _fastly_service_id, null);

        Future<Response> future = client.softPurgeKeys(keys);
        Response res = future.get();

        printResult(res);
    }

    @Test
    public void testPurgeKey() throws ExecutionException, InterruptedException, IOException {
        FastlyApiClient client = new FastlyApiClient(_fastly_api_key, _fastly_service_id, null);

        Future<Response> future = client.softPurgeKey("a");
        Response res = future.get();

        printResult(res);
    }
}