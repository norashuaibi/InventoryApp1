package com.example.android.inventoryapp.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
/**
 * Created by nora on 10/21/2017.
 */
public class Utils {


        public static byte[] getImageBytes(Bitmap bitmap) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
            return outputStream.toByteArray();
        }

        public static Bitmap getImage(byte[] image) {
            return BitmapFactory.decodeByteArray(image, 0, image.length);
        }

        public static byte[] getBytes(InputStream inputStream) throws IOException {
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];

            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            return byteBuffer.toByteArray();
        }

    }
