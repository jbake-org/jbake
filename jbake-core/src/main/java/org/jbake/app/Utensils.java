package org.jbake.app;

import org.jbake.app.configuration.JBakeConfiguration;

/**
 * A helper class to wrap all the utensils that are needed to bake.
 */
public class Utensils {
    private JBakeConfiguration configuration;
    private ContentStore contentStore;
    private Crawler crawler;
    private Renderer renderer;
    private Asset asset;

    public JBakeConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(JBakeConfiguration configuration) {
        this.configuration = configuration;
    }

    public ContentStore getContentStore() {
        return contentStore;
    }

    public void setContentStore(ContentStore contentStore) {
        this.contentStore = contentStore;
    }

    public Crawler getCrawler() {
        return crawler;
    }

    public void setCrawler(Crawler crawler) {
        this.crawler = crawler;
    }

    public Renderer getRenderer() {
        return renderer;
    }

    public void setRenderer(Renderer renderer) {
        this.renderer = renderer;
    }

    public Asset getAsset() {
        return asset;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
    }
}

