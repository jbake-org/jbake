package com.jbake.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

// Copyright 2012 Jonathan Bullock
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

public class Bake {

	private static final String USAGE = "Usage: bake <source path> <destination path>";
	
	private static File source;
	private static File destination;
	private static Configuration cfg;
	
	private static List<Content> publishedPosts = new ArrayList<Content>();
	private static List<Content> draftPosts = new ArrayList<Content>();
	private static List<Content> publishedPages = new ArrayList<Content>();
	private static List<Content> draftPages = new ArrayList<Content>();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println(USAGE);
		} else {
			if (args.length != 2) {
				System.out.println(USAGE);
			} else {
				File sourceArg = new File(args[0]);
				File destinationArg = new File(args[1]);
				if (!sourceArg.exists() || !destinationArg.exists()) {
					System.out.println(USAGE);
				} else {
					// baking happens here!
					source = sourceArg;
					destination = destinationArg;
					startBaking();
				}
			}
		}
	}
	
	private static void startBaking() {
		System.out.println("Baking has started...");
		File templates = new File(source.getPath()+File.separator+"templates");
		if (templates.exists()) {
			cfg = new Configuration();
			try {
				cfg.setDirectoryForTemplateLoading(new File(source.getPath()+File.separator+"templates"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			cfg.setObjectWrapper(new DefaultObjectWrapper());
		} else {
			System.err.println("Error: Required 'templates' folder cannot be found!");
		}
		
		// process content
		File content = new File(source.getPath()+File.separator+"content");
		if (content.exists()) {
			crawl(content);
		} else {
			System.err.println("Error: Required 'content' folder cannot be found!");
		}
		
		// copy media files
		File media = new File(source.getPath()+File.separator+"media");
		if (media.exists()) {
			copyMedia(media);
		} else {
			System.out.println("Warning: No 'media' folder was found!");
		}
		
		// sort posts
		Collections.sort(publishedPosts, new Comparator<Content>() {
			@Override
			public int compare(Content c1, Content c2) {
				return c1.getDate().compareTo(c2.getDate());
			}
		});
		Collections.reverse(publishedPosts);
		
		// write index file
		mixIndex(publishedPosts);
		
		// write rss/atom feed file
		if (publishedPosts.size() > 20) {
			writeFeed(publishedPosts.subList(0, 20));
		} else {
			writeFeed(publishedPosts);
		}
		
		System.out.println("...finished!");
	}

	private static void crawl(File path) {
		File[] contents = path.listFiles(getFileFilter());
		if (contents != null) {
			Arrays.sort(contents);
			for (int i = 0; i < contents.length; i++) {
				if (contents[i].isFile()) {
					System.out.print("Processing [" + contents[i].getPath() + "]... ");
					Content content = processFile(contents[i]);
					
					if (content.getType().equals(Type.POST)) {
						if (content.getStatus().equals(Status.PUBLISHED)) {
							publishedPosts.add(content);
						}
						if (content.getStatus().equals(Status.DRAFT)) {
							draftPosts.add(content);
						}
					}
					if (content.getType().equals(Type.PAGE)) {
						if (content.getStatus().equals(Status.PUBLISHED)) {
							publishedPages.add(content);
						}
						if (content.getStatus().equals(Status.DRAFT)) {
							draftPages.add(content);
						}
					}
					
					mix(contents[i], content);
					
					System.out.println("done!");
				} 
				
				if (contents[i].isDirectory()) {
					crawl(contents[i]);
				}
			}
		}
	}
	
	private static void mix(File file, Content content) {
		String outputFilename = file.getPath().replace(source.getPath()+File.separator+"content", destination.getPath());
		outputFilename = outputFilename.substring(0, outputFilename.indexOf("."));
		
		if (content.getStatus().equals(Status.DRAFT)) {
			outputFilename = outputFilename + "-draft";
		}
		
		File outputFile = new File(outputFilename+".html");
		
		if (content.getType().equals(Type.POST)) {
			content.setUri(outputFile.getPath().replace(destination.getPath(), ""));
		}
		
		Map model = new HashMap();
		model.put("content", content);
		
		try {
			Template template = null;
			if (content.getType().equals(Type.POST)) {
				template = cfg.getTemplate("post.ftl");
			}
			if (content.getType().equals(Type.PAGE)) {
				template = cfg.getTemplate("page.ftl");
			}
			
			if (!outputFile.exists()) {
				outputFile.getParentFile().mkdirs();
				outputFile.createNewFile();
			}
			
			Writer out = new OutputStreamWriter(new FileOutputStream(outputFile));	
			template.process(model, out);
			out.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TemplateException e) {
			e.printStackTrace();
		}
		
	}
	
	private static void mixIndex(List<Content> posts) {
		File outputFile = new File(destination.getPath()+File.separator+"index.html");
		Map model = new HashMap();
		model.put("posts", posts);
		
		try {
			Template template = cfg.getTemplate("index.ftl");
			
			if (!outputFile.exists()) {
				outputFile.getParentFile().mkdirs();
				outputFile.createNewFile();
			}
			
			Writer out = new OutputStreamWriter(new FileOutputStream(outputFile));	
			template.process(model, out);
			out.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TemplateException e) {
			e.printStackTrace();
		}
	}
	
	private static void writeFeed(List<Content> posts) {
		File outputFile = new File(destination.getPath()+File.separator+"feed.xml");
		Map model = new HashMap();
		model.put("posts", posts);
		model.put("pubdate", new Date());
		
		try {
			Template template = cfg.getTemplate("feed.ftl");
			
			if (!outputFile.exists()) {
				outputFile.getParentFile().mkdirs();
				outputFile.createNewFile();
			}
			
			Writer out = new OutputStreamWriter(new FileOutputStream(outputFile));	
			template.process(model, out);
			out.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TemplateException e) {
			e.printStackTrace();
		}
		
	}
	
	private static Content processFile(File file) {
		Content content = new Content();
		
		String fileHeader = readHeader(file);
		String fileBody = readBody(file);
		
		Matcher matcher = null;
		String uri = file.getPath().replace(destination.getPath(), "");
		String title = "";
		Date date = new Date();
		String type = "";
		String tags = "";
		String status = "";
		
		// get title
		Pattern titlePattern = Pattern.compile("title=(.*)");
		matcher = titlePattern.matcher(fileHeader);
		if (matcher.find()) {
			title = matcher.group(1);
			content.setTitle(title);
		}
		
		// get date 
		Pattern datePattern = Pattern.compile("date=(.*)");
		matcher = datePattern.matcher(fileHeader);
		if (matcher.find()) {
			DateFormat df = new SimpleDateFormat("yyy-MM-dd");
			try {
				date = df.parse(matcher.group(1));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			content.setDate(date);
		}
		
		// get type
		Pattern typePattern = Pattern.compile("type=(.*)");
		matcher = typePattern.matcher(fileHeader);
		if (matcher.find()) {
			type = matcher.group(1);
			if (type.equalsIgnoreCase("post")) {
				content.setType(Type.POST);
			}
			if (type.equalsIgnoreCase("page")) {
				content.setType(Type.PAGE);
			}
		}
		
		// get tags
		Pattern tagsPattern = Pattern.compile("tags=(.*)");
		matcher = tagsPattern.matcher(fileHeader);
		if (matcher.find()) {
			tags = matcher.group(1);
			content.setTags(tags.split(","));
		}
		
		// get status
		Pattern statusPattern = Pattern.compile("status=(.*)");
		matcher = statusPattern.matcher(fileHeader);
		if (matcher.find()) {
			status = matcher.group(1);
			if (status.equalsIgnoreCase("draft")) {
				content.setStatus(Status.DRAFT);
			}
			if (status.equalsIgnoreCase("published")) {
				content.setStatus(Status.PUBLISHED);
			}
		}
		
		// get body
		content.setBody(fileBody);
		
		return content;
	}
	
	private static String readHeader(File file) {
		StringBuffer header = new StringBuffer();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = "";
			while (!(line = reader.readLine()).equals("~~~~~~")) {
				header.append(line + "\n");
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return header.toString();
	}
	
	private static String readBody(File file) {
		StringBuffer body = new StringBuffer();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = "";
			boolean inBody = false;
			while ((line = reader.readLine()) != null) {
				if (inBody) {
					body.append(line + "\n");
				}
				if (line.equals("~~~~~~")) {
					inBody = true;
				}
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return body.toString();
	}
	
	private static void copyMedia(File media) {
		File[] contents = media.listFiles();
		if (contents != null) {
			Arrays.sort(contents);
			for (int i = 0; i < contents.length; i++) {
				if (contents[i].isFile()) {
					System.out.print("Copying [" + contents[i].getPath() + "]... ");
					File sourceFile = contents[i];
					File destFile = new File(sourceFile.getPath().replace(source.getPath(), destination.getPath()));
					try {
						FileUtils.copyFile(sourceFile, destFile);
					} catch (IOException e) {
						e.printStackTrace();
					}
					System.out.println("done!");
				} 
				
				if (contents[i].isDirectory()) {
					copyMedia(contents[i]);
				}
			}
		}
		
	}
	
	private static FileFilter getFileFilter() {
		return new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				if (pathname.isFile()) {
					if (pathname.getPath().endsWith(".html")) {
						return true;
					} else if (pathname.getPath().endsWith(".md")) {
						return false;
					} else {
						return false;
					}
				} else {
					return true;
				}
			}
		};
	}
}
