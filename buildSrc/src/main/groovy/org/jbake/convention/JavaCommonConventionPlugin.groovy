package org.jbake.convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.JavaVersion
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test

class JavaCommonConventionPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.plugins.apply('java')
        //project.plugins.apply('jacoco')
        project.plugins.apply('checkstyle')

        project.repositories {
            mavenCentral()
        }

        project.dependencies {
            implementation "org.slf4j:slf4j-api:${project.slf4jVersion}"
            implementation "org.slf4j:jul-to-slf4j:${project.slf4jVersion}"
            implementation "org.slf4j:jcl-over-slf4j:${project.slf4jVersion}"
            implementation "ch.qos.logback:logback-classic:${project.logbackVersion}"
            implementation "ch.qos.logback:logback-core:${project.logbackVersion}"

            testImplementation "org.junit.jupiter:junit-jupiter-api:${project.junit5Version}"

            testImplementation "org.itsallcode:junit5-system-extensions:${project.junit5SystemExtVersion}"
        }

        project.tasks.withType(JavaCompile).configureEach {
            // Skip smokeTest source set as it only contains Kotlin sources
            if (it.name.contains('SmokeTest')) return
            sourceCompatibility = "$javaVersion"
            targetCompatibility = "$javaVersion"
        }

        //set jvm for all Test tasks (like test and smokeTest)
        project.tasks.withType(Test).configureEach {
            def args = ['-Xms512m', '-Xmx3g', '-Dorientdb.installCustomFormatter=false=false', '-Djna.nosys=true']

            /**
             * jdk9 build is unable to determine the amount of MaxDirectMemorySize
             * See https://pastebin.com/ECvQeHx0
             */
            if (JavaVersion.current().java9Compatible) {
                args << '-XX:MaxDirectMemorySize=2g'
            }
            jvmArgs args
        }

        project.tasks.register('javadocJar', Jar) {
            archiveClassifier.set('javadoc')
            from project.javadoc
        }

        project.tasks.register('sourcesJar', Jar) {
            archiveClassifier.set('sources')
            from project.sourceSets.main.allSource
        }

        project.tasks.withType(AbstractArchiveTask).configureEach {
            preserveFileTimestamps = false
            reproducibleFileOrder = true
        }

        project.test {
            useJUnitPlatform()

            testLogging {
                events "passed", "skipped", "failed"
                exceptionFormat "full"
            }
        }
    }
}
