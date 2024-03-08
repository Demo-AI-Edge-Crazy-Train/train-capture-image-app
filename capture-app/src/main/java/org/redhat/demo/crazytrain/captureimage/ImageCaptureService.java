
package org.redhat.demo.crazytrain.captureimage;

import jakarta.inject.Singleton;
import jakarta.ws.rs.Path;

import java.nio.file.FileSystems;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.opencv.imgcodecs.Imgcodecs;

// Using Singleton here to make sure there won't be two instances of the OpenCV capture process running
@Singleton
public class ImageCaptureService {

    private static final Logger LOGGER = Logger.getLogger(ImageCaptureService.class);

    @ConfigProperty(name = "capture.videoDeviceIndex")
    int videoDeviceIndex;

    @ConfigProperty(name = "capture.tmpFolder")
    String tmpFolder;

    public ImageCaptureService() {
    }

    static {
        if(!System.getProperty("os.name").contains("Mac")){ // This is a workaround for the issue with OpenCV on Mac
            // Load the native OpenCV library
            LOGGER.info("Loading OpenCV library...");
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
     }
    }

    public Mat captureImage(VideoCapture camera) {
        try {
            // Create the OpenCV camera
            if (camera == null) {
                LOGGER.infof("Opening camera at index %d", videoDeviceIndex);
                camera = new VideoCapture(this.videoDeviceIndex);
            }
    
            // If somehow something goes wrong, reload the OpenCV camera
            if (!camera.isOpened()) {
                camera.release();
                camera.open(this.videoDeviceIndex);
            }

            // Last check before running a capture
            if(camera.isOpened() == false) {
                LOGGER.error("Error: Camera not opened");
                return null;
            }
                        // Read an image
            
                Mat image = new Mat();
                camera.read(image);
                if (image.empty()) {
                    LOGGER.error("Error: Image is empty");
                    return null;
                }
                // camera.release();
                // Save the image to a local file
               
                // LOGGER.infof("Saving image to %s and absolute path %s", filename, tmpFolder);
                // if (!Imgcodecs.imwrite(tmpFolder+"/"+filename, image)) {
                //     LOGGER.error("Failed to save image");
                //     return;
                // }
                // // Get the current timestamp
                // LOGGER.infof("Saving image to %s and absolute path %s", filename, tmpFolder);
                return image;           
        } catch (Exception e) {
            LOGGER.error("Image capture and upload process failed: " + e.getMessage());
            return null;
        }
    }

 
    
}