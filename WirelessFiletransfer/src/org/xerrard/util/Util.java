package org.xerrard.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 
 * @ClassName:Util
 * @Description:工具类
 * @author:xerrard
 * @date:2014年7月21日
 */
public class Util {

    public static void long2Byte(byte[] bb, long x, int offset) {
        bb[offset + 0] = (byte) (x >> 56);
        bb[offset + 1] = (byte) (x >> 48);
        bb[offset + 2] = (byte) (x >> 40);
        bb[offset + 3] = (byte) (x >> 32);
        bb[offset + 4] = (byte) (x >> 24);
        bb[offset + 5] = (byte) (x >> 16);
        bb[offset + 6] = (byte) (x >> 8);
        bb[offset + 7] = (byte) (x >> 0);
    }

    public static void long2Byte(byte[] bb, long x) {
        bb[0] = (byte) (x >> 56);
        bb[1] = (byte) (x >> 48);
        bb[2] = (byte) (x >> 40);
        bb[3] = (byte) (x >> 32);
        bb[4] = (byte) (x >> 24);
        bb[5] = (byte) (x >> 16);
        bb[6] = (byte) (x >> 8);
        bb[7] = (byte) (x >> 0);
    }

    public static long getLong(byte[] bb) {
        return ((((long) bb[0] & 0xff) << 56) | (((long) bb[1] & 0xff) << 48)
                | (((long) bb[2] & 0xff) << 40) | (((long) bb[3] & 0xff) << 32)
                | (((long) bb[4] & 0xff) << 24) | (((long) bb[5] & 0xff) << 16)
                | (((long) bb[6] & 0xff) << 8) | (((long) bb[7] & 0xff) << 0));
    }

    public static long getLong(byte[] bb, int offset) {
        return ((((long) bb[offset + 0] & 0xff) << 56)
                | (((long) bb[offset + 1] & 0xff) << 48)
                | (((long) bb[offset + 2] & 0xff) << 40)
                | (((long) bb[offset + 3] & 0xff) << 32)
                | (((long) bb[offset + 4] & 0xff) << 24)
                | (((long) bb[offset + 5] & 0xff) << 16)
                | (((long) bb[offset + 6] & 0xff) << 8) | (((long) bb[offset + 7] & 0xff) << 0));
    }

    public static String Bytes2String(byte[] bytes, int offset, int end) {

        byte[] filenamebytes = new byte[end - offset + 1];
        for (int i = offset; i <= end; i++) {
            filenamebytes[i - offset] = bytes[i];
        }
        return new String(filenamebytes);
    }

    /**
     * <p>
     * Description:把InitStr字串写入到DecFilePath对应地址的文件中
     * <p>
     * 
     * @date:2014年7月21日
     * @param InitStr
     * @param DecFilePath
     * @throws IOException
     */
    public static void stringToFile(String InitStr, String DecFilePath)
            throws IOException {
        File sourceFile = new File(DecFilePath);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(sourceFile);
            fos.write(InitStr.getBytes());
            fos.close();
        }
        catch (FileNotFoundException e) {

        }

    }

    /**
     * <p>
     * Description:将int型的ip地址转换成ip地址字符串
     * <p>
     * 
     * @date:2014年7月21日
     * @param ip
     * @return
     */
    public static String intToIpAddress(int ip) {
        StringBuffer ipBuf = new StringBuffer();
        ipBuf.append(ip & 0xff).append('.').append((ip >>>= 8) & 0xff)
                .append('.').append((ip >>>= 8) & 0xff).append('.')
                .append((ip >>>= 8) & 0xff);

        return ipBuf.toString();
    }

}
