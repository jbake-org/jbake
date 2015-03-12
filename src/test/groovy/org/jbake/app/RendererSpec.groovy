package org.jbake.app

import org.apache.commons.configuration.CompositeConfiguration
import org.apache.commons.io.FileUtils
import org.jbake.model.DocumentTypes
import org.junit.Assert
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Created by frank on 12.03.15.
 */
class RendererSpec extends Specification {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder()

    @Shared
    def templateMap = [
            'groovyTemplates': '.gsp',
            'thymeleafTemplates': '.thyme',
            'templates' : '.tfl'
    ]

    File sourceFolder
    File destinationFolder
    CompositeConfiguration config
    ContentStore db
    Crawler crawler


    def setup(){

        setupFolders()
        loadConfig()

        db = DBUtil.createDB("memory", "documents"+System.currentTimeMillis())
    }

    def cleanup(){
        db.drop()
        db.close()
    }

    def setupFolders(){
        sourceFolder = new File(this.class.getResource("/").getFile())
        if (!sourceFolder.exists()) {
            throw new Exception("Cannot find sample data structure!")
        }

        destinationFolder = folder.getRoot()

    }

    def loadConfig(){
        config = ConfigUtil.load(sourceFolder)
        Assert.assertEquals(".html", config.getString(ConfigUtil.Keys.OUTPUT_EXTENSION))
    }

    def getTemplateFolder(String templateFolderName){
        def templateFolder = new File(sourceFolder, templateFolderName)
        if (!templateFolder.exists()) {
            throw new Exception("Cannot find template folder!")
        }
        templateFolder
    }

    def Map<String, Object> crawlContentAndProcessFile(File sampleFile, String templateSuffix) {

        crawlContent(templateSuffix)

        def content = processFile(sampleFile)
        content
    }

    def crawlContent(String templateSuffix){
        modifyConfiguration(templateSuffix)
        crawler = new Crawler(db, sourceFolder, config)
        crawler.crawl(new File(sourceFolder.getPath() + File.separator + "content"))
    }

    def modifyConfiguration(String templateSuffix){

        if ( templateSuffix != null ) {
            Iterator<String> keys = config.getKeys()
            while (keys.hasNext()) {
                String key = keys.next()
                if (key.startsWith("template") && key.endsWith(".file")) {
                    String old = (String) config.getProperty(key)
                    config.setProperty(key, old.substring(0, old.length() - 4) + templateSuffix)
                }
            }
        }

    }

    def processFile(def sampleFile){
        Parser parser = new Parser(config, sourceFolder.getPath())
        def content = parser.processFile(sampleFile)
        content.put("uri", "/" + sampleFile.name)
        content
    }

    def getOutput(String fileName){
        FileUtils.readFileToString( new File(destinationFolder, fileName) )
    }

    @Unroll
    def "should render a Post with template Folder #templateFolderName and suffix #templateSuffix"(){

        given:
        String filename = "second-post.html"
        String filePath = sourceFolder.getPath() + File.separator + "content" + File.separator + "blog" + File.separator + "2013" + File.separator + filename
        File sampleFile = new File(filePath)

        Map<String, Object> content = crawlContentAndProcessFile(sampleFile, templateSuffix)
        def templateFolder = getTemplateFolder(templateFolderName)
        def renderer = new Renderer(db, destinationFolder, templateFolder, config)

        when:
        renderer.render(content)


        then:

        String output = getOutput(filename)
        output.contains("<h2>Second Post</h2>")
        output.contains("<p class=\"post-date\">28")
        output.contains("2013</p>")
        output.contains("Lorem ipsum dolor sit amet")
        output.contains("<h5>Published Posts</h5>")
        output.contains("blog/2012/first-post.html")

        where:

        templateFolderName << templateMap.keySet()
        templateSuffix << templateMap.keySet().collect { templateMap."$it" }
    }

    @Unroll
    def "should render a Page with template Folder #templateFolderName and suffix #templateSuffix"(){

        given:
        String filename = "about.html"
        String filePath = sourceFolder.getPath() + File.separator + "content" + File.separator + filename
        File sampleFile = new File(filePath)

        def content = crawlContentAndProcessFile(sampleFile,templateSuffix)
        def templateFolder = getTemplateFolder(templateFolderName)
        def renderer = new Renderer(db, destinationFolder, templateFolder, config)

        when:
        renderer.render(content)

        then:
        String output = getOutput(filename)

        output.contains("<h4>About</h4>")
        output.contains("All about stuff!")
        output.contains("<h5>Published Pages</h5>")
        output.contains("/projects.html");

        where:

        templateFolderName << templateMap.keySet()
        templateSuffix << templateMap.keySet().collect { templateMap."$it" }

    }

