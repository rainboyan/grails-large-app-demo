package org.grails.demo

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
// import grails.boot.config.tools.ProfilingGrailsApplicationPostProcessor
import groovy.transform.CompileStatic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan

@CompileStatic
@ComponentScan
class Application extends GrailsAutoConfiguration {
    static void main(String[] args) {
        def now = System.currentTimeMillis()

        GrailsApp.run(Application, args)

        println "${System.currentTimeMillis()-now}ms"
    }

    // @Bean
    // ProfilingGrailsApplicationPostProcessor grailsApplicationPostProcessor() {
    //     return new ProfilingGrailsApplicationPostProcessor( this, applicationContext, classes() as Class[])
    // }
}