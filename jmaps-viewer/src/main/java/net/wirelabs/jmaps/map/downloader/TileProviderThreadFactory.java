package net.wirelabs.jmaps.map.downloader;



import lombok.NonNull;

import java.util.concurrent.ThreadFactory;

public class TileProviderThreadFactory implements ThreadFactory {

    private int threadsSpawned = 0;

    @Override
    public Thread newThread(@NonNull Runnable runnable) {

        Thread thread = new Thread(runnable, "TileProvider-" + threadsSpawned++);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.setDaemon(true);
        return thread;
    }

}
