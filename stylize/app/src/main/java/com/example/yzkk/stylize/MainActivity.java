package com.example.yzkk.stylize;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final int INPUT_SIZE = 224;
    private static final int IMAGE_MEAN = 117;
    private static final float IMAGE_STD = 1;
    private static final String MODEL_FILE = "file:///android_asset/stylize_quantized.pb";
    private Stylizer stylizer;
    private Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initModel();
        tip();
    }

    private void initModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    stylizer = Stylizer.create(getAssets(),
		                                       MODEL_FILE,
			    		                       INPUT_SIZE,
			                                   IMAGE_MEAN,
					                           IMAGE_STD);
	            } catch (final Exception e) {
	                throw new RuntimeException("Error initializing TensorFlow", e);
	            }
	        }
	    });
    }

    private void tip() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("You have arrived here!");
        builder.setTitle("Congratulation");
        builder.setPositiveButton("Yeah", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                MainActivity.this.finish();
            }
        });
        builder.create().show();
    }

}
