package org.jbake.app.configuration;

import ch.qos.logback.classic.spi.LoggingEvent;
import org.apache.commons.io.FileUtils;
import org.jbake.TestUtils;
import org.jbake.app.JBakeException;
import org.jbake.app.LoggingTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jbake.TestUtils.getTestResourcesAsSourceFolder;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConfigUtilTest extends LoggingTest {

    private Path sourceFolder;
    private ConfigUtil util;

    @BeforeEach
    public void setup(@TempDir Path folder) {
        this.sourceFolder = folder;
        this.util = new ConfigUtil();
    }

    @Test
    public void shouldLoadSiteHost() throws Exception {
        JBakeConfiguration config = util.loadConfig(TestUtils.getTestResourcesAsSourceFolder());
        assertThat(config.getSiteHost()).isEqualTo("http://www.jbake.org");
    }

    @Test
    public void shouldLoadADefaultConfiguration() throws Exception {
        JBakeConfiguration config = util.loadConfig(TestUtils.getTestResourcesAsSourceFolder());
        assertDefaultPropertiesPresent(config);
    }

    @Test
    public void shouldLoadACustomConfiguration() throws Exception {
        File customConfigFile = new File(sourceFolder.toFile(), "jbake.properties");

        BufferedWriter writer = new BufferedWriter(new FileWriter(customConfigFile));
        writer.append("test.property=12345");
        writer.close();

        JBakeConfiguration configuration = util.loadConfig(sourceFolder.toFile());

        assertThat(configuration.get("test.property")).isEqualTo("12345");
        assertDefaultPropertiesPresent(configuration);
    }

    @Test
    public void shouldThrowAnExceptionIfSourcefolderDoesNotExist() throws Exception {
        File nonExistentSourceFolder = mock(File.class);
        when(nonExistentSourceFolder.getAbsolutePath()).thenReturn("/tmp/nonexistent");
        when(nonExistentSourceFolder.exists()).thenReturn(false);

        JBakeException e = assertThrows(JBakeException.class, () -> util.loadConfig(nonExistentSourceFolder));
        assertThat(e.getMessage()).isEqualTo("The given source folder '/tmp/nonexistent' does not exist.");
    }

    @Test
    public void shouldAddSourcefolderToConfiguration() throws Exception {

        File sourceFolder = TestUtils.getTestResourcesAsSourceFolder();
        JBakeConfiguration config = util.loadConfig(sourceFolder);

        assertThat(config.getSourceFolder()).isEqualTo(sourceFolder);
    }

    @Test
    public void shouldThrowAnExceptionIfSourcefolderIsNotADirectory() throws Exception {

        File sourceFolder = mock(File.class);
        when(sourceFolder.exists()).thenReturn(true);
        when(sourceFolder.isDirectory()).thenReturn(false);

        JBakeException e = assertThrows(JBakeException.class, () -> util.loadConfig(sourceFolder));
        assertThat(e.getMessage()).isEqualTo("The given source folder is not a directory.");
    }

    @Test
    public void shouldReturnDestinationFolderFromConfiguration() throws Exception {
        File sourceFolder = TestUtils.getTestResourcesAsSourceFolder();
        File expectedDestinationFolder = new File(sourceFolder, "output");
        JBakeConfiguration config = util.loadConfig(sourceFolder);

        assertThat(config.getDestinationFolder()).isEqualTo(expectedDestinationFolder);
    }

    @Test
    public void shouldReturnAssetFolderFromConfiguration() throws Exception {
        File sourceFolder = TestUtils.getTestResourcesAsSourceFolder();
        File expectedDestinationFolder = new File(sourceFolder, "assets");
        JBakeConfiguration config = util.loadConfig(sourceFolder);

        assertThat(config.getAssetFolder()).isEqualTo(expectedDestinationFolder);
    }

    @Test
    public void shouldReturnTemplateFolderFromConfiguration() throws Exception {
        File sourceFolder = TestUtils.getTestResourcesAsSourceFolder();
        File expectedDestinationFolder = new File(sourceFolder, "templates");
        JBakeConfiguration config = util.loadConfig(sourceFolder);

        assertThat(config.getTemplateFolder()).isEqualTo(expectedDestinationFolder);
    }

    @Test
    public void shouldReturnContentFolderFromConfiguration() throws Exception {
        File sourceFolder = TestUtils.getTestResourcesAsSourceFolder();
        File expectedDestinationFolder = new File(sourceFolder, "content");
        JBakeConfiguration config = util.loadConfig(sourceFolder);

        assertThat(config.getContentFolder()).isEqualTo(expectedDestinationFolder);
    }

    @Test
    public void shouldGetTemplateFileDoctype() throws Exception {
        File sourceFolder = TestUtils.getTestResourcesAsSourceFolder();
        File expectedTemplateFile = new File(sourceFolder, "templates/index.ftl");
        JBakeConfiguration config = util.loadConfig(sourceFolder);

        File templateFile = config.getTemplateFileByDocType("masterindex");

        assertThat(templateFile).isEqualTo(expectedTemplateFile);
    }

    @Test
    public void shouldLogWarningIfDocumentTypeNotFound() throws Exception {
        File sourceFolder = TestUtils.getTestResourcesAsSourceFolder();
        JBakeConfiguration config = util.loadConfig(sourceFolder);

        config.getTemplateFileByDocType("none");

        verify(mockAppender, times(1)).doAppend(captorLoggingEvent.capture());

        LoggingEvent loggingEvent = captorLoggingEvent.getValue();

        assertThat(loggingEvent.getMessage()).isEqualTo("Cannot find configuration key '{}' for document type '{}'");

    }

    @Test
    public void shouldGetTemplateOutputExtension() throws Exception {

        String docType = "masterindex";
        File sourceFolder = TestUtils.getTestResourcesAsSourceFolder();
        DefaultJBakeConfiguration config = (DefaultJBakeConfiguration) util.loadConfig(sourceFolder);
        config.setTemplateExtensionForDocType(docType, ".xhtml");

        String extension = config.getOutputExtensionByDocType(docType);

        assertThat(extension).isEqualTo(".xhtml");
    }

    @Test
    public void shouldGetMarkdownExtensionsAsList() throws Exception {
        File sourceFolder = TestUtils.getTestResourcesAsSourceFolder();
        DefaultJBakeConfiguration config = (DefaultJBakeConfiguration) util.loadConfig(sourceFolder);

        List<String> markdownExtensions = config.getMarkdownExtensions();

        assertThat(markdownExtensions).containsExactly("HARDWRAPS", "AUTOLINKS", "FENCED_CODE_BLOCKS", "DEFINITIONS");
    }

    @Test
    public void shouldReturnConfiguredDocTypes() throws Exception {

        File sourceFolder = TestUtils.getTestResourcesAsSourceFolder();
        DefaultJBakeConfiguration config = (DefaultJBakeConfiguration) util.loadConfig(sourceFolder);

        List<String> docTypes = config.getDocumentTypes();

        assertThat(docTypes).containsExactly("allcontent", "masterindex", "feed", "archive", "tag", "tagsindex", "sitemap", "post", "page");

    }

    @Test
    public void shouldReturnAListOfAsciidoctorOptionsKeys() throws Exception {
        File sourceFolder = TestUtils.getTestResourcesAsSourceFolder();
        DefaultJBakeConfiguration config = (DefaultJBakeConfiguration) util.loadConfig(sourceFolder);
        config.setProperty("asciidoctor.option.requires", "asciidoctor-diagram");
        config.setProperty("asciidoctor.option.template_dirs", "src/template1,src/template2");

        List<String> options = config.getAsciidoctorOptionKeys();

        assertThat(options).contains("requires", "template_dirs");
    }

    @Test
    public void shouldReturnAnAsciidoctorOption() throws Exception {
        File sourceFolder = TestUtils.getTestResourcesAsSourceFolder();
        DefaultJBakeConfiguration config = (DefaultJBakeConfiguration) util.loadConfig(sourceFolder);
        config.setProperty("asciidoctor.option.requires", "asciidoctor-diagram");
        config.setProperty("asciidoctor.option.template_dirs", "src/template1,src/template2");

        Object option = config.getAsciidoctorOption("requires");

        assertThat(String.valueOf(option)).contains("asciidoctor-diagram");
    }

    @Test
    public void shouldReturnAnAsciidoctorOptionWithAListValue() throws Exception {
        File sourceFolder = TestUtils.getTestResourcesAsSourceFolder();
        DefaultJBakeConfiguration config = (DefaultJBakeConfiguration) util.loadConfig(sourceFolder);
        config.setProperty("asciidoctor.option.requires", "asciidoctor-diagram");
        config.setProperty("asciidoctor.option.template_dirs", "src/template1,src/template2");

        Object option = config.getAsciidoctorOption("template_dirs");

        assertTrue(option instanceof List);
        assertThat((List<String>) option).contains("src/template1", "src/template2");
    }

    @Test
    public void shouldReturnEmptyStringIfOptionNotAvailable() throws Exception {
        File sourceFolder = TestUtils.getTestResourcesAsSourceFolder();
        DefaultJBakeConfiguration config = (DefaultJBakeConfiguration) util.loadConfig(sourceFolder);

        Object option = config.getAsciidoctorOption("template_dirs");

        assertThat(String.valueOf(option)).isEmpty();
    }

    @Test
    public void shouldLogAWarningIfAsciidocOptionCouldNotBeFound() throws Exception {
        File sourceFolder = TestUtils.getTestResourcesAsSourceFolder();
        DefaultJBakeConfiguration config = (DefaultJBakeConfiguration) util.loadConfig(sourceFolder);

        config.getAsciidoctorOption("template_dirs");

        verify(mockAppender, times(1)).doAppend(captorLoggingEvent.capture());

        LoggingEvent loggingEvent = captorLoggingEvent.getValue();

        assertThat(loggingEvent.getMessage()).isEqualTo("Cannot find asciidoctor option '{}.{}'");
    }

    @Test
    public void shouldHandleNonExistingFiles() throws Exception {

        File source = TestUtils.getTestResourcesAsSourceFolder();
        File expectedTemplateFolder = new File(source, "templates");
        File expectedAssetFolder = new File(source, "assets");
        File expectedContentFolder = new File(source, "content");
        File expectedDestinationFolder = new File(source, "output");
        DefaultJBakeConfiguration config = (DefaultJBakeConfiguration) util.loadConfig(source);

        config.setTemplateFolder(null);
        config.setAssetFolder(null);
        config.setContentFolder(null);
        config.setDestinationFolder(null);

        File templateFolder = config.getTemplateFolder();
        File assetFolder = config.getAssetFolder();
        File contentFolder = config.getContentFolder();
        File destinationFolder = config.getDestinationFolder();

        assertThat(templateFolder).isEqualTo(expectedTemplateFolder);
        assertThat(assetFolder).isEqualTo(expectedAssetFolder);
        assertThat(contentFolder).isEqualTo(expectedContentFolder);
        assertThat(destinationFolder).isEqualTo(expectedDestinationFolder);
    }

    @Test
    void shouldSetCustomFoldersWithAbsolutePaths() throws Exception {
        // given
        Path source = sourceFolder.resolve("source");
        Path theme = sourceFolder.resolve("theme");
        Path destination = sourceFolder.resolve("destination");

        File originalSource = TestUtils.getTestResourcesAsSourceFolder();
        FileUtils.copyDirectory(originalSource, source.toFile());
        File originalTheme = TestUtils.getTestResourcesAsSourceFolder("/fixture-theme");
        FileUtils.copyDirectory(originalTheme, theme.toFile());

        Path expectedTemplateFolder = theme.resolve("templates");
        Path expectedAssetFolder = theme.resolve("assets");
        Path expectedContentFolder = source.resolve("content");
        Path expectedDestination = destination.resolve("output");

        File properties = source.resolve("jbake.properties").toFile();
        BufferedWriter fw = Files.newBufferedWriter(properties.toPath());

        fw.write(PropertyList.ASSET_FOLDER.getKey() + "=" + TestUtils.getOsPath(expectedAssetFolder));
        fw.newLine();
        fw.write(PropertyList.TEMPLATE_FOLDER.getKey() + "=" + TestUtils.getOsPath(expectedTemplateFolder));
        fw.newLine();
        fw.write(PropertyList.DESTINATION_FOLDER.getKey() + "=" + TestUtils.getOsPath(expectedDestination));
        fw.close();

        // when

        DefaultJBakeConfiguration config = (DefaultJBakeConfiguration) util.loadConfig(source.toFile());

        File templateFolder = config.getTemplateFolder();
        File assetFolder = config.getAssetFolder();
        File contentFolder = config.getContentFolder();
        File destinationFolder = config.getDestinationFolder();

        // then
        assertThat(config.getTemplateFolderName()).isEqualTo(expectedTemplateFolder.toString());
        assertThat(templateFolder).isEqualTo(expectedTemplateFolder.toFile());

        assertThat(config.getAssetFolderName()).isEqualTo(expectedAssetFolder.toString());
        assertThat(assetFolder).isEqualTo(expectedAssetFolder.toFile());

        assertThat(destinationFolder).isEqualTo(expectedDestination.toFile());
        assertThat(contentFolder).isEqualTo(expectedContentFolder.toFile());
    }

    @Test
    public void shouldReturnIgnoreFileFromConfiguration() throws Exception {
        File sourceFolder = TestUtils.getTestResourcesAsSourceFolder();
        JBakeConfiguration config = util.loadConfig(sourceFolder);

        assertThat(config.getIgnoreFileName()).isEqualTo(".jbakeignore");
    }

    private void assertDefaultPropertiesPresent(JBakeConfiguration config) throws IllegalAccessException {
        for (Field field : JBakeConfiguration.class.getFields()) {

            if (field.isAccessible()) {
                String key = (String) field.get("");
                System.out.println("Key: " + key);
                assertThat(config.get(key)).isNotNull();
            }
        }
    }

}
