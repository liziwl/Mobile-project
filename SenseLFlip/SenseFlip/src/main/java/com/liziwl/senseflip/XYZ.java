package com.liziwl.senseflip;

import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class XYZ {
    public double x;
    public double y;
    public double z;
    public long dataTimeNano;
    // static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public XYZ(double x, double y, double z) {
        this(x, y, z, System.currentTimeMillis());
    }

    public XYZ(double x, double y, double z, long dataTime) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dataTimeNano = dataTime;
    }

    public long getDataTimeNano() {
        return dataTimeNano;
    }

    // public String getDataTimeHuman() {
    //     Date date = new Date(dataTimeNano);
    //     return simpleDateFormat.format(date);
    // }

    @NonNull
    @Override
    public String toString() {
        return String.format("%s X: %.3f, y: %.3f, z: %.3f", getDataTimeNano(), x, y, z);
    }
}

class XYZComparator implements Comparator<XYZ> {
    public int compare(XYZ data1, XYZ data2) {
        if (data1.dataTimeNano < data2.dataTimeNano) {
            return -1;
        } else if (data1.dataTimeNano > data2.dataTimeNano) {
            return 1;
        } else {
            return 0;
        }
    }
}
