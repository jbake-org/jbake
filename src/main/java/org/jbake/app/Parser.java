package org.jbake.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.petebevin.markdown.MarkdownProcessor;

/**
 * Parses a File for content.
 * 
 * @author Jonathan Bullock <jonbullock@gmail.com>
 *
 */
public class Parser {
	
	private Map<String, Object> content = new HashMap<String, Object>();
	
	/**
	 * Creates a new instance of Parser.
	 */
	public Parser() {
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
				content.put("uri", file.getName());
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
				System.out.println("Missing required header");
			}
			if (!statusFound) {
				System.out.println("Missing required header tag: status");
			}
			if (!typeFound) {
				System.out.println("Missing required header tag: type");
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
		
		if (file.getPath().endsWith(".md")) {
			MarkdownProcessor markdown = new MarkdownProcessor();
			content.put("body", markdown.markdown(body.toString()));
		} else {
			content.put("body", body.toString());
		}
	}	
}
