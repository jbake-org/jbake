package me.champeau.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
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
        'org.jbake'       | 'jbake-core'                   | '2.3.2'
        'org.freemarker'  | 'freemarker'                   | '2.3.19'
        'org.pegdown'     | 'pegdown'                      | '1.4.2'
        'org.asciidoctor' | 'asciidoctorj'                 | '1.5.1'

    }

    def "set dependency version by extension"(){

        given:
        project.jbake.version = '2.3.0'

        when:
        project.evaluate()

        then:
        project.configurations.jbake.dependencies.find {
            it.name == 'jbake-core' && it.version == '2.3.0'
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

    def "use asciidoctor-java-integration if version < 2.3.1"(){

        given:
        project.jbake.version = '2.3.0'

        when:
        project.evaluate()

        then:
        project.configurations.jbake.dependencies.find {
            it.group == 'org.asciidoctor' &&
            it.name == 'asciidoctor-java-integration' &&
            it.version == '0.1.4'
        }

    }

    def "input dir should be configured by extension"(){
        given:
        def srcDirName = "src/jbake-project"
        def expectedFile = project.file("$project.rootDir/$srcDirName")

        when:
        project.jbake.srcDirName = srcDirName

        then:
        project.tasks.jbake.input == expectedFile
    }

    def "output dir should be configured by extension"(){
        given:
        def destDirName = "jbake-out"
        def expectedFile = project.file("$project.buildDir/$destDirName")

        when:
        project.jbake.destDirName = destDirName

        then:
        project.tasks.jbake.output == expectedFile
    }

    def "clearcache should be configured by extension"(){
        given:
        def clearCache = true

        when:
        project.jbake.clearCache = clearCache

        then:
        project.tasks.jbake.clearCache == clearCache
    }

    def "should be configurable by extension"(){
        given:
        def configuration = [:]
        configuration['render.tags'] = false

        when:
        project.jbake.configuration = configuration

        then:
        project.tasks.jbake.configuration['render.tags'] == false
    }

}
