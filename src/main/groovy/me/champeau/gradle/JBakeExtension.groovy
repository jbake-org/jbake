package me.champeau.gradle

/**
 * Created by frank on 12.10.14.
 */
class JBakeExtension {

    String version = '2.3.2'
    String pegdownVersion = '1.4.2'
    String freemarkerVersion = '2.3.19'
    String asciidoctorJavaIntegrationVersion = '0.1.4'
    String asciidoctorjVersion = '1.5.1'
    String srcDirName = 'src/jbake'
    String destDirName = 'jbake'
    boolean clearCache = false
    Map<String, Object> configuration = [:]

}
