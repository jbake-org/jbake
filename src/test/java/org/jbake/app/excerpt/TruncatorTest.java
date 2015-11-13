package org.jbake.app.excerpt;

import static org.apache.commons.lang.StringEscapeUtils.unescapeHtml;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.nio.charset.Charset;
import org.apache.sis.internal.jdk7.StandardCharsets; // in place of jdk7's java.nio.charset.StandardCharsets 

import org.junit.Test;

import com.google.common.base.Joiner;

import org.jbake.app.excerpt.TruncateContentHandler.Unit;

public class TruncatorTest {

    private final static String HTML_DOC = Joiner.on(System.getProperty("line.separator")).join(
"<p>Faire un blog sur mes pérégrinations informatiques me trottait dans la tête depuis un certain temps. Histoire d&rsquo;en conserver une trace. Et si en plus cela pouvait servir à d&rsquo;autres&hellip;</p><p>Après avoir installé un <a href='http://atao60.github.io/maven-site-demo/'>site javadoc</a> de démonstration sur <a href='https://github.com/'>Github</a> à l&rsquo;aide de <a href='http://maven.apache.org/'>Maven</a>, la possibilité de créer un blog sur le même principe est devenue incontournable. Le temps de faire le lien avec <a href='http://jekyllrb.com/'>Jekyll</a> puis <a href='http://jbake.org/'>JBake</a>&nbsp;!</p><p>Le choix de <a href='http://jbake.org/'>JBake</a> permet de rester dans un cadre 100% JVM.</p><p>Trois articles ont fourni les bases&nbsp;:</p>",
"<ul>",
"  <li><p><a href='http://melix.github.io/blog/2014/02/hosting-jbake-github.html'>Authoring your blog on GitHub with JBake and Gradle</a><br/>Cédric Champeau, 3/2/14</p></li>",
"  <li><p><a href='http://www.ybonnel.fr/tags/jbake.html'>Migration de blogger à jbake</a><br/>Yan Bonnel, 2/7/14</p></li>",
"  <li><p><a href='http://docs.ingenieux.com.br/project/jbake/walkthrough.html'>JBake Maven Plugin Walkthough</a> </p></li>",
"</ul><p>N&rsquo;ayant jamais utilisé <a href='https://www.gradle.org/'>Gradle</a>, j&rsquo;ai opté pour <a href='http://maven.apache.org/'>Maven</a> avec l&rsquo;extension <a href='https://github.com/ingenieux/jbake-maven-plugin'>jbake-maven-plugin</a>. Yan Bonnel fournit dans son billet toutes les informations pour se lancer. D&rsquo;autant qu&rsquo;il met aussi à disposition le <a href='http://github.com/ybonnel/blog'>code</a> de son propre blog <a href='http://www.ybonnel.fr/'>JustAnOtherDevBlog</a>. Et pour rendre à César&hellip; Cédric Champeau fait <a href='https://github.com/melix/blog'>de même</a> pour son <a href='http://melix.github.io/blog/'>Blog</a> .</p><p>Ne reste plus qu&rsquo;à tester&nbsp;:</p>",
"<ul>",
"  <li>la répercussion en temps réel des modifications sur l&rsquo;affichage du blog grâce à <a href='http://livereload.com/'>livereload</a> qui est intégrée à <a href='https://github.com/ingenieux/jbake-maven-plugin'>jbake-maven-plugin</a>,</li>",
"  <li>le transfert du blog vers <a href='https://github.com/'>Github</a> à l&rsquo;aide de <a href='https://github.com/github/maven-plugins'>Github site plugin</a>.</li>",
"</ul><p>Ce qui aurait dû rester une promenade de santé s&rsquo;est avéré un parcours du combattant. </p><p>Commençons avec <a href='http://livereload.com/'>livereload</a>&nbsp;: dès qu&rsquo;une modification est enregistrée, <a href='https://github.com/ingenieux/jbake-maven-plugin/issues/6'>livereload s&rsquo;arrête</a>. Pour le moment, il faut travailler avec un <a href='https://github.com/atao60/jbake-maven-plugin'>fork</a> de la version 0.0.9-SNAPSHOT.</p><p>Et les déboires continuent avec <a href='https://github.com/github/maven-plugins'>Github site plugin</a>&nbsp;:</p>",
"<ul>",
"  <li><p>la version 0.9 bloque avec des messages abscons, cf.&nbsp;<a href='https://github.com/github/maven-plugins/issues/69'>Error creating commit: Invalid request #69</a>,</p></li>",
"  <li><p>le passage à la version 0.10 requiert une bibliothèque qui n&rsquo;est pas disponible sur Maven Central, cf.&nbsp;<a href='https://github.com/github/maven-plugins/issues/74'>artifact egit.github.core 3.1.0.201310021548-r not available on maven central #74</a>,</p></li>",
"  <li><p>et reste au final que l&rsquo;API de <a href='https://github.com/github/maven-plugins'>Github site plugin</a> a changé&nbsp;: elle requiert maintenant que le nom et l&rsquo;email du compte <a href='https://github.com/'>Github</a> soient renseignés, cf.&nbsp;<a href='https://github.com/github/maven-plugins/issues/77'>Error deploying when email address not public #77</a>.</p></li>",
"</ul><p>Pourquoi pas de fournir un nom, mais aucune envie d&rsquo;une adresse email qui soit rendue publique.</p><p>Ne reste donc qu&rsquo;à remplacer <a href='https://github.com/github/maven-plugins'>Github site plugin</a> par <a href='http://maven.apache.org/plugins/maven-scm-publish-plugin/'>maven-scm-publish-plugin</a>.</p><p>Ah, quel plaisir d&rsquo;avoir enfin son petit blog perso. Et cerise sur le gâteau&nbsp;: <a href='https://github.com/'>Github</a> permet de publier un tel site avec son propre nom de domaine.</p><p>Le dépôt Github du blog est disponible <a href='https://github.com/atao60/pop-tech'>ici</a>.</p><p>Dans une série de billets à venir, je donnerai plus de détail pour arriver à ce résultat. À bientôt.</p>"
            );
    
