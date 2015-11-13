package org.jbake.app.excerpt;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;
import static org.jbake.app.excerpt.TruncateContentHandler.SPACE_PATTERN;
import static org.jbake.app.excerpt.TruncateContentHandler.SPACE_PATTERN_BASE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.sis.internal.jdk7.StandardCharsets; //java.nio.charset.StandardCharsets only since JDK 1.7
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.jbake.app.excerpt.TruncateContentHandler.Unit;
import org.xml.sax.SAXException;

/**
 * Create an excerpt of a HTML fragment.
 * 
 * Wrap Tika HTML parser (based on Tagsoup) with TruncateContentHandler and
 * ToXmlContentHandler.
 * 
 * The generated excerpt is ready to be inserted as valid HTML in any document.
 */
public class Truncator {

	private enum TruncationStatus {
		unknown, truncated, unchanged
	}

	public final static int NO_LIMIT = TruncateContentHandler.NO_LIMIT;
    public final static Unit DEFAULT_UNIT = Unit.word;
    public final static String DEFAULT_ELLIPSIS = "...";
    public final static String DEFAULT_READ_MORE = "";

	private final static String HTML_CONTENT_TYPE = "text/html";
	private final static String READ_MORE_TAG = "@@@readmore***tag@@@";
	private final static int DEFAULT_LIMIT = 60;
	private final static Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
	private final static boolean DEFAULT_COUNTING_WITH_SPACES = false;
	private final static boolean DEFAULT_SMART_TRUNCATION = true;
	private final static TruncationStatus DEFAULT_TRUNCATION_STATUS = TruncationStatus.unknown;
	private final static String EMPTY_STRING = "";
	private final static String HTML_TAG_NAME_WITH_PREFIX = "^(?:[^:]+:)?a$";
	private final static String HTML_BODY_TAG_CONTENT = "(?s)^.*<body>(.*)</body>\\s*</html>\\s*$";
	private final static String KEEP_ONLY_THE_SELECTION = "$1";
	private final static String CLOSING_HTML_TAG = "</%s>";
	private final static String UNCLOSED_HTML_TAG_WITH_BLANK_HEAD_AND_TAIL = "(?s)" + SPACE_PATTERN
			+ "*<%s(?:>|\\s[^>]*[^/]>)" + SPACE_PATTERN + "*$";
	public final static String ELISIONABLED = "[j|t|d|l|qu|s|m|n]";
	public final static String FRENCH_ELISION = "['|\\u2019|\\u02bc]";

	final Unit unit;
	final int limit;
	final Charset charset;

	String source;
	boolean countingWithSpaces = DEFAULT_COUNTING_WITH_SPACES;
	boolean smartTruncation = DEFAULT_SMART_TRUNCATION;
	String ellipsis = DEFAULT_ELLIPSIS;
	String readmore = DEFAULT_READ_MORE;
	TruncationStatus truncationStatus = DEFAULT_TRUNCATION_STATUS;

	public Truncator() {
		this(DEFAULT_UNIT, DEFAULT_LIMIT, DEFAULT_CHARSET);
	}

	public Truncator(final int limit) {
		this(DEFAULT_UNIT, limit);
	}

	public Truncator(final Unit unit, final int limit) {
		this(unit, limit, DEFAULT_CHARSET);
	}

	public Truncator(final Unit unit, final int limit, final Charset charset) {
		checkArgument(unit != null, "The count type (word, character or code point) must be defined.");
		checkArgument(limit >= NO_LIMIT, "Either no limit (-1), or a limit with zero or positive number.");
		checkArgument(charset != null, "Charset can't be null.");

		this.unit = unit;
		this.limit = limit;
		this.charset = charset;
	}

	public Truncator source(final String source) {
		checkArgument(source != null, "The source can't be null.");

		this.source = source;
		truncationStatus = TruncationStatus.unknown;
		return this;
	}

	public Truncator countingWithSpaces(final boolean countingWithSpaces) {
		this.countingWithSpaces = countingWithSpaces;
		truncationStatus = TruncationStatus.unknown;
		return this;
	}

