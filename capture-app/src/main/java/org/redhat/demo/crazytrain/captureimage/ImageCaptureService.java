package org.redhat.demo.crazytrain.captureimage;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.opencv.videoio.VideoCapture;
import org.redhat.demo.crazytrain.mqtt.MqttPublisher;

import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class ImageCaptureService {
    private static final Logger LOGGER = Logger.getLogger(ImageCaptureService.class);
    private final VideoCapture camera = new VideoCapture(0); // Use default camera
    private final String tmpFolder = "/tmp/crazy-train-images";
    @ConfigProperty(name = "capture.dropbox.token")
    private  String dtoken;

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);

    void onStart(@Observes StartupEvent ev) {
        // Put your startup logic here
        for (int i = 0; i < 10; i++) {
           // MqttPublisher mqttPublisher = new MqttPublisher("tcp://localhost:1883", "train-image");
            executor.scheduleAtFixedRate(new ImageCaptureTask(i, camera, 0,tmpFolder), 0, 1, TimeUnit.SECONDS);
        }
    }  
}
