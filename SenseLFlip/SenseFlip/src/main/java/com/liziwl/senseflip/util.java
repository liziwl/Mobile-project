package com.liziwl.senseflip;

import android.os.Environment;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.PriorityQueue;

public class util {
    public static String getNowHumanTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }

    public static String getNowTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMddHHmmssSSS");
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }

    public static String fileNameFormater(String prefix) {
        return prefix + "_" + getNowTime() + ".csv";
    }

    public static String joinPath(String baseFilename) throws IOException {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File subFolder = new File(dir, "oneAuth");
        File base = new File(subFolder, baseFilename);
        if (!base.getParentFile().exists()) {
            if (!base.getParentFile().mkdirs()) {
                throw new IOException("Creating file fail");
            }
        }
        return base.getAbsolutePath();
    }

    public static void writeLineToFile(String context, String baseFilename) throws IOException {
        File file = new File(joinPath(baseFilename));
        //第二个参数意义是说是否以append方式添加内容
        BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file, true), StandardCharsets.UTF_8));
        bw.write(context);
        bw.write(System.lineSeparator());
        bw.flush();
    }

    public static void dumpQueue(PriorityQueue<XYZ> priorityQueue, String baseFilename) throws IOException {
        File file = new File(joinPath(baseFilename));
        BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file, true), StandardCharsets.UTF_8));
        CSVPrinter csvPrinter = new CSVPrinter(bw, CSVFormat.EXCEL);
        while (priorityQueue.peek() != null) {
            XYZ head = priorityQueue.poll();
            csvPrinter.printRecord(head.getXYZ());
        }
        csvPrinter.flush();
    }
}
