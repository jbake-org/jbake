package org.jbake.app.excerpt;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.UUID;
import org.apache.tika.sax.ContentHandlerDecorator;
import org.apache.tika.sax.ToTextContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * SAX event handler that writes content up to an optional limit in a character stream or other decorated handler.
 * 
 * This class is based on {@link org.apache.tika.sax.WriteOutContentHandler} with some improvements.
 * The counter is now accessible by any derived class. It's also possible to chose:
 * <li>the type of item to count: characters, Unicode code points or words</li>
 * <li>when counting characters or code points, whether or not spaces has to be counted</li>
 * <li>when counting characters or code points, whether or not the last word can be splitted</li>
 * 
 * Required {@link popsuite.blog.util.ToXmlContentHandler}: 
 * it doesn't work with the original {@link  org.apache.tika.sax.ToXMLContentHandler}. 
 */
public class TruncateContentHandler extends ContentHandlerDecorator {

    public enum Unit { word, codePoint, character };
    
    public final static int        NO_LIMIT            = -1;
    private final static int       DEFAULT_LIMIT       = 100 * 1000;
    private final static Unit      DEFAULT_UNIT        = Unit.character;

    private final static String    EMPTY_STRING        = "";
    /* SPACE_PATTERN_BASE must be defined in accordance with the method {@link #isSpace} for Unicode Code Point */
    public final static String     SPACE_PATTERN_BASE  = "\\p{javaWhitespace}\\p{Z}";
    public final static String     SPACE_PATTERN       = "[" + SPACE_PATTERN_BASE + "]";
    private final static String    SPACES_PATTERN      = SPACE_PATTERN + "+";
    private final static String    NO_SPACE_PATTERN    = "[^" + SPACE_PATTERN_BASE + "]";
    private final static String    TRUNCATED_LAST_WORD = "(?s)" + SPACES_PATTERN + NO_SPACE_PATTERN + "*$";
    private final static String    WITH_DELIMITER_SPLITTER_PATTERN = "(?=(?!^)%1$s)(?<!%1$s)|(?!%1$s)(?<=%1$s)";
    private final static String    SPACES_TAIL         = "(?s)" + SPACES_PATTERN + "$";

    boolean              withSpaces          = true;
    /* even with LF, FF, ... */
    boolean              withAllSpaces       = false;
    boolean              withSmartTruncation = true;
    Unit                 unit                = DEFAULT_UNIT;

    /**
     * The unique tag associated with exceptions from stream.
     */
    final UUID           tag                 = UUID.randomUUID();

    /**
     * The maximum number of characters to write to the character stream. Set to
     * -1 for no limit.
     */
    final int           writeLimit;

    /**
     * Number of characters written so far.
     */
    int               writeCount             = 0;

    /**
     * Creates a content handler that writes content up to the given write limit
     * to the given content handler.
     *
     * @param handler
     *            content handler to be decorated
     * @param writeLimit
     *            write limit
     */
    public TruncateContentHandler(final ContentHandler handler, final int writeLimit) {
        super(handler);
        if (writeLimit < NO_LIMIT)
            throw new IllegalArgumentException("Either no limit (-1), zero or positive number as limit.");
        this.writeLimit = writeLimit;
    }

    /**
     * Creates a content handler that writes content up to the given write limit
     * to the given character stream.
     *
     * @param writer
     *            character stream
     * @param writeLimit
     *            write limit
     */
    public TruncateContentHandler(final Writer writer, final int writeLimit) {
        this(new ToTextContentHandler(writer), writeLimit);
    }

    /**
     * Creates a content handler that writes character events to the given
     * writer.
     *
     * @param writer
     *            writer
     */
    public TruncateContentHandler(final Writer writer) {
        this(writer, NO_LIMIT);
    }

    /**
     * Creates a content handler that writes character events to the given
     * output stream using the default encoding.
     *
     * @param stream
     *            output stream
     */
    public TruncateContentHandler(final OutputStream stream) {
        this(new OutputStreamWriter(stream, Charset.defaultCharset()));
    }

    /**
     * Creates a content handler that writes character events to an internal
     * string buffer. Use the {@link #toString()} method to access the collected
     * character content.
     * <p>
     * The internal string buffer is bounded at the given number of characters.
     * If this write limit is reached, then a {@link SAXException} is thrown.
     * The {@link #isWriteLimitReached(Throwable)} method can be used to detect
     * this case.
     *
     * @param writeLimit
     *            maximum number of characters to include in the string, or -1
     *            to disable the write limit
     */
    public TruncateContentHandler(final int writeLimit) {
        this(new StringWriter(), writeLimit);
    }

