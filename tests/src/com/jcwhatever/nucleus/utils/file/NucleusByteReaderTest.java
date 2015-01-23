package com.jcwhatever.nucleus.utils.file;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import com.jcwhatever.nucleus.utils.ArrayUtils;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Tests {@link NucleusByteReader}.
 */
public class NucleusByteReaderTest {

    private enum TestEnum {
        CONSTANT,
        DEFAULT
    }

    /**
     * Make sure {@code #getBytesRead} returns the correct value.
     */
    @Test
    public void testGetBytesRead() throws Exception {

        NucleusByteReader reader = getReader(new byte[] { 0, 0, 0});

        reader.getByte();
        reader.getByte();
        reader.getByte();

        assertEquals(3, reader.getBytesRead());

        reader.close();
    }

    /**
     * Make sure {@code #read} returns the correct value.
     */
    @Test
    public void testRead() throws Exception {

        NucleusByteReader reader = getReader(new byte[] { 0, 1, 2});

        assertEquals(0, reader.getByte());
        assertEquals(1, reader.getByte());
        assertEquals(2, reader.getByte());

        reader.close();
    }

    /**
     * Make sure {@code #skip} works correctly.
     */
    @Test
    public void testSkip() throws Exception {
        NucleusByteReader reader = getReader(new byte[] { 0, 1, 2});

        assertEquals(0, reader.getByte());
        reader.skip(1);
        assertEquals(2, reader.getByte());

        reader.close();
    }

    /**
     * Make sure {@code #getBoolean} returns the correct value.
     */
    @Test
    public void testGetBoolean() throws Exception {

        NucleusByteReader reader = getReader(new byte[] { 0xF, 0, 0xF });

        assertEquals(true, reader.getBoolean());
        assertEquals(true, reader.getBoolean());
        assertEquals(true, reader.getBoolean());
        assertEquals(true, reader.getBoolean());

        assertEquals(0, reader.getByte());

        assertEquals(true, reader.getBoolean());
        assertEquals(true, reader.getBoolean());
        assertEquals(true, reader.getBoolean());
        assertEquals(true, reader.getBoolean());
        assertEquals(false, reader.getBoolean());
        assertEquals(false, reader.getBoolean());
        assertEquals(false, reader.getBoolean());

        reader.close();
    }

    /**
     * Make sure {@code #getByte} returns the correct value.
     */
    @Test
    public void testGetByte() throws Exception {
        NucleusByteReader reader = getReader(new byte[] { 0, 1, 2 });

        assertEquals(0, reader.getByte());
        assertEquals(1, reader.getByte());
        assertEquals(2, reader.getByte());

        reader.close();
    }

    /**
     * Make sure {@code #getBytes} returns the correct value.
     */
    @Test
    public void testGetBytes() throws Exception {
        NucleusByteReader reader = getReader(new byte[] { 0, 0, 0, 1, 5 });

        assertArrayEquals(new byte[]{5}, reader.getBytes());

        reader.close();
    }

    /**
     * Make sure {@code getShort} returns the correct value.
     */
    @Test
    public void testGetShort() throws Exception {
        NucleusByteReader reader = getReader(new byte[] { 0, 5 });

        assertEquals(5, reader.getShort());

        reader.close();
    }

    /**
     * Make sure {@code #getInteger} returns the correct value.
     */
    @Test
    public void testGetInteger() throws Exception {
        NucleusByteReader reader = getReader(new byte[] { 0, 0, 0, 5 });

        assertEquals(5, reader.getInteger());

        reader.close();
    }

    /**
     * Make sure {@code #getLong} returns the correct value.
     */
    @Test
    public void testGetLong() throws Exception {
        NucleusByteReader reader = getReader(new byte[] { 0, 0, 0, 0, 0, 0, 0, 5 });

        assertEquals(5, reader.getLong());

        reader.close();
    }

    /**
     * Make sure {@code #getString} returns the correct value.
     */
    @Test
    public void testGetString() throws Exception {

        String test = "test";

        byte[] stringBytes = test.getBytes(StandardCharsets.UTF_16);

        byte[] bytes = new byte[stringBytes.length + 2];

        ArrayUtils.copyFromEnd(stringBytes, bytes);

        bytes[1] = (byte)stringBytes.length; // string length

        NucleusByteReader reader = getReader(bytes);

        assertEquals("test", reader.getString());

        reader.close();

    }

    /**
     * Make sure {@code #getSmallString} returns the correct value.
     */
    @Test
    public void testGetSmallString() throws Exception {

        byte[] bytes  = getSmallStringBytes("test");

        NucleusByteReader reader = getReader(bytes);

        assertEquals("test", reader.getSmallString());

        reader.close();
    }

    /**
     * Make sure {@code #getFloat} returns the correct value.
     */
    @Test
    public void testGetFloat() throws Exception {

        byte[] bytes  = getSmallStringBytes("0.1");

        NucleusByteReader reader = getReader(bytes);

        assertEquals(0.1F, reader.getFloat(), 0.0F);

        reader.close();
    }

    /**
     * Make sure {@code #getDouble} returns the correct value.
     */
    @Test
    public void testGetDouble() throws Exception {

        byte[] bytes  = getSmallStringBytes("0.1");

        NucleusByteReader reader = getReader(bytes);

        assertEquals(0.1D, reader.getDouble(), 0.0D);

        reader.close();
    }

    /**
     * Make sure {@code #getEnum} returns the correct value.
     */
    @Test
    public void testGetEnum() throws Exception {
        byte[] bytes  = getSmallStringBytes(TestEnum.CONSTANT.name());

        NucleusByteReader reader = getReader(bytes);

        assertEquals(TestEnum.CONSTANT, reader.getEnum(TestEnum.class));

        reader.close();
    }

    /**
     * Make sure {@code #getUUID} returns the correct value.
     */
    @Test
    public void testGetUUID() throws Exception {

        UUID uuid = UUID.randomUUID();

        byte[] most = getLongBytes(uuid.getMostSignificantBits());
        byte[] least = getLongBytes(uuid.getLeastSignificantBits());

        byte[] bytes = new byte[16];

        ArrayUtils.copyFromStart(most, bytes);
        ArrayUtils.copyFromEnd(least, bytes);

        NucleusByteReader reader = getReader(bytes);

        assertEquals(uuid, reader.getUUID());

        reader.close();
    }

    private NucleusByteReader getReader(byte[] array) {

        InputStream stream = new ByteArrayInputStream(array);

        return new NucleusByteReader(stream);
    }

    private byte[] getSmallStringBytes(String test) {
        byte[] stringBytes = test.getBytes(StandardCharsets.UTF_8);

        byte[] bytes = new byte[stringBytes.length + 1];

        ArrayUtils.copyFromEnd(stringBytes, bytes);

        bytes[0] = (byte)stringBytes.length; // string length

        return bytes;
    }

    public byte[] getLongBytes(long longValue) throws IOException {

        return new byte[]{

                (byte) (longValue >> 56 & 0xFF),
                (byte) (longValue >> 48 & 0xFF),
                (byte) (longValue >> 40 & 0xFF),
                (byte) (longValue >> 32 & 0xFF),
                (byte) (longValue >> 24 & 0xFF),
                (byte) (longValue >> 16 & 0xFF),
                (byte) (longValue >> 8 & 0xFF),
                (byte) (longValue & 0xFF)
        };
    }
}