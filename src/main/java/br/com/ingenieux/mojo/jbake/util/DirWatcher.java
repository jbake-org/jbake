package br.com.ingenieux.mojo.jbake.util;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

/**
 * Example to watch a directory (or tree) for changes to files.
 */

public class DirWatcher {
	private final WatchService watcher;

	private final Map<WatchKey, Path> keys;

	@SuppressWarnings("unchecked")
	static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>) event;
	}

	/**
	 * Register the given directory with the WatchService
	 */
	private void register(Path dir) throws IOException {
		WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE,
				ENTRY_MODIFY);
		keys.put(key, dir);
	}

	/**
	 * Register the given directory, and all its sub-directories, with the
	 * WatchService.
	 */
	private void registerAll(final Path start) throws IOException {
		// register directory and sub-directories
		Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir,
					BasicFileAttributes attrs) throws IOException {
				register(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	/**
	 * Creates a WatchService and registers the given directory
	 */
	public DirWatcher(Path dir) throws IOException {
		this.watcher = FileSystems.getDefault().newWatchService();
		this.keys = new HashMap<WatchKey, Path>();

		registerAll(dir);
	}

	/**
	 * Process all events for keys queued to the watcher
	 */
	public Long processEvents() {
		// wait for key to be signalled
		WatchKey key;
		try {
			key = watcher.poll(1L, TimeUnit.SECONDS);
		} catch (InterruptedException x) {
			return null;
		}
		
		if (null == key)
			return null;

		Path dir = keys.get(key);
		if (dir == null)
			throw new IllegalStateException("WatchKey not recognized!!");

		for (WatchEvent<?> event : key.pollEvents()) {
			Kind<?> kind = event.kind();

			// TBD - provide example of how OVERFLOW event is handled
			if (kind == OVERFLOW) {
				continue;
			}

			// Context for directory entry event is the file name of entry
			WatchEvent<Path> ev = cast(event);
			Path name = ev.context();
			Path child = dir.resolve(name);

			// if directory is created, and watching recursively, then
			// register it and its sub-directories
			if (kind == ENTRY_CREATE) {
				try {
					if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
						registerAll(child);
					}
				} catch (IOException x) {
					// ignore to keep sample readbale
				}
			}
		}

		// reset key and remove from set if directory no longer accessible
		boolean valid = key.reset();
		if (!valid) {
			keys.remove(key);

			// all directories are inaccessible
			if (keys.isEmpty()) {
				return null;
			}
		}
		
		return Long.valueOf(System.currentTimeMillis());
	}

	static void usage() {
		System.err.println("usage: java WatchDir [-r] dir");
		System.exit(-1);
	}
}