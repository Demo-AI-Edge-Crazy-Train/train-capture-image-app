package org.redhat.demo.crazytrain.dropbox;

import java.util.concurrent.*;

import org.jboss.logging.Logger;
import org.redhat.demo.crazytrain.util.Util;



public class DropboxUploader {
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private final ExecutorService uploadExecutor = Executors.newSingleThreadExecutor();
    private static final Logger LOGGER = Logger.getLogger(DropboxUploader.class);

    public DropboxUploader(String dtoken) {
        uploadExecutor.submit(() -> {
            while (true) {
                try {
                    String filePath = queue.take(); // This will block if the queue is empty
                    try {
                        Util.uploadToDropbox(filePath, dtoken);
                        LOGGER.info("Uploaded file: " + filePath);
                    } catch (Exception e) {
                        // Handle the exception
                        LOGGER.error("Failed to upload file: " + e.getMessage());
                        e.printStackTrace();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    public void enqueueFileForUpload(String filePath) {
        queue.add(filePath);
    }
}
