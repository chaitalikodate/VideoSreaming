package com.OenAI.Task3.service;



import org.opencv.core.*;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.video.BackgroundSubtractor;
import org.opencv.video.Video;
import org.opencv.videoio.VideoCapture;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class VideoProcessingService {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private CascadeClassifier faceCascade;
    private VideoCapture capture;
    private BackgroundSubtractor backgroundSubtractor;
    private Net objectDetectionNet;
    private boolean running;

    public VideoProcessingService() {
        faceCascade = new CascadeClassifier("C:\\Users\\Chitali Kodate\\Downloads\\haarcascade_frontalface_default.xml");
        backgroundSubtractor = Video.createBackgroundSubtractorMOG2();
        objectDetectionNet = Dnn.readNetFromCaffe(
                "C:\\Users\\Chitali Kodate\\Downloads\\deploy.prototxt",
                "C:\\Users\\Chitali Kodate\\Downloads\\res10_300x300_ssd_iter_140000_fp16.caffemodel"
        );
        running = false;
    }

    public void startCapture() {
        if (!running) {
            capture = new VideoCapture(0);
            running = true;
        }
    }

    public void stopCapture() {
        if (running) {
            capture.release();
            running = false;
        }
    }

    public boolean isRunning() {
        return running;
    }

    public Mat processFrame() {
        if (!running || capture == null) {
            return null;
        }

        Mat frame = new Mat();
        if (capture.read(frame)) {
            if (!frame.empty()) {
                
                Mat grayFrame = new Mat();
                Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
                Rect[] faces = detectFaces(grayFrame);
                for (Rect face : faces) {
                    Imgproc.rectangle(frame, face.tl(), face.br(), new Scalar(0, 255, 0), 2);
                }

                
                Mat fgMask = new Mat();
                backgroundSubtractor.apply(frame, fgMask);
                Imgproc.GaussianBlur(fgMask, fgMask, new Size(15, 15), 0);
                Imgproc.threshold(fgMask, fgMask, 25, 255, Imgproc.THRESH_BINARY);

                List<Rect> movingObjects = detectMotion(fgMask);
                for (Rect obj : movingObjects) {
                    Imgproc.rectangle(frame, obj.tl(), obj.br(), new Scalar(0, 0, 255), 2);
                }

               
                Mat detectedObjects = detectObjects(frame);
                if (detectedObjects != null) {
                    frame = detectedObjects;
                }

                return frame;
            } else {
                System.out.println("Captured frame is empty.");
            }
        } else {
            System.out.println("Failed to capture frame.");
        }
        return null;
    }

    private Rect[] detectFaces(Mat grayFrame) {
        MatOfRect faces = new MatOfRect();
        faceCascade.detectMultiScale(grayFrame, faces);
        return faces.toArray();
    }

    private List<Rect> detectMotion(Mat fgMask) {
       
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(fgMask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        List<Rect> movingObjects = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            Rect boundingRect = Imgproc.boundingRect(contour);
            if (boundingRect.area() > 500) { // Filtering small areas
                movingObjects.add(boundingRect);
            }
        }

        return movingObjects;
    }

    private Mat detectObjects(Mat frame) {
        Mat blob = Dnn.blobFromImage(frame, 1.0, new Size(300, 300), new Scalar(104, 177, 123), false, false);
        objectDetectionNet.setInput(blob);
        Mat detection = objectDetectionNet.forward();

        
        if (detection.empty() || detection.size(2) <= 0 || detection.size(3) != 7) {
            System.out.println("Invalid detection matrix size: " + detection.size());
            return frame;
        }

        Mat result = frame.clone();
        int rows = (int) detection.size(2);  

        for (int i = 0; i < rows; i++) {
         
            int rowOffset = i * (int) detection.size(3);
            float[] detectionData = new float[7];
            detection.get(0, rowOffset, detectionData);

            float confidence = detectionData[2];
            if (confidence > 0.5) {
                int startX = (int) (detectionData[3] * frame.cols());
                int startY = (int) (detectionData[4] * frame.rows());
                int endX = (int) (detectionData[5] * frame.cols());
                int endY = (int) (detectionData[6] * frame.rows());

                
                startX = Math.max(0, Math.min(startX, frame.cols() - 1));
                startY = Math.max(0, Math.min(startY, frame.rows() - 1));
                endX = Math.max(0, Math.min(endX, frame.cols() - 1));
                endY = Math.max(0, Math.min(endY, frame.rows() - 1));

                Imgproc.rectangle(result, new Point(startX, startY), new Point(endX, endY), new Scalar(255, 0, 0), 2);
            }
        }
        return result;
    }
}
