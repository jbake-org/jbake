package me.champeau.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Created by frank on 03.10.14.
 */
class JBakePluginTest extends Specification {


    @Unroll
    def "should have a Task called #taskName"(){

        given:

        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'me.champeau.jbake'

        expect:
        project.tasks."${taskName}"

        where:
        taskName << ['jbake', 'clean']
    }

}
