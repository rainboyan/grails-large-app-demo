/*===============================================================================================
 *         Create  Bean & Config use Groovy
 *==============================================================================================*/
(1..2000).each {
    def writer = new StringWriter()

    writer << """
package org.grails.demo

class Demo${it}Bean {
    String name
}
"""

    new File('./src/main/groovy/org/grails/demo', "Demo${it}Bean.groovy") << writer.toString()

    writer = new StringWriter()

    writer << """
package org.grails.demo

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import groovy.transform.CompileStatic

@CompileStatic
@Configuration
class Demo${it}Config {
    @Bean
    def demo${it}Bean() {
        new Demo${it}Bean(name: 'demo')
    }
}
"""

    new File('./src/main/groovy/org/grails/demo', "Demo${it}Config.groovy") << writer.toString()
}

//== Use resources.groovy
/*
new File('./grails-app/conf/spring', "resources.groovy").withWriter { writer ->
    writer << """
import org.grails.demo.*

beans = {
"""

    (1..2000).each {
        writer << "demo${it}Bean(Demo${it}Bean)\n"
    }

    writer << """
}
"""
}
*/

/*===============================================================================================
 *         Create  Bean & Config use Java
 *==============================================================================================*/
/*
(1..2000).each {
    def writer = new StringWriter()

    writer << """
package org.grails.demo;

public class Demo${it}Bean {
    private String name;

    public Demo${it}Bean(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
"""

    new File('./src/main/groovy/org/grails/demo', "Demo${it}Bean.java") << writer.toString()
    
    writer = new StringWriter()

    writer << """
package org.grails.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class Demo${it}Config {
    @Bean
    public Demo${it}Bean demo${it}Bean() {
        return new Demo${it}Bean("Demo ${it}");
    }
}
"""

    new File('./src/main/groovy/org/grails/demo', "Demo${it}Config.java") << writer.toString()
}
*/

/*===============================================================================================
 *         Create Grails Demo Domain and Services
 *==============================================================================================*/
(1..1000).each {
    // Domains
    def writer = new StringWriter()

    writer << """
package org.grails.demo

class Demo${it}Domain {
    String name
}
"""

    new File('./grails-app/domain/org/grails/demo', "Demo${it}Domain.groovy") << writer.toString()

    // Services
    writer = new StringWriter()

    writer << """
package org.grails.demo

import grails.gorm.services.Service

@Service(Demo${it}Domain)
interface Demo${it}DomainService {

    Demo${it}Domain get(Serializable id)

    List<Demo${it}Domain> list(Map args)

    Long count()

    void delete(Serializable id)

    Demo${it}Domain save(Demo${it}Domain demo${it}Domain)

}
"""

    new File('./grails-app/services/org/grails/demo', "Demo${it}DomainService.groovy") << writer.toString()

    // Controllers
    writer = new StringWriter()

    writer << """
package org.grails.demo

class Demo${it}Controller {
    def index(Integer max) {}
    def show(Long id) {}
    def create() {}
    def save() {}
    def edit(Long id) {}
    def update() {}
    def delete(Long id) {}
}
"""

    new File('./grails-app/controllers/org/grails/demo', "Demo${it}Controller.groovy") << writer.toString()

    // TagLibs
    writer = new StringWriter()

    writer << """
package org.grails.demo

class Demo${it}TagLib {

    def demo${it} = { attrs, body ->
        def name = attrs.name

        out << name
    }
}
"""

    new File('./grails-app/taglib/org/grails/demo', "Demo${it}TagLib.groovy") << writer.toString()
}