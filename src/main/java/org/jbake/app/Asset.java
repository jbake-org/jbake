package org.jbake.app;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;

/**
 * Deals with assets (static files such as css, js or image files).
 *
 * @author Jonathan Bullock <jonbullock@gmail.com>
 *
 */
public class Asset {

    public static final String ASSET_FOLDER_NAME = "assets";

	private File source;
	private File destination;
	
	/**
	 * Creates an instance of Asset.
	 * 
	 * @param source
	 * @param destination
	 */
	public Asset(File source, File destination) {
		this.source = source;
		this.destination = destination;
	}

    public boolean isAsset(Path child)
            throws IOException {
        Path base = Paths.get(destination.getPath(), File.separator, ASSET_FOLDER_NAME);

        Path parentPath = child;
        while ( parentPath != null) {
            if (base.equals(parentPath)) {
                return true;
            }
            parentPath = parentPath.getParent();
        }
        return false;
    }

    /**
	 * Copy all files from supplied path. 
	 * 
	 * @param path	The starting path
	 */
	public void copy(File path) {
		File[] assets = path.listFiles();
		if (assets != null) {
			Arrays.sort(assets);
			for (int i = 0; i < assets.length; i++) {
				if (assets[i].isFile()) {
                    copySingleAsset(assets[i]);
				} 
				
				if (assets[i].isDirectory()) {
					copy(assets[i]);
				}
			}
		}
	}

    public void copySingleAsset(File asset) {
        System.out.print("Copying [" + asset.getPath() + "]... ");
        File sourceFile = asset;
        File destFile = new File(sourceFile.getPath().replace(source.getPath()+File.separator+ASSET_FOLDER_NAME, destination.getPath()));
        try {
            FileUtils.copyFile(sourceFile, destFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("done!");
    }
}
