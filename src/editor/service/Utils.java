package editor.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class Utils {
    private Utils() { }

    public static String readFile(String fileName) {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(fileName));
            String line;
            while (true) {
                line = br.readLine();
                if (line == null)
                    break;
                sb.append(line);
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
        return sb.toString();
    }

    public static Collection<Integer> getIntersection(Collection<Integer> first, Collection<Integer> second) {
        Collection<Integer> intersection = new ArrayList<Integer>();
        for (Integer i : first) {
            if (second.contains(i))
                intersection.add(i);
        }
        return intersection;
    }
}