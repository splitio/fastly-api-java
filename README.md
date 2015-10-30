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

# Run tests

Create a file under src/test/resources called: keys.properties containing:

```
fastly.api.key=<YOUR_FASTLY_API_KEY>
fastly.service.id=<YOUR_FASTLY_SERVICE_KEY>
```

# Any Issues?

Contact me at pato@split.io
