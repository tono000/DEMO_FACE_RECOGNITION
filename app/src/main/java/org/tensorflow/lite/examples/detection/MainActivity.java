package org.tensorflow.lite.examples.detection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.tensorflow.lite.examples.detection.KMeansOnline.Centroid;
import org.tensorflow.lite.examples.detection.MTCNN.Align;
import org.tensorflow.lite.examples.detection.MTCNN.BorderedText;
import org.tensorflow.lite.examples.detection.MTCNN.Box;
import org.tensorflow.lite.examples.detection.MTCNN.MTCNN;
import org.tensorflow.lite.examples.detection.MTCNN.Util;
import org.tensorflow.lite.examples.detection.MobileFaceNet.MobileFaceNet;
import org.tensorflow.lite.examples.detection.KMeansOnline.KMeansOnline;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {

    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;
    private MTCNN detect;
    private MobileFaceNet mfn;
    private String filename = "file:///android_asset/labelmap.txt";
    private Context context;
    private KMeansOnline kmeans = new KMeansOnline();
    private BorderedText borderedText;
    private Canvas canvas;
    public static Bitmap imageBitmap;
    public static Bitmap bitmapCrop;

    public float[][] embedding = new float[1][192];
    public String text;
    ArrayList<Centroid> centroids = new ArrayList<>();

    public static final int PERMISSION_REQUESS = 15;
    public static final int MY_AVATAR_KEY = 1975;
    DeviceOrShoot ahihi = new DeviceOrShoot();
    public static final int REQUEST_IMAGE_CAPTURE = 1969;
    public static final int PERMISSION_CAPTURE = 2001;
    public static final int RESULT_LOAD_IMAGE = 2000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            detect = new     MTCNN(getAssets());
            mfn = new MobileFaceNet(getAssets());
            centroids = getCentroids();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Hàm đổi avt, gọi intent để chọn cách đổi từ camera hoặc gallery
    public void ChangeAVTOnclick(View view) {
        Intent avatar = new Intent(this, ahihi.getClass());
        startActivityForResult(avatar, MY_AVATAR_KEY);
    }

    public void ChangeBtnOnclick(View view) {
        EditText editText = (EditText) findViewById(R.id.textview2);
        text = editText.getText().toString();
        faceCrop(imageBitmap);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_AVATAR_KEY) { // Nhận result cho việc chọn cách đổi avt
            if (resultCode == DeviceOrShoot.RESULT_OK) {
                String reply = data.getStringExtra(DeviceOrShoot.MY_REPLY);
                if (reply.equals("camera"))
                    UseCamera();
                else UseGallery();
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {  // Nhận result cho cách đổi avt bằng camera
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            ImageView imgView = (ImageView) findViewById(R.id.avatar);
            imgView.setImageBitmap(imageBitmap);

        } else if (requestCode == RESULT_LOAD_IMAGE) {  // Nhận result cho cách đổi avt bằng gallery
            if (resultCode == RESULT_OK) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();
                EditText editText = (EditText) findViewById(R.id.textview2);
                editText.setText("");
                text=null;
                ImageView imageView = (ImageView) findViewById(R.id.avatar);
                imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                imageBitmap = BitmapFactory.decodeFile(picturePath);

            }
        }
        //centroids = getCentroids();
        System.out.println("Num of centroids: " + centroids.size());
        faceCrop(imageBitmap);
    }

        //faceCompare(bitmapCrop);

////        String dataline = "";
////        for (int i = 0; i < embedding[0].length; i++)
////            dataline += embedding[0][i];
////        try {
////            kmeans.writeToFile(filename, dataline, true, true);
////            System.out.println("Successful write to file.");
////        } catch (FileNotFoundException e) {
////            e.printStackTrace();
////        }
////        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            centroids = getCentroids();
//            System.out.println("Num of centroids: " + centroids.size());
////        }
//        try {
//            String label = "";
//            float dis = 0.0f;
//            Centroid nearest = nearestCentroid(embedding, centroids);
//            if (nearest != null) {
//                label = nearest.getNum();
//                dis = kmeans.getDistance(embedding, nearest.getCoordinates());
//                System.out.print("Class: " + label + " - Distance: " + dis);
//            }
//            TextView result = findViewById(R.id.result);
//            result.setText("Class: " + label + " - Distance: " + dis);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }

    // Hàm đổi avt bằng gallery
    private void UseGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MainActivity.PERMISSION_REQUESS);
        } else {
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, RESULT_LOAD_IMAGE);
        }
    }

    // Hàm đổi avt sử dụng camera
    private void UseCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_CAPTURE);
        }
        takePictureIntent();
    }

    // Intent chụp ảnh camera
    private void takePictureIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            //Them ham luu anh vao day
        } catch (ActivityNotFoundException e) {
            System.out.println(e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CAPTURE)          // Dành cho chụp ảnh
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.CAMERA)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        takePictureIntent();
                    }
                }
            }
        if (requestCode == PERMISSION_REQUESS)      // Dành cho lấy từ gallery
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Intent ii = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(ii, RESULT_LOAD_IMAGE);
                    } else Toast.makeText(this, "Permissinog denied...", Toast.LENGTH_SHORT).show();
                }
            }
    }

    private void faceCrop(Bitmap bitmap) {
        if (bitmap == null) {
            Toast.makeText(this, "Choose an image", Toast.LENGTH_LONG).show();
            return;
        }

        Bitmap bitmapTemp1 = bitmap.copy(bitmap.getConfig(), true);
        Bitmap bitmapTemp2 = bitmap.copy(bitmap.getConfig(), true);
        long start_detect = System.currentTimeMillis();
        Vector<Box> boxes1 = detect.detectFaces(bitmapTemp1, bitmapTemp1.getWidth() / 8);
        long end_detect = System.currentTimeMillis();

        System.out.println("Num of faces: ");
        System.out.println(boxes1.size());
        for (int i = 0; i < boxes1.size(); i++) {
            Box box1 = boxes1.get(i);

            box1.toSquareShape();
            box1.limitSquare(bitmapTemp1.getWidth(), bitmapTemp1.getHeight());
            Rect rect1 = box1.transform2Rect();

            Util.drawBox(bitmapTemp1, box1, 5);
            ImageView imageView = (ImageView) findViewById(R.id.avatar);
            imageView.setImageBitmap(bitmapTemp1);
        }

        //Align faces
        if (boxes1.size()!=0) {
            Box box1 = boxes1.get(0);
            box1.toSquareShape();
            box1.limitSquare(bitmapTemp2.getWidth(), bitmapTemp2.getHeight());
            Rect rect1 = box1.transform2Rect();
            bitmapCrop = MyUtil.crop(bitmapTemp2, rect1);
            bitmapCrop = Align.face_align(bitmapCrop, box1.landmark);

            ImageView imageView = (ImageView) findViewById(R.id.face);
            imageView.setImageBitmap(bitmapCrop);
        }
        //faceCompare(bitmapCrop);
        long start_recog = System.currentTimeMillis();

        try {
            Box box;
            Bitmap temp;
            Bitmap drawText = bitmapTemp1;
            Centroid nearest = null;

            // Loop through box in boxes1
            for (int i = 0; i < boxes1.size(); i++) {

                box = boxes1.get(i);
                box.toSquareShape();
                box.limitSquare(bitmapTemp2.getWidth(), bitmapTemp2.getHeight());
                Rect rect = box.transform2Rect();
                temp = MyUtil.crop(bitmapTemp2, rect);
                temp = Align.face_align(temp, box.landmark);
                faceCompare(temp);
                nearest = nearestCentroid(embedding, centroids);

                if (nearest != null ) {
                    //result.setText("Class: " + nearest.getNum() + " - Distance: " + KMeansOnline.getDistance(embedding, nearest.getCoordinates()));
                    drawText = drawTextToBitmap(context, drawText, nearest.getNum(), rect);
//                    ImageView imageView1 = (ImageView) findViewById(R.id.avatar);
//                    imageView1.setImageBitmap(drawText);
                } else {
//                    EditText editText = (EditText) findViewById(R.id.textview);
//                    text = editText.getText().toString();
                    if (text != null && !text.equals("") && i==0) {
                        drawText = drawTextToBitmap(context, drawText, text, rect);
                        Centroid cent = new Centroid(text, embedding);
                        centroids.add(cent);
                        //result.setText("Not match with any classes in dataset");

//                    ImageView imageView1 = (ImageView) findViewById(R.id.avatar);
//                    imageView1.setImageBitmap(drawText);
                    }
                    if (text==null || i!=0 || text.equals(""))
                    drawText = drawTextToBitmap(context, drawText, "Unknown", rect);
                }
                ImageView imageView1 = (ImageView) findViewById(R.id.avatar);
                imageView1.setImageBitmap(drawText);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        long end_recog = System.currentTimeMillis();
        TextView timeTextView= findViewById(R.id.textview);
        timeTextView.setText("detect time " + (end_detect - start_detect) +"ms, recog time " + (end_recog - start_recog) +"ms");

    }

    public void faceCompare(Bitmap bitmap) {
        if (bitmap == null) {
            Toast.makeText(this, "Choose an image", Toast.LENGTH_LONG).show();
            return;
        }

        embedding = mfn.getEmbedding(bitmap);
//        for (int i = 0; i < embedding[0].length; i++) {
//            System.out.print(embedding[0][i] + " ");
//        }
    }

    public ArrayList<Centroid> getCentroids() {
        int n = 1;
        String count;
        BufferedReader reader = null;
        BufferedWriter bw;
        count = "";
        ArrayList<Centroid> listCenTroid = new ArrayList<Centroid>();
//        try {
////            InputStream assetIs = getAssets().open("labelmap.txt");
////            OutputStream copyOs = openFileOutput("labelmap.txt", MODE_PRIVATE);
////            byte[] buffer = new byte[4096];
////            int bytesRead;
////            while ((bytesRead = assetIs.read(buffer)) != -1) {
////                copyOs.write(buffer, 0, bytesRead);
////            }
////            assetIs.close();
////            copyOs.close();
//            // now you can open and modify the copy
//            OutputStream copyOs = getApplication().openFileOutput("labelmap.txt", MODE_APPEND);
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
        try {
            reader = new BufferedReader(new InputStreamReader(getAssets().open("labelmap.txt")));
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                String id = "";
                String vector = "";
                if (n % 2 == 1) {
                    id = mLine;
                    n++;
                }
                if (n % 2 == 0){
                    vector = reader.readLine();
                }
                Centroid cenTroid = new Centroid(id, readCentroid(vector));
                listCenTroid.add(cenTroid);
                n++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                }
            }
        }
        return listCenTroid;
    }

    public Centroid nearestCentroid(float[][] emb, ArrayList<Centroid> centroids) throws FileNotFoundException {
        float minimumDistance = 0;
        Centroid nearest;
        Centroid temp = null;
        float unknownthresh=0.48f;
        float currentDistance = 0.0f;


        for (Centroid centroid : centroids) {
            // Compute distance between centroid and embedding
            currentDistance = KMeansOnline.getDistance(emb, centroid.getCoordinates());
           // System.out.println("Dis: " + currentDistance);

            if (currentDistance > minimumDistance) {
                minimumDistance = currentDistance;
                temp = centroid;
            }
        }

        System.out.println("Dis: " + minimumDistance);
        if (minimumDistance<unknownthresh)
            temp = null;
        nearest = temp;

        return nearest;
    }

    public static float[][] readCentroid(String s) {
//        float[][] cent = new float[1][192];
//        List<String> list = Arrays.asList(s.split(" "));
//        for (int i = 0; i < 192; i++) {
//            cent[0][i] = Float.parseFloat(list.get(i));
//        }
//        return cent;
        float[][] cent = new float[1][192];
        String [] strings = s.split("\\s");
        int i = 0;
        for (String w : strings) {
            cent[0][i] = Float.parseFloat(w);
            i++;
        }
        return cent;
    }

    public Bitmap drawTextToBitmap(Context gContext, Bitmap bitmap,
                                   String gText, Rect rect) {

        android.graphics.Bitmap.Config bitmapConfig =
                bitmap.getConfig();
        // set default bitmap config if none
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if(bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        // resource bitmaps are immutable,
        // so we need to convert it to mutable one
        bitmap = bitmap.copy(bitmapConfig, true);

        Canvas canvas = new Canvas(bitmap);
        // new antialised Paint
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // text color - #3D3D3D
        paint.setColor(Color.rgb(0, 255, 0));
        // text size in pixels
        int minimum = Math.min(width, height);
        float temp = (float) (0.1 * minimum);
        paint.setTextSize((int) (temp));
        // text shadow
        paint.setShadowLayer(5f, 0f, 1f, Color.WHITE);

        // draw text to the Canvas center
        Rect bounds = new Rect();
        paint.getTextBounds(gText, 0, gText.length(), bounds);
        int x = rect.left;
        int y = rect.bottom + 17;

        canvas.drawText(gText, x, y, paint);

        return bitmap;
    }
}