// Generate 2000 Spring Demo Bean and Config

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

// Generate 1000 Grails Demo Domain and Services
(1..1000).each {
    def writer = new StringWriter()

    writer << """
package org.grails.demo

class Demo${it}Domain {
    String name
}
"""

    new File('./grails-app/domain/org/grails/demo', "Demo${it}Domain.groovy") << writer.toString()

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
}
