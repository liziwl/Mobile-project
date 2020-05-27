package com.liziwl.senseflip;

import java.util.Comparator;

public class XYZComparator implements Comparator<XYZ> {

    @Override
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
