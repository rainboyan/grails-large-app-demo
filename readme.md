# Grails Large App Demo

## Grails run-app development

```
grails create-app org.grails.demo.grails-large-app-demo
cd grails-large-app-demo
groovy gen_demo_class.groovy
grails run-app
```

## Grails run war production

```
cd grails-large-app-demo
grails war
cd build/libs
java -jar grails-large-app-demo-0.1.war
```

### Grails run app with spring Beans and Configs, use `@ComponentScan`

Environment:

* Mac OS 11.6.4, M1
* Grails 5.1.2
* Java 11.0.14-zulu

|Bean & Config   |    Default(Not Used) (  Production      )     |  @ComponentScan  (  Production  )   |
|----------------|-----------------------------------------------|-------------------------------------|
|10   + 10       |    2715ms, 719.5M    (  6595ms,  659.2M )     |  2791ms,  421.5M (  6783ms, 657.3M )|
|50   + 50       |    2727ms, 722.2M    (  6635ms,  665.2M )     |  2921ms,  661.9M (  6973ms, 631.6M )|
|100  + 100      |    2734ms, 665.2M    (  6546ms,  617.3M )     |  3137ms,  759.8M (  7183ms, 722.1M )|
|500  + 500      |    2763ms, 614.0M    (  6776ms,  618.3M )     |  4641ms,  794.4M (  9658ms, 773.1M )|
|1000 + 1000     |    2848ms, 721.8M    (  7047ms,  713.0M )     |  6282ms,  976.8M ( 12002ms, 928.7M )|
|2000 + 2000     |    3049ms, 745.6M    (  8704ms,  706.0M )     |  9532ms, 1011.7M ( 20637ms,  1.17G )|


### Grails run app with 1000 Domains and Services

|Domain & Services   |   Default  (  Production     )    |
|--------------------|-----------------------------------|
|1000 + 1000         |   24800ms, 1.10G  (27023ms, 1.43G)|


## Todo
- [ ] How to improve the performance of Spring Boot and Grails applications?

## Related links

* [Grails Large App Demo](https://github.com/rainboyan/grails-large-app-demo)
* [Grails Config & Application Class](https://docs.grails.org/latest/guide/conf.html)
* [Grails and Spring](https://docs.grails.org/latest/guide/spring.html)
* [Spring Framework Doc - Generating an Index of Candidate Components](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-scanning-index)
* [spring-framework#25886 - Use spring.components only for JARs that define an index](https://github.com/spring-projects/spring-framework/issues/25886)