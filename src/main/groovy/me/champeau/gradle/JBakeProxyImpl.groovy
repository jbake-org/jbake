package me.champeau.gradle

import groovy.transform.Field
import org.apache.commons.configuration.CompositeConfiguration
import org.apache.commons.configuration.MapConfiguration

import java.lang.reflect.Constructor


/**
 * Created by frank on 12.10.14.
 */
class JBakeProxyImpl implements JBakeProxy{

    Class delegate
    def input
    def output
    def clearCache

    def jbake

    def jbake() {
        if(jbake) {
            jbake.bake()
        }
    }

    def prepare() {
        Constructor constructor = delegate.getConstructor(File.class,File.class,boolean)

        jbake = constructor.newInstance(input, output, clearCache)
        jbake.setupPaths()
    }

    def getConfig(){
        jbake.config
    }

    def setConfig(Object config) {
        jbake.config = config
    }
}
