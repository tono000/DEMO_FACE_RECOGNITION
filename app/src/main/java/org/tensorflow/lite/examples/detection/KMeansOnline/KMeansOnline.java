package org.tensorflow.lite.examples.detection.KMeansOnline;

import android.content.ContextWrapper;
import android.os.Build;
import android.util.Log;
import android.util.Pair;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.content.Context;

import androidx.annotation.RequiresApi;

import com.fasterxml.jackson.databind.JsonSerializer;

import org.checkerframework.checker.units.qual.C;
import org.tensorflow.lite.examples.detection.KMeansOnline.CosineDistance;
import org.tensorflow.lite.examples.detection.MainActivity;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

import static java.util.stream.Collectors.toSet;
import static org.tensorflow.lite.examples.detection.KMeansOnline.CosineDistance.cosDistance;

public class KMeansOnline {
    private float threshold;
    DataOutputStream dos;

    int n_clusters;
    private ArrayList<Centroid> centroids;
    private Vector<String> labels;

    String FILE_NAME;

    public KMeansOnline() {
        threshold = 0.2f;
        dos = null;
        n_clusters = 0;
        centroids = new ArrayList<>();
        labels = new Vector<>();
        FILE_NAME = "file:///android_asset/labelmap.txt";
    }

    public boolean isFileExists(String fileName) {
        File file = new File(fileName);
        return file.exists();
    }

