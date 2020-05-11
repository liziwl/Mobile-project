package com.liziwl.senseflip;

import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;

public class XYZ {
    public double x;
    public double y;
    public double z;
    public long dataTime;
    static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public XYZ(double x, double y, double z) {
        this(x, y, z, System.currentTimeMillis());
    }

    public XYZ(double x, double y, double z, long dataTime) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dataTime = dataTime;
    }

    public long getDataTime() {
        return dataTime;
    }

    public String getDataTimeHuman() {
        Date date = new Date(dataTime);
        return simpleDateFormat.format(date);
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("%s X: %.3f, y: %.3f, z: %.3f", getDataTimeHuman(), x, y, z);
    }
}