	public Truncator smartTruncation(final boolean smartTruncation) {
		this.smartTruncation = smartTruncation;
		truncationStatus = TruncationStatus.unknown;
		return this;
	}

	public Truncator ellipsis(final String ellipsis) {
		checkArgument(ellipsis != null, "The ellipsis can't be null.");

		this.ellipsis = ellipsis;
		return this;
	}

	public Truncator readmore(final String readmore) {
		checkArgument(readmore != null, "The 'Read More' message can't be null.");

		this.readmore = readmore;
		return this;
	}

	public boolean isTroncated() {
		checkState(truncationStatus != TruncationStatus.unknown, "Run the truncator before asking.");
		
		return truncationStatus == TruncationStatus.truncated;
	}

	public String run() 
	throws TruncatorException {
		checkState(source != null, "Not ready: a source is required.");

		final TruncatedDoc doc = createFullValidXmlDocFromFragment();

		byte[] buffer = removeOpenLink(doc.getBuffer(), doc.getCurrentElementName());

		buffer = closeAllTagsLeftOpenAfterTruncature(buffer);

		return keepOnlyBodyContent(buffer);
	}

	private static class TruncatedDoc {
		private final String currentElementName;
		private final byte[] buffer;

		TruncatedDoc(final byte[] buffer, final String currentElementName) {
			super();
			this.currentElementName = currentElementName;
			this.buffer = buffer;
		}

		public String getCurrentElementName() {
			return currentElementName;
		}

		public byte[] getBuffer() {
			return buffer;
		}
	}

	/*
	 * Generate a complete and valid xml document from the fragment then
	 * truncate it
	 */
	private TruncatedDoc createFullValidXmlDocFromFragment() 
	throws TruncatorException {
		try {
			final InputStream is = new ByteArrayInputStream(source.getBytes(charset));
			final ByteArrayOutputStream os = new ByteArrayOutputStream();
			
			final String elementName = parse(is, os);
			final byte[] result = os.toByteArray();
			return new TruncatedDoc(result, elementName);
		} catch(SAXException e){
			throw new TruncatorException("Truncator: unable to read the source", e);
		} catch(UnsupportedEncodingException e){
			throw new TruncatorException("Truncator: unable to read the source", e);
		}
	}
	
	private String parse(InputStream is, OutputStream os)
	throws TruncatorException, SAXException, UnsupportedEncodingException  {
		final ToXmlContentHandler textHandler = new ToXmlContentHandler(os, charset);

		// ignore the spaces
		final TruncateContentHandler writerhandler = new TruncateContentHandler(textHandler, limit);
		writerhandler.setCountingWithSpaces(countingWithSpaces);
		writerhandler.setWithSmartTruncation(smartTruncation);
		writerhandler.setUnit(unit);
		
		final AutoDetectParser parser = new AutoDetectParser();
		final Metadata metadata = new Metadata();
		metadata.add(Metadata.CONTENT_TYPE, HTML_CONTENT_TYPE);
		truncationStatus = TruncationStatus.unchanged;
		try {
			parser.parse(is, writerhandler, metadata, new ParseContext());
			return EMPTY_STRING;
		} catch (IOException e) {
			if (!writerhandler.isWriteLimitReached(e))
				throw new TruncatorException("Truncator: unable to parse the source", e);
		} catch (SAXException e) {
			if (!writerhandler.isWriteLimitReached(e))
				throw new TruncatorException("Truncator: unable to parse the source", e);
		} catch (TikaException e) {
			if (!writerhandler.isWriteLimitReached(e))
				throw new TruncatorException("Truncator: unable to parse the source", e);
		} 
		writerhandler.endDocument();
		truncationStatus = TruncationStatus.truncated;
		if (textHandler.getCurrentElementName().matches(HTML_TAG_NAME_WITH_PREFIX)) {
			return textHandler.getCurrentElementName();
		}
		return EMPTY_STRING;
	}

	/*
	 * Keep only the truncated fragment, i.e. the content of the "body" element
	 * a regex can be used here as there is only one body element and it's a
	 * valid html document (thanks to tagsoup).
	 */
	private String keepOnlyBodyContent(final byte[] buffer) {
		String result = new String(buffer);
		result = result.replaceFirst(HTML_BODY_TAG_CONTENT, KEEP_ONLY_THE_SELECTION);

		if (!isTroncated())
			return result;

		return result.replace(READ_MORE_TAG, readmore);
	}

