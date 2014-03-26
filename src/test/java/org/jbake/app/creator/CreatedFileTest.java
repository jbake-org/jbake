package org.jbake.app.creator;

import org.junit.Assert;
import org.junit.Test;

public class CreatedFileTest {

    @Test
    public void provideDefaultValuesForTypeAndStatus() throws Exception {
        FileMetadata createdFile = new FileMetadata("", "");

        Assert.assertEquals("post", createdFile.getType());
        Assert.assertEquals("draft", createdFile.getStatus());
    }
}
