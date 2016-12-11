package com.example.hzw.minipic;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Uri imgUri;
    ImageView imv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        imv = (ImageView) findViewById(R.id.imageView);
    }

    public void onGet(View v) {
        /*Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(it,100);*/

        String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        String fname = "p" + System.currentTimeMillis() + ".jpg";
        imgUri = Uri.parse("file://" + dir + "/" + fname);
        Intent it = new Intent("android.media.action.IMAGE_CAPTURE");
        it.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
        startActivityForResult(it, 100);
    }

    public void onPick(View v)
    {
        Intent it = new Intent("android.intent.action.PICK");
        it.setType("image/*");
        startActivityForResult(it, 101);
    }

    public void onShare(View v)
    {
        if(imgUri != null)
        {
            Intent it = new Intent(Intent.ACTION_SEND);
            it.setType("image/*");
            it.putExtra(Intent.EXTRA_STREAM, imgUri);
            startActivity(it);
        }
    }

    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /*if(resultCode == Activity.RESULT_OK && requestCode ==100)
        {
            Bundle extras = data.getExtras();

            Bitmap bmp = (Bitmap) extras.get("data");

            ImageView imv = (ImageView) findViewById(R.id.imageView);

            imv.setImageBitmap(bmp);
        }else
        {
            Toast.makeText(this, "没有拍到照片", Toast.LENGTH_LONG).show();
        }*/

        if(resultCode == Activity.RESULT_OK) {
            switch (requestCode)
            {
                case 100:
                    Intent it = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, imgUri);
                    sendBroadcast(it);
                    break;
                case 101:
                    imgUri = convertUri(data.getData());
                    break;
            }

            /*Bitmap bmp = BitmapFactory.decodeFile(imgUri.getPath());
            imv.setImageBitmap(bmp);*/

            showImg(requestCode);
        }else {
            Toast.makeText(this, requestCode == 100 ? "没有拍到照片" : "没有选取照片", Toast.LENGTH_LONG).show();
        }
    }

    void showImg(int code) {
        int iw, ih, vw, vh;
        Boolean needRotate;

        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inJustDecodeBounds = true; //只读取文件信息
        BitmapFactory.decodeFile(imgUri.getPath(), option);

        iw = option.outWidth;
        ih = option.outHeight;
        vw = imv.getWidth();
        vh = imv.getHeight();

        //int scaleFactor = Math.min(iw / vw, ih / vh);
        int scaleFactor;
        if(iw < ih)
        {
            needRotate = false;
            scaleFactor = Math.min(iw/vw, ih/vh);
        }else
        {
            needRotate = true;
            scaleFactor = Math.min(ih/vw, iw/vh);
        }

        option.inJustDecodeBounds = false;
        option.inSampleSize = scaleFactor;


        Bitmap bmp = BitmapFactory.decodeFile(imgUri.getPath(), option);

        if(needRotate)
        {
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight() , matrix, true);
        }

        imv.setImageBitmap(bmp);

        new AlertDialog.Builder(this).setTitle("图像文件信息")
                .setMessage("图像文件路径：" + imgUri.getPath() +
                        "\n 原始尺寸：" + iw + "x" + ih +
                        "\n 载入尺寸：" + bmp.getWidth() + "x" + bmp.getHeight() +
                        "\n 显示尺寸: " + vw + "x" + vh)
                .setNeutralButton("关闭", null)
                .show();
    }

    Uri convertUri(Uri uri)
    {
        if(uri.toString().substring(0,7).equals("content")){
            String[] colName = {MediaStore.MediaColumns.DATA};
            Cursor cursor = getContentResolver().query(uri, colName, null, null, null);
            cursor.moveToFirst();
            uri = Uri.parse("file://" + cursor.getString(0));
            cursor.close();
        }
        return uri;
    }
}
