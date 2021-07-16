package org.tensorflow.lite.examples.detection.KMeansOnline;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Centroid {
    private String n;
    private float[][] coordinates;

    public Centroid() {
        this.n = "0";
        this.coordinates = new float[1][192];
    }

    public Centroid(String n, float[][] coordinates) {
        this.n = n;
        this.coordinates = coordinates;
    }

    public String getNum() { return n; }

    public float[][] getCoordinates() {
        return coordinates;
    }

    public void setNum(String num) { this.n = num; }

    public void setCoordinate(float[][] vector) {
        this.coordinates = vector;
    }

//    public float[][] readCentroid(String s) {
////        float[][] cent = new float[1][192];
////        List<String> list = Arrays.asList(s.split(" "));
////        for (int i = 0; i < 192; i++) {
////            cent[0][i] = Float.parseFloat(list.get(i));
////        }
////        return cent;
//        float[][] cent = new float[192][192];
//        String [] strings = s.split("\\s");
//        int i = 0;
//        for (String w : strings) {
//            cent[0][i] = Float.parseFloat(w);
//            i++;
//        }
//        return cent;
//    }
}