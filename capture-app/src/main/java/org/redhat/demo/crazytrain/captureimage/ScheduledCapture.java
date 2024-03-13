package org.redhat.demo.crazytrain.captureimage;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;


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


/**
 * ScheduledCapture is a service that captures images from a camera using the OpenCV library
 */

@ApplicationScoped
public class ScheduledCapture {
    private  VideoCapture camera; 
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
    @ConfigProperty(name = "capture.nbImgSec")
    int nbImgSec;

    @ConfigProperty(name = "capture.saveImage")
    boolean saveImage;


    private static final Logger LOGGER = Logger.getLogger(ScheduledCapture.class);
    Util util = null;
    // Start the camera when the application starts and set the resolution
    void onStart(@Observes StartupEvent ev) {
            camera = new VideoCapture(0); 
            camera.set(Videoio.CAP_PROP_FRAME_WIDTH, 640); // Max resolution for Logitech C505
            camera.set(Videoio.CAP_PROP_FRAME_HEIGHT, 480); // Max resolution for Logitech C505
            camera.set(Videoio.CAP_PROP_AUTOFOCUS, 0); // Try to disable autofocus
            camera.set(Videoio.CAP_PROP_FOCUS, 255); // Try to disable autofocus
            camera.set(Videoio.CAP_PROP_EXPOSURE, 15); // Try to set exposure
            util = new Util();
    }
    
    // Capture and save a defined number of images every second
    @Scheduled(every = "10s")
    void captureAndSaveImage() {
        // Capture and save a defined number of images every second
       for(int i = 0; i < nbImgSec; i++) {
            // Capture the image
            Mat image = imageCaptureService.captureImage(this.camera);
            // Publish the image to the MQTT broker
            long timestamp = System.currentTimeMillis();
            MqttPublisher mqttPublisher = new MqttPublisher(broker.trim(), topic.trim());
            if(util != null) {
                String jsonMessage = util.matToJson(image, timestamp);
                try {
                    mqttPublisher.publish(jsonMessage);
                    LOGGER.infof("Message with id %s published to topic: %s", timestamp, topic);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
            // Save the image to the file system (asynchronously)
            if(saveImage){
                String filepath = tmpFolder+"/" + timestamp + ".jpg";
                imageService.saveImageAsync(image, filepath).thenAccept(success -> {
                        if (success) {
                            LOGGER.infof("Image saved successfully");
                        } else {
                            LOGGER.error("Failed to save image");
                        }
                    });
            }
            try {
                // Sleep for the defined interval
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                LOGGER.error("Error: Thread interrupted");
            }
       }
    }
}