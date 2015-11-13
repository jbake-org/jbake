package org.jbake.app.excerpt;

public enum CharacterEntityReference {

    lt('<'), gt('>'), amp('&'), quot('"', true);
    private final char    character;
    private final boolean conditional;

    private CharacterEntityReference(final char ref) {
        this(ref, false);
    }

    private CharacterEntityReference(final char ref, final boolean conditional) {
        this.character = ref;
        this.conditional = conditional;
    }

    public static CharacterEntityReference findByWithCondition(char ref, boolean condition) {
        for (CharacterEntityReference r : CharacterEntityReference.values()) {
            if (r.character == ref)
                return (!r.conditional || condition) ? r : null;
        }
        return null;
    }

}
