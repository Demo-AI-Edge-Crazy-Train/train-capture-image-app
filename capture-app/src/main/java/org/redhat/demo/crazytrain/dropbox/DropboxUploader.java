package org.redhat.demo.crazytrain.dropbox;

import java.util.concurrent.*;

import org.jboss.logging.Logger;
import org.redhat.demo.crazytrain.util.Util;

/**
 * DropboxUploader is a class that uploads files to Dropbox.
 */

public class DropboxUploader {
    // queue is used to store the files to be uploaded
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    // ExecutorService is used to run the upload thread
    private final ExecutorService uploadExecutor = Executors.newSingleThreadExecutor();
    private static final Logger LOGGER = Logger.getLogger(DropboxUploader.class);
    // Constructor
    public DropboxUploader(String dtoken) {
        // Start the upload thread
        uploadExecutor.submit(() -> {
            while (true) {
                try {
                    // Take the next file from the queue
                    String filePath = queue.take(); // This will block if the queue is empty
                    try {
                        // Upload the file to Dropbox
                        Util.uploadToDropbox(filePath, dtoken);
                        LOGGER.debug("Uploaded file: " + filePath);
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
    // Add a file to the upload queue
    public void enqueueFileForUpload(String filePath) {
        // Add the file to the queue
        queue.add(filePath);
    }
}
