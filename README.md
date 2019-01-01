# spring-boot-kmetrics
Kafka metrics for Spring Boot Actuator

Kafka exposes almost all consumer and producer metrics through JMX, so this is a custom MeterBinder to expose these metrics. 

I had also created a springboot autoconfigure and starter modules to simplify the usage (you just need to include the starter module dependency to your project).

```xml
<dependency>
	<groupId>org.kmetrics</groupId>
	<artifactId>spring-boot-starter-kmetrics</artifactId>
	<version>x.x.x</version>
</dependency>
```
