package net.wirelabs.jmaps.map.downloader;

import java.util.concurrent.ThreadFactory;

public class TileDownloaderThreadFactory implements ThreadFactory {

    int threadsSpawned = 0;

    @Override
    public Thread newThread(Runnable runnable) {

        Thread thread = new Thread(runnable, "TileDownloader-" + threadsSpawned++);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.setDaemon(true);
        return thread;
    }

}
