package com.example.lize_app;

import android.content.Context;

import com.example.lize_app.ui.central.CentralTempUI;
import com.example.lize_app.utils.Log;
import com.example.lize_app.utils.OtherUsefulFunction;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.junit.Test;
import org.mockito.Mock;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() { assertEquals(4, 2 + 2); }

    @Test
    public void division_isCorrect() { assertEquals(0, 15 / 16); }

    @Test
    public void get_Cell_Style() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        CellStyle cellStyle = workbook.createCellStyle();
//        Log.d(cellStyle.getDataFormatString());
//        Log.d(String.valueOf(cellStyle.getAlignment()));
//        Log.d(String.valueOf(cellStyle.getBorderBottom()));
    }

    @Test
    public void testHashMapWithInteger() {
        HashMap<Integer, Integer> h = new HashMap<>();
        h.put(94,87);
        assertEquals(87, h.get(new Integer(94)).intValue());

        h = new HashMap<>();
        h.put(new Integer(250), new Integer(260));
        assertEquals(260, h.get(new Integer(250)).intValue());
    }

    @Test
    public void testBoolean() {
        Boolean b = null;
        //assertEquals(null, b.booleanValue());
        b = true;
        assertEquals(true, b.booleanValue());
    }

}