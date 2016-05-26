package io.split.fastly.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * read version from pom file to be used in subsequent header to identify this client.
 *
 * Created by patricioe on 5/25/16.
 */
public class VersionResolver {

    private static final Logger _log = LoggerFactory.getLogger(VersionResolver.class);

    private static VersionResolver _instance;
    private String _version;

    public String getVersion() {
        return _version;
    }

    private VersionResolver() {
        setVersion();
    }

    public static VersionResolver instance() {
        if (_instance == null) {
            synchronized (VersionResolver.class) {
                if (_instance == null) {
                    _instance = new VersionResolver();
                }
            }
        }

        return _instance;
    }

    private void setVersion() {
        Properties props = new Properties();
        try {
            props.load(this.getClass().getClassLoader().getResourceAsStream("version.properties"));
        } catch (IOException e) {
            _log.warn("Fastly Client was unable to read version to include in headers. Defaulting in 'undefined'", e);
        }
        _version = (String) props.getOrDefault("client.version", "undefined");
    }

}
