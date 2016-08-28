package me.champeau.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class JBakePreviewTask extends DefaultTask {

    JBakePreviewTask() {
        group = 'Documentation'
        description = 'Serves up the jbake content and opens a browser'
    }

    @TaskAction
    void jbakePreview() {

    }
}
