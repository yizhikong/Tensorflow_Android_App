package com.example.yzkk.stylize;

/**
 * Created by yzkk on 17-3-13.
 */
import android.content.res.AssetManager;
import android.graphics.Bitmap;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

public class Stylizer {

    private TensorFlowInferenceInterface inferenceInterface;

    private static final String INPUT_NODE = "img_placeholder";
    private static final String OUTPUT_NODE = "add_37";


    public Stylizer() {
    }

    public static Stylizer create(AssetManager assetManager,
                                  String modelFileName) {
        Stylizer stylizer = new Stylizer();
        stylizer.inferenceInterface = new TensorFlowInferenceInterface();
        if (stylizer.inferenceInterface.initializeTensorFlow(assetManager, modelFileName) != 0) {
            throw new RuntimeException("TF initialization failed");
        }
	return stylizer;
    }

    public static void stylizeImage(final Bitmap img, Stylizer stylizer) {
        int[] intValues = new int[img.getWidth() * img.getHeight()];
        float[] floatValues = new float[img.getWidth() * img.getHeight() * 3];
        img.getPixels(intValues, 0, img.getWidth(), 0, 0, img.getWidth(), img.getHeight());

        for (int i = 0; i < intValues.length; ++i) {
            final int val = intValues[i];
            floatValues[i * 3] = ((val >> 16) & 0xFF);
            floatValues[i * 3 + 1] = ((val >> 8) & 0xFF);
            floatValues[i * 3 + 2] = (val & 0xFF);
        }

        // Copy the input data into TensorFlow.
        stylizer.inferenceInterface.fillNodeFloat(
                INPUT_NODE, new int[] {1, img.getWidth(), img.getHeight(), 3}, floatValues);
        stylizer.inferenceInterface.runInference(new String[] {OUTPUT_NODE});
        stylizer.inferenceInterface.readNodeFloat(OUTPUT_NODE, floatValues);

        for (int i = 0; i < intValues.length; ++i) {
            intValues[i] = 0xFF000000
                            | (((int) floatValues[i * 3]) << 16)
                            | (((int) floatValues[i * 3 + 1]) << 8)
                            | ((int) floatValues[i * 3 + 2]);
        }

        img.setPixels(intValues, 0, img.getWidth(), 0, 0, img.getWidth(), img.getHeight());
    }

}
