package com.hauyu.adsdk.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AesUtils {
    private static void appendHex(StringBuffer paramStringBuffer, byte paramByte) {
        paramStringBuffer.append(
                "0123456789ABCDEF".charAt(0xF & paramByte >> 4)).append(
                "0123456789ABCDEF".charAt(paramByte & 0xF));
    }

    public static String decrypt(String key, String content) {
        try {
            String str = new String(decrypt(getRawKey(key.getBytes()),
                    toByte(content)));
            return str;
        } catch (Exception localException) {
            System.out.println("decrypt error: " + localException);
        }
        return null;
    }

    public static byte[] decrypt(byte[] key,
                                 byte[] content) throws Exception {
        SecretKeySpec localSecretKeySpec = new SecretKeySpec(key,
                "AES");
        Cipher localCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        localCipher.init(2, localSecretKeySpec, new IvParameterSpec(
                new byte[localCipher.getBlockSize()]));
        return localCipher.doFinal(content);
    }

    public static String decryptRaw(String seed, byte[] content) {
        try {
            String str = new String(
                    decrypt(getRawKey(seed.getBytes()), content));
            return str;
        } catch (Exception localException) {
            System.out.println("decrypt raw error: " + localException);
        }
        return null;
    }

    public static String encrypt(String key, String content) {
        try {
            byte[] result = encrypt(getRawKey(key.getBytes()),
                    content.getBytes());
            return toHex(result);
        } catch (Exception localException) {
        }
        return null;
    }

    private static byte[] encrypt(byte[] key,
                                  byte[] content) throws Exception {
        SecretKeySpec localSecretKeySpec = new SecretKeySpec(key,
                "AES");
        Cipher localCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        localCipher.init(1, localSecretKeySpec, new IvParameterSpec(
                new byte[localCipher.getBlockSize()]));
        return localCipher.doFinal(content);
    }

    public static byte[] encryptRaw(String key, String content) {
        try {
            byte[] arrayOfByte = encrypt(getRawKey(key.getBytes()),
                    content.getBytes());
            return arrayOfByte;
        } catch (Exception localException) {
            System.out.println("encrypt raw error: " + localException);
        }
        return null;
    }

    public static String fromHex(String content) {
        return new String(toByte(content));
    }

    private static byte[] getRawKey(byte[] key) throws Exception {
        byte[] arrayOfByte = new byte[16];
        if (key == null)
            throw new IllegalArgumentException("seed == null");
        if (key.length == 0)
            throw new IllegalArgumentException("seed.length == 0");
        if (key.length < 16) {
            int i = 0;
            while (i < arrayOfByte.length) {
                if (i < key.length) {
                    arrayOfByte[i] = key[i];
                } else {
                    arrayOfByte[i] = 0;
                }
                i++;
            }
        }
        return arrayOfByte;
    }

    public static byte[] toByte(String content) {
        int i = content.length() / 2;
        byte[] arrayOfByte = new byte[i];
        for (int j = 0; j < i; j++)
            arrayOfByte[j] = Integer.valueOf(
                    content.substring(j * 2, 2 + j * 2), 16).byteValue();
        return arrayOfByte;
    }

    public static String toHex(String content) {
        return toHex(content.getBytes());
    }

    public static String toHex(byte[] content) {
        if (content == null)
            return "";
        StringBuffer localStringBuffer = new StringBuffer(
                2 * content.length);
        for (int i = 0; i < content.length; i++)
            appendHex(localStringBuffer, content[i]);
        return localStringBuffer.toString();
    }

    private static String readFromFile(File f) {
        if (!f.exists()) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        try {
            byte[] buf = new byte[4096];
            int read = 0;
            FileInputStream fis = new FileInputStream(f);
            while ((read = fis.read(buf)) > 0) {
                builder.append(new String(buf, 0, read));
            }
            fis.close();
            return builder.toString();
        } catch (Exception e) {
        }
        return null;
    }

    private static void writeToFile(File f, String out) {
        try {
            if (f.exists()) {
                f.delete();
            }
            f.createNewFile();
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(out.getBytes());
            fos.close();
        } catch (Exception e) {
        }
    }

    private static void usage() {
        String usage = "java -jar aes.jar <-k rawkey> [-e/-d] [-i input] [-o output] [-s str]";
        System.out.println(usage);
    }

    private static boolean isEmpty(String str) {
        if (str == null || str.trim().equals("")) {
            return true;
        }
        return false;
    }

    // java -jar xxx.jar -k 111222 [-e]/[-d] -i "input" -o "output"
    public static void main(String args[]) {
        String key = null;
        String input = null;
        String output = null;
        String inputstr = null;
        boolean encrypt = true; // 默认加密

        int optSetting = 0;
        for (; optSetting < args.length; optSetting++) {
            if ("-k".equals(args[optSetting])) {
                key = args[optSetting + 1];
            } else if ("-i".equals(args[optSetting])) {
                input = args[optSetting + 1];
            } else if ("-o".equals(args[optSetting])) {
                output = args[optSetting + 1];
            } else if ("-e".equals(args[optSetting])) {
                encrypt = true;
            } else if ("-d".equals(args[optSetting])) {
                encrypt = false;
            } else if ("-s".equals(args[optSetting])) {
                inputstr = args[optSetting + 1];
            }
        }
        if (isEmpty(key)) {
            usage();
            return;
        }
        if (isEmpty(input) && isEmpty(inputstr)) {
            usage();
            return;
        }
        String originString = inputstr;
        if (!isEmpty(input)) {
            originString = readFromFile(new File(input));
            if (isEmpty(originString)) {
                System.out.println("无法读取输入文件");
                return;
            }
        }
        if (isEmpty(originString)) {
            System.out.println("无法读取输入字符串");
            return;
        }
        String outputString = null;
        if (encrypt) {
            outputString = encrypt(key, originString);
        } else {
            outputString = decrypt(key, originString);
        }
        if (!isEmpty(output)) {
            writeToFile(new File(output), outputString);
        } else {
            System.out.println(outputString);
        }
    }
}