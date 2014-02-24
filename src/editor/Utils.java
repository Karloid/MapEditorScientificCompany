package editor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Utils {

    public static String readFile(String fileName) {
        String string = "";
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(fileName));
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                string += line;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return string;
    }

    public static double getAngleTo(int x1, int y1, int x2, int y2) {
        int deltaX = x2 - x1;
        int deltaY = y2 - y1;
        double angleInRadians =  (Math.atan2(deltaY, deltaX));
        //   double angleInRadians =  (Math.atan2(deltaY, deltaX) * 180 / Math.PI);
        return angleInRadians;
    }

    public static Double getDistanceTo(int x, int y, int x1, int y1) {
        return Math.sqrt(Math.pow(x1 - x, 2) + Math.pow(y1 - y, 2));
    }
}