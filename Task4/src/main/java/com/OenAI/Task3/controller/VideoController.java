package com.OenAI.Task3.controller;

import com.OenAI.Task3.service.VideoProcessingService;
import com.OenAI.Task3.service.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.opencv.core.Mat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Controller
@RequestMapping("/video")
public class VideoController {

    @Autowired
    private VideoProcessingService videoProcessingService;

    @PostMapping("/start")
    public ResponseEntity<String> startVideo() {
        videoProcessingService.startCapture();
        return ResponseEntity.ok("Video capture started.");
    }

    @PostMapping("/stop")
    public ResponseEntity<String> stopVideo() {
        videoProcessingService.stopCapture();
        return ResponseEntity.ok("Video capture stopped.");
    }

    @GetMapping("/stream")
    public ResponseEntity<StreamingResponseBody> streamVideo() {
        StreamingResponseBody responseBody = outputStream -> {
            try {
                while (videoProcessingService.isRunning()) {
                    Mat frame = videoProcessingService.processFrame();
                    if (frame == null || frame.empty()) {
                        break;
                    }

                    BufferedImage bufferedImage = Utils.matToBufferedImage(frame);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(bufferedImage, "jpg", baos);
                    byte[] imageData = baos.toByteArray();

                    outputStream.write(("--frame\r\n" +
                            "Content-Type: image/jpeg\r\n" +
                            "Content-Length: " + imageData.length + "\r\n\r\n").getBytes());
                    outputStream.write(imageData);
                    outputStream.write("\r\n".getBytes());
                    outputStream.flush();

                    Thread.sleep(33); // ~30 FPS
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println("Streaming stopped.");
            }
        };

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("multipart/x-mixed-replace;boundary=frame"));
        headers.set("X-Accel-Buffering", "no");

        return new ResponseEntity<>(responseBody, headers, HttpStatus.OK);
    }
}
