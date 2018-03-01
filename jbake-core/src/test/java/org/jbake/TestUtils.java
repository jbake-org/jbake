package org.jbake;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.apache.commons.vfs2.util.Os;

public class TestUtils {
	
	/**
	 * Hides the assets on Windows that start with a dot (e.g. .test.txt but not test.txt) so File.isHidden() returns true for those files.
	 */
	public static void hideAssets(File assets) throws IOException, InterruptedException {
        if ( Os.isFamily(Os.OS_FAMILY_WINDOWS) ) {
			final File[] hiddenFiles = assets.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.startsWith(".");
				}
			});
			for (File file : hiddenFiles) {
				final Process process = Runtime.getRuntime().exec(new String[] {"attrib" , "+h", file.getAbsolutePath()});
				process.waitFor();
			}
		}
	}
}
