package org.jbake.app;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.io.IOUtils;
import org.jbake.parser.Engines;
import org.jbake.parser.MarkupEngine;
import org.jbake.parser.ParserContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses a File for content.
 *
 * @author Jonathan Bullock <jonbullock@gmail.com>
 */
public class Parser {

    private CompositeConfiguration config;
    private String contentPath;

    /**
     * Creates a new instance of Parser.
     */
    public Parser(CompositeConfiguration config, String contentPath) {
        this.config = config;
        this.contentPath = contentPath;
    }

    private static String fileExt(File src) {
        String name = src.getName();
        int idx = name.lastIndexOf('.');
        if (idx > 0) {
            return name.substring(idx + 1);
        } else {
            return "";
        }
    }

    /**
     * Process the file by parsing the contents.
     *
     * @param    file
     * @return The contents of the file
     */
    public Map<String, Object> processFile(File file) {
        Map<String,Object> content = new HashMap<String, Object>();
        InputStream is = null;
        List<String> fileContents = null;
        try {
            is = new FileInputStream(file);
            fileContents = IOUtils.readLines(is, config.getString("render.encoding"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        boolean hasHeader = hasHeader(fileContents);
        ParserContext context = new ParserContext(
                file,
                fileContents,
                config,
                contentPath,
                hasHeader,
                content
        );

        MarkupEngine engine = Engines.get(fileExt(file));
        if (engine==null) {
            System.err.println("Unable to find suitable markup engine for "+file);
            return null;
        }

        if (hasHeader) {
            // read header from file
            processHeader(fileContents, content);
        }
        // then read engine specific headers
        engine.processHeader(context);

        if (content.get("type")==null||content.get("status")==null) {
            // output error
            System.err.println("Error parsing meta data from header!");
            return null;
        }

        // generate default body
        processBody(fileContents, content);

        // eventually process body using specific engine
        if (engine.validate(context)) {
            engine.processBody(context);
        } else {
            System.out.println("Incomplete source file (" + file + ") for markup engine:" + engine.getClass().getSimpleName());
            return null;
        }

        return content;
    }

    /**
     * Checks if the file has a meta-data header.
     *
     * @param contents Contents of file
     * @return true if header exists, false if not
     */
    private boolean hasHeader(List<String> contents) {
        boolean headerValid = false;
        boolean headerSeparatorFound = false;
        boolean statusFound = false;
        boolean typeFound = false;

        List<String> header = new ArrayList<String>();

        for (String line : contents) {
            header.add(line);
            if (line.contains("=")) {
                if (line.startsWith("type=")) {
                    typeFound = true;
                }
                if (line.startsWith("status=")) {
                    statusFound = true;
                }
            }
            if (line.equals("~~~~~~")) {
                headerSeparatorFound = true;
                header.remove(line);
                break;
            }
        }

        if (headerSeparatorFound) {
            headerValid = true;
            for (String headerLine : header) {
                if (!headerLine.contains("=")) {
                    headerValid = false;
                    break;
                }
            }
        }

        if (!headerValid || !statusFound || !typeFound) {
            return false;
        }
        return true;
    }

    /**
     * Process the header of the file.
     *
     * @param contents Contents of file
     * @param content
     */
    private void processHeader(List<String> contents, final Map<String, Object> content) {
        for (String line : contents) {
            if (line.equals("~~~~~~")) {
                break;
            } else {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    if (parts[0].equalsIgnoreCase("date")) {
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                        Date date = null;
                        try {
                            date = df.parse(parts[1]);
                            content.put(parts[0], date);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    } else if (parts[0].equalsIgnoreCase("tags")) {
                        content.put(parts[0], parts[1].split(","));
                    } else {
                        content.put(parts[0], parts[1]);
                    }
                }
            }
        }
    }

    /**
     * Process the body of the file.
     *
     * @param contents Contents of file
     * @param content
     */
    private void processBody(List<String> contents, final Map<String, Object> content) {
        StringBuilder body = new StringBuilder();
        boolean inBody = false;
        for (String line : contents) {
            if (inBody) {
                body.append(line).append("\n");
            }
            if (line.equals("~~~~~~")) {
                inBody = true;
            }
        }

        if (body.length() == 0) {
            for (String line : contents) {
                body.append(line).append("\n");
            }
        }

        content.put("body", body.toString());
    }

}
