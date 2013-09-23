package org.jbake.app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtil {

	public static void extract(InputStream is, File outputPath) throws IOException {
		ZipInputStream zis = new ZipInputStream(is);
		ZipEntry entry;
		byte[] buffer = new byte[1024];
		
		while ((entry = zis.getNextEntry()) != null) {
			File outputFile = new File(outputPath.getCanonicalPath() + File.separatorChar + entry.getName());
			File outputParent = new File(outputFile.getParent());
			outputParent.mkdirs();
			
			if (entry.isDirectory()) {
				if (!outputFile.exists()) {
					outputFile.mkdir();
				}
			} else {
				FileOutputStream fos = new FileOutputStream(outputFile);
				
				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}

				fos.close();
			}
		}
	}
}
