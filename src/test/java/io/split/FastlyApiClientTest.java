package io.split;

import com.ning.http.client.Response;
import io.split.fastly.client.FastlyApiClient;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class FastlyApiClientTest extends BaseFastlyTest {

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

}