package  org.redhat.demo.crazytrain.util;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.WriteMode;

import org.opencv.core.Mat;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.jboss.logging.Logger;
import org.eclipse.paho.client.mqttv3.MqttException;

import jakarta.inject.Inject;


import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;



import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.zip.GZIPOutputStream;


public class Util {

    private static final Logger LOGGER = Logger.getLogger(Util.class);
    private static final Object lock = new Object();



    private MqttClient client;
    @Inject
    private ObjectMapper mapper = new ObjectMapper();
    
     

     void publishToMqtt(byte[] imageBytes, String topic, String id) {
                    // Convert the resized image to a byte array

        // Compress the byte array
        String compressedImage = compressMessage(imageBytes);
     
        ObjectNode node = mapper.createObjectNode().put("id", id).put("image", compressedImage);
         try {
             String jsonMessage = mapper.writeValueAsString(node);
             LOGGER.infof("json message received from captured image before publish to the topic %s ", jsonMessage);
             LOGGER.infof(" size of the message %s",jsonMessage.getBytes().length);
             MqttMessage message = new MqttMessage(jsonMessage.getBytes());
             message.setQos(1);
              client.publish(topic, message);
             Files.write(Paths.get("output.json"), jsonMessage.getBytes());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            // Add additional error handling here if needed
        } catch (MqttException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();    
        }
    }
     byte[] matToByteArray(Mat image) {
        byte[] data = new byte[(int) (image.total() * image.channels())];
        image.get(0, 0, data);
        return data;
    }

     String compressMessage(byte[] originalMessage) {
            try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
                gzipOutputStream.write(originalMessage);
                gzipOutputStream.close();
                return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
            } catch (IOException e) {
                throw new RuntimeException("Failed to compress message", e);
            }
        }
     public static void uploadToDropbox(String filepath, String token) {
        synchronized (lock) {
            LOGGER.info("Uploading image to Dropbox with token "+token);
            try (InputStream in = new FileInputStream(filepath)) {
                DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/crazytrain/images").build();
                DbxClientV2 client = new DbxClientV2(config, token);
                client.files().uploadBuilder("/" + filepath).withMode(WriteMode.ADD).uploadAndFinish(in);
                // FileMetadata metadata = (FileMetadata)client.files().getMetadata("dropbox/crazytrain/images/" + filename);
                // LOGGER.info("File exists: " + metadata.getPathLower());
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (com.dropbox.core.DbxException e) {
                throw new RuntimeException(e);
            }
            LOGGER.info("Image uploaded to Dropbox");
        }
    }   
}