    /**
     * Creates a content handler that writes character events to an internal
     * string buffer. Use the {@link #toString()} method to access the collected
     * character content.
     * <p>
     * The internal string buffer is bounded at 100k characters. If this write
     * limit is reached, then a {@link SAXException} is thrown. The
     * {@link #isWriteLimitReached(Throwable)} method can be used to detect this
     * case.
     */
    public TruncateContentHandler() {
        this(DEFAULT_LIMIT);
    }

    protected int getWriteLimit() {
        return writeLimit;
    }

    protected boolean isWriteLimitReached() {
        return writeLimit != NO_LIMIT && writeLimit < writeCount;
    }

    public boolean isCountingWithSpaces() {
        return withSpaces;
    }

    public void setCountingWithSpaces(final boolean withSpaces) {
        this.withSpaces = withSpaces;
    }

    public boolean isCountingWithAllSpaces() {
        return withAllSpaces;
    }

    public void setCountingWithAllSpaces(final boolean withSpaces) {
        this.withAllSpaces = withSpaces;
    }

    public boolean isWithSmartTruncation() {
    	return withSmartTruncation;
    }

    public void setWithSmartTruncation(final boolean smart) {
        this.withSmartTruncation = smart;
    }

    public Unit getUnit() {
    	return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    /**
     * Writes the given characters to the given character stream.
     */
    @Override 
    public void characters(final char[] ch, final int start, final int length) 
    throws SAXException {
        if (unit == Unit.character) {
            countingByCharacter(ch, start, length);
        } else if (unit == Unit.codePoint) {
            countingByCodePoint(ch, start, length);
        } else {
            countingByWord(ch, start, length);
        }
    }

    private void countingByCharacter(final char[] ch, final int start, final int length) 
    throws SAXException {
        if (writeLimit == NO_LIMIT) {
            super.characters(ch, start, length);
            final String origine = new String(Arrays.copyOfRange(ch, start, start + length));
            final String stripped = isCountingWithSpaces() ? origine 
            		: origine.replaceAll(SPACES_PATTERN, EMPTY_STRING);
            writeCount += stripped.length();
            return;
        }

        boolean reached = false;
        int next = start;
        for (int i = start; !reached && i < start + length; i++) {
            next = i + 1;
            if (isCountingWithSpaces() || ! isSpace(ch[i])) {
                writeCount++;
                if (writeCount >= writeLimit) reached = true;
            }
        }
        final boolean reachedBeforeEnd = reached && (next < start + length);
        if (reachedBeforeEnd && isWithSmartTruncation()
                        && ((!isSpace(ch[next - 1]) && !isSpace(ch[next])) || isSpace(ch[next - 1]))) {
            final int smartLentgh = new String(
            		Arrays.copyOfRange(ch, start, next)).replaceFirst(TRUNCATED_LAST_WORD,
                    EMPTY_STRING).length();
            next = start + smartLentgh;
        }
        super.characters(ch, start, next - start);
        if (reachedBeforeEnd)
            throw new WriteLimitReachedException("Your document contained more than " + writeLimit
                            + " Unicode unit points (characters), and so your requested limit has been"
                            + " reached. To receive the full text of the document,"
                            + " increase your limit. (Text up to the limit is" + " however available).", tag);

    }

    private void countingByCodePoint(final char[] ch, final int start, final int length) throws SAXException {
        final String origine = new String(Arrays.copyOfRange(ch, start, start + length));
        if (writeLimit == NO_LIMIT || length == 0) {
            super.characters(ch, start, length);
            final String stripped = isCountingWithSpaces() ? origine 
            		: origine.replaceAll(SPACES_PATTERN, EMPTY_STRING);
            writeCount += stripped.codePointCount(0, stripped.length());
            return;
        }

        boolean reached = false;
        int next = 0;
        int current = 0;
        for (int i = 0; ! reached && i < origine.codePointCount(0, origine.length()); i++) {
            current = origine.offsetByCodePoints(0, i);
            final int currentCodePoint = origine.codePointAt(current);
            next = current + Character.charCount(currentCodePoint);
            if (isCountingWithSpaces() || ! isSpace(currentCodePoint)) {
                writeCount++;
                if (writeCount >= writeLimit) reached = true;
            }
        }
        final boolean reachedBeforeEnd = reached && (next < length);
        if (reachedBeforeEnd
                        && isWithSmartTruncation()
                        && ((!isSpace(origine.codePointAt(current)) && !isSpace(origine.codePointAt(next))) 
                                        || isSpace(origine.codePointAt(current)))) {
            final int smartLentgh = new String(
            		Arrays.copyOfRange(ch, start, start + next)).replaceFirst(TRUNCATED_LAST_WORD,
                    EMPTY_STRING).length();
            next = smartLentgh;
        }
        super.characters(ch, start, next);
        if (reachedBeforeEnd)
            throw new WriteLimitReachedException("Your document contained more than " + writeLimit
                            + " Unicode code points (characters), and so your requested limit has been"
                            + " reached. To receive the full text of the document,"
                            + " increase your limit. (Text up to the limit is" + " however available).", tag);
    }

    private void countingByWord(final char[] ch, final int start, final int length) 
    throws SAXException {
        final String origine = new String(Arrays.copyOfRange(ch, start, start + length));
        if (writeLimit == NO_LIMIT || length == 0) {
            super.characters(ch, start, length);
            final String[] splitted = origine.split(SPACES_PATTERN);
            if (splitted.length > 0) {
                writeCount += splitted.length + (splitted[0].isEmpty() ? -1 : 0);
            }
            return;
        }
        
        final String[] splitted = origine.split(String.format(WITH_DELIMITER_SPLITTER_PATTERN, SPACE_PATTERN));
        final StringBuilder result = new StringBuilder();
        boolean reachedBeforeEnd = false;
        for (int i = 0; ! reachedBeforeEnd && i < splitted.length; i++) {
            final String token = splitted[i];
            final int count = writeCount + (isSpace(token.codePointAt(0)) ? 0 : 1);
            if (count > writeLimit) {
                reachedBeforeEnd = true;
            } else {
                writeCount = count;
                result.append(token);
            }
        }
        
        final int size = !reachedBeforeEnd ? result.length()
                    : result.toString().replaceFirst(SPACES_TAIL, EMPTY_STRING).length();
        super.characters(ch, start, size);
        if (reachedBeforeEnd)
            throw new WriteLimitReachedException("Your document contained more than " + writeLimit
                            + " words, and so your requested limit has been"
                            + " reached. To receive the full text of the document,"
                            + " increase your limit. (Text up to the limit is" + " however available).", tag);
    }

    @Override 
    public void ignorableWhitespace(final char[] ch, final int start, final int length) 
    throws SAXException {
        final boolean counting = isCountingWithSpaces() && isCountingWithAllSpaces() && (getUnit() != Unit.word);
        int writeCountInc = counting ? length : 0;
        int next = length;
        if (writeLimit == NO_LIMIT || !counting) {
            super.ignorableWhitespace(ch, start, next);
            writeCount += writeCountInc;
            return;
        }
        final boolean reachedBeforeEnd = writeCount + writeCountInc > writeLimit;
        if (reachedBeforeEnd) {
            writeCountInc = writeLimit - writeCount;
            next = writeCountInc;
        }
        writeCount += writeCountInc;
        super.ignorableWhitespace(ch, start, next);
        if (reachedBeforeEnd)
            throw new WriteLimitReachedException("Your document contained more than " + writeLimit
                            + unit.name() + "s, and so your requested limit has been"
                            + " reached. To receive the full text of the document,"
                            + " increase your limit. (Text up to the limit is" + " however available).", tag);

    }

    /**
     * Checks whether the given exception (or any of it's root causes) was
     * thrown by this handler as a signal of reaching the write limit.
     *
     * @param t
     *            throwable
     * @return <code>true</code> if the write limit was reached,
     *         <code>false</code> otherwise
     */
    public boolean isWriteLimitReached(final Throwable t) {
        if (t instanceof WriteLimitReachedException) {
            return tag.equals(((WriteLimitReachedException) t).tag); 
        } else {
            return (t.getCause() != null) && isWriteLimitReached(t.getCause());
        }
    }

    /**
     * The exception used as a signal when the write limit has been reached.
     */
    private static class WriteLimitReachedException extends SAXException {

		private static final long serialVersionUID = 1L;
		
		/** Serializable tag of the handler that caused this exception */
        final Serializable tag;

        WriteLimitReachedException(final String message, final Serializable tag) {
            super(message);
            this.tag = tag;
        }

    }

    /*
     * is equivalent to the regex [\\p{javaWhitespace}\\p{Z}] defined by SPACE_PATTERN_BASE
     * 
     * \p{Z} will catch the no break spaces, i.e.: NO-BREAK SPACE (U+00A0),
     * NARROW NO-BREAK SPACE (U+202F), ...
     */
    private static boolean isSpace(final char c) {
    	return Character.isWhitespace(c) || Character.isSpaceChar(c);
    }

    private static boolean isSpace(final int c) {
        return Character.isWhitespace(c) || Character.isSpaceChar(c);
    }

}
