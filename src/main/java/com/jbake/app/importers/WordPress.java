package com.jbake.app.importers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.jbake.main.Content;
import com.jbake.main.Status;
import com.jbake.main.Type;

public class WordPress {
	
	private static final String USAGE = "Usage: wordpress <host> <database> <user> <pass> <destination_path>";
	
	private static String HOST;
	private static String DATABASE;
	private static String USER;
	private static String PASS;
	private static File DESTINATION;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println(USAGE);
		} else {
			if (args.length != 5) {
				System.out.println(USAGE);
			} else {
				HOST = args[0];
				DATABASE = args[1];
				USER = args[2];
				PASS = args[3];
				DESTINATION = new File(args[4]);
				if (!DESTINATION.exists()) {
					System.out.println(USAGE);
				} else {
					// deal with pages
					processPages();
					
					// deal with posts
					processPosts();
				}
			}
		}
	}

	private static void processPages() {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://"+HOST+"/"+DATABASE+"?user="+USER+"&password="+PASS+"&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull");
			
			stmt = conn.createStatement();
			rs = stmt.executeQuery("select * from wp_posts where post_type = 'page'");
			while (rs.next()) {
				Content content = new Content();
				content.setTitle(rs.getString("post_title"));
				Timestamp date = rs.getTimestamp("post_date_gmt");
				if (date == null) {
					date = rs.getTimestamp("post_date");
				}
				content.setDate(date);
				String status = rs.getString("post_status");
				if (status.equalsIgnoreCase("publish")) {
					content.setStatus(Status.PUBLISHED);
				}
				if (status.equalsIgnoreCase("draft")) {
					content.setStatus(Status.DRAFT);
				}
				content.setType(Type.PAGE);
				content.setBody(rs.getString("post_content"));
				
//				System.out.println("Post ID = [" + rs.getString("ID") + "]");
//				System.out.println("Page title = ["+ rs.getString("post_title") + "]");
//				System.out.println("Page status = ["+ rs.getString("post_status") + "]");
				
				String slug = rs.getString("post_name");
				if (slug.equals("")) {
					slug = rs.getString("post_title").toLowerCase().replaceAll("[^a-z0-9-]", "");
				}
				
				File outputFile = new File(DESTINATION+File.separator+slug+".html");
				writeFile(outputFile, content);
			}
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null)
					rs.close();
				
				if (stmt != null)
					stmt.close();
				
				if (conn != null) 
					conn.close();
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void processPosts() {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://"+HOST+"/"+DATABASE+"?user="+USER+"&password="+PASS+"&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull");
			
			stmt = conn.createStatement();
			rs = stmt.executeQuery("select * from wp_posts where post_type = 'post' and (post_status = 'draft' or post_status = 'publish')");
			while (rs.next()) {
				Content content = new Content();
				content.setTitle(rs.getString("post_title"));
				Timestamp date = rs.getTimestamp("post_date_gmt");
				if (date == null) {
					date = rs.getTimestamp("post_date");
				}
				content.setDate(date);
				String status = rs.getString("post_status");
				if (status.equalsIgnoreCase("publish")) {
					content.setStatus(Status.PUBLISHED);
				}
				if (status.equalsIgnoreCase("draft")) {
					content.setStatus(Status.DRAFT);
				}
				content.setType(Type.PAGE);
				content.setBody(rs.getString("post_content"));
				content.setTags(getTags(rs.getLong("ID")));
				
				String slug = rs.getString("post_name");
				if (slug.equals("")) {
					slug = rs.getString("post_title").toLowerCase().replaceAll("\\s", "-").replaceAll("[^a-z0-9-]", "");
				}
				
				SimpleDateFormat year = new SimpleDateFormat("yyyy");
				SimpleDateFormat month = new SimpleDateFormat("MM");
				SimpleDateFormat day = new SimpleDateFormat("dd");
				File outputFile = new File(DESTINATION+File.separator+(year.format(date))+File.separator+(month.format(date))+File.separator+(day.format(date))+File.separator+slug+".html");
				writeFile(outputFile, content);
			}
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null)
					rs.close();
				
				if (stmt != null)
					stmt.close();
				
				if (conn != null) 
					conn.close();
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static String[] getTags(long id) {
		StringBuffer tags = new StringBuffer();
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://"+HOST+"/"+DATABASE+"?user="+USER+"&password="+PASS);
			
			// deal with pages
			stmt = conn.createStatement();
			rs = stmt.executeQuery("select wp_terms.term_id, wp_terms.slug from wp_term_taxonomy inner join wp_term_relationships on wp_term_taxonomy.term_taxonomy_id = wp_term_relationships.term_taxonomy_id inner join wp_terms on wp_term_taxonomy.term_id = wp_terms.term_id where wp_term_taxonomy.taxonomy = 'post_tag' and wp_term_relationships.object_id = " + id);
			while (rs.next()) {
				tags.append(rs.getString("slug")+",");
//				System.out.println("Tag = " + rs.getString("slug"));
			}
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null)
					rs.close();
				
				if (stmt != null)
					stmt.close();
				
				if (conn != null) 
					conn.close();
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (tags.length() > 0) {
			return tags.toString().substring(0, tags.length()-1).split(",");
		} else {
			return new String[]{};
		}
	}
	
	private static void writeFile(File file, Content content) {
		System.out.print("Writing file [" + file.getPath() + "]...");
		try {
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			
			Writer out = new OutputStreamWriter(new FileOutputStream(file));
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			
			out.write("title=" + content.getTitle() + "\n");
			if (content.getDate() != null) {
				out.write("date=" + df.format(content.getDate()) + "\n");
			}
			out.write("type=" + content.getType().toString().toLowerCase() + "\n");
			if (content.getTags() != null && content.getTags().length > 0) {
				out.write("tags=" + content.getTagsAsString() + "\n");
			}
			out.write("status=" + content.getStatus().toString().toLowerCase() + "\n");
			out.write("~~~~~~\n");
			out.write("\n");
			out.write(content.getBody());
			
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("done!");
	}
}