	/*
	 * Close all the tags left open after the truncature to get a complete and
	 * valid xml document
	 */
	private byte[] closeAllTagsLeftOpenAfterTruncature(final byte[] buffer)
	throws TruncatorException {
		if (!isTroncated()) {
			return buffer;
		}

		try {
			
			final InputStream is = new ByteArrayInputStream(buffer);
			final ByteArrayOutputStream os = new ByteArrayOutputStream();

			final ToXmlContentHandler textHandler = new ToXmlContentHandler(os, charset);

			final AutoDetectParser parser = new AutoDetectParser();
			final Metadata metadata = new Metadata();
			metadata.add(Metadata.CONTENT_TYPE, HTML_CONTENT_TYPE);
			parser.parse(is, textHandler, metadata, new ParseContext());
			return os.toByteArray();
		
		} catch (UnsupportedEncodingException e) {
			throw new TruncatorException("Truncator: unable to start writing content", e);
		} catch (IOException e) {
			throw new TruncatorException("Truncator: unable to parse the source", e);
		} catch (SAXException e) {
			throw new TruncatorException("Truncator: unable to parse the source", e);
		} catch (TikaException e) {
			throw new TruncatorException("Truncator: unable to parse the source", e);
		} 
	}

	/*
	 * Tagsoup doesn't like link with attributes inside the "read more": it
	 * removes the attributes! So we need here a late substitution. Then you are
	 * solely responsible for the readmore content.
	 */
	private byte[] removeOpenLink(final byte[] buffer, final String currentElementName)
	throws TruncatorException {
		if (!isTroncated())
			return buffer;

		try {
			String truncated = new String(buffer, charset.name());

			if (currentElementName.isEmpty()) {
				truncated = addEllipsis(truncated);
				truncated = addReadMore(truncated);
			} else {
				final String unclosedEmptyPattern = 
						format(UNCLOSED_HTML_TAG_WITH_BLANK_HEAD_AND_TAIL, currentElementName);
				final Pattern pattern = Pattern.compile(format(unclosedEmptyPattern, currentElementName));
				final Matcher matcher = pattern.matcher(truncated);
				if (matcher.find()) {
					final StringBuffer sb = new StringBuffer();
					matcher.appendReplacement(sb, EMPTY_STRING);
					truncated = sb.toString();
					truncated = addEllipsis(truncated);
					truncated = addReadMore(truncated);
				} else {
					truncated = addEllipsis(truncated);
					truncated += format(CLOSING_HTML_TAG, currentElementName);
					truncated = addReadMore(truncated);
				}
			}
			return truncated.getBytes(charset);
		} catch (UnsupportedEncodingException e) {
			throw new TruncatorException("Truncator: unable to start writing content", e);
		}
	}

	private String addReadMore(final String text) {
		return text + READ_MORE_TAG;
	}

	private String addEllipsis(final String text) {
		return applySpecialFinalRules(text) + ellipsis;
	}

	/*
	 * ATM only some french rules
	 * 
	 * TODO localization
	 */
	public String applySpecialFinalRules(final String text) {

		final StringBuilder psb = new StringBuilder("(?s)" + SPACE_PATTERN + "(?:");
		psb.append("(?:\\u00ab" + SPACE_PATTERN + "?)"); // opening french quotation mark
		psb.append("|");
		psb.append("(?:" + ELISIONABLED + FRENCH_ELISION + ")"); // french elision apostrophe
		psb.append("|");
		psb.append("(?:[^\\p{C}" + SPACE_PATTERN_BASE + "&&[^ày\\&]])"); // an orphan character but "à", "y" and "&"
		psb.append(")$");

		final Pattern pattern = Pattern.compile(psb.toString());
		final Matcher matcher = pattern.matcher(text);
		if (matcher.find()) {
			final StringBuffer sb = new StringBuffer();
			matcher.appendReplacement(sb, EMPTY_STRING);
			return sb.toString();
		} else {
			return text;
		}
	}
}
