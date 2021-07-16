package org.tensorflow.lite.examples.detection.MobileFaceNet;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;

import org.tensorflow.lite.examples.detection.MyUtil;
import org.tensorflow.lite.Interpreter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

    public class MobileFaceNet {
        private static final String MODEL_FILE = "MobileFaceNet_None.tflite";

        public static final int INPUT_IMAGE_SIZE = 112;

        private Interpreter interpreter;

        public MobileFaceNet(AssetManager assetManager) throws IOException {
            Interpreter.Options options = new Interpreter.Options();
            options.setNumThreads(4);
            interpreter = new Interpreter(MyUtil.loadModelFile(assetManager, MODEL_FILE), options);
        }

        public float[][] getEmbedding(Bitmap bitmap) {
            Bitmap bitmapScale = Bitmap.createScaledBitmap(bitmap, INPUT_IMAGE_SIZE, INPUT_IMAGE_SIZE, true);
            float[][] embedding = new float[1][192];
            float[][][][] datasets = getImageDatasets(bitmapScale);
            interpreter.run(datasets, embedding);
            MyUtil.l2Normalize(embedding, 1e-10);
            //for (int i = 0; i < embedding[0].length; i++)
             //   System.out.print(String.valueOf(embedding[0][i]) + " ");
            return embedding;
        }

    private float[][][][] getImageDatasets(Bitmap bitmap) {
        int[] ddims = {1, INPUT_IMAGE_SIZE, INPUT_IMAGE_SIZE, 3};
        float[][][][] datasets = new float[ddims[0]][ddims[1]][ddims[2]][ddims[3]];
        datasets[0] = MyUtil.normalizeImage(bitmap);
        return datasets;
    }
}