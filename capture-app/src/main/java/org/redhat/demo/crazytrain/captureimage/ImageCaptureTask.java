package org.redhat.demo.crazytrain.captureimage;



import java.nio.file.FileSystems;

import java.util.Base64;
import java.util.UUID;

import org.jboss.logging.Logger;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.redhat.demo.crazytrain.mqtt.MqttPublisher;



// Using Singleton here to make sure there won't be two instances of the OpenCV capture process running
//@Singleton
public class ImageCaptureTask implements Runnable{

    private static final Logger LOGGER = Logger.getLogger(ImageCaptureTask.class);
    private final int id;

    private final VideoCapture camera;
    private final int videoDeviceIndex;
    private final String tmpFolder;
    // private final MqttPublisher mqttPublisher;

    // public ImageCaptureTask(int id, VideoCapture camera, int videoDeviceIndex, String tmpFolder, MqttPublisher mqttPublisher) {
    //     this.id = id;
    //     this.camera = camera;
    //     this.videoDeviceIndex = videoDeviceIndex;
    //     this.tmpFolder = tmpFolder;
    //     this.mqttPublisher = mqttPublisher;
    // }
    public ImageCaptureTask(int id, VideoCapture camera, int videoDeviceIndex, String tmpFolder) {
        this.id = id;
        this.camera = camera;
        this.videoDeviceIndex = videoDeviceIndex;
        this.tmpFolder = tmpFolder;
    }

   
    public void run() {
        try {
            // If somehow something goes wrong, reload the OpenCV camera
            if (!camera.isOpened()) {
                camera.release();
                camera.open(this.videoDeviceIndex);
            }

            // // Last check before running a capture
            // if(camera.isOpened() == false) {
            //     LOGGER.error("Error: Camera not opened");
            //     return;
            // }
  
     
            // Read an image
            camera.set(Videoio.CAP_PROP_FRAME_WIDTH, 640); // Max resolution for Logitech C505
            camera.set(Videoio.CAP_PROP_FRAME_HEIGHT, 480); // Max resolution for Logitech C505
            camera.set(Videoio.CAP_PROP_AUTOFOCUS, 0); // Try to disable autofocus
            camera.set(Videoio.CAP_PROP_FOCUS, 255); // Try to disable autofocus
            camera.set(Videoio.CAP_PROP_EXPOSURE, 15); // Try to set exposure
           // Thread.sleep(200);
            for(int i = 0; i < 10; i++) {
                Mat image = new Mat();
                camera.read(image);
                if (image.empty()) {
                    LOGGER.error("Error: Image is empty");
                    return;
                }
                // camera.release();
                // Save the image to a local file
                long timestamp = System.currentTimeMillis();
    
                // Convert the timestamp to a string and append the file extension
                String filename  =  String.format("%d.jpg", timestamp);
                String absolutePath = FileSystems.getDefault().getPath(tmpFolder,filename).toString();
                // Get the current timestamp
                LOGGER.infof("Saving image to %s and absolute path %s", filename, absolutePath);
                // Convert the timestamp to a string and append the file extension
                //String filename = timestamp + ".jpg";
                //MatOfInt params = new MatOfInt(Imgcodecs.IMWRITE_JPEG_QUALITY, 100);
                // String imageBase64 = Base64.getEncoder().encodeToString(image.dump().getBytes());

                // // Create a JSON object with a unique ID and the image
                // String json = String.format("{\"id\": \"%s\", \"image\": \"%s\"}", UUID.randomUUID(), imageBase64);
    
                // Publish the JSON object as an MQTT message
               // mqttPublisher.publish(json);
    
                if (!Imgcodecs.imwrite(absolutePath, image)) {
                    LOGGER.error("Failed to save image");
                    return;
                }
                // uploader.enqueueFileForUpload(absolutePath);

                
                // Files.delete(Paths.get(absolutePath));
                // LOGGER.infof("file deleted %s", filename);
            }
            

        } catch (Exception e) {
            LOGGER.error("Image capture and upload process failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
}