package org.firstinspires.ftc.teamcode.mechanisms;

import org.opencv.core.Mat;
import org.opencv.objdetect.ArucoDetector;
import org.opencv.objdetect.DetectorParameters;
import org.opencv.objdetect.Dictionary;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;

import java.util.ArrayList;
import java.util.List;

public class Turret {
    public static void main(String[] args) {
        nu.pattern.OpenCV.loadLocally();

        Dictionary dict = Objdetect.getPredefinedDictionary(Objdetect.DICT_APRILTAG_36h11);
        ArucoDetector detector = new ArucoDetector(dict, new DetectorParameters());

        VideoCapture cap = new VideoCapture(2);
        /* try 0 for integrated camera, and 1, 2, or 3 for webcam.
        Alternately, go to terminal and input command 'v4l2-ctl --list-devices', and find desired output */
        if (!cap.isOpened()) {
            System.out.println("Your code is horrible lol :3");
            return;
        }

        Mat frame = new Mat();
        while (cap.read(frame)) {
            List<Mat> corners = new ArrayList<>();
            Mat ids = new Mat();
            detector.detectMarkers(frame, corners, ids);
            if (!ids.empty()) {
                System.out.println("Detected tag IDs: " + ids.dump());
            }
        }
        cap.release();
    }
}