package me.champeau.gradle

import org.gradle.api.internal.AbstractTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
//import org.jbake.app.Watcher

class JBakeWatchTask extends AbstractTask {

    File input = new File("$project.projectDir/src/jbake")
    File output = new File("$project.buildDir/jbake")

    JBakeWatchTask() {
        group = 'jbake'
        description = 'Watches your source directory for changes and bakes whenever there are changes'
    }

    @TaskAction
    void jbakeWatch() {
        // no opt for now.
    //    new Watcher(input, output).with {
    //        watch();
    //    }
    }
}