    public int getNumClusters(String fileName) {
        int n = 0;
        try {
            File file = new File(fileName);
            Scanner scan = new Scanner(file);
            String data = scan.nextLine();
            n = Integer.parseInt(data);
            return n;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return n;
    }

//    public Centroid getCentroids(String fileName) {
//        try {
//            File file = new File(fileName);
//            Scanner scan = new Scanner(file);
//            ArrayList<Centroid> centroids = new ArrayList<Centroid>();
//            while (scan.hasNextLine()) {
//
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//    }

//    // Read num of clusters from the 1st line of file
//    public int getNumClusters(String FIlE_NAME) {
//        int n = 0;
//        if (isFileExists(FIlE_NAME) == true) {
//            FileInputStream fis = null;
//            BufferedReader br = null;
//
//            try {
//                fis = new FileInputStream(FIlE_NAME);
//                br = new BufferedReader(new InputStreamReader(fis));
//                String line = br.readLine();
//                n = Integer.parseInt(line);
//                br.close();
//                fis.close();
//                return n;
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return n;
//    }
//

//    public List<Centroid> getCentroids(String FILE_NAME) {
//        int n = 1;
//        String count;
//        BufferedReader reader;
//        BufferedWriter bw;
//        count = "";
//        ArrayList<Centroid> listCenTroid = new ArrayList<Centroid>();

//        try {
//            InputStream assetIs = getAssets().open("labelmap.txt");
//            OutputStream copyOs = openFileOutput("labelmap.txt", MODE_PRIVATE);
//            byte[] buffer = new byte[4096];
//            int bytesRead;
//            while ((bytesRead = assetIs.read(buffer)) != -1) {
//                copyOs.write(buffer, 0, bytesRead);
//            }
//            assetIs.close();
//            copyOs.close();
            // now you can open and modify the copy
//            OutputStream copyOs = getActivityopenFileOutput("labelmap.txt", MODE_APPEND);
//
//            BufferedWriter writer =
//                    new BufferedWriter(
//                            new OutputStreamWriter(copyOs));
//            String strings = "";
//            for (int i = 0; i < listCenTroid.size(); i++){
//
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//
//        try {
//            reader = new BufferedReader(new InputStreamReader(getAssets().open("labelmap.txt")));
//            String mLine;
//            while ((mLine = reader.readLine()) != null) {
//                //process line
//                if (n == 1){
//                    count = mLine;
//                }
//                if ( n > 1){
//                    int id = 0;
//                    String vector = "";
//                    if (n % 2 == 0) {
//                        id = Integer.parseInt(mLine);
//                        n++;
//                    }
//                    if (n % 2 == 1){
//                        vector = reader.readLine();
//                    }
//                    Centroid cenTroid = new Centroid(id, readCentroid(vector));
//                    Log.e("eeeee",Integer.toString(id));
//                    listCenTroid.add(cenTroid);
//                }
//                n++;
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (reader != null) {
//                try {
//                    reader.close();
//                } catch (IOException e) {
//                    //log the exception
//                }
//            }
//        }
//    }

            // Read labels and centroids from file
//    @RequiresApi(api = Build.VERSION_CODES.O)
//    public List<Centroid> getCentroids(String FILE_NAME) {
//        int n = getNumClusters(FILE_NAME);
//        List<Centroid> cent_list = new ArrayList<>();
//
//        if (isFileExists(FILE_NAME) == true) {
//            FileInputStream fis = null;
//            BufferedReader br = null;
//
//
//            try {
//                fis = new FileInputStream(FILE_NAME);
//                br = new BufferedReader(new InputStreamReader(fis));
//                String line = br.readLine();
//
//                while (line != null) {
//                    for (int i = 1; i <= 7; i += 2) {
//                        Centroid new_centroid = new Centroid();
//
//                        // Num of images in cluster
//                        line = Files.readAllLines(Paths.get("file:///android_asset/labelmap.txt")).get(i * 2);
//                        new_centroid.setNum(line);
//
//                        // Centroid values
//                        line = Files.readAllLines(Paths.get("file:///android_asset/labelmap.txt")).get(i * 2 + 1);
//                        new_centroid.readCentroid(line);
//
//                        // Add centroid read from file to list centroids
//                        cent_list.add(new_centroid);
//                    }
//                }
//                return cent_list;
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return cent_list;
//    }



            // Write num of clusters and centroids to file
    public boolean writeToFile(String fileName, String dataLine,
                               boolean isAppendMode, boolean isNewLine) throws FileNotFoundException {
        dos = null;

        if (isNewLine) {
            dataLine = "\n" + dataLine;
        }

        try {
            File outFile = new File(fileName);
            if (isAppendMode) {
                dos = new DataOutputStream(new FileOutputStream(fileName, true));
            } else {
                dos = new DataOutputStream(new FileOutputStream(outFile));
            }
            dos.writeBytes(dataLine);
            dos.close();
        } catch (FileNotFoundException ex) {
            return (false);

        } catch (IOException ex) {
            return (false);
        }
        return (true);
    }


    public static float getDistance(float[][] p1, float[][] p2) {
        return cosDistance(p1, p2);
    }

    // Sum of 2 vectors
    public float[][] sumVector(float[][] v1, float[][] v2) {
        float[][] sum = new float[1][192];
        for (int i = 0; i < v1[0].length; i++) {
            sum[0][i] = v1[0][i] + v2[0][i];
        }
        return sum;
    }

    /* Re-compute new centroid when image is assigned to cluster
       v: old centroid
       n: old num of images in cluster
    */
    private float[][] computeCentroid(float[][] v, int n) {
        float [][] sum = new float[1][192];
        for (int i = 0; i < v[0].length; i++) {
            sum[0][i] = (sum[0][i] * n + 1) / (n + 1);
        }
        return sum;
    }

    // Update number of images and centroid in each cluster
//    private void updateCentroid(Centroid cent, float[][] vec) {
//        int n = cent.getNum() + 1;
//        cent.setNum(n);
//        for (int i = 0; i < vec[0].length; i++) {
//            cent.setCoordinate(vec);
//        }
//    }
//


//    public void assignCentroid(float[][] emb, List<Centroid> centroids, String FILE_NAME) throws FileNotFoundException {
//        // number of clusters got from label.txt
//        n_clusters = getNumClusters(FILE_NAME);
//
//        // no cluster
////        if (n_clusters == 0) {
////            // Init new centroid
////            Centroid newcentroid = new Centroid();
////            this.centroids.add(newcentroid); // Add new centroid to centroits list
////            n_clusters += 1; // Update num of centroids
////            // Rewrite num of cluster in txt
////
////            // write new n and centroid to file
////            // func used to write n and centroid
////            String num = String.valueOf(newcentroid.getNum() + 1);
////            String centroidString = "";
////            for (int i = 0; i < newcentroid.getCoordinates()[0].length; i++) {
////                centroidString += newcentroid.getCoordinates()[0][i] + " ";
////            }
////            boolean writeFileNum = writeToFile(FILE_NAME, num, true, true);
////            boolean writeFileString = writeToFile(FILE_NAME, centroidString, true, true);
////        }
//        //else {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            centroids = getCentroids(FILE_NAME);
//        }
//        Centroid nearest = nearestCentroid(emb, centroids);
//
//        // Re-compute centroid
////        float[][] temp = computeCentroid(nearest.getCoordinates(), nearest.getNum());
////        updateCentroid(nearest, temp);
//
//        // Write to file
////        String num = String.valueOf(nearest.getNum() + 1);
////        String centroidString = "";
////        for (int i = 0; i < nearest.getCoordinates()[0].length; i++) {
////            centroidString += nearest.getCoordinates()[0][i] + " ";
////        }
////        boolean writeFileNum = writeToFile(FILE_NAME, num, true, true);
////        boolean writeFileString = writeToFile(FILE_NAME, centroidString, true, true);
//        //}
//    }


}