package org.jbake.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.io.IOUtils;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Asciidoctor.Factory;
import org.asciidoctor.Attributes;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.DocumentHeader;
import org.asciidoctor.Options;
import org.asciidoctor.OptionsBuilder;

import com.petebevin.markdown.MarkdownProcessor;

/**
 * Parses a File for content.
 * 
 * @author Jonathan Bullock <jonbullock@gmail.com>
 *
 */
public class Parser {
	
	private CompositeConfiguration config;
	private Map<String, Object> content = new HashMap<String, Object>();
	private Asciidoctor asciidoctor;
	
	/**
	 * Creates a new instance of Parser.
	 */
	public Parser() {
		asciidoctor = Factory.create();
	}
	
	public Parser(CompositeConfiguration config) {
		this.config = config;
		asciidoctor = Factory.create();
	}
	
	/**
	 * Process the file by parsing the contents.
	 * 
	 * @param	file
	 * @return	The contents of the file
	 */
	public Map<String, Object> processFile(File file) {
		content = new HashMap<String, Object>();
		BufferedReader reader = null;
		List<String> fileContents = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			fileContents = IOUtils.readLines(reader);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		boolean hasHeader = hasHeader(fileContents);
		
		if (file.getPath().endsWith(".md") || file.getPath().endsWith(".html")) {
			if (hasHeader) {
				// process jbake header
				processHeader(fileContents);
				processBody(fileContents, file);
			} else {
				// output error
				System.err.println("Error parsing meta data from header!");
				return null;
			}
		} else if (file.getPath().endsWith(".asciidoc") || file.getPath().endsWith(".ad") || file.getPath().endsWith(".adoc")) {
			if (hasHeader) {
				// process jbake header
				processHeader(fileContents);
				processBody(fileContents, file);
			} else {
				// try extracting meta data out of asciidoc header instead
				if (validateAsciiDoc(file)) {
					processAsciiDocHeader(file);
					processAsciiDoc(fileContents);
				} else {
					// output error
					System.err.println("Error parsing meta data from header!");
					return null;
				}
			}
		} else {
			return null;
		}
		return content;
	}
	
	/**
	 * Checks if the file has a meta-data header.
	 * 
	 * @param contents	Contents of file	
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
	 * @param contents	Contents of file 
	 */
	private void processHeader(List<String> contents) {
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
	 * Validate if the AsciiDoc file has the required elements.
	 * 
	 * @param contents	Contents of file 
	 */
	private boolean validateAsciiDoc(File file) {
		DocumentHeader header = asciidoctor.readDocumentHeader(file);
		boolean statusFound = false;
		boolean typeFound = false;
		
		for (String key : header.getAttributes().keySet()) {
			if (key.equals("jbake-status")) {
				statusFound = true;
			}
			if (key.equals("jbake-type")) {
				typeFound = true;
			}
		}
		
		if (!statusFound || !typeFound) {
			return false;
		}
		return true;
	}
	
	/**
	 * Process the header of an AsciiDoc file.
	 * 
	 * @param contents	Contents of file 
	 */
	private void processAsciiDocHeader(File file) {
		DocumentHeader header = asciidoctor.readDocumentHeader(file);
		if (header.getDocumentTitle() != null) {
			content.put("title", header.getDocumentTitle());
		}
		Map<String, Object> attributes = header.getAttributes(); 
		for (String key : attributes.keySet()) {
			if (key.equals("jbake-status")) {
				if (attributes.get(key) != null) {
					content.put("status", attributes.get(key));
				}
			} else if (key.equals("jbake-type")) {
				if (attributes.get(key) != null) {
					content.put("type", attributes.get(key));
				}
			} else if (key.equals("revdate")) {
				if (attributes.get(key) != null && attributes.get(key) instanceof String) {
					
					DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
					Date date = null;
					try {
						date = df.parse((String)attributes.get(key));
						content.put("date", date);
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
			} else if (key.equals("jbake-tags")) {
				if (attributes.get(key) != null && attributes.get(key) instanceof String) {
					content.put("tags", ((String)attributes.get(key)).split(","));
				}
			} else {
				content.put(key, attributes.get(key));
			}
		}
	}
	
	/**
	 * Process the body of the file.
	 * 
	 * @param contents	Contents of file
	 * @param file		Source file
	 */
	private void processBody(List<String> contents, File file) {
		StringBuffer body = new StringBuffer();
		boolean inBody = false;
		for (String line : contents) {
			if (inBody) {
				body.append(line + "\n");
			}
			if (line.equals("~~~~~~")) {
				inBody = true;
			}
		}
		
		if (body.length() == 0) {
			for (String line : contents) {
				body.append(line + "\n");
			}
		}
		
		if (file.getPath().endsWith(".md")) {
			MarkdownProcessor markdown = new MarkdownProcessor();
			content.put("body", markdown.markdown(body.toString()));
		} else if (file.getPath().endsWith(".ad") || file.getPath().endsWith(".asciidoc") || file.getPath().endsWith(".adoc")) {
			processAsciiDoc(body);
		} else {
			content.put("body", body.toString());
		}
	}
	
	/**
	 * Process the body of the file.
	 * 
	 * @param contents	Contents of file
	 * @param file		Source file
	 */
	private void processAsciiDoc(List<String> contents) {
		StringBuffer body = new StringBuffer();
		for (String line : contents) {
			body.append(line + "\n");
		}
		
		processAsciiDoc(body);
	}
	
	private void processAsciiDoc(StringBuffer contents) {
		Attributes attributes = AttributesBuilder.attributes(config.getString("asciidoctor.options")).get();
		Options options = OptionsBuilder.options().attributes(attributes).get();
		content.put("body", asciidoctor.render(contents.toString(), options));
	}
}
