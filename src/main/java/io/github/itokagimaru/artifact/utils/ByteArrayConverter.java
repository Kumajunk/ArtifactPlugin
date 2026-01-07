package io.github.itokagimaru.artifact.utils;

import java.nio.ByteBuffer;

public class ByteArrayConverter {
    public static byte[] toByte(double val){
        return ByteBuffer.allocate(Double.BYTES).putDouble(val).array();
    }

    public static byte[] toByte(double[] array){
        ByteBuffer buffer = ByteBuffer.allocate(Double.BYTES * array.length);
        for (double val : array){
            buffer.putDouble(val);
        }
        return buffer.array();
    }

    public static double ByteToDouble(byte[] array){
        return ByteBuffer.wrap(array).getDouble();
    }

    public static double[] ByteToDoubleArray(byte[] array){
        ByteBuffer buffer = ByteBuffer.wrap(array);

        double[] values = new double[array.length / Double.BYTES];
        for (int i = 0; i < values.length; i++) {
            values[i] = buffer.getDouble();
        }
        return values;
    }
}
