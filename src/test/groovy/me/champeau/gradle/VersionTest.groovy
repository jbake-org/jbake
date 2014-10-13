package me.champeau.gradle

import spock.lang.Specification
import spock.lang.Unroll

/**
 * Created by frank on 13.10.14.
 */
class VersionTest extends Specification {

    def "fill by string"(){
        given:
        Version version = new Version("2.3.0")

        expect:
        version.major == 2
        version.minor == 3
        version.bugfix == 0
    }

    def "ignore bugfix if not present"(){
        given:
        Version version = new Version("2.0")

        expect:
        version.major == 2
        version.minor == 0
        version.bugfix == 0
    }


    def "should be equal"(){
        given:
        Version one = new Version("1.1.1")
        Version two = new Version("1.1.1")

        expect:
        two == one

    }

    @Unroll
    def "#versionOne should be less than #versionTwo"(){

        given:
        Version one = new Version(versionOne)
        Version two = new Version(versionTwo)

        expect:

        one < two

        where:
        versionOne | versionTwo
        "1.0.0"    | "1.0.1"
        "1.0.0"    | "1.1.0"
        "1.0.0"    | "1.1.1"
        "0.0.0"    | "0.0.1"
        "10.0.12"  | "11.11.11"
        "10.11.10" | "10.12.10"
    }

    @Unroll
    def "#versionTwo should be bigger than #versionOne"(){

        given:
        Version one = new Version(versionTwo)
        Version two = new Version(versionOne)

        expect:

        one > two

        where:
        versionOne | versionTwo
        "1.0.0"    | "1.0.1"
        "1.0.0"    | "1.1.0"
        "1.0.0"    | "1.1.1"
        "0.0.0"    | "0.0.1"
        "10.0.12"  | "11.11.11"
        "10.11.10" | "10.12.10"
    }

}
