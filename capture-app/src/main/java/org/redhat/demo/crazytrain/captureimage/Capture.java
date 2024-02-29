// package  org.redhat.demo.crazytrain.captureimage;

// import com.dropbox.core.DbxRequestConfig;
// import com.dropbox.core.v2.DbxClientV2;
// import com.dropbox.core.v2.files.FileMetadata;
// import com.dropbox.core.v2.files.WriteMode;

// import org.opencv.core.Mat;
// import org.opencv.core.Size;
// import org.opencv.imgcodecs.Imgcodecs;
// import org.opencv.imgproc.Imgproc;
// import org.opencv.videoio.VideoCapture;

// import com.fasterxml.jackson.core.JsonProcessingException;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.databind.node.ObjectNode;

// import org.eclipse.paho.client.mqttv3.MqttClient;
// import org.eclipse.paho.client.mqttv3.MqttMessage;
// import org.jboss.logging.Logger;
// import org.eclipse.paho.client.mqttv3.MqttException;

// import jakarta.enterprise.context.ApplicationScoped;
// import jakarta.enterprise.event.Observes;
// import jakarta.inject.Inject;
// import io.quarkus.runtime.ShutdownEvent;
// import io.quarkus.runtime.StartupEvent;

// import java.io.IOException;
// import java.io.InputStream;
// import java.nio.file.Files;
// import java.nio.file.Paths;
// import java.util.Base64;
// import java.util.UUID;
// import java.util.concurrent.Executors;
// import java.util.concurrent.ScheduledExecutorService;
// import java.util.concurrent.TimeUnit;

// import javax.annotation.processing.FilerException;

// import org.opencv.imgcodecs.Imgcodecs;

// import java.io.ByteArrayOutputStream;
// import java.io.FileInputStream;
// import java.util.zip.GZIPOutputStream;


// @ApplicationScoped
// public class Capture {

//     private static final Logger LOGGER = Logger.getLogger(Capture.class);


//     private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
//     private MqttClient client;
//     private VideoCapture camera;
//     @Inject
//     private ObjectMapper mapper = new ObjectMapper();
//     void onStart(@Observes StartupEvent ev) {
//         // try {    
//             // client = new MqttClient("tcp://localhost:1883", "capture-image");
//             // client.connect();
//             scheduler.scheduleAtFixedRate(this::captureAndPublish, 0, 1, TimeUnit.SECONDS);
//         // } catch (MqttException e) {
//         //     throw new RuntimeException(e);
//         // }
//     }

//     void onStop(@Observes ShutdownEvent ev) {
//         scheduler.shutdown();
//         // if (client.isConnected()) {
//         //     try {
//         //         client.disconnect();
//         //     } catch (MqttException e) {
//         //         throw new RuntimeException(e);
//         //     }
//         // }
//     }

//     private void captureAndPublish() {
//         LOGGER.info("Capturing image");
//         camera = new VideoCapture(0); // Use default camera
//         if (!camera.isOpened()) {
//             camera.release();
//             return;
//         }

//         // Add a delay to give the camera time to initialize
//         try {
//             Thread.sleep(200);
//         } catch (InterruptedException e) {
//             e.printStackTrace();
//         }
//         Mat image = new Mat();
//         camera.read(image);
//         String filename = UUID.randomUUID().toString()+".jpg";
//         //Mat resizedImage = new Mat();
//         //Imgproc.resize(image, resizedImage, new Size(100, 100));
//         if (!Imgcodecs.imwrite(filename, image)) {
//             LOGGER.error("Failed to save image");
//         }
//         LOGGER.info("Uploading image to Dropbox");
//         uploadToDropbox(filename);
//         LOGGER.info("Image captured and published");
//           // Delete the local file
//           try {
//             Files.delete(Paths.get(filename));
//         } catch (Exception e) {
//             throw new RuntimeException("Failed to delete local file", e);
//         }
//                     // Convert the resized image to a byte array
//         // byte[] imageBytes = matToByteArray(resizedImage);

//         //  // Compress the byte array
//         // String compressedImage = compressMessage(imageBytes);
     
//         // ObjectNode node = mapper.createObjectNode().put("id", id).put("image", compressedImage);
//         // try {
//         //     String jsonMessage = mapper.writeValueAsString(node);
//         //     //LOGGER.infof("json message received from captured image before publish to the topic %s ", jsonMessage);
//         //     LOGGER.infof(" size of the message %s",jsonMessage.getBytes().length);
//         //     MqttMessage message = new MqttMessage(jsonMessage.getBytes());
//         //     message.setQos(1);
//         //     client.publish("train-image", message);
//         //     Files.write(Paths.get("output.json"), jsonMessage.getBytes());
//         // } catch (JsonProcessingException e) {
//         //     e.printStackTrace();
//         //     // Add additional error handling here if needed
//         // } catch (MqttException e) {
//         //     throw new RuntimeException(e);
//         // } catch (IOException e) {
//         //     e.printStackTrace();    
//         // }

//         camera.release();
//     }

//     private byte[] matToByteArray(Mat image) {
//         byte[] data = new byte[(int) (image.total() * image.channels())];
//         image.get(0, 0, data);
//         return data;
//     }

//     private String compressMessage(byte[] originalMessage) {
//             try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//                 GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
//                 gzipOutputStream.write(originalMessage);
//                 gzipOutputStream.close();
//                 return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
//             } catch (IOException e) {
//                 throw new RuntimeException("Failed to compress message", e);
//             }
//         }
//     private void uploadToDropbox(String filename) {
//         LOGGER.info("Uploading image to Dropbox");
//         try (InputStream in = new FileInputStream(filename)) {
//             DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/crazytrain/images").build();
//             DbxClientV2 client = new DbxClientV2(config, "sl.BwhQkWzGtt43vzTOzHu90VoJwySq89oy48ZBmw9ghSWx6SrpO18ia1xnYGaAes3XkcHx0n_QAIy03vO87bgXpwKsTt2U1ySBRGIE4m3W7BAVYhISVmcTFZd2_3tOugx166wNmt3BR7nM");
//             client.files().uploadBuilder("/" + filename).withMode(WriteMode.ADD).uploadAndFinish(in);
//             FileMetadata metadata = (FileMetadata)client.files().getMetadata("dropbox/crazytrain/images/" + filename);
//             LOGGER.info("File exists: " + metadata.getPathLower());
//         } catch (IOException e) {
//             throw new RuntimeException(e);
//         } catch (com.dropbox.core.DbxException e) {
//             throw new RuntimeException(e);
//         }
//         LOGGER.info("Image uploaded to Dropbox");
//     }   
// }