    private final static String SIXTY_WORDS_HTML_DOC = unescapeHtml(
                    "<p>Faire un blog sur mes pérégrinations informatiques me trottait dans la tête depuis un certain temps. Histoire d&rsquo;en conserver une trace. Et si en plus cela pouvait servir à d&rsquo;autres&hellip;</p><p>Après avoir installé un <a href='http://atao60.github.io/maven-site-demo/'>site javadoc</a> de démonstration sur <a href='https://github.com/'>Github</a> à l&rsquo;aide de <a href='http://maven.apache.org/'>Maven</a>, la possibilité de créer un blog sur le même principe est...</p>"
                    );
    private final static String TWENTY_WORDS_HTML_DOC = unescapeHtml(
                    "<p>Faire un blog sur mes pérégrinations informatiques me trottait dans la tête depuis un certain temps. Histoire d&rsquo;en conserver...</p>"
                    );
    private final static String TWENTY_SPACES_AND_CHARS_HTML_DOC = unescapeHtml(
                    "<p>Faire un blog sur...</p>"
                    );
    private final static String TWENTY_CHARS_HTML_DOC = unescapeHtml(
                    "<p>Faire un blog sur mes...</p>"
                    );
    private final static String STRICT_TWENTY_SPACES_AND_CHARS_HTML_DOC = unescapeHtml(
                    "<p>Faire un blog sur me...</p>"
                    );
    private final static String STRICT_TWENTY_CHARS_HTML_DOC = unescapeHtml(
                    "<p>Faire un blog sur mes pér...</p>"
                    );

    private final static Charset   DEFAULT_CHARSET                    = StandardCharsets.UTF_8;
    private final static String    DEFAULT_ELLIPSIS                   = "...";
    private final static String    NEW_ELLIPSIS                       = "[...]";
    @SuppressWarnings("unused")
    private final static String    DEFAULT_READMORE                   = "";
    private final static String    NEW_READMORE                       = " Read More &gt;";
    
    @Test
    public void testDefaultConstructor() 
    throws Exception {
        String result = new Truncator().source(HTML_DOC).run();
        result = removeTagSoupArtifacts(result);
        assertThat(result, equalTo(SIXTY_WORDS_HTML_DOC));
    }

    /*
     * Any space or elision apostrophe are separators.
     */
    @Test
    public void testWithDefaultWordLimitConstructor() 
    throws Exception {
        final int limit = 20;
        String result = new Truncator(limit).source(HTML_DOC).run();
        result = removeTagSoupArtifacts(result);
        assertThat(result, equalTo(TWENTY_WORDS_HTML_DOC));
        assertThat(result.split("\\s+|(?<=[j|t|d|l|u|s|n|m])" + Truncator.FRENCH_ELISION).length, equalTo(limit));
    }

    @Test
    public void testDefaultEllipsisAndReadMore() 
    throws Exception {
        String result = new Truncator(20).source(HTML_DOC).run();
        result = removeTagSoupArtifacts(result);
        result = removeAllHtmlTags(result);
        assertThat(result.endsWith(DEFAULT_ELLIPSIS), is(true));
    }

    @Test
    public void testNewEllipsisWithDefaultReadMore() 
    throws Exception {
        final Truncator truncator = new Truncator(20).source(HTML_DOC);
        String result = truncator.ellipsis(NEW_ELLIPSIS).run();
        result = removeTagSoupArtifacts(result);
        result = removeAllHtmlTags(result);
        assertThat(result.endsWith(NEW_ELLIPSIS), is(true));
    }

    @Test
    public void testDefaultEllipsisWithNewReadMore() 
    throws Exception {
        final Truncator truncator = new Truncator(20).source(HTML_DOC);
        String result = truncator.readmore(NEW_READMORE).run();
        result = removeTagSoupArtifacts(result);
        result = removeAllHtmlTags(result);
        String withoutReadmore = result.substring(0, result.length() - NEW_READMORE.length());
        assertThat(result.endsWith(NEW_READMORE), is(true));
        assertThat(withoutReadmore.endsWith(DEFAULT_ELLIPSIS), is(true));
    }

