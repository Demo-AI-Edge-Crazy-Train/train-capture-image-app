package org.redhat.demo.crazytrain.consumer;


import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.JsonNode;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;



@ApplicationScoped
public class MqttConsumer {
    private static final Logger LOGGER = Logger.getLogger(MqttConsumer.class);
    

     @Incoming("image")
     public void consume(String jsonMessage) {
         // Parse the JSON message
         try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(jsonMessage);
                long id = jsonNode.get("id").asLong();
                byte[] imageBytes  = jsonNode.get("image").binaryValue();
                Mat image = new Mat(480, 640, CvType.CV_8UC3);
                image.put(0, 0, imageBytes);
        
                String filename = "output.png";
                if (!Imgcodecs.imwrite(filename, image)) {
                    LOGGER.error("Failed to save image");
                }
        } catch (Exception e) {
            LOGGER.error("Error parsing JSON message", e);
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

    public void writeFile(byte[] data, String filename){
        try {
            OutputStream out = new FileOutputStream(filename);
            out.write(data);
            out.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to write file", e);
        }
    }
    public void writeFile(String data, String filename){
        try {
              BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
              writer.write(data);
              writer.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to write file", e);
        }
    }
}
