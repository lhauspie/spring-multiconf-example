# Composing Configuration Files

## Why

I'm working on a project with several components for my client.
Some components need the same config subset.
Some other components need all the configuration.
And so on.

To simplify the problem, let's say I have a batch that need to consume the exact same external API than my REST API.
Both component will need a set of configuration properties like:
- Base URL of the external API
- API Key
- OAuth2 information
- and so one

The solution could be to have a common configuration file `app-config.yaml`.
But what do we have to do when some configuration properties cannot be shared?

Let's say my batch needs to produce kafka messages but not my REST-API.
We might decide to have two configuration files:
- `app-batch.yaml` to configure the batch
- `app-api.yml` to configure the api

But what will happen when I'll have to update the API Key to consume the external API for instance?
I will have to update the same information several times (once for batch and another one for rest api)...

And I'm talking about only two components.
The problem grows with the number of components.


## What

The solution we're exploring with my team is to make a kind of configuration composition.

Instead of having two configuration files `application-batch.yaml` and `application-api.yaml` that will contain config duplication, I would like to have for example `external-api.yaml` and `message-borker.yaml` and then compose them at runtime for each component.

The team uses Spring Boot for all our backend components so let's try to compose configuration with spring boot.


## How

In spring boot, there is several ways to change the configuration file at runtime:
- [spring.config.location](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html#application-properties.core.spring.config.location)
- [spring.config.additional-location](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html#application-properties.core.spring.config.additional-location)
- [spring.config.import](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html#application-properties.core.spring.config.import)


As the documentation indicates, the first option allows to completely replace the default configuration file, the second allows to request the **merge** of the default configuration file with one or more other files, and the third allows to **import** one or more configuration files independently of each other in addition to the default configuration file.

We can then take advantage of two of them to make our composition.


### Split configuration files

`external-api.yaml` (the configuration file dedicated to external API):
```yaml
app.properties:
  first-property: "OVERRIDEN FIRST"
```

`message-borker.yaml` (the configuration file dedicated to Message Broker):
```yaml
app.properties:
  second-property: "OVERRIDEN SECOND"
```


### Compose configuration files

`application-rest-api.yaml` tells Spring to import the configuration file dedicated to the external API:
```yaml
spring.config.import:
  - external-api.yaml
```

`application-batch.yaml` tells Spring to import the configuration file dedicated to the external API and the one dedicated to the message broker:
```yaml
spring.config.import:
  - external-api.yaml
  - message-broker.yaml
```

This is purely a configuration files composition.


### Specify the configuration file to be loaded at runtime

It is now possible to merge the configuration file corresponding to the executed component with the default configuration:
```shell
java -Dspring.config.additional-location="configuration/application-rest-api.yaml" -jar target/application.jar
```


## Result

With the default configuration file containing:
```yaml
app.properties:
  first-property: "FIRST"
  second-property: "SECOND"
```

Running the application without additional configuration files gives the following result:
```shell
$ java -jar target/application.jar

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::             (v3.1.0-M1)

2023-03-05T21:36:49.048+01:00  INFO 82691 --- [           main] c.l.m.e.MulticonfExampleApplication      : Starting MulticonfExampleApplication v0.0.1-SNAPSHOT using Java 17 with PID 82691 (/Users/lhauspie/projects/perso/sources/multiconf-exampe/target/application.jar started by lhauspie in /Users/lhauspie/projects/perso/sources/multiconf-exampe)
2023-03-05T21:36:49.050+01:00  INFO 82691 --- [           main] c.l.m.e.MulticonfExampleApplication      : No active profile set, falling back to 1 default profile: "default"
first property: FIRST
second property: SECOND
2023-03-05T21:36:49.525+01:00  INFO 82691 --- [           main] c.l.m.e.MulticonfExampleApplication      : Started MulticonfExampleApplication in 0.834 seconds (process running for 1.318)
```

Specifying the file `application-rest-api.yaml`, the result becomes :
```shell
java -Dspring.config.additional-location="configuration/application-rest-api.yaml" -jar target/application.jar

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::             (v3.1.0-M1)

2023-03-05T21:40:14.577+01:00  INFO 82878 --- [           main] c.l.m.e.MulticonfExampleApplication      : Starting MulticonfExampleApplication v0.0.1-SNAPSHOT using Java 17 with PID 82878 (/Users/lhauspie/projects/perso/sources/multiconf-exampe/target/application.jar started by lhauspie in /Users/lhauspie/projects/perso/sources/multiconf-exampe)
2023-03-05T21:40:14.579+01:00  INFO 82878 --- [           main] c.l.m.e.MulticonfExampleApplication      : No active profile set, falling back to 1 default profile: "default"
first property: OVERRIDEN FIRST     <<========
second property: SECOND
2023-03-05T21:40:14.977+01:00  INFO 82878 --- [           main] c.l.m.e.MulticonfExampleApplication      : Started MulticonfExampleApplication in 0.771 seconds (process running for 1.136)
```

Specifying the file `application-batch.yaml`, the result becomes :
```shell
java -Dspring.config.additional-location="configuration/application-batch.yaml" -jar target/application.jar

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::             (v3.1.0-M1)

2023-03-05T21:40:14.577+01:00  INFO 82878 --- [           main] c.l.m.e.MulticonfExampleApplication      : Starting MulticonfExampleApplication v0.0.1-SNAPSHOT using Java 17 with PID 82878 (/Users/lhauspie/projects/perso/sources/multiconf-exampe/target/application.jar started by lhauspie in /Users/lhauspie/projects/perso/sources/multiconf-exampe)
2023-03-05T21:40:14.579+01:00  INFO 82878 --- [           main] c.l.m.e.MulticonfExampleApplication      : No active profile set, falling back to 1 default profile: "default"
first property: OVERRIDEN FIRST     <<========
second property: OVERRIDEN SECOND   <<========
2023-03-05T21:40:14.977+01:00  INFO 82878 --- [           main] c.l.m.e.MulticonfExampleApplication      : Started MulticonfExampleApplication in 0.771 seconds (process running for 1.136)
```

We have succeeded in composing configuration files and in overloading the default configurations.

I can't wait to see what it will look like in our project.