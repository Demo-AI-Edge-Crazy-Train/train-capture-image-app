package org.redhat.demo.crazytrain.captureimage;

import org.eclipse.microprofile.faulttolerance.Asynchronous;
import org.jboss.logging.Logger;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class ImageService {
    private static final Logger LOGGER = Logger.getLogger(ImageService.class);

    @Asynchronous
    public CompletionStage<Boolean> saveImageAsync(Mat image, String filePath) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return Imgcodecs.imwrite(filePath, image);
            } catch (Exception e) {
                LOGGER.errorf("Failed to save image %s", e.getMessage());
                throw new RuntimeException("Failed to save image", e);
            }
        });
    }
}
