package com.OenAI.Task3.service;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.MatOfRect;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;


@Service
public class VideoProcessingService {
	

	static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private CascadeClassifier faceCascade;
    private VideoCapture capture;

    public VideoProcessingService() {
        // Load the Haar Cascade XML file from the resources directory
        String xmlFile = getClass().getClassLoader().getResource("static/haarcascade_frontalface_default.xml").getPath();
        faceCascade = new CascadeClassifier(xmlFile);
        capture = new VideoCapture(0);
    }

    public Mat processFrame() {
        Mat frame = new Mat();
        if (capture.read(frame)) {
            Mat grayFrame = new Mat();
            Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
            Rect[] faces = detectFaces(grayFrame);
            for (Rect face : faces) {
                Imgproc.rectangle(frame, face.tl(), face.br(), new Scalar(0, 255, 0), 2);
            }
        }
        return frame;
    }

    private Rect[] detectFaces(Mat grayFrame) {
        MatOfRect faces = new MatOfRect();
        faceCascade.detectMultiScale(grayFrame, faces);
        return faces.toArray();
    }
}

