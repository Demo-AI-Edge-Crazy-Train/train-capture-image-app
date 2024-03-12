package org.redhat.demo.crazytrain.captureimage;

import org.eclipse.microprofile.faulttolerance.Asynchronous;
import org.jboss.logging.Logger;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CompletableFuture;
/**
 * ImageService is a service that saves images to the file system
 */

@ApplicationScoped
public class ImageService {
    private static final Logger LOGGER = Logger.getLogger(ImageService.class);
    // Save the image to the file system (asynchronously)
    @Asynchronous
    public CompletionStage<Boolean> saveImageAsync(Mat image, String filePath) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Save the image to the file system using the OpenCV library
                return Imgcodecs.imwrite(filePath, image);
            } catch (Exception e) {
                LOGGER.errorf("Failed to save image %s", e.getMessage());
                throw new RuntimeException("Failed to save image", e);
            }
        });
    }
}