    @Test
    public void testNewReadMoreAndNewEllipsis() 
    throws Exception {
        final Truncator truncator = new Truncator(20).source(HTML_DOC);
        String result = truncator.ellipsis(NEW_ELLIPSIS).readmore(NEW_READMORE).run();
        result = removeTagSoupArtifacts(result);
        result = removeAllHtmlTags(result);
        String withoutReadmore = result.substring(0, result.length() - NEW_READMORE.length());
        assertThat(result.endsWith(NEW_READMORE), is(true));
        assertThat(withoutReadmore.endsWith(NEW_ELLIPSIS), is(true));
    }

    @Test
    public void testWithCharLimitConstructor() 
    throws Exception {
    	final int limit = 20;
    	final Truncator truncator = new Truncator(Unit.character, limit, DEFAULT_CHARSET).source(HTML_DOC);
        String result = truncator.run();
        result = removeTagSoupArtifacts(result);
        assertThat(result, equalTo(TWENTY_CHARS_HTML_DOC));
        result = removeAllSpaces(removeEllipsis(removeAllHtmlTags(result), DEFAULT_ELLIPSIS));
        assertThat(result.length(), lessThanOrEqualTo(limit));
    }
    
    @Test
    public void testWithSpaceAndCharLimitConstructor() 
    throws Exception {
    	final int limit = 20;
    	final Truncator truncator = new Truncator(Unit.character, limit, DEFAULT_CHARSET).source(HTML_DOC);
        String result = truncator.countingWithSpaces(true).run();
        result = removeTagSoupArtifacts(result);
        assertThat(result, equalTo(TWENTY_SPACES_AND_CHARS_HTML_DOC));
        result = removeEllipsis(removeAllHtmlTags(result), DEFAULT_ELLIPSIS);
        assertThat(result.length(), lessThanOrEqualTo(limit));
    }
    
    @Test
    public void testWithStrictCharLimitConstructor() 
    throws Exception {
    	final int limit = 20;
    	final Truncator truncator = new Truncator(Unit.character, limit, DEFAULT_CHARSET).source(HTML_DOC);
        String result = truncator.smartTruncation(false).run();
        result = removeTagSoupArtifacts(result);
        assertThat(result, equalTo(STRICT_TWENTY_CHARS_HTML_DOC));
        result = removeAllSpaces(removeEllipsis(removeAllHtmlTags(result), DEFAULT_ELLIPSIS));
        assertThat(result.length(), equalTo(limit));
    }
    
    @Test
    public void testWithStrictSpaceAndCharLimitConstructor() 
    throws Exception {
    	final int limit = 20;
    	final Truncator truncator = new Truncator(Unit.character, limit, DEFAULT_CHARSET).source(HTML_DOC);
        String result = truncator.smartTruncation(false).countingWithSpaces(true).run();
        result = removeTagSoupArtifacts(result);
        assertThat(result, equalTo(STRICT_TWENTY_SPACES_AND_CHARS_HTML_DOC));
        result = removeEllipsis(removeAllHtmlTags(result), DEFAULT_ELLIPSIS);
        assertThat(result.length(), equalTo(limit));
    }
    
    /*
     * TODO: check why Tagsoup removes the tags "<br/>"
     */
    @Test
    public void testIsTruncated() 
    throws Exception {
    	final Truncator truncator = new Truncator(Unit.character, TruncateContentHandler.NO_LIMIT, DEFAULT_CHARSET).source(HTML_DOC);
        String result = truncator.run();
        assertThat(truncator.isTroncated(), equalTo(false));
        result = removeTagSoupArtifacts(result);
        assertThat(result.replaceAll("\t", ""), equalTo(unescapeHtml(HTML_DOC).replaceAll("<br/>", "").replaceAll("\n\\s*", "")));
    }

    private static String removeTagSoupArtifacts(final String o) {
    	String r = replaceAttributeDelimiters(o);
        r = removeShapeAttribute(r);
        return removeEndOfLine(r);
    }
    
    private static String replaceAttributeDelimiters(final String o) {
    	return o.replaceAll("=\"([^\"]*)\"(\\s|>)", "='$1'$2");
    }
    
    /*
     * TagSoup adds shape attribute in a href tag
     * https://groups.google.com/forum/#!topic/tagsoup-friends/EfB6i12xBLw
     */
    private static String removeShapeAttribute(final String o) {
    	return o.replaceAll(" shape='rect'", "");
    }
    
    private static String removeEndOfLine(final String o) {
    	return o.replaceAll("\\n", "");
    }

    private String removeAllSpaces(final String s) {
    	 return s.replaceAll("\\s", "");
    }
    private String removeAllHtmlTags(final String s) {
    	return s.replaceAll("</?[^>]+>", "");
    }

    private static String removeEllipsis(final String s, final String e) {
        return s.replaceFirst("(?s)" + e +"$", "");
    }
    
}
