package org.jbake.app.configuration;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import org.jbake.app.JBakeException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class JBakeConfigurationInspectorTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Mock
    private Appender<ch.qos.logback.classic.spi.ILoggingEvent> mockAppender;

    @Captor
    private ArgumentCaptor<LoggingEvent> captorLoggingEvent;

    Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

    @Before
    public void setup() {

        root.addAppender(mockAppender);
        root.setLevel(Level.INFO);
    }

    @After
    public void teardown() {
        root.detachAppender(mockAppender);
    }

    @Test
    public void should_throw_exception_if_source_folder_does_not_exist() throws Exception {
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
    public void should_throw_exception_if_source_folder_is_not_readable() throws Exception {
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
    public void should_throw_exception_if_template_folder_does_not_exist() throws Exception {
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
    public void should_throw_exception_if_content_folder_does_not_exist() throws Exception {
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
    public void should_create_destination_folder_if_not_exists() throws Exception {
        String contentFolderName = "content";
        String templateFolderName = "template";
        String destinationFolderName = "output";

        File templateFolder = folder.newFolder(templateFolderName);
        File contentFolder = folder.newFolder(contentFolderName);
        File destinationFolder = new File(folder.getRoot(),destinationFolderName);
        File assetFolder = folder.newFolder("assets");

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
    public void should_throw_exception_if_destination_folder_not_writable() throws Exception {
        String contentFolderName = "content";
        String templateFolderName = "template";
        String destinationFolderName = "output";

        File templateFolder = folder.newFolder(templateFolderName);
        File contentFolder = folder.newFolder(contentFolderName);
        File destinationFolder = mock(File.class);
        when(destinationFolder.exists()).thenReturn(true);
//        when(destinationFolder.isDirectory()).thenReturn(true);
//        when(destinationFolder.canWrite()).thenReturn(false);

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
    public void should_log_warning_if_asset_folder_does_not_exist() throws Exception {
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
//        when(configuration.getAssetFolderName()).thenReturn(assetFolderName);


        JBakeConfigurationInspector inspector = new JBakeConfigurationInspector(configuration);

        inspector.inspect();

        verify(mockAppender, times(1)).doAppend(captorLoggingEvent.capture());

        LoggingEvent loggingEvent = captorLoggingEvent.getValue();

        assertThat(loggingEvent.getMessage()).isEqualTo("No asset folder '{}' was found!");
        assertThat(loggingEvent.getFormattedMessage()).isEqualTo("No asset folder '"+assetFolder.getAbsolutePath()+"' was found!");

    }


}