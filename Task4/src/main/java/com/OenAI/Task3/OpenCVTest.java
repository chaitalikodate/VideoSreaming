package com.OenAI.Task3;
 // Adjust the package name according to your project structure

import org.opencv.core.Core;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OpenCVTest implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(OpenCVTest.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // Load the OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        // Print the OpenCV version
        System.out.println("OpenCV Version: " + Core.VERSION);
    }
}



