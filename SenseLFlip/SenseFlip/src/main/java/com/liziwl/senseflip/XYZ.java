package com.liziwl.senseflip;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class XYZ {
    public double x;
    public double y;
    public double z;
    public long timestamp;

    public XYZ(double x, double y, double z) {
        this(x, y, z, System.currentTimeMillis());
    }

    public XYZ(double x, double y, double z, long dataTime) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.timestamp = dataTime;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("%s X: %.3f, y: %.3f, z: %.3f", getTimestamp(), x, y, z);
    }

    public List<Double> getXYZ() {
        ArrayList<Double> arrayList = new ArrayList<Double>(3);
        arrayList.add(x);
        arrayList.add(y);
        arrayList.add(z);
        return arrayList;
    }

}

class XYZComparator implements Comparator<XYZ> {
    public int compare(XYZ data1, XYZ data2) {
        if (data1.timestamp < data2.timestamp) {
            return -1;
        } else if (data1.timestamp > data2.timestamp) {
            return 1;
        } else {
            return 0;
        }
    }
}
