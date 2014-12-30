package me.champeau.gradle

/**
 * Created by frank on 13.10.14.
 */
class Version implements Comparable {

    Integer major
    Integer minor
    Integer bugfix

    Version(String version) {
        def tokens = version.tokenize('.')

        this.major = (tokens.size>=1)?tokens.get(0).toInteger():0
        this.minor = (tokens.size>=2)?tokens.get(1)?.toInteger():0
        this.bugfix = (tokens.size>=3)?tokens.get(2)?.toInteger():0
    }

    @Override
    int compareTo(Object other) {

        def ret = 0

        if ( this.major == other.major && this.minor == other.minor && this.bugfix == other.bugfix ){
            return 0
        }

        if ( this.major <= other.major ) {
            if ( this.minor < other.minor ) {
                ret = -1
            }
            if ( this.bugfix < other.bugfix ) {
                ret = -1
            }
        }

        if ( this.major >= other.major ) {
            if ( this.minor > other.minor ) {
                ret = 1
            }
            if ( this.bugfix > other.bugfix ) {
                ret = 1
            }
        }

        return ret

    }

}
