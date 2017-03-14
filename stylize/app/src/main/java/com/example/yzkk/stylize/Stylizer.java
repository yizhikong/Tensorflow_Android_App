package com.example.yzkk.stylize;

/**
 * Created by yzkk on 17-3-13.
 */
import android.content.res.AssetManager;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

public class Stylizer {

    private TensorFlowInferenceInterface inferenceInterface;
    private int inputSize;
    private int imageMean;
    private float imageStd;

    public Stylizer() {
    }

    public static Stylizer create(AssetManager assetManager,
                                  String modelFileName,
				  int inputSize,
				  int imageMean,
				  float imageStd) {
        Stylizer stylizer = new Stylizer();
        stylizer.inferenceInterface = new TensorFlowInferenceInterface();
        if (stylizer.inferenceInterface.initializeTensorFlow(assetManager, modelFileName) != 0) {
            throw new RuntimeException("TF initialization failed");
        }
	stylizer.inputSize = inputSize;
	stylizer.imageMean = imageMean;
	stylizer.imageStd = imageStd;
	return stylizer; 
    }

}
