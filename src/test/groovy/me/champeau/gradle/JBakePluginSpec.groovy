package me.champeau.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Created by frank on 03.10.14.
 */
class JBakePluginSpec extends Specification {

    public static final String PLUGIN_ID = 'me.champeau.jbake'
    Project project

    def setup(){
        project = ProjectBuilder.builder().build()
        project.apply plugin: PLUGIN_ID
    }


    def "should add a JBakeTask"(){

        expect:
        project.tasks.jbake instanceof JBakeTask
    }

    def "should add jbake configuration"(){

        expect:
        project.configurations.jbake
    }

    def "should define default jbake version"(){

        expect:
        project.jbake.version != null
    }

    @Unroll
    def "should add dependency #name #version"(){

        when:
        project.evaluate()

        then:
        project.configurations.jbake.dependencies.find {
            it.name == name && it.version == version
        }

        where:
        group             | name                           | version
        'org.jbake'       | 'jbake-core'                   | '2.3.0'
        'org.freemarker'  | 'freemarker'                   | '2.3.19'
        'org.pegdown'     | 'pegdown'                      | '1.4.2'
        'org.asciidoctor' | 'asciidoctor-java-integration' | '0.1.4'

    }

    def "set dependency version by extension"(){

        given:
        project.jbake.version = '2.3.2'

        when:
        project.evaluate()

        then:
        project.configurations.jbake.dependencies.find {
            it.name == 'jbake-core' && it.version == '2.3.2'
        }

    }

    def "switch to asciidoctorj if version > 2.3.0"(){

        given:
        project.jbake.version = '2.3.1'

        when:
        project.evaluate()

        then:
        project.configurations.jbake.dependencies.find {
            it.group == 'org.asciidoctor' &&
            it.name == 'asciidoctorj' &&
            it.version == '1.5.1'
        }
    }

}
