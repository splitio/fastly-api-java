package io.split;

import com.ning.http.client.Response;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class FastlyApiClientTest extends BaseFastlyTest {

    @Test
    public void testPurgeAll() throws ExecutionException, InterruptedException, IOException {
        FastlyApiClient client = new FastlyApiClient(_fastly_api_key, _fastly_service_id, null);

        Future<Response> future = client.purgeAll();
        Response res = future.get();

        System.out.println(res.getStatusCode());
        System.out.println(res.getStatusText());
        System.out.println(res.getResponseBody());

        future = client.softPurge("http://example.com", Collections.EMPTY_MAP);
        res = future.get();
    }

}