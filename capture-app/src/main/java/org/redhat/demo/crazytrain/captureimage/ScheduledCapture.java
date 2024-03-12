package org.redhat.demo.crazytrain.captureimage;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import java.util.Base64;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.jboss.logging.Logger;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.redhat.demo.crazytrain.mqtt.MqttPublisher;
import org.redhat.demo.crazytrain.util.Util;

import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;

@ApplicationScoped
public class ScheduledCapture {
    private  VideoCapture camera; 
    @Inject
    ImageCaptureService imageCaptureService;
    @Inject
    ImageService imageService;

    @ConfigProperty(name = "capture.interval")
    int interval;

    @ConfigProperty(name = "capture.tmpFolder") 
    String tmpFolder;

    @ConfigProperty(name = "capture.brokerMqtt")
    String broker;

    @ConfigProperty(name = "capture.topic")
    String topic;

    @ConfigProperty(name = "capture.nbImgSec")
    int nbImgSec;


    private static final Logger LOGGER = Logger.getLogger(ScheduledCapture.class);
    Util util = null;

    void onStart(@Observes StartupEvent ev) {
            camera = new VideoCapture(0); 
            camera.set(Videoio.CAP_PROP_FRAME_WIDTH, 640); // Max resolution for Logitech C505
            camera.set(Videoio.CAP_PROP_FRAME_HEIGHT, 480); // Max resolution for Logitech C505
            camera.set(Videoio.CAP_PROP_AUTOFOCUS, 0); // Try to disable autofocus
            camera.set(Videoio.CAP_PROP_FOCUS, 255); // Try to disable autofocus
            camera.set(Videoio.CAP_PROP_EXPOSURE, 15); // Try to set exposure
            util = new Util();
    }
    
    @Scheduled(every = "10s")
    void captureAndSaveImage() {
       //for(int i = 0; i < nbImgSec; i++) {
           // LOGGER.infof("iteration %s", i);
            Mat image = imageCaptureService.captureImage(this.camera);
            long timestamp = System.currentTimeMillis();
            String filepath = tmpFolder+"/" + timestamp + ".jpg";
            MqttPublisher mqttPublisher = new MqttPublisher(broker.trim(), topic.trim());
            if(util != null) {
                String jsonMessage = util.matToJson(image, timestamp);
               // LOGGER.infof("json message received from captured image before publish to the topic %s ", jsonMessage);
                LOGGER.infof(" size of the message %s",jsonMessage.getBytes().length);
                try {
                    mqttPublisher.publish(jsonMessage);
                    LOGGER.infof("Message published to topic: %s", topic);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }

        //     imageService.saveImageAsync(image, filepath).thenAccept(success -> {
        //         if (success) {
        //             LOGGER.infof("Image saved successfully");
        //         } else {
        //             LOGGER.error("Failed to save image");
        //         }
        //     });
        //     try {
        //         Thread.sleep(interval);
        //     } catch (InterruptedException e) {
        //         LOGGER.error("Error: Thread interrupted");
        //     }
       // }
    }
}