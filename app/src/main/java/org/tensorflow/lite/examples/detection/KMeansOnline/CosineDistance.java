package org.tensorflow.lite.examples.detection.KMeansOnline;

import java.util.Collection;
import java.util.Map;

public class CosineDistance {

    // Cosine Distance
    public static float cosDistance(float[][] v1, float[][] v2) {
        float dotProduct = dotProduct(v1, v2);
        float sumNorm = vectorNorm(v1) * vectorNorm(v2);
        return dotProduct / sumNorm;
    }

    public static float dotProduct(float[][] v1, float[][] v2) {
        float result = 0f;
        for (int i = 0; i < v1[0].length; i++) {
            result += v1[0][i] * v2[0][i];
        }
        return result;
    }

    public static float vectorNorm(float[][] v) {
        float result = 0f;
        for (int i = 0; i < v[0].length; i++) {
            result += v[0][i] * v[0][i];
        }
        result = (float) Math.sqrt(result);
        return result;
    }
}
