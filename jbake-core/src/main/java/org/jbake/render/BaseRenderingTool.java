package org.jbake.render;

public abstract class BaseRenderingTool implements RenderingTool
{
    @Override
    public boolean isRendersInPlace()
    {
        return false; // Most renderers put the result elsewhere.
    }
}
