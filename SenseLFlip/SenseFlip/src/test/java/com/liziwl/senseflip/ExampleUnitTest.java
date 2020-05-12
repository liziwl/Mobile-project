package com.liziwl.senseflip;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testXYZ() {
        XYZ xyz = new XYZ(1, 2, 3);
        for (Double d : xyz.getXYZ()) {
            System.out.println(d);
        }
    }
}