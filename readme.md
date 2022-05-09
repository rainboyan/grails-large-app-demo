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

```
grails run-app
```

## Performance Result

Spring Boot 2.2 M1, Provide a configuration option to enable lazy initialisation

https://spring.io/blog/2019/03/14/lazy-initialization-in-spring-boot-2-2

> It’s possible to enable lazy initialization in any version of Spring Boot if you’re happy to get your hands dirty and write a BeanFactoryPostProcessor. Spring Boot 2.2 just makes it easier via the introduction of a new property, spring.main.lazy-initialization (there are also equivalent methods on both SpringApplication and SpringApplicationBuilder). When set to true, bean definitions in the application will be configured to use lazy initialization.

Currently, Grails 5 support lazy initialization like this,

```
        gspTagLibraryLookup(TagLibraryLookup) { bean ->
            bean.lazyInit = true
        }
```

In Grails, if not set `lazyInit`, the `BeanDefinition.isLazyInit()` will be `false` default.

We can't config `spring.main.lazy-initialization` to enable global lazy initialization.

In Spring Boot `LazyInitializationBeanFactoryPostProcessor`, this PostProcessor will set LazyInit `true` if getLazyInit() was `null`,  but Grails always set `false`.

```
	private void postProcess(ConfigurableListableBeanFactory beanFactory,
			Collection<LazyInitializationExcludeFilter> filters, String beanName,
			AbstractBeanDefinition beanDefinition) {
		Boolean lazyInit = beanDefinition.getLazyInit();
		if (lazyInit != null) {
			return;
		}
		Class<?> beanType = getBeanType(beanFactory, beanName);
		if (!isExcluded(filters, beanName, beanDefinition, beanType)) {
			beanDefinition.setLazyInit(true);
		}
	}
```

