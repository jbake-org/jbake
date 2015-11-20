package org.jbake.render;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import static org.junit.Assert.assertTrue;

public class ServiceLoaderTest {

    @Test
    public void testLoadRenderer() throws Exception {

        URL serviceDescription = ClassLoader.getSystemClassLoader().getResource("META-INF/services/org.jbake.render.RenderingTool");
        File services = new File(serviceDescription.toURI());
        assertTrue("Service definitions File exists", services.exists());

        FileReader fileReader = new FileReader(services);
        BufferedReader reader = new BufferedReader(fileReader);

        String serviceProvider;
        List<String> renderingToolClasses = new ArrayList<String>();

        for (RenderingTool tool : ServiceLoader.load(RenderingTool.class)) {
            renderingToolClasses.add(tool.getClass().getName());
        }

        while ((serviceProvider = reader.readLine()) != null) {
            assertTrue("Rendering tool " + serviceProvider + " loaded", renderingToolClasses.contains(serviceProvider));
        }
    }
}
