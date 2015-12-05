package org.jbake.render.support;

import org.apache.commons.configuration.CompositeConfiguration;

public class MockCompositeConfiguration extends CompositeConfiguration {

    private boolean _bool = false;
    private String _string = "random string";

    public MockCompositeConfiguration withInnerBoolean(boolean bool) {
        _bool = bool;
        return this;
    }

    public MockCompositeConfiguration withInnerString(String string) {
        _string = string;
        return this;
    }

    @Override
    public boolean getBoolean(String key) {
        return _bool;
    }

    @Override
    public String getString(String string) {
        return _string;
    }
}