    @Unroll
    def "should render allcontent Page  with template Folder #templateFolderName and suffix #templateSuffix"(){
        given:
        DocumentTypes.addDocumentType("paper");
        DBUtil.updateSchema(db);

        String filename = "allcontent.html"
        String filePath = sourceFolder.getPath() + File.separator + "content" + File.separator + filename
        File sampleFile = new File(filePath)

        def content = crawlContentAndProcessFile(sampleFile,templateSuffix)
        def templateFolder = getTemplateFolder(templateFolderName)
        def renderer = new Renderer(db, destinationFolder, templateFolder, config)

        when:
        renderer.render(content)

        then:
        String output = getOutput(filename)
        output.contains("blog/2013/second-post.html")
        output.contains("blog/2012/first-post.html")
        output.contains("papers/published-paper.html")
        output.contains("papers/draft-paper.html");


        where:

        templateFolderName << templateMap.keySet()
        templateSuffix << templateMap.keySet().collect { templateMap."$it" }


    }

    @Unroll
    def "should render Index with tempalte Folder #templateFolderName and suffix #templateSuffix"(){
        given:
        String fileName = "index.html"

        crawlContent(templateSuffix)
        def templateFolder = getTemplateFolder(templateFolderName)
        def renderer = new Renderer(db, destinationFolder, templateFolder, config)

        when:
        //exec
        renderer.renderIndex(fileName);

        then:
        def output = getOutput(fileName)
        output.contains("<h4><a href=\"blog/2012/first-post.html\"")
        output.contains("<h4><a href=\"blog/2013/second-post.html\"");

        where:

        templateFolderName << templateMap.keySet()
        templateSuffix << templateMap.keySet().collect { templateMap."$it" }

    }

    @Unroll
    def "should render Feed with tempalte Folder #templateFolderName and suffix #templateSuffix"(){
        given:
        String fileName = "feed.xml"

        crawlContent(templateSuffix)
        def templateFolder = getTemplateFolder(templateFolderName)
        def renderer = new Renderer(db, destinationFolder, templateFolder, config)

        when:
        //exec
        renderer.renderFeed(fileName);

        then:
        def output = getOutput(fileName)
        output.contains("<description>My corner of the Internet</description>")
        output.contains("<title>Second Post</title>")
        output.contains("<title>First Post</title>");

        where:

        templateFolderName << templateMap.keySet()
        templateSuffix << templateMap.keySet().collect { templateMap."$it" }

    }
    @Unroll
    def "should render Archive with tempalte Folder #templateFolderName and suffix #templateSuffix"(){
        given:
        String fileName = "archive.html"

        crawlContent(templateSuffix)
        def templateFolder = getTemplateFolder(templateFolderName)
        def renderer = new Renderer(db, destinationFolder, templateFolder, config)

        when:
        //exec
        renderer.renderIndex(fileName);

        then:
        def output = getOutput(fileName)
        output.contains("<a href=\"blog/2013/second-post.html\"")
        output.contains("<a href=\"blog/2012/first-post.html\"");

        where:

        templateFolderName << templateMap.keySet()
        templateSuffix << templateMap.keySet().collect { templateMap."$it" }

    }
    @Unroll
    def "should render Tags with tempalte Folder #templateFolderName and suffix #templateSuffix"(){
        given:

        crawlContent(templateSuffix)
        def templateFolder = getTemplateFolder(templateFolderName)
        def renderer = new Renderer(db, destinationFolder, templateFolder, config)

        when:
        renderer.renderTags(crawler.getTags(), "tags");

        then:
        def output = FileUtils.readFileToString(new File(destinationFolder.toString() + File.separator + "tags" + File.separator + "blog.html"))
        output.contains("<a href=\"blog/2013/second-post.html\"")
        output.contains("<a href=\"blog/2012/first-post.html\"");

        where:

        templateFolderName << templateMap.keySet()
        templateSuffix << templateMap.keySet().collect { templateMap."$it" }

    }
    @Unroll
    def "should render Sitemap with tempalte Folder #templateFolderName and suffix #templateSuffix"(){
        given:

        DocumentTypes.addDocumentType("paper");
        DBUtil.updateSchema(db);

        String fileName = "sitemap.xml"

        crawlContent(templateSuffix)
        def templateFolder = getTemplateFolder(templateFolderName)
        def renderer = new Renderer(db, destinationFolder, templateFolder, config)

        when:
        //exec
        renderer.renderSitemap(fileName);

        then:
        def output = getOutput(fileName)
        output.contains("blog/2013/second-post.html")
        output.contains("blog/2012/first-post.html")
        output.contains("papers/published-paper.html")
        !output.contains("draft-paper.html");

        where:

        templateFolderName << templateMap.keySet()
        templateSuffix << templateMap.keySet().collect { templateMap."$it" }

    }

}
