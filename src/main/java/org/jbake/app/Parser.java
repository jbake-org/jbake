package org.jbake.app;

import static org.apache.commons.lang.BooleanUtils.toBooleanObject;
import static org.apache.commons.lang.math.NumberUtils.isNumber;
import static org.apache.commons.lang.math.NumberUtils.toInt;
import static org.asciidoctor.AttributesBuilder.attributes;
import static org.asciidoctor.OptionsBuilder.options;
import static org.asciidoctor.SafeMode.UNSAFE;

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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Asciidoctor.Factory;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.DocumentHeader;
import org.asciidoctor.Options;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;

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
	private String currentPath;
	private DateFormat dateFormat;
	private PegDownProcessor pegdownProcessor;
	
	/**
	 * Creates a new instance of Parser.
	 */
	public Parser(CompositeConfiguration config, String contentPath) {
		this.config = config;
		this.dateFormat = new SimpleDateFormat(config.getString(ConfigUtil.DATE_FORMAT));
		this.asciidoctor = Factory.create();

		String[] mdExts = config.getStringArray("markdown.extensions");

		if (mdExts.length > 0) {
		    int extensions = Extensions.NONE;

		    for (int index = 0; index < mdExts.length; index++) {
		        if (mdExts[index].equals("HARDWRAPS")) {
		            extensions |= Extensions.HARDWRAPS;
		        }
		        else if (mdExts[index].equals("AUTOLINKS")) {
		            extensions |= Extensions.AUTOLINKS;
		        }
		        else if (mdExts[index].equals("FENCED_CODE_BLOCKS")) {
		            extensions |= Extensions.FENCED_CODE_BLOCKS;
		        }
		        else if (mdExts[index].equals("DEFINITIONS")) {
		            extensions |= Extensions.DEFINITIONS;
		        }
		        else if (mdExts[index].equals("ABBREVIATIONS")) {
		            extensions |= Extensions.ABBREVIATIONS;
		        }
		        else if (mdExts[index].equals("QUOTES")) {
		            extensions |= Extensions.QUOTES;
		        }
		        else if (mdExts[index].equals("SMARTS")) {
		            extensions |= Extensions.SMARTS;
		        }
		        else if (mdExts[index].equals("SMARTYPANTS")) {
		            extensions |= Extensions.SMARTYPANTS;
		        }
		        else if (mdExts[index].equals("SUPPRESS_ALL_HTML")) {
		            extensions |= Extensions.SUPPRESS_ALL_HTML;
		        }
		        else if (mdExts[index].equals("SUPPRESS_HTML_BLOCKS")) {
		            extensions |= Extensions.SUPPRESS_HTML_BLOCKS;
		        }
		        else if (mdExts[index].equals("SUPPRESS_INLINE_HTML")) {
		            extensions |= Extensions.SUPPRESS_INLINE_HTML;
		        }
		        else if (mdExts[index].equals("TABLES")) {
		            extensions |= Extensions.TABLES;
		        }
		        else if (mdExts[index].equals("WIKILINKS")) {
		            extensions |= Extensions.WIKILINKS;
		        }
		        else if (mdExts[index].equals("ALL")) {
		            extensions = Extensions.ALL;
		        }
		    }

		    this.pegdownProcessor = new PegDownProcessor(extensions);
		}
	}
	
	/**
	 * Process the file by parsing the contents.
	 * 
	 * @param	file
	 * @return	The contents of the file
	 */
	public Map<String, Object> processFile(File file) {
		content = new HashMap<String, Object>();
		currentPath = file.getParent();
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
			} else {// try extracting meta data out of asciidoc header instead
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
						Date date = null;
						try {
							date = dateFormat.parse(parts[1]);
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
		
		Set<String> headerAttKeys = header.getAttributes().keySet();
		statusFound = headerAttKeys.contains("jbake-status");
		typeFound = headerAttKeys.contains("jbake-type");
		
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
					
					Date date = null;
					try {
						date = dateFormat.parse((String)attributes.get(key));
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
		    if (pegdownProcessor == null) {
		        pegdownProcessor = new PegDownProcessor();
		    }

		    String markdown = pegdownProcessor.markdownToHtml(body.toString());
		    content.put("body", markdown);
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
		Options options = getAsciiDocOptionsAndAttributes();
		content.put("body", asciidoctor.render(contents.toString(), options));
	}
	
	private Options getAsciiDocOptionsAndAttributes() {
		final AttributesBuilder attributes = attributes(config
				.getStringArray("asciidoctor.attributes"));
		if (config.getBoolean("asciidoctor.attributes.export", false)) {
			final String prefix = config.getString(
					"asciidoctor.attributes.export.prefix", "");
			for (final Iterator<String> it = config.getKeys(); it.hasNext();) {
				final String key = it.next();
				if (!key.startsWith("asciidoctor")) {
					attributes.attribute(prefix + key, config.getProperty(key));
				}
			}
		}
		final Configuration optionsSubset = config.subset("asciidoctor.option");
		final Options options = options().attributes(attributes.get()).get();
		for (final Iterator<String> iterator = optionsSubset.getKeys(); iterator
				.hasNext();) {
			final String name = iterator.next();
			options.setOption(name,
					guessTypeByContent(optionsSubset.getString(name)));
		}
		options.setBaseDir(currentPath);
		options.setSafe(UNSAFE);
		return options;
	}
   
   /**
    * Guess the type by content it has. 
    * @param value
    * @return boolean,integer of string as fallback
    */
   private Object guessTypeByContent(String value){
      if (toBooleanObject(value)!=null)
         return toBooleanObject(value);
      if(isNumber(value))
         return toInt(value);
      return value;
   }
}
