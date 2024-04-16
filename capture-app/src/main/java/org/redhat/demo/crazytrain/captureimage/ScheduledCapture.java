package org.redhat.demo.crazytrain.captureimage;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.jboss.logging.Logger;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.redhat.demo.crazytrain.mqtt.MqttPublisher;
import org.redhat.demo.crazytrain.util.Util;

import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import io.vertx.mutiny.core.Vertx;


/**
 * ScheduledCapture is a service that captures images from a camera using the OpenCV library
 */

@ApplicationScoped
@Path("/capture")
public class ScheduledCapture {
    private  VideoCapture camera; 
    private boolean captureEnabled = false;

    @Inject
    ImageCaptureService imageCaptureService;
    @Inject
    ImageService imageService;
    // interval in milliseconds
    @ConfigProperty(name = "capture.interval")
    int interval;
    // tmpFolder is the folder where the images are saved
    @ConfigProperty(name = "capture.tmpFolder") 
    String tmpFolder;
    // broker is the MQTT broker
    @ConfigProperty(name = "capture.brokerMqtt")
    String broker;
    // topic is the MQTT topic
    @ConfigProperty(name = "capture.topic")
    String topic;
    // nbImgSec is the number of images captured every second
    @ConfigProperty(name = "capture.periodicCapture")
    int periodicCapture;

    @ConfigProperty(name = "capture.saveImage")
    boolean saveImage;

    @ConfigProperty(name = "capture.videoDeviceIndex")
    int videoDeviceIndex;

    MqttPublisher mqttPublisher = null;

    private Long timerId;



    private static final Logger LOGGER = Logger.getLogger(ScheduledCapture.class);
    Util util = null;
    // Start the camera when the application starts and set the resolution
    void onStart(@Observes StartupEvent ev) {
        Logger.getLogger(ScheduledCapture.class).info("The application is starting...");
            camera = new VideoCapture(videoDeviceIndex); 
            camera.set(Videoio.CAP_PROP_FRAME_WIDTH, 640); // Max resolution for Logitech C505
            camera.set(Videoio.CAP_PROP_FRAME_HEIGHT, 480); // Max resolution for Logitech C505
            //camera.set(Videoio.CAP_PROP_AUTOFOCUS, 0); // Try to disable autofocus
            //camera.set(Videoio.CAP_PROP_FOCUS, 255); // Try to disable autofocus
            camera.set(Videoio.CAP_PROP_EXPOSURE, 15); // Try to set exposure
            util = new Util();
            mqttPublisher = new MqttPublisher(broker.trim(), topic.trim());

    }

    @Inject
    Vertx vertx;


    // Capture and save a defined number of images every second
    //@Scheduled(every = "PT0.1S")
    void captureAndSaveImage() {
        LOGGER.debugf("The Thread name is %s" + Thread.currentThread().getName());
        // if(!captureEnabled) {
        //     LOGGER.debug("Capture is disabled");
        //     return;
        // }
        // Capture and save a defined number of images every second
       //for(int i = 0; i < nbImgSec; i++) {
            // Capture the image
            long start = System.nanoTime();
            Mat image = imageCaptureService.captureImage(this.camera);
            long end = System.nanoTime();
            LOGGER.debugf("Time to capture image: %d ms", (end - start) / 1000000);
            // Publish the image to the MQTT broker
            long timestamp = System.currentTimeMillis();
            if(util != null) {
                long start2 = System.nanoTime();
                String jsonMessage = util.matToJson(image, timestamp);
                long end2 = System.nanoTime();
                LOGGER.debugf("Time to convert image to json: %d ms", (end2 - start2) / 1000000);
                LOGGER.debugf("JSON Message with id %s", jsonMessage);
                try {
                    long start3 = System.nanoTime();
                    mqttPublisher.publish(jsonMessage);
                    long end3 = System.nanoTime();
                    LOGGER.debugf("Time to publish image: %d ms", (end3 - start3) / 1000000);
                    LOGGER.debugf("Message with id %s published to topic: %s", timestamp, topic);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
            // Save the image to the file system (asynchronously)
            if(saveImage){
                String filepath = tmpFolder+"/" + timestamp + ".jpg";
                imageService.saveImageAsync(image, filepath).thenAccept(success -> {
                        if (success) {
                            LOGGER.debug("Image saved successfully");
                        } else {
                            LOGGER.error("Failed to save image");
                        }
                    });
            //}
            // try {
            //     // Sleep for the defined interval
            //     Thread.sleep(interval);
            // } catch (InterruptedException e) {
            //     LOGGER.error("Error: Thread interrupted");
            // }
       }
    }
    @GET
    @Path("/start")
    public Response startCapture() {
        LOGGER.info("Capture started");
        mqttPublisher.connect();
        //captureEnabled = true;
        if (timerId != null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Capture is already running").build();
        }

        timerId = vertx.setPeriodic(periodicCapture, id -> captureAndSaveImage());
        return Response.ok("Capture started").build();
    }

    @GET
    @Path("/stop")
    public Response stopCapture() {
       // captureEnabled = false;
        LOGGER.info("Capture stopped");
        if (timerId == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Capture is not running").build();
        }
        vertx.cancelTimer(timerId);
        timerId = null;
        imageCaptureService.releaseCamera(this.camera);
        mqttPublisher.disconnect();
        return Response.ok("Capture stopped").build();
    }
}