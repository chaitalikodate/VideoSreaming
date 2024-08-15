package com.OenAI.Task3.service.utils;

import java.awt.image.BufferedImage;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class Utils {
    public static BufferedImage matToBufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1) {
            Mat mat2 = new Mat();
            Imgproc.cvtColor(mat, mat2, Imgproc.COLOR_BGR2RGB);
            type = BufferedImage.TYPE_3BYTE_BGR;
            mat = mat2;
        }
        byte[] b = new byte[mat.channels() * mat.cols() * mat.rows()];
        mat.get(0, 0, b); // get all the pixels
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        image.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), b);
        return image;
    }
}
