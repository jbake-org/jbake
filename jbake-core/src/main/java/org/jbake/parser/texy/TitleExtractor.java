package org.jbake.parser.texy;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Enumeration;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.jbake.app.Crawler;
import org.jbake.parser.ParserContext;
import org.jbake.parser.RawMarkupEngine;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Extracts a title from a XHTML document.
 */
public class TitleExtractor
{

    /**
     * Extracts the text of the first highest heading from the given XHTML.
     * TODO: Move this whole extracting somewhere up to JBake!
     */
    public void tryExtractHighestHeader(ParserContext context)
    {
        String xhtmlString = context.getBody();
        if (xhtmlString == null || "".equals(xhtmlString))
            return;

        // The result from texy needs to be wrapped, because it has no root element.
        ByteArrayInputStream divStart = new ByteArrayInputStream("<div class=\"jbakeWrapper\">".getBytes(StandardCharsets.UTF_8));
        ByteArrayInputStream divEnd = new ByteArrayInputStream("</div>".getBytes(StandardCharsets.UTF_8));
        ByteArrayInputStream is = new ByteArrayInputStream(xhtmlString.getBytes(StandardCharsets.UTF_8));
        Enumeration<InputStream> streams = new IteratorEnumeration(Arrays.asList(new InputStream[]{divStart, is, divEnd}).iterator());

        try (SequenceInputStream wrapped = new SequenceInputStream(streams);) {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document xmlDocument = builder.parse(wrapped);
            XPath xPath = XPathFactory.newInstance().newXPath();

            // Find the first of the highest heading.
            for (int i = 1; i <= 6; i++) {
                // We want it to ignore namespaces, otherwise we would have to maintain a list of all XHTML namespaces.
                String xpath = "//*[local-name()='h" + i + "']";
                NodeList nodeList = (NodeList) xPath.compile(xpath).evaluate(xmlDocument, XPathConstants.NODESET);
                if (0 == nodeList.getLength())
                    continue;
                Element titleElm = (Element) nodeList.item(0);
                titleElm.setAttribute("class", titleElm.getAttribute("class") + " " + RawMarkupEngine.CSS_CLASS_EXTRACTED_TITLE);
                context.getDocumentModel().put(Crawler.Attributes.TITLE, titleElm.getTextContent());
                context.setBody(innerXml(xmlDocument));
                return;
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Failed extracting a title: " + e.getMessage());
        }
    }

    /**
     * Getting an inner XML is a bit complicated in JDK.
     * TODO: All this just to get rid if the previously added <div>? Let's use JSoup I guess.
     */
    public static String innerXml(Node docRootNode)
    {
        try {
            StringWriter writer = new StringWriter();

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            //transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");

            // Find the root element (may not be the first node).
            Node rootElement = docRootNode.getFirstChild();
            while (rootElement != null && rootElement.getNodeType() != Node.ELEMENT_NODE)
                rootElement = rootElement.getNextSibling();
            if (rootElement == null)
                throw new RuntimeException("No root element found in given document node.");

            // Serialize the child nodes.
            NodeList childNodes = rootElement.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                transformer.transform(new DOMSource(childNodes.item(i)), new StreamResult(writer));
            }
            return writer.toString();

        }
        catch (Exception e) {
            throw new RuntimeException("Failed getting inner XML for given node. " + e.getMessage());
        }
    }

}
