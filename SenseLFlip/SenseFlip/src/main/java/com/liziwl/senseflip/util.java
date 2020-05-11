package com.liziwl.senseflip;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class util {
    public static String getNowTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMddHHmmssSSS");
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }

    public static String fileNameFormater(String prefix) {
        return prefix + "_" + getNowTime() + ".csv";
    }

    public static String joinPath(String baseFilename) {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File base = new File(dir, baseFilename);
        return base.getAbsolutePath();
    }

    public static void writeToFile(String context, String baseFilename) {
        try {
            File file = new File(joinPath(baseFilename));
            //第二个参数意义是说是否以append方式添加内容
            BufferedWriter bw = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(file, true), "UTF-8"));
            bw.write(context);
            bw.write(System.lineSeparator());
            bw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
