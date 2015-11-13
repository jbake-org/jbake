package org.jbake.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jbake.app.Parser.END_OF_HEADER;
import static org.jbake.app.Parser.EOL;
import static org.jbake.app.ContentTag.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.PrintWriter;

import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.ConfigUtil.Keys;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.googlecode.htmlcompressor.compressor.HtmlCompressor;

/**
 * Just to test if the parser finds out the truncator
 * and the jbake default parameters
 */
public class ParserWithTruncatorTest {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();
	
	private CompositeConfiguration config;
	private Parser parser;
	private File rootPath;
	private HtmlCompressor htmlCompressor;
	
	private File withSummaryFile;
	private File withOutSummaryFile;
	
	private final String baseHeader = "title=This is a Title = This is a valid Title" + EOL 
			+ status + "=draft" + EOL 
			+ type + "=post"+ EOL 
			+ date + "=2013-09-02"+ EOL 
			+ END_OF_HEADER;
	private final int length = 3;  // by default the three first words
	private final String withoutSummaryHeader = summaryLength + "=" + String.valueOf(length) + EOL
			+ baseHeader;
    private final String summaryText = "un petit résumé pour faire les tests";
    private final String escapedSummaryText = "un petit r&#233;sum&#233; pour faire les tests";
	
	private final String withSummaryHeader = summary + "=" + summaryText + EOL 
			+ baseHeader;
	private final String body = "Test User " + EOL
			+ "" + EOL
			+ "JBake now supports AsciiDoc." + EOL;
	
    private final String strippedExcerpt = "<p>Test User</p> <p>JBake</p>";
    private final String escapedStrippedExcerpt = "&lt;p&gt;Test User&lt;/p&gt; &lt;p&gt;JBake&lt;/p&gt;";
    
    private final String    DEFAULT_ELLIPSIS                   = "...";
    private final String    NEW_ELLIPSIS                       = "[...]";

    private final String    DEFAULT_READMORE                   = "";
    private final String    NEW_READMORE                       = " Read More &gt;";
	
	@Before
	public void createSampleFile() throws Exception {
		rootPath = new File(this.getClass().getResource(".").getFile());
		config = ConfigUtil.load(rootPath);
		parser = new Parser(config, rootPath.getPath());
		htmlCompressor= new HtmlCompressor();
		
		withOutSummaryFile = folder.newFile("withOutSummaryFile.ad");
		PrintWriter out = new PrintWriter(withOutSummaryFile);
		out.println(withoutSummaryHeader);
		out.println(body);
		out.close();
		
		withSummaryFile = folder.newFile("withSummaryFile.ad");
		out = new PrintWriter(withSummaryFile);
		out.println(withSummaryHeader);
		out.println(body);
		out.close();
		
	}
	
    @Test
    public void ignoreWithSummaryFile() {
        Content content = parser.processSource(withSummaryFile);
        assertNotNull(content);
        assertNotNull(content.get(summary));
        assertThat(content.getString(summaryForHome, null))
            .contains("JBake now supports AsciiDoc.");
        assertThat(content.getString(summaryForFeed, null))
            .contains("JBake now supports AsciiDoc.");
    }
    
    @Test
    public void ignoreWithExcerptFile() {
        Content content = parser.processSource(withOutSummaryFile);
        assertNotNull(content);
        assertNull(content.get(summary));
        assertThat(content.getString(summaryForHome, null))
            .contains("JBake now supports AsciiDoc.");
        assertThat(content.getString(summaryForFeed, null))
            .contains("JBake now supports AsciiDoc.");
    }
    
    @Test
    public void parseWithSummaryFile() {
        config.setProperty(Keys.INDEX_SUMMERY, true);
        config.setProperty(Keys.FEED_SUMMERY, true);
        Content content = parser.processSource(withSummaryFile);
        assertNotNull(content);
        assertNotNull(content.get(summary));
        assertThat(content.get(summary)).isEqualTo(summaryText);
        assertThat(htmlCompressor.compress(content.getString(summaryForHome, null)))
            .isEqualTo(summaryText);
        assertThat(htmlCompressor.compress(content.getString(summaryForFeed, null)))
            .isEqualTo(escapedSummaryText);
    }
    
    @Test
    public void parseWithExcerptFile() {
        config.setProperty(Keys.INDEX_SUMMERY, true);
        config.setProperty(Keys.FEED_SUMMERY, true);
        Content content = parser.processSource(withOutSummaryFile);
        assertNotNull(content);
        assertNull(content.get(summary));
        assertThat(htmlCompressor.compress(content.getString(summaryForHome, null)))
            .isEqualTo(strippedExcerpt.replaceAll("</p>$", DEFAULT_ELLIPSIS + DEFAULT_READMORE + "</p>"));
        // The readmore is not used on the feed page
        assertThat(htmlCompressor.compress(content.getString(summaryForFeed, null)))
            .isEqualTo(escapedStrippedExcerpt.replaceAll("&lt;/p&gt;$", DEFAULT_ELLIPSIS + "&lt;/p&gt;"));
    }
    
    @Test
    public void parseWithExcerptFileAndNewEllipsisAndNewReadMore() {
        config.setProperty(Keys.INDEX_SUMMERY, true);
        config.setProperty(Keys.FEED_SUMMERY, true);
        config.setProperty(Keys.SUMMERY_ELLIPSIS, NEW_ELLIPSIS);
        config.setProperty(Keys.SUMMERY_READMORE, NEW_READMORE);
        Content content = parser.processSource(withOutSummaryFile);
        assertNotNull(content);
        assertNull(content.get(summary));
        assertThat(htmlCompressor.compress(content.getString(summaryForHome, null)))
            .isEqualTo(strippedExcerpt.replaceAll("</p>$", NEW_ELLIPSIS + NEW_READMORE + "</p>"));
        // The readmore is not used on the feed page
        assertThat(htmlCompressor.compress(content.getString(summaryForFeed, null)))
            .isEqualTo(escapedStrippedExcerpt.replaceAll("&lt;/p&gt;$", NEW_ELLIPSIS + "&lt;/p&gt;"));
    }
    
}
