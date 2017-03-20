package com.example.yzkk.stylize;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final int INPUT_SIZE = 224;
    private static final int IMAGE_MEAN = 117;
    private static final float IMAGE_STD = 1;
    private static final String MODEL_FILE = "file:///android_asset/wave.pb";
    private Stylizer stylizer;
    private Executor executor = Executors.newSingleThreadExecutor();

    private  Button open_btn;
    private  Button stylize_btn;
    private  Button save_btn;

    private ImageView img_view;
    private Bitmap bitmap;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initModel();

        open_btn = (Button)findViewById(R.id.open_btn);
        stylize_btn = (Button)findViewById(R.id.stylize_btn);
        save_btn = (Button)findViewById(R.id.save_btn);
        img_view = (ImageView)findViewById(R.id.img_view);



        open_btn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 1);
            }
        });

        stylize_btn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                try {
                    Stylizer.stylizeImage(bitmap, stylizer);
                    img_view.setImageBitmap(bitmap);
                    save_btn.setEnabled(true);
                } catch (Exception e) {
                    Log.e("Exception", e.getMessage(), e);
                }
            }
        });

        save_btn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
                String fileName = "stylize" + UUID.randomUUID().toString();
                try {
                    // it's okay! as long as you should open the permission in the AVD!
                    File file = new File(dir + fileName + ".jpg");
                    if (!file.exists())
                        file.createNewFile();
                    file.setWritable(Boolean.TRUE);
                    FileOutputStream out = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                    Uri uri = Uri.fromFile(file);
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
                    Toast.makeText(MainActivity.this, "saved", Toast.LENGTH_SHORT ).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    stylizer = Stylizer.create(getAssets(), MODEL_FILE);
	            } catch (final Exception e) {
	                throw new RuntimeException("Error initializing TensorFlow", e);
	            }
	        }
	    });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Uri uri = data.getData();
            Log.e("uri", uri.toString());
            ContentResolver cr = this.getContentResolver();
            try {
                Bitmap bm = BitmapFactory.decodeStream(cr.openInputStream(uri));
                bitmap = bm.copy(bm.getConfig(), true);
                img_view.setImageBitmap(bm);
                stylize_btn.setEnabled(true);
            } catch (FileNotFoundException e) {
                Log.e("Exception", e.getMessage(),e);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
