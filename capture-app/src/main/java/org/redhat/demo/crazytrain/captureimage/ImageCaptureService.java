package org.redhat.demo.crazytrain.captureimage;


import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.WriteMode;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgcodecs.Imgcodecs;



import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@ApplicationScoped
public class ImageCaptureService {

    private final VideoCapture camera = new VideoCapture(0);

    public void captureAndUploadImage() {
        try {
            // Capture the image
            Mat image = new Mat();
            camera.read(image);

            // Save the image to a local file
            // Get the current timestamp
            long timestamp = System.currentTimeMillis();

            // Convert the timestamp to a string and append the file extension
            String filename = timestamp + ".jpg";
            if (!Imgcodecs.imwrite(filename, image)) {
                System.out.println("Failed to save image");
                return;
            }

            // Upload the local file to Dropbox
            String dropboxAccessToken = "";
            DbxRequestConfig config = DbxRequestConfig.newBuilder("test").build();
            DbxClientV2 client = new DbxClientV2(config, dropboxAccessToken);
            try (InputStream in = new FileInputStream(filename)) {
                client.files().uploadBuilder("/images/" + filename)
                        .withMode(WriteMode.OVERWRITE)
                        .uploadAndFinish(in);
            }catch (Exception e) {
                e.printStackTrace();
                System.out.println("Image capture and upload process failed: " + e.getMessage());
            }

            // Delete the local file
            Files.delete(Paths.get(filename));
        } catch (Exception e) {
            System.out.println("Image capture and upload process failed: " + e.getMessage());
        }
    }
}