I create [a large Grails app](https://github.com/rainboyan/grails-large-app-demo) which has 2000 Beans,  and I configure them in `conf/spring/resources.groovy`,

then, I test it on my old MacBook,

* Mac OS 10.14.6, later 2013 8G RAM
* Grails 5.1.7
* Java 11.0.14-zulu

|Bean & Config   |              | resources.groovy |   lazy-initialization: true    |   lazy-initialization: false   |
|----------------|--------------|------------------|--------------------------------|--------------------------------|
|   0 +    0     |    7606ms    |                  |             6478ms             |            7146ms              |
|2000 +    0     |              |     18614ms      |            17251ms             |           17704ms              |


Also I edit the Grails source code, to enable  lazy initialization like Spring Boot 2.2, 

```

    protected AbstractBeanDefinition createBeanDefinition() {
        AbstractBeanDefinition bd = new GenericBeanDefinition();
...
        if(clazz != null) {
             // bd.setLazyInit( clazz.getAnnotation(Lazy.class) != null);
             if (clazz.getAnnotation(Lazy.class) != null) {
                 bd.setLazyInit(true);
             }
             bd.setBeanClass(clazz);
         }
...
        return bd;
    }
```

the test result was below, it's better than previous version.

|Bean & Config   |              | resources.groovy |   lazy-initialization: true    |   lazy-initialization: false   |
|----------------|--------------|------------------|--------------------------------|--------------------------------|
|   0 +    0     |6243ms (18% ↓)|                  |             5862ms (23% ↓)     |                                |
|2000 +    0     |              |     18885ms      |            14423ms (24% ↓)     |           18462ms              |


If I create 2000 `DemoConfig` to configure 2000 `DemoBean`, and enable `@ComponentScan` to scan all configs.

```
@CompileStatic
@ComponentScan
class Application extends GrailsAutoConfiguration { 
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }
}
```

The result is better than use Grails `BeanBuilder` configure in `resources.groovy`.

|Bean & Config   |              |  @ComponentScan  |   lazy-initialization: true    |   lazy-initialization: false   |
|----------------|--------------|------------------|--------------------------------|--------------------------------|
|   0 +    0     |    7606ms    |                  |                                |                                |
|2000 + 2000     |              |     25235ms      |            11809ms  (53% ↓↓)    |           21468ms              |


more than that, if I add `@Configuration(proxyBeanMethods = false)` to all `DemoConfig`, the start time will be greatly reduced. It's only 9809ms (61% ↓↓↓) .

```
@Configuration(proxyBeanMethods = false)
public class Demo1000Config {
       @Bean
       public Demo1000Bean demo1000Bean() {
           return new Demo1000Bean("Demo 1000");
       }
}
```

|Bean & Config   |              |  @ComponentScan  |   lazy-initialization: true    |   lazy-initialization: false   |
|----------------|--------------|------------------|--------------------------------|--------------------------------|
|   0 +    0     |    7606ms    |                  |                                |                                |
|2000 + 2000     |              |     15889ms      |            9809ms (61% ↓↓↓)    |           16857ms              |


### 1000 Domains and Services

Grails 5.1.7 with 1000 Domains and Services

|Domain & Services   |    5.1.7     |    5.1.8 (lazy-initialization: true)    |
|--------------------|--------------|-----------------------------------------|
|1000 +    0         |   42500ms    |   40394ms                               |
|1000 + 1000         |   52758ms    |   48021ms                               |




### Plugin Loading Time

Loading plugins:      303ms
Application Started: 7945ms

|Domain & Services   |    Plugin         |    doWithSpring (475ms)     |    doWithDynamicMethods (1ms)    |    doWithApplicationContext (63ms)  |
|--------------------|-------------------|-----------------------------|----------------------------------|-------------------------------------|
|   0 +    0         |  restResponder    |    30ms                     |         0ms                      |         0ms                         |
|                    |  i18n             |    12ms                     |         0ms                      |         0ms                         |
|                    |  eventBus         |     6ms                     |         0ms                      |         0ms                         |
|                    |  core             |    17ms                     |         0ms                      |         0ms                         |
|                    |  codecs           |     0ms                     |         0ms                      |         0ms                         |
|                    |  dataSource       |    84ms                     |         0ms                      |         0ms                         |
|                    |  controllers      |    47ms                     |         0ms                      |         0ms                         |
|                    |  urlMappings      |    15ms                     |         0ms                      |         0ms                         |
|                    |  assetPipeline    |    43ms                     |         0ms                      |         0ms                         |
|                    |  groovyPages      |    83ms                     |         0ms                      |         0ms                         |
|                    |  scaffolding      |     2ms                     |         0ms                      |         0ms                         |
|                    |  mimeTypes        |     5ms                     |         0ms                      |         0ms                         |
|                    |  domainClass      |     9ms                     |         0ms                      |         0ms                         |
|                    |  controllersAsync |     3ms                     |         0ms                      |         0ms                         |
|                    |  converters       |    13ms                     |         0ms                      |         0ms                         |
|                    |  hibernate        |    91ms                     |         0ms                      |        62ms                         |
|                    |  fields           |     6ms                     |         0ms                      |         0ms                         |
|                    |  interceptors     |     1ms                     |         0ms                      |         0ms                         |
|                    |  services         |     2ms                     |         0ms                      |         0ms                         |
|                    |  cache            |     5ms                     |         0ms                      |         1ms                         |

|Bean                               |   Class                                                                               |    Time   |
|-----------------------------------|---------------------------------------------------------------------------------------|-----------|
|convertersConfigurationInitializer |   org.grails.web.converters.configuration.ConvertersConfigurationInitializer          |    69ms   |
|grailsPromiseFactory               |   org.grails.async.factory.future.CachedThreadPoolPromiseFactory                      |    17ms   |
|grailsEventBus                     |   org.grails.events.bus.spring.EventBusFactoryBean                                    |    25ms   |
|grailsEventBus                     |   org.grails.events.bus.ExecutorEventBus                                              |    25ms   |
|requestMappingHandlerMapping       |   org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping  |    28ms   |
|requestMappingHandlerAdapter       |   org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter  |    28ms   |
|groovyMarkupConfigurer             |   org.springframework.web.servlet.view.groovy.GroovyMarkupConfigurer                  |    64ms   |
|gspTagLibraryLookup                |   org.grails.taglib.TagLibraryLookup                                                  |    75ms   |
|webEndpointServletHandlerMapping   |   org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping  |    11ms   |


Loading plugins:       296ms
Application Started: 50013ms

|Domain & Services   |    Plugin         |    doWithSpring (4156ms)    |    doWithDynamicMethods (1ms)    |    doWithApplicationContext (77ms)  |
|--------------------|-------------------|-----------------------------|----------------------------------|-------------------------------------|
| 1000 + 1000        |  restResponder    |    13ms                     |        0ms                       |        0ms                          |
|                    |  i18n             |    11ms                     |        0ms                       |        0ms                          |
|                    |  eventBus         |    25ms                     |        0ms                       |        0ms                          |
|                    |  core             |    24ms                     |        0ms                       |        0ms                          |
|                    |  codecs           |     0ms                     |        0ms                       |        0ms                          |
|                    |  dataSource       |    90ms                     |        0ms                       |        0ms                          |
|                    |  controllers      |    42ms                     |        0ms                       |        0ms                          |
|                    |  urlMappings      |    15ms                     |        0ms                       |        0ms                          |
|                    |  assetPipeline    |    43ms                     |        0ms                       |        0ms                          |
|                    |  groovyPages      |    82ms                     |        0ms                       |       75ms                          |
|                    |  scaffolding      |     3ms                     |        0ms                       |        0ms                          |
|                    |  mimeTypes        |     8ms                     |        0ms                       |        0ms                          |
|                    |  domainClass      |     9ms                     |        0ms                       |        0ms                          |
|                    |  controllersAsync |     4ms                     |        1ms                       |        0ms                          |
|                    |  converters       |    11ms                     |        0ms                       |        0ms                          |
|                    |  hibernate        |  3770ms      ↑↑↑            |        0ms                       |        0ms                          |
|                    |  fields           |     4ms                     |        0ms                       |        0ms                          |
|                    |  interceptors     |     1ms                     |        0ms                       |        0ms                          |
|                    |  services         |     2ms                     |        0ms                       |        0ms                          |
|                    |  cache            |     6ms                     |        0ms                       |        1ms                          |


| Bean                                |   Class                                                                                |    Time    |
|-------------------------------------|----------------------------------------------------------------------------------------|------------|
| annotationHandlerAdapter            |   org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter   |    11ms    |
| classPathFileSystemWatcher          |   org.springframework.boot.devtools.classpath.ClassPathFileSystemWatcher               |   192ms ↑↑ |
| convertersConfigurationInitializer  |   org.grails.web.converters.configuration.ConvertersConfigurationInitializer           |    46ms    |
| grailsPromiseFactory                |   org.grails.async.factory.future.CachedThreadPoolPromiseFactory                       |    15ms    |
| grailsEventBus                      |   org.grails.events.bus.spring.EventBusFactoryBean                                     |    23ms    |
| grailsEventBus                      |   org.grails.events.bus.ExecutorEventBus                                               |    23ms    |
| groovyMarkupConfigurer              |   org.springframework.web.servlet.view.groovy.GroovyMarkupConfigurer                   |    57ms    |
| gspTagLibraryLookup                 |   org.grails.taglib.TagLibraryLookup                                                   |    76ms    |
| requestMappingHandlerMapping        |   org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping   |    32ms    |
| requestMappingHandlerAdapter        |   org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter   |    36ms    |
| webEndpointServletHandlerMapping    |   org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping   |    12ms    |
| demo261DomainService                |   org.grails.datastore.mapping.config.DatastoreServiceMethodInvokingFactoryBean        |    19ms    |
| demo54DomainService                 |   org.grails.datastore.mapping.config.DatastoreServiceMethodInvokingFactoryBean        |    12ms    |
| demo272DomainService                |   org.grails.datastore.mapping.config.DatastoreServiceMethodInvokingFactoryBean        |    12ms    |
| demo668DomainService                |   org.grails.datastore.mapping.config.DatastoreServiceMethodInvokingFactoryBean        |    12ms    |
| demo400DomainService                |   org.grails.datastore.mapping.config.DatastoreServiceMethodInvokingFactoryBean        |    13ms    |
| demo683DomainService                |   org.grails.datastore.mapping.config.DatastoreServiceMethodInvokingFactoryBean        |    11ms    |
| demo894DomainService                |   org.grails.datastore.mapping.config.DatastoreServiceMethodInvokingFactoryBean        |    11ms    |
| demo979DomainService                |   org.grails.datastore.mapping.config.DatastoreServiceMethodInvokingFactoryBean        |    11ms    |
| demo958DomainService                |   org.grails.datastore.mapping.config.DatastoreServiceMethodInvokingFactoryBean        |    13ms    |
| demo554DomainService                |   org.grails.datastore.mapping.config.DatastoreServiceMethodInvokingFactoryBean        |    12ms    |
| demo764DomainService                |   org.grails.datastore.mapping.config.DatastoreServiceMethodInvokingFactoryBean        |    13ms    |
| demo500DomainService                |   org.grails.datastore.mapping.config.DatastoreServiceMethodInvokingFactoryBean        |    11ms    |
| demo581DomainService                |   org.grails.datastore.mapping.config.DatastoreServiceMethodInvokingFactoryBean        |    11ms    |
| demo675DomainService                |   org.grails.datastore.mapping.config.DatastoreServiceMethodInvokingFactoryBean        |    14ms    |
| demo128DomainService                |   org.grails.datastore.mapping.config.DatastoreServiceMethodInvokingFactoryBean        |    12ms    |



## Todo
- [ ] How to improve the performance of Spring Boot and Grails applications?

## Related links

* [Grails Large App Demo](https://github.com/rainboyan/grails-large-app-demo)
* [grails-core# #12500 Performance Improvement: Enable Lazy Initialization](https://github.com/grails/grails-core/issues/12500)
* [Grails Config & Application Class](https://docs.grails.org/latest/guide/conf.html)
* [Grails and Spring](https://docs.grails.org/latest/guide/spring.html)
* [Spring Framework Doc - Generating an Index of Candidate Components](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-scanning-index)
* [spring-framework#25886 - Use spring.components only for JARs that define an index](https://github.com/spring-projects/spring-framework/issues/25886)
* [Lazy Initialization in Spring Boot 2.2](https://spring.io/blog/2019/03/14/lazy-initialization-in-spring-boot-2-2)
* [Spring Boot Docker](https://spring.io/guides/topicals/spring-boot-docker/)
* [Spring Boot Docker](https://spring.io/blog/2015/12/10/spring-boot-memory-performance)
* [How Fast is Spring?](https://spring.io/blog/2018/12/12/how-fast-is-spring)
* [dsyer/spring-boot-allocations](https://github.com/dsyer/spring-boot-allocations)
* [dsyer/spring-boot-micro-apps](https://github.com/dsyer/spring-boot-micro-apps)
* [Application Startup Tracking](https://docs.spring.io/spring-framework/docs/5.3.x/reference/html/core.html#context-functionality-startup)
* [Application Startup Steps](https://docs.spring.io/spring-framework/docs/5.3.x/reference/html/core.html#application-startup-steps)
* [lwaddicor/spring-startup-analysis](https://github.com/lwaddicor/spring-startup-analysis)