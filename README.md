# fastly-api-java
Java API Wrapper for Fastly (https://docs.fastly.com/api)

# Add snapshot repo to your maven pom.xml

```xml
<repositories>
    <repository>
      <id>sonatype snapshots</id>
      <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
    </repository>
    .....
```

# Build

```export GPG_TTY=$(tty) && mvn clean install```

# Run Integration tests

Create a file under src/test/resources called: keys.properties containing:

```
fastly.api.key=<YOUR_FASTLY_API_KEY>
fastly.service.id=<YOUR_FASTLY_SERVICE_KEY>
```

And remove the Ignore annotation from the FastlyApiClientIntegrationTest.

# how to deploy a version of this library?

http://central.sonatype.org/pages/apache-maven.html#performing-a-snapshot-deployment

# Any Issues?

Contact me at pato@split.io
