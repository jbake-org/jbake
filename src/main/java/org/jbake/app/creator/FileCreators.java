package org.jbake.app.creator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileCreators {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileCreators.class);

    public static void create(String fileName) {
        MarkdownFileCreator markdownFileCreator = new MarkdownFileCreator(new Scanner(System.in));

        if (markdownFileCreator.accepts(fileName)) {
            List<String> metadataLines = markdownFileCreator.obtainMetadata();
            File newFile = new File(fileName);

            FileWriter writer = null;
            try {
                writer = new FileWriter(newFile);

                for (String metadataLine : metadataLines) {
                    writer.write(metadataLine);
                    writer.write("\n");
                }

                writer.write(markdownFileCreator.getSeparator());
                writer.write("\n\n");
            } catch (IOException e) {
                LOGGER.error("Could not create a new file", e);
            } finally {
                IOUtils.closeQuietly(writer);
            }
        }
    }
}
