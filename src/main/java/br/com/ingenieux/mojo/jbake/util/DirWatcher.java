package br.com.ingenieux.mojo.jbake.util;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Example to watch a directory (or tree) for changes to files.
 */

public class DirWatcher {

  private final FileAlterationObserver observer;

  private final FileAlterationMonitor monitor;

  private final BlockingQueue<Long> changeQueue = new ArrayBlockingQueue<Long>(1);

  /**
   * Creates a WatchService and registers the given directory
   */
  public DirWatcher(File dir) throws IOException {
    this.observer = new FileAlterationObserver(dir);
    this.monitor = new FileAlterationMonitor(1000, observer);

    observer.addListener(new FileAlterationListenerAdaptor() {
      @Override
      public void onFileCreate(File file) {
        onUpdated();
      }

      @Override
      public void onFileChange(File file) {
        onUpdated();
      }
    });
  }

  public void start() throws Exception {
    monitor.start();
  }

  public void stop() {
    try {
      monitor.stop();
    } catch (Exception exc) {
    }
  }

  private void onUpdated() {
    changeQueue.add(Long.valueOf(System.currentTimeMillis()));
  }

  /**
   * Process all events for keys queued to the watcher
   */
  public Long processEvents() throws InterruptedException {
    return changeQueue.poll(1, TimeUnit.SECONDS);
  }
}