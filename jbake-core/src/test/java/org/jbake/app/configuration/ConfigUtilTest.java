package org.jbake.app.configuration;

import org.jbake.app.JBakeException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConfigUtilTest {


    @Rule
    public TemporaryFolder sourceFolder = new TemporaryFolder();

    ConfigUtil util;

    @Before
    public void setUp() throws Exception {
        util = new ConfigUtil();
    }

    @Test
    public void should_load_a_default_configuration() throws Exception {
        JBakeConfiguration config = util.loadConfig(new File(this.getClass().getResource("/fixture").getFile()));
        assertDefaultPropertiesPresent(config);
    }

    @Test
    public void should_load_a_custom_configuration() throws Exception {
        File customConfigFile = sourceFolder.newFile("jbake.properties");

        BufferedWriter writer = new BufferedWriter(new FileWriter(customConfigFile));
        writer.append("test.property=12345");
        writer.close();

        JBakeConfiguration configuration = util.loadConfig(sourceFolder.getRoot());

        assertThat(configuration.get("test.property")).isEqualTo("12345");
        assertDefaultPropertiesPresent(configuration);
    }

    @Test
    public void should_throw_an_exception_if_sourcefolder_does_not_exist() throws Exception {
        File nonExistentSourceFolder = mock(File.class);
        when(nonExistentSourceFolder.getPath()).thenReturn("/tmp/nonexistent");
        when(nonExistentSourceFolder.exists()).thenReturn(false);

        try {
            JBakeConfiguration configuration = util.loadConfig(nonExistentSourceFolder);
            fail("Exception should be thrown, as source folder does not exist");
        } catch (JBakeException e) {

            assertThat(e.getMessage()).isEqualTo("The given source folder '/tmp/nonexistent' does not exist.");
        }
    }

    @Test
    public void should_add_sourcefolder_to_configuration() throws Exception {

        File sourceFolder = new File(this.getClass().getResource("/fixture").getFile());
        JBakeConfiguration config = util.loadConfig(sourceFolder);

        assertThat(config.getSourceFolder()).isEqualTo(sourceFolder);

    }

    @Test
    public void should_throw_an_exception_if_sourcefolder_is_not_a_directory() throws Exception {

        File sourceFolder = mock(File.class);
        when(sourceFolder.exists()).thenReturn(true);
        when(sourceFolder.isDirectory()).thenReturn(false);

        try {
            JBakeConfiguration config = util.loadConfig(sourceFolder);
            fail("Exception should be thrown if given source folder is not a directory.");
        }
        catch ( JBakeException e ) {
            assertThat( e.getMessage() ).isEqualTo("The given source folder is not a directory.");
        }

    }

    @Test
    public void should_return_destination_folder_from_configuration() throws Exception {
        File sourceFolder = new File(this.getClass().getResource("/fixture").getFile());
        File expectedDestinationFolder = new File(sourceFolder,"output");
        JBakeConfiguration config = util.loadConfig(sourceFolder);

        assertThat(config.getDestinationFolder()).isEqualTo(expectedDestinationFolder);
    }

    @Test
    public void should_return_asset_folder_from_configuration() throws Exception {
        File sourceFolder = new File(this.getClass().getResource("/fixture").getFile());
        File expectedDestinationFolder = new File(sourceFolder,"assets");
        JBakeConfiguration config = util.loadConfig(sourceFolder);

        assertThat(config.getAssetFolder()).isEqualTo(expectedDestinationFolder);
    }

    @Test
    public void should_return_template_folder_from_configuration() throws Exception {
        File sourceFolder = new File(this.getClass().getResource("/fixture").getFile());
        File expectedDestinationFolder = new File(sourceFolder,"templates");
        JBakeConfiguration config = util.loadConfig(sourceFolder);

        assertThat(config.getTemplateFolder()).isEqualTo(expectedDestinationFolder);
    }

    @Test
    public void should_get_template_file_doctype() throws Exception {
        File sourceFolder = new File(this.getClass().getResource("/fixture").getFile());
        File expectedTemplateFile = new File(sourceFolder, "templates/index.ftl");
        JBakeConfiguration config = util.loadConfig(sourceFolder);

        File templateFile = config.getTemplateFileByDocType("masterindex");

        assertThat(templateFile).isEqualTo( expectedTemplateFile );
    }

    @Test
    public void should_get_template_output_extension() throws Exception {

        String docType = "masterindex";
        File sourceFolder = new File(this.getClass().getResource("/fixture").getFile());
        DefaultJBakeConfiguration config = (DefaultJBakeConfiguration) util.loadConfig(sourceFolder);
        config.setTemplateExtensionForDocType(docType, ".xhtml");

        String extension = config.getOutputExtensionByDocType(docType);

        assertThat(extension).isEqualTo(".xhtml");
    }

    @Test
    public void should_get_markdown_extensions_as_list() throws Exception {
        File sourceFolder = new File(this.getClass().getResource("/fixture").getFile());
        DefaultJBakeConfiguration config = (DefaultJBakeConfiguration) util.loadConfig(sourceFolder);

        List<String> markdownExtensions = config.getMarkdownExtensions();

        assertThat(markdownExtensions).containsExactly("HARDWRAPS", "AUTOLINKS", "FENCED_CODE_BLOCKS", "DEFINITIONS");
    }

    @Test
    public void should_return_configured_doc_types() throws Exception {

        File sourceFolder = new File(this.getClass().getResource("/fixture").getFile());
        DefaultJBakeConfiguration config = (DefaultJBakeConfiguration) util.loadConfig(sourceFolder);

        List<String> docTypes = config.getDocumentTypes();

        assertThat(docTypes).containsExactly("allcontent", "masterindex","feed", "archive", "tag", "tagsindex", "sitemap", "post", "page");

    }

    @Test
    public void should_return_a_list_of_asciidoctor_options_keys() throws Exception {
        File sourceFolder = new File(this.getClass().getResource("/").getFile());
        DefaultJBakeConfiguration config = (DefaultJBakeConfiguration) util.loadConfig(sourceFolder);
        config.setProperty("asciidoctor.option.requires", "asciidoctor-diagram");
        config.setProperty("asciidoctor.option.template_dirs", "src/template1,src/template2");

        List<String> options = config.getAsciidoctorOptionKeys();

        assertThat(options).contains("requires","template_dirs");
    }

    @Test
    public void should_return_an_asciidoctor_option() throws Exception {
        File sourceFolder = new File(this.getClass().getResource("/").getFile());
        DefaultJBakeConfiguration config = (DefaultJBakeConfiguration) util.loadConfig(sourceFolder);
        config.setProperty("asciidoctor.option.requires", "asciidoctor-diagram");
        config.setProperty("asciidoctor.option.template_dirs", "src/template1,src/template2");

        Object option = config.getAsciidoctorOption("requires");

        assertThat(String.valueOf(option)).contains("asciidoctor-diagram");
    }

    @Test
    public void should_return_an_asciidoctor_option_with_a_list_value() throws Exception {
        File sourceFolder = new File(this.getClass().getResource("/").getFile());
        DefaultJBakeConfiguration config = (DefaultJBakeConfiguration) util.loadConfig(sourceFolder);
        config.setProperty("asciidoctor.option.requires", "asciidoctor-diagram");
        config.setProperty("asciidoctor.option.template_dirs", "src/template1,src/template2");

        Object option = config.getAsciidoctorOption("template_dirs");

        assertThat(option instanceof List);
        assertThat((List<String>)option).contains("src/template1","src/template2");
    }

    @Test
    public void should_return_empty_string_if_option_not_available() throws Exception {
        File sourceFolder = new File(this.getClass().getResource("/").getFile());
        DefaultJBakeConfiguration config = (DefaultJBakeConfiguration) util.loadConfig(sourceFolder);

        Object option = config.getAsciidoctorOption("template_dirs");

        assertThat(String.valueOf(option)).isEmpty();
    }

    private void assertDefaultPropertiesPresent(JBakeConfiguration config) throws IllegalAccessException {

        for(Field field : JBakeConfiguration.class.getFields() ) {

            if (field.isAccessible()) {
                String key = (String) field.get(new String());
                System.out.println("Key: " + key);
                assertThat(config.get(key)).isNotNull();
            }
        }
    }

}
