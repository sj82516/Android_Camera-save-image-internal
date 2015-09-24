package yuanchieh.takepicture;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;


public class MainActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "MainActivity";

    private ImageView imageView;
    private DisplayMetrics displayMetrics;
    private Button btnCamera;
    private Button btnPhoto;
    private final static int CAMERA = 1;
    private final static int PHOTO = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        imageView = (ImageView) findViewById(R.id.img);
        btnCamera = (Button) this.findViewById(R.id.camera);
        btnCamera.setOnClickListener(this);
        btnPhoto = (Button) this.findViewById(R.id.photo);
        btnPhoto.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.camera) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, CAMERA);
            }

        } else if (view.getId() == R.id.photo) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, PHOTO);
        } else {
            Log.d(TAG, "Wrong");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

            File path = Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_PICTURES);
            File file = new File(path, "DemoPicture.jpg");
            try{
                path.mkdir();
                OutputStream os = new FileOutputStream(file);
                byte[] picData = stream.toByteArray();
                os.write(picData);
                os.close();

                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(file);
                mediaScanIntent.setData(contentUri);
                this.sendBroadcast(mediaScanIntent);
            }catch (IOException e){
                Log.w("ExternalStorage","Error " + file,e );
            }

        }else if(requestCode == PHOTO && resultCode == RESULT_OK){
            Uri uri = data.getData();
            ContentResolver contentResolver = this.getContentResolver();
            try{
                Bitmap bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri));
                if(bitmap.getWidth()>bitmap.getHeight())
                    ScalePic(bitmap,displayMetrics.heightPixels);
                else
                    ScalePic(bitmap,displayMetrics.widthPixels);
            }catch (FileNotFoundException e){
                Log.d(TAG,e.toString());
            }
        }else{
            Log.d(TAG,"photo wrong");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private void ScalePic(Bitmap bitmap,int phone)
    {
        float mScale = 1 ;

        if(bitmap.getWidth() > phone )
        {
            //判斷縮放比例
            mScale = (float)phone/(float)bitmap.getWidth();

            Matrix mMat = new Matrix() ;
            mMat.setScale(mScale, mScale);

            Bitmap mScaleBitmap = Bitmap.createBitmap(bitmap,
                    0,
                    0,
                    bitmap.getWidth(),
                    bitmap.getHeight(),
                    mMat,
                    false);
            imageView.setImageBitmap(mScaleBitmap);
        }
        else imageView.setImageBitmap(bitmap);
    }
}
