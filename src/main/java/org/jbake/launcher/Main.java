package org.jbake.launcher;

import java.io.File;

import org.jbake.app.Oven;

/**
 * Launcher for JBake.
 * 
 * @author Jonathan Bullock <jonbullock@gmail.com>
 *
 */
public class Main {

	public static final String VERSION = "v2.0";
	private static final String USAGE = "Usage: bake <source path> <destination path>";
	
	/**
	 * Runs the app with the given arguments.
	 * 
	 * @param String[] args
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println(USAGE);
		} else {
			if (args.length != 2) {
				System.out.println(USAGE);
			} else {
				File source = new File(args[0]);
				File destination = new File(args[1]);
				if (!source.exists() || !destination.exists()) {
					System.out.println(USAGE);
				} else {
					try {
						Oven oven = new Oven(source, destination);
						oven.setupPaths();
						oven.bake();
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}
			}
		}
	}

}
