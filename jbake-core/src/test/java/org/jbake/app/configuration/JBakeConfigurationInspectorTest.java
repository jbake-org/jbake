package org.jbake.app.configuration;

import ch.qos.logback.classic.spi.LoggingEvent;
import org.jbake.app.JBakeException;
import org.jbake.app.LoggingTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JBakeConfigurationInspectorTest extends LoggingTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void shouldThrowExceptionIfSourceFolderDoesNotExist() throws Exception {
        File nonExistentFile = new File(folder.getRoot(), "nofolder");
        JBakeConfiguration configuration = mock(JBakeConfiguration.class);
        when(configuration.getSourceFolder()).thenReturn(nonExistentFile);

        JBakeConfigurationInspector inspector = new JBakeConfigurationInspector(configuration);

        try {
            inspector.inspect();
            fail("should throw a JBakeException");
        } catch (JBakeException e) {
            assertThat(e.getMessage()).isEqualTo("Error: Source folder must exist: " + nonExistentFile.getAbsolutePath());
        }
    }

    @Test
    public void shouldThrowExceptionIfSourceFolderIsNotReadable() throws Exception {
        File nonReadableFile = mock(File.class);
        when(nonReadableFile.exists()).thenReturn(true);
        when(nonReadableFile.isDirectory()).thenReturn(true);
        when(nonReadableFile.canRead()).thenReturn(false);

        JBakeConfiguration configuration = mock(JBakeConfiguration.class);
        when(configuration.getSourceFolder()).thenReturn(nonReadableFile);

        JBakeConfigurationInspector inspector = new JBakeConfigurationInspector(configuration);

        try {
            inspector.inspect();
            fail("should throw a JBakeException");
        } catch (JBakeException e) {
            assertThat(e.getMessage()).isEqualTo("Error: Source folder is not readable: " + nonReadableFile.getAbsolutePath());
        }
    }

    @Test
    public void shouldThrowExceptionIfTemplateFolderDoesNotExist() throws Exception {
        String templateFolderName = "template";
        File expectedFolder = new File(folder.getRoot(), templateFolderName);
        JBakeConfiguration configuration = mock(JBakeConfiguration.class);
        when(configuration.getSourceFolder()).thenReturn(folder.getRoot());
        when(configuration.getTemplateFolder()).thenReturn(expectedFolder);
        when(configuration.getTemplateFolderName()).thenReturn(templateFolderName);

        JBakeConfigurationInspector inspector = new JBakeConfigurationInspector(configuration);

        try {
            inspector.inspect();
            fail("should throw a JBakeException");
        } catch (JBakeException e) {
            assertThat(e.getMessage()).isEqualTo("Error: Required folder cannot be found! Expected to find [" + templateFolderName + "] at: " + expectedFolder.getAbsolutePath());
        }

    }

    @Test
    public void shouldThrowExceptionIfContentFolderDoesNotExist() throws Exception {
        String contentFolderName = "content";
        String templateFolderName = "template";
        File templateFolder = folder.newFolder(templateFolderName);
        File contentFolder = new File(folder.getRoot(), contentFolderName);

        JBakeConfiguration configuration = mock(JBakeConfiguration.class);
        when(configuration.getSourceFolder()).thenReturn(folder.getRoot());
        when(configuration.getTemplateFolder()).thenReturn(templateFolder);
        when(configuration.getTemplateFolderName()).thenReturn(templateFolderName);
        when(configuration.getContentFolder()).thenReturn(contentFolder);
        when(configuration.getContentFolderName()).thenReturn(contentFolderName);

        JBakeConfigurationInspector inspector = new JBakeConfigurationInspector(configuration);

        try {
            inspector.inspect();
            fail("should throw a JBakeException");
        } catch (JBakeException e) {
            assertThat(e.getMessage()).isEqualTo("Error: Required folder cannot be found! Expected to find [" + contentFolderName + "] at: " + contentFolder.getAbsolutePath());
        }
    }

    @Test
    public void shouldCreateDestinationFolderIfNotExists() throws Exception {
        String contentFolderName = "content";
        String templateFolderName = "template";
        String destinationFolderName = "output";

        File templateFolder = folder.newFolder(templateFolderName);
        File contentFolder = folder.newFolder(contentFolderName);
        File destinationFolder = new File(folder.getRoot(), destinationFolderName);

        JBakeConfiguration configuration = mock(JBakeConfiguration.class);
        when(configuration.getSourceFolder()).thenReturn(folder.getRoot());
        when(configuration.getTemplateFolder()).thenReturn(templateFolder);
        when(configuration.getTemplateFolderName()).thenReturn(templateFolderName);
        when(configuration.getContentFolder()).thenReturn(contentFolder);
        when(configuration.getContentFolderName()).thenReturn(contentFolderName);
        when(configuration.getDestinationFolder()).thenReturn(destinationFolder);
        when(configuration.getAssetFolder()).thenReturn(destinationFolder);

        JBakeConfigurationInspector inspector = new JBakeConfigurationInspector(configuration);

        inspector.inspect();

        assertThat(destinationFolder).exists();
    }

    @Test
    public void shouldThrowExceptionIfDestinationFolderNotWritable() throws Exception {
        String contentFolderName = "content";
        String templateFolderName = "template";

        File templateFolder = folder.newFolder(templateFolderName);
        File contentFolder = folder.newFolder(contentFolderName);
        File destinationFolder = mock(File.class);
        when(destinationFolder.exists()).thenReturn(true);

        JBakeConfiguration configuration = mock(JBakeConfiguration.class);
        when(configuration.getSourceFolder()).thenReturn(folder.getRoot());
        when(configuration.getTemplateFolder()).thenReturn(templateFolder);
        when(configuration.getTemplateFolderName()).thenReturn(templateFolderName);
        when(configuration.getContentFolder()).thenReturn(contentFolder);
        when(configuration.getContentFolderName()).thenReturn(contentFolderName);
        when(configuration.getDestinationFolder()).thenReturn(destinationFolder);

        JBakeConfigurationInspector inspector = new JBakeConfigurationInspector(configuration);

        try {
            inspector.inspect();
            fail("should throw JBakeException");
        } catch (JBakeException e) {
            assertThat(e.getMessage()).contains("Error: Destination folder is not writable:");
        }

    }

    @Test
    public void shouldLogWarningIfAssetFolderDoesNotExist() throws Exception {
        String contentFolderName = "content";
        String templateFolderName = "template";
        String destinationFolderName = "output";
        String assetFolderName = "assets";

        File templateFolder = folder.newFolder(templateFolderName);
        File contentFolder = folder.newFolder(contentFolderName);
        File destinationFolder = folder.newFolder(destinationFolderName);
        File assetFolder = new File(folder.getRoot(), assetFolderName);

        JBakeConfiguration configuration = mock(JBakeConfiguration.class);
        when(configuration.getSourceFolder()).thenReturn(folder.getRoot());
        when(configuration.getTemplateFolder()).thenReturn(templateFolder);
        when(configuration.getTemplateFolderName()).thenReturn(templateFolderName);
        when(configuration.getContentFolder()).thenReturn(contentFolder);
        when(configuration.getContentFolderName()).thenReturn(contentFolderName);
        when(configuration.getDestinationFolder()).thenReturn(destinationFolder);
        when(configuration.getAssetFolder()).thenReturn(assetFolder);


        JBakeConfigurationInspector inspector = new JBakeConfigurationInspector(configuration);

        inspector.inspect();

        verify(mockAppender, times(1)).doAppend(captorLoggingEvent.capture());

        LoggingEvent loggingEvent = captorLoggingEvent.getValue();

        assertThat(loggingEvent.getMessage()).isEqualTo("No asset folder '{}' was found!");
        assertThat(loggingEvent.getFormattedMessage()).isEqualTo("No asset folder '" + assetFolder.getAbsolutePath() + "' was found!");

    }


}