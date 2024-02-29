package org.redhat.demo.crazytrain.consumer;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Base64;
import java.util.zip.GZIPInputStream;


import io.smallrye.reactive.messaging.annotations.Blocking;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import org.eclipse.microprofile.reactive.messaging.Message;
import io.smallrye.reactive.messaging.annotations.Broadcast;
import jakarta.enterprise.context.ApplicationScoped;



import org.jboss.logging.Logger;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;



@ApplicationScoped
public class MqttConsumer {
    private static final Logger LOGGER = Logger.getLogger(MqttConsumer.class);
    
     private JsonObject lastMessage;

     @Incoming("image")
     public void consume(String jsonMessage) {
         // Parse the JSON message
         JsonReader jsonReader = null;
         JsonObject jsonObject = null;
         try {
            jsonReader = Json.createReader(new StringReader(jsonMessage));
            jsonObject = jsonReader.readObject();
            jsonReader.close();
         } catch (Exception e) {
             LOGGER.error("Error parsing JSON message", e);
         }
            // Get the id and base64 image string from the JSON
            String id = jsonObject.getString("id");
            String compressedImage = jsonObject.getString("image");
            LOGGER.info("Received image with id: " + id);
    
            // Decode the base64 image string to a byte array
            byte[] imageBytes = decompressMessage(compressedImage);

        // Convert the decompressed data to an OpenCV Mat object
             Mat image = Imgcodecs.imdecode(new MatOfByte(imageBytes), Imgcodecs.IMREAD_UNCHANGED);
             String filename = "output.png";
             if (!Imgcodecs.imwrite(filename, image)) {
                 System.out.println("Failed to save image");
             }
     }
        private byte[] decompressMessage(String compressedMessage) {
        byte[] decodedBytes = Base64.getDecoder().decode(compressedMessage);
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(decodedBytes);
             GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipInputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, len);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to decompress message", e);
        }
    }
}
