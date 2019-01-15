package org.jbake.app;

import org.jbake.TestUtils;
import org.jbake.app.configuration.ConfigUtil;
import org.jbake.app.configuration.DefaultJBakeConfiguration;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * Created by frank on 28.03.16.
 */
public class FileUtilTest {

    @Test
    public void testGetRunningLocation() throws Exception {

        File path = FileUtil.getRunningLocation();
        assertEquals(new File("build/classes").getAbsolutePath(), path.getPath());
    }

    @Test
    public void testGetContentRoothPath() throws Exception {

        File source = TestUtils.getTestResourcesAsSourceFolder();
        ConfigUtil util = new ConfigUtil();
        DefaultJBakeConfiguration config = (DefaultJBakeConfiguration) util.loadConfig(source);

        String path = FileUtil.getUriPathToContentRoot(config, new File(config.getContentFolder(), "index.html"));
        assertThat(path).isEqualTo("");

        path = FileUtil.getUriPathToContentRoot(config, new File(config.getContentFolder(), "/blog/index.html"));
        assertThat(path).isEqualTo("../");

        path = FileUtil.getUriPathToContentRoot(config, new File(config.getContentFolder(), "/blog/level2/index.html"));
        assertThat(path).isEqualTo("../../");
    }

}
