package com.example.lize_app;

import com.example.lize_app.utils.OtherUsefulFunction;

import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class String_to_Byte_array_Unit_Test {

    @Test
    public void stringToBytes() {
//        assertEquals(0, "80FF01Fe".getBytes()[0]);
//        assertEquals(70, "80FF01Fe".getBytes()[3]);

//        assertEquals(new byte[] { 0, 127, -127, 126 }, "80FF01Fe".getBytes());
//        assertEquals(128, new CentralTempUI().hexStringToByteArray("80")[0]);

//        assertEquals(0x7F, new CentralTempUI().hexStringToByteArray("7F")[0]);
//        assertEquals(new byte[] { 0, 127, -127, 126 }, new CentralTempUI().hexStringToByteArray("80FF01FE"));
//        assertEquals(new byte[] { 0, 127, -127, 126 }, new CentralTempUI().hexStringToByteArray("80FF01Fe"));

//        assertEquals(new byte[] { 65, 66, 67 }, "ABC".getBytes());

        assertEquals(128, OtherUsefulFunction.hexStringToByteArray("80FF01Fe")[0] & 0xff);
        assertEquals(0xFF, OtherUsefulFunction.hexStringToByteArray("80FF01Fe")[1] & 0xff);
        assertEquals(254, OtherUsefulFunction.hexStringToByteArray("80FF01Fe")[3] & 0xff);

    }

    @Test
    public void bytesToString() {
        byte[] b = {0x00, -0x7f, -0x80, -0x01, 0x01, 0x7f};
        assertEquals("00, 81, 80, ff, 01, 7f, ", OtherUsefulFunction.byteArrayToHexString(b, ", "));
        assertEquals("00, -81, -80, -ff, 01, 7f, ", OtherUsefulFunction.byteArrayToHexString(b, ", ", "", "-"));
        assertEquals(true, 1 == 0x01);
        assertEquals(false, -81 == -0x7f);
        assertEquals(true, 256 == 0x01 * 256);

        b = new byte[]{0x51};
        assertEquals(81, OtherUsefulFunction.byteArrayToSignedInt(b));

        b = new byte[]{-0x01, -0x01, -0x0d, 0x51};
        assertEquals("ff, ff, f3, 51, ", OtherUsefulFunction.byteArrayToHexString(b, ", "));
        assertEquals(-3247, OtherUsefulFunction.byteArrayToSignedInt(b));
        b = new byte[]{0x00, 0x00, -0x31, 0x5f};
        assertEquals("00, 00, cf, 5f, ", OtherUsefulFunction.byteArrayToHexString(b, ", "));
        assertEquals(53087, OtherUsefulFunction.byteArrayToSignedInt(b));

        b = new byte[]{0x00, (byte)-16};
        assertEquals("00, f0, ", OtherUsefulFunction.byteArrayToHexString(b, ", "));
        assertEquals(240, OtherUsefulFunction.byteArrayToSignedInt(b));

        b = new byte[]{-0x01, -0x01, -0x01, -0x03};
        assertEquals("ff, ff, ff, fd, ", OtherUsefulFunction.byteArrayToHexString(b, ", "));
        assertEquals(-3, OtherUsefulFunction.byteArrayToSignedInt(b));
        b = new byte[]{-0x03};
        assertEquals("fd, ", OtherUsefulFunction.byteArrayToHexString(b, ", "));
        assertEquals(-3, OtherUsefulFunction.byteArrayToSignedInt(b));
        b = new byte[]{(byte)-3, (byte)-88};
        assertEquals("fd, a8, ", OtherUsefulFunction.byteArrayToHexString(b, ", "));
        assertEquals(-600, OtherUsefulFunction.byteArrayToSignedInt(b));
    }

    // =====================================================================================
    // https://www.baeldung.com/java-string-to-byte-array
    @Test
    public void whenGetBytesWithNamedCharset_thenOK()
            throws UnsupportedEncodingException {
        String inputString = "Hello World!";
        String charsetName = "IBM01140";

        byte[] byteArrray = inputString.getBytes("IBM01140");

        assertArrayEquals(
                new byte[] { -56, -123, -109, -109, -106, 64, -26,
                        -106, -103, -109, -124, 90 },
                byteArrray);
    }

    @Test
    public void whenGetBytesWithCharset_thenOK() {
        String inputString = "Hello ਸੰਸਾਰ!";
        Charset charset = Charset.forName("ASCII");

        byte[] byteArrray = inputString.getBytes(charset);

        assertArrayEquals(
                new byte[] { 72, 101, 108, 108, 111, 32, 63, 63, 63,
                        63, 63, 33 },
                byteArrray);
    }

    @Test
    public void whenEncodeWithCharset_thenOK() {
        String inputString = "Hello ਸੰਸਾਰ!";
        Charset charset = StandardCharsets.US_ASCII;

        byte[] byteArrray = charset.encode(inputString).array();

        assertArrayEquals(
                new byte[] { 72, 101, 108, 108, 111, 32, 63, 63, 63, 63, 63, 33 },
                byteArrray);
    }
}