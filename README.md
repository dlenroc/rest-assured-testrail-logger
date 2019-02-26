# rest-assured-testrail-logger
Logs HTTP requests sent by REST-assured in TestRail formatting

# Repository
```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```

# Dependency
```xml
<dependency>
    <groupId>com.github.dlenroc</groupId>
    <artifactId>rest-assured-testrail-logger</artifactId>
    <version>master-SNAPSHOT</version>
</dependency>
```

# Enable listener
```kotlin
@get:Rule val testRail = TestRailJunit4Rule()
```