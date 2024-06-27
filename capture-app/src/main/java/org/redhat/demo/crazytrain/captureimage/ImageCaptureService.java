
package org.redhat.demo.crazytrain.captureimage;

import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

/**
 * ImageCaptureService is a service that captures images from a camera using the OpenCV library
 */

// Using Singleton here to make sure there won't be two instances of the OpenCV capture process running
@Singleton
public class ImageCaptureService {

    private static final Logger LOGGER = Logger.getLogger(ImageCaptureService.class);
    // videoDeviceIndex is the index of the video device
    @ConfigProperty(name = "capture.videoDeviceIndex")
    int videoDeviceIndex;
    // tmpFolder is the folder where the images are saved
    @ConfigProperty(name = "capture.tmpFolder")
    String tmpFolder;

    public ImageCaptureService() {
    }

    // static {
    //     // Load the native OpenCV library
    //     if(!System.getProperty("os.name").contains("Mac")){ // This is a workaround for the issue with OpenCV on Mac
    //         // Load the native OpenCV library
    //         System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    //     }
    // }

    public Mat captureImage(VideoCapture camera) {
        try {
            // Create the OpenCV camera
            if (camera == null) {
                LOGGER.debugf("Opening camera at index %d", videoDeviceIndex);
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
            // Read an image from the camera
            Mat image = new Mat();
            camera.read(image);
            // Release the camera
            if (image.empty()) {
                LOGGER.error("Error: Image is empty");
                return null;
            }
            return image;           
        } catch (Exception e) {
            LOGGER.error("Image capture and upload process failed: " + e.getMessage());
            return null;
        }
    }
    public Mat readImage(String imagePath) {
            Mat mat = Imgcodecs.imread(imagePath);
            if (mat.empty()) {
                throw new IllegalArgumentException("Image not found at " + imagePath);
            }
            return mat;
    }
    public void releaseCamera(VideoCapture camera) {
        if (camera != null) {
            camera.release();

        }else{
            LOGGER.error("Error: Camera is null");
        }
    }
    
}