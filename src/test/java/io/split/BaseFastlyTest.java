package io.split;

import com.google.common.base.Preconditions;
import org.junit.Before;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by patricioe on 10/22/15.
 */
public class BaseFastlyTest {

    protected String _fastly_api_key;
    protected String _fastly_service_id;

    @Before
    public void before() throws IOException {
        Properties properties = new Properties();
        properties.load(this.getClass().getClassLoader().getResourceAsStream("keys.properties"));
        _fastly_api_key = properties.getProperty("fastly.api.key");
        Preconditions.checkNotNull(_fastly_api_key,
                "Fastly Key must exists. Please check that \"keys.properties\" exists in the path " +
                        "and there is entry for 'fastly.api.key'");

        _fastly_service_id = properties.getProperty("fastly.service.id");
        Preconditions.checkNotNull(_fastly_service_id,
                "Fastly Key must exists. Please check that \"keys.properties\" exists in the path " +
                        "and there is entry for 'fastly.service.id'");
    }
}
