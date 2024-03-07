package org.redhat.demo.crazytrain.captureimage;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import org.jboss.logging.Logger;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;

@ApplicationScoped
public class ScheduledCapture {
    private  VideoCapture camera; 
    @Inject
    ImageCaptureService imageCaptureService;
    @Inject
    ImageService imageService;
    private static final Logger LOGGER = Logger.getLogger(ScheduledCapture.class);

    void onStart(@Observes StartupEvent ev) {
            camera = new VideoCapture(0); 
            camera.set(Videoio.CAP_PROP_FRAME_WIDTH, 640); // Max resolution for Logitech C505
            camera.set(Videoio.CAP_PROP_FRAME_HEIGHT, 480); // Max resolution for Logitech C505
            camera.set(Videoio.CAP_PROP_AUTOFOCUS, 0); // Try to disable autofocus
            camera.set(Videoio.CAP_PROP_FOCUS, 255); // Try to disable autofocus
            camera.set(Videoio.CAP_PROP_EXPOSURE, 15); // Try to set exposure
    }
    

    @Scheduled(every = "1s")
    void captureAndSaveImage() {
       for(int i = 0; i < 10; i++) {
           // LOGGER.infof("iteration %s", i);
            Mat image = imageCaptureService.captureImage(this.camera);
            long timestamp = System.currentTimeMillis();
            String filepath = "/tmp/crazy-train-images/image-" + timestamp + ".jpg";
            imageService.saveImageAsync(image, filepath).thenAccept(success -> {
                if (success) {
                    LOGGER.infof("Image saved successfully");
                } else {
                    LOGGER.error("Failed to save image");
                }
            });
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                LOGGER.error("Error: Thread interrupted");
            }
        }
    }
}