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



    @Inject
    private ObjectMapper mapper = new ObjectMapper();


    public String matToJson(Mat image, long id) {            
        byte[] imageBytes = matToByteArray(image);
        String jsonMessage = null;
        ObjectNode node = mapper.createObjectNode().put("id", id).put("image", imageBytes);
         try {
             jsonMessage = mapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            // Add additional error handling here if needed
        } catch (IOException e) {
            e.printStackTrace();    
        }
        return jsonMessage;
    }
    public  byte[] matToByteArray(Mat image) {
        byte[] data = new byte[(int) (image.total() * image.channels())];
        image.get(0, 0, data);
        return data;
    }

    public  String compressMessage(byte[] originalMessage) {
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

