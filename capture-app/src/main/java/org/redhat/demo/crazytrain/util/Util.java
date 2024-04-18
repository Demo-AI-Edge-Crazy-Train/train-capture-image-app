package  org.redhat.demo.crazytrain.util;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.WriteMode;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


import org.jboss.logging.Logger;

import jakarta.inject.Inject;


import java.io.IOException;
import java.io.InputStream;

import java.util.Base64;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.zip.GZIPOutputStream;

/**
 * Util is a utility class that provides methods to convert images to JSON, compress messages, and upload files to Dropbox.
 */

public class Util {

    private static final Logger LOGGER = Logger.getLogger(Util.class);
    private static final Object lock = new Object();
    // ObjectMapper is used to convert the image to JSON
    @Inject
    private ObjectMapper mapper = new ObjectMapper();

    // Write the image to a file
    public void writeFile(String data, String filename){
        try {
              BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
              writer.write(data);
              writer.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to write file", e);
        }
    }
    // Convert the image to JSON
    public String matToJson(Mat image, long id) {            
        // Encode the image into a WebP format
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".webp", image, matOfByte, new MatOfInt(Imgcodecs.IMWRITE_WEBP_QUALITY, 80));
        byte[] imageBytes = matOfByte.toArray();
        String jsonMessage = null;
        String base64String = Base64.getEncoder().encodeToString(imageBytes);
        LOGGER.debug("Legnth image converted to base64   "+base64String.length());
        ObjectNode node = mapper.createObjectNode().put("id", id).put("image", base64String);
         try {
             jsonMessage = mapper.writeValueAsString(node);
            //writeFile(jsonMessage, "test.json");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            // Add additional error handling here if needed
        } catch (IOException e) {
            e.printStackTrace();    
        }
        return jsonMessage;
    }
    // Convert the image to a byte array
    public  byte[] matToByteArray(Mat image) {
        byte[] data = new byte[(int) (image.total() * image.channels())];
        image.get(0, 0, data);
        return data;
    }
    // Compress the message
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
    // Upload the image to Dropbox
    public static void uploadToDropbox(String filepath, String token) {
        // Synchronize the method to avoid concurrent access
        synchronized (lock) {
            LOGGER.debug("Uploading image to Dropbox with token "+token);
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
            LOGGER.debug("Image uploaded to Dropbox");
        }
    }   
}

