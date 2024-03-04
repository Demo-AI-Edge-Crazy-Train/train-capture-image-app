package org.redhat.demo.crazytrain.captureimage;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.logging.Logger;
import java.nio.file.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgcodecs.Imgcodecs;
import nu.pattern.OpenCV;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.WriteMode;










@ApplicationScoped
public class ImageCaptureService {

    private static final Logger LOGGER = Logger.getLogger(ImageCaptureService.class);

    // static {
    //     System.load("/deployments/lib/libopencv_java480.so");
    // }
    VideoCapture camera;

    public void captureAndUploadImage() {
        try {
            camera = new VideoCapture(0);
            // Capture the image
            if(camera.isOpened() == false) {
                System.out.println("Error: Camera not opened");
                return;
            }
            Mat image = new Mat();
            camera.read(image);

            // Save the image to a local file
            // Get the current timestamp
            long timestamp = System.currentTimeMillis();

            // Convert the timestamp to a string and append the file extension
            String filename = timestamp + ".jpg";
            if (!Imgcodecs.imwrite(filename, image)) {
                LOGGER.error("Failed to save image");
                return;
            }

            // Upload the local file to Dropbox
            // String dropboxAccessToken = "";
            // DbxRequestConfig config = DbxRequestConfig.newBuilder("test").build();
            // DbxClientV2 client = new DbxClientV2(config, dropboxAccessToken);
            // try (InputStream in = new FileInputStream(filename)) {
            //     client.files().uploadBuilder("/images/" + filename)
            //             .withMode(WriteMode.OVERWRITE)
            //             .uploadAndFinish(in);
            // }catch (Exception e) {
            //     e.printStackTrace();
            //     LOGGER.error("Image capture and upload process failed: " + e.getMessage());
            // }

            // Delete the local file
            Files.delete(Paths.get(filename));
        } catch (Exception e) {
            LOGGER.error("Image capture and upload process failed: " + e.getMessage());
        }
    }
}