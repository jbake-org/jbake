package me.champeau.gradle

/**
 * Created by frank on 12.10.14.
 */
public interface JBakeProxy {

    def jbake()
    def prepare()
    def getConfig()
    def setConfig(Object config)
}