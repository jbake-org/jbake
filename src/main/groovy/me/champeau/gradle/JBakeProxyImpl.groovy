package me.champeau.gradle

import java.lang.reflect.Constructor


/**
 * Created by frank on 12.10.14.
 */
class JBakeProxyImpl implements JBakeProxy{

    Class delegate
    def input
    def output
    def clearCache

    def jbake() {

        Constructor constructor = delegate.getConstructor(File.class,File.class,boolean)

        def instance = constructor.newInstance(input, output, clearCache)
        instance.with {
            setupPaths()
            bake()
        }
    }
}
