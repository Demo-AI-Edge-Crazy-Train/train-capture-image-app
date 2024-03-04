package org.redhat.demo.crazytrain.captureimage;

import jakarta.inject.Singleton;
import jakarta.ws.rs.Path;

import java.nio.file.FileSystems;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgcodecs.Imgcodecs;

// Using Singleton here to make sure there won't be two instances of the OpenCV capture process running
@Singleton
public class ImageCaptureService {

    private static final Logger LOGGER = Logger.getLogger(ImageCaptureService.class);
    private VideoCapture camera;

    @ConfigProperty(name = "capture.videoDeviceIndex")
    int videoDeviceIndex;

    @ConfigProperty(name = "java.io.tmpdir")
    String tmpFolder;

    public ImageCaptureService() {
    }

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public void captureImage() {
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
                return;
            }

            // Read an image
            Mat image = new Mat();
            camera.read(image);

            // Save the image to a local file
            // Get the current timestamp
            long timestamp = System.currentTimeMillis();

            // Convert the timestamp to a string and append the file extension
            String filename = FileSystems.getDefault().getPath(tmpFolder, String.format("%d.jpg", timestamp)).toString();
            if (!Imgcodecs.imwrite(filename, image)) {
                LOGGER.error("Failed to save image");
                return;
            }
        } catch (Exception e) {
            LOGGER.error("Image capture and upload process failed: " + e.getMessage());
        }
    }
}