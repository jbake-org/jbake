package org.jbake.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
	
	/**
	 * Creates a new instance of Parser.
	 */
	public Parser() {
	}
	
	public Parser(CompositeConfiguration config) {
		this.config = config;
	}
	
	/**
	 * Process the file by parsing the contents.
	 * 
	 * @param	file
	 * @return	The contents of the file
	 */
	public Map<String, Object> processFile(File file) {
		content = new HashMap<String, Object>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			List<String> fileContents = IOUtils.readLines(reader);
			
			boolean validFile = false;
			validFile = validate(fileContents);
			if (validFile) {
				processHeader(fileContents);
				processBody(fileContents, file);
			} else if (file.getPath().endsWith(".asciidoc") || file.getPath().endsWith(".ad")) {
				validFile = validateAsciiDoc(file);
				if (validFile) {
					processAsciiDocHeader(file);
					processBody(fileContents, file);
				} else {
					return null;
				}
			} else {
				return null;
			}			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return content;
	}
	
	/**
	 * Validates if the file has the required elements.
	 * 
	 * @param contents	Contents of file	
	 * @return true if valid, false if not
	 */
	private boolean validate(List<String> contents) {
		boolean headerFound = false;
		boolean statusFound = false;
		boolean typeFound = false;
		
		for (String line : contents) {
			if (line.equals("~~~~~~")) {
				headerFound = true;
			}
			if (line.startsWith("type=")) {
				typeFound = true;
			}
			if (line.startsWith("status=")) {
				statusFound = true;
			}
		}
		
		if (!headerFound || !statusFound || !typeFound) {
			System.out.println("");
			if (!headerFound) {
				System.out.println("Missing required JBake header");
			}
			if (!statusFound) {
				System.out.println("Missing required header value: status");
			}
			if (!typeFound) {
				System.out.println("Missing required header value: type");
			}
			
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
		Asciidoctor asciidoctor = Factory.create();
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
			System.out.println("");
			if (!statusFound) {
				System.out.println("Missing required header value: jbake-status");
			}
			if (!typeFound) {
				System.out.println("Missing required header value: jbake-type");
			}
			
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
		Asciidoctor asciidoctor = Factory.create();
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
			} else if (key.equals("docdate")) {
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
		} else if (file.getPath().endsWith(".ad") || file.getPath().endsWith(".asciidoc")) {
			Asciidoctor asciidoctor = Factory.create();
//			Map<String, Object> options = new HashMap<String, Object>();
//			Map<String, Object> attributes = new HashMap<String, Object>();
//			attributes.put("source-highlighter", config.getString("asciidoctor.highlighter"));
//			options.put("attributes", attributes);
			Attributes attributes = AttributesBuilder.attributes(config.getString("asciidoctor.options")).get();
			Options options = OptionsBuilder.options().attributes(attributes).get();
			content.put("body", asciidoctor.render(body.toString(), options));
		} else {
			content.put("body", body.toString());
		}
	}	
}
