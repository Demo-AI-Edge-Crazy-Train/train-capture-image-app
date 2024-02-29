package org.redhat.demo.crazytrain.captureimage;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.enterprise.inject.Produces;

import io.quarkus.scheduler.Scheduled;

@ApplicationScoped
public class ScheduledCapture {

    @Inject
    ImageCaptureService imageCaptureService;

    @Scheduled(every = "1s")
    void captureAndUploadImage() {
        imageCaptureService.captureAndUploadImage();
    }
}
