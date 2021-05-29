package org.jbake.app.configuration;

import ch.qos.logback.classic.spi.LoggingEvent;
import org.jbake.TestUtils;
import org.jbake.app.LoggingTest;
import org.jbake.exception.JBakeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JBakeConfigurationInspectorTest extends LoggingTest {

    private Path folder;

    @BeforeEach
    public void setup(@TempDir Path folder) {
        this.folder = folder;
    }


    @Test
    public void shouldThrowExceptionIfSourceFolderDoesNotExist() throws Exception {
        File nonExistentFile = new File(folder.toFile(), "nofolder");
        JBakeConfiguration configuration = mock(JBakeConfiguration.class);
        when(configuration.getSourceFolder()).thenReturn(nonExistentFile);

        JBakeConfigurationInspector inspector = new JBakeConfigurationInspector(configuration);

        JBakeException e = assertThrows(JBakeException.class, inspector::inspect);

        assertThat(e.getMessage()).isEqualTo("Error: Source folder must exist: " + nonExistentFile.getAbsolutePath());
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

        JBakeException e = assertThrows(JBakeException.class, inspector::inspect);

        assertThat(e.getMessage()).isEqualTo("Error: Source folder is not readable: " + nonReadableFile.getAbsolutePath());
    }

    @Test
    public void shouldThrowExceptionIfTemplateFolderDoesNotExist() throws Exception {
        String templateFolderName = "template/custom";
        File expectedFolder = new File(folder.toFile(), templateFolderName);
        JBakeConfiguration configuration = mock(JBakeConfiguration.class);
        when(configuration.getSourceFolder()).thenReturn(folder.toFile());
        when(configuration.getTemplateFolder()).thenReturn(expectedFolder);

        JBakeConfigurationInspector inspector = new JBakeConfigurationInspector(configuration);

        JBakeException e = assertThrows(JBakeException.class, inspector::inspect);

        assertThat(e.getMessage()).isEqualTo("Error: Required folder cannot be found! Expected to find [template.folder] at: " + expectedFolder.getAbsolutePath());
    }

    @Test
    public void shouldThrowExceptionIfContentFolderDoesNotExist() throws Exception {
        String contentFolderName = "content";
        String templateFolderName = "template";
        File templateFolder = TestUtils.newFolder(folder.toFile(), templateFolderName);
        File contentFolder = new File(folder.toFile(), contentFolderName);

        JBakeConfiguration configuration = mock(JBakeConfiguration.class);
        when(configuration.getSourceFolder()).thenReturn(folder.toFile());
        when(configuration.getTemplateFolder()).thenReturn(templateFolder);
        when(configuration.getContentFolder()).thenReturn(contentFolder);

        JBakeConfigurationInspector inspector = new JBakeConfigurationInspector(configuration);


        JBakeException e = assertThrows(JBakeException.class, inspector::inspect);

        assertThat(e.getMessage()).isEqualTo("Error: Required folder cannot be found! Expected to find [content.folder] at: " + contentFolder.getAbsolutePath());
    }

    @Test
    public void shouldCreateDestinationFolderIfNotExists() throws Exception {
        String contentFolderName = "content";
        String templateFolderName = "template";
        String destinationFolderName = "output";

        File templateFolder = TestUtils.newFolder(folder.toFile(), templateFolderName);
        File contentFolder = TestUtils.newFolder(folder.toFile(), contentFolderName);
        File destinationFolder = new File(folder.toFile(), destinationFolderName);

        JBakeConfiguration configuration = mock(JBakeConfiguration.class);
        when(configuration.getSourceFolder()).thenReturn(folder.toFile());
        when(configuration.getTemplateFolder()).thenReturn(templateFolder);
        when(configuration.getContentFolder()).thenReturn(contentFolder);
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

        File templateFolder = TestUtils.newFolder(folder.toFile(), templateFolderName);
        File contentFolder = TestUtils.newFolder(folder.toFile(), contentFolderName);
        File destinationFolder = mock(File.class);
        when(destinationFolder.exists()).thenReturn(true);

        JBakeConfiguration configuration = mock(JBakeConfiguration.class);
        when(configuration.getSourceFolder()).thenReturn(folder.toFile());
        when(configuration.getTemplateFolder()).thenReturn(templateFolder);
        when(configuration.getContentFolder()).thenReturn(contentFolder);
        when(configuration.getDestinationFolder()).thenReturn(destinationFolder);

        JBakeConfigurationInspector inspector = new JBakeConfigurationInspector(configuration);

        JBakeException e = assertThrows(JBakeException.class, inspector::inspect);

        assertThat(e.getMessage()).contains("Error: Destination folder is not writable:");
    }

    @Test
    public void shouldLogWarningIfAssetFolderDoesNotExist() throws Exception {
        String contentFolderName = "content";
        String templateFolderName = "template";
        String destinationFolderName = "output";
        String assetFolderName = "assets";

        File templateFolder = TestUtils.newFolder(folder.toFile(), templateFolderName);
        File contentFolder = TestUtils.newFolder(folder.toFile(), contentFolderName);
        File destinationFolder = TestUtils.newFolder(folder.toFile(), destinationFolderName);
        File assetFolder = new File(folder.toFile(), assetFolderName);

        JBakeConfiguration configuration = mock(JBakeConfiguration.class);
        when(configuration.getSourceFolder()).thenReturn(folder.toFile());
        when(configuration.getTemplateFolder()).thenReturn(templateFolder);
        when(configuration.getContentFolder()).thenReturn(contentFolder);
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
