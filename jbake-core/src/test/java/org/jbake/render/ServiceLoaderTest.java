package org.jbake.render;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceLoaderTest {

    @Test
    void testLoadRenderer() throws Exception {

        URL serviceDescription = ClassLoader.getSystemClassLoader().getResource("META-INF/services/org.jbake.render.RenderingTool");
        File services = new File(serviceDescription.toURI());
        assertTrue(services.exists(), "Service definitions File exists");

        FileReader fileReader = new FileReader(services);
        BufferedReader reader = new BufferedReader(fileReader);

        String serviceProvider;
        List<String> renderingToolClasses = new ArrayList<String>();

        for (RenderingTool tool : ServiceLoader.load(RenderingTool.class)) {
            renderingToolClasses.add(tool.getClass().getName());
        }

        while ((serviceProvider = reader.readLine()) != null) {
            assertTrue(renderingToolClasses.contains(serviceProvider), "Rendering tool " + serviceProvider + " loaded");
        }
    }
}
