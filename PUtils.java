package com.LRAKE_IoT_PAL.mobile_client;

//下3個是hash用(https://blog.csdn.net/qq_24280381/article/details/72024860，https://blog.csdn.net/BigData_Mining/article/details/82789398)

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class PUtils {

    public static byte[] xor4(byte[] a, byte[] b, byte[] c, byte[] d) {
        int result1 = Math.min(a.length, b.length);
        int result2 = Math.min(c.length, d.length);
        int result3 = Math.min(result1, result2);
        byte[] result = new byte[Math.min(result3, d.length)];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) (((int) a[i]) ^ ((int) b[i])^ ((int) c[i])^ ((int) d[i]));
        }
        return result;

    }
    public static byte[] xor2(byte[] a, byte[] b) {
        byte[] result = new byte[Math.min(a.length, b.length)];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) (((int) a[i]) ^ ((int) b[i]));
        }
        return result;

    }
    public static String hash(String string, String algorithm) {
        if (string.isEmpty()) {
            return "";
        }
        MessageDigest hash = null;
        try {
            hash = MessageDigest.getInstance(algorithm);
            byte[] bytes = hash.digest(string.getBytes("UTF-8"));
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    } /* 加密 */
    public static String Encrypt1(String PlainText, String SSK) throws Exception {
        if (SSK == null) {
            System.out.print("Key is null");
            return null;
        }
        // 將SessionKey利用MD5產生出128bits的hashSessionKey
        String hashSSK = getMD5Str(SSK);
        byte[] rawSSK = hashSSK.getBytes();
        SecretKeySpec skeySpec = new SecretKeySpec(rawSSK, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec iv = new IvParameterSpec("0102030405060708".getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        byte[] EncryptedMessage = cipher.doFinal(PlainText.getBytes());
        String CipherText = byte2hex(EncryptedMessage).toLowerCase();
        return CipherText;
    }
    /* 解密 */
    public static String Decrypt1(String CipherText, String SSK) throws Exception {
        try {
            // 判斷SSK是否正確
            if (SSK == null) {
                System.out.print("Key is null");
                return null;
            }
            // 將SessionKey利用MD5產生出128bits的hashSessionKey
            String hashSSK = getMD5Str(SSK);
            byte[] rawSSK = hashSSK.getBytes("ASCII");
            SecretKeySpec skeySpec = new SecretKeySpec(rawSSK, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec("0102030405060708".getBytes());
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] EncryptedMessage = hex2byte(CipherText);
            try {
                byte[] DecryptedMessage = cipher.doFinal(EncryptedMessage);
                String PlainText = new String(DecryptedMessage);
                return PlainText;
            } catch (Exception e) {
                System.out.println(e.toString());
                return null;
            }
        } catch (Exception ex) {
            System.out.println(ex.toString());
            return null;
        }
    }
    /* 解密過程中Hex to byte */
    public static byte[] hex2byte(String strhex) {
        if (strhex == null) {
            return null;
        }
        int l = strhex.length();
        if (l % 2 == 1) {
            return null;
        }
        byte[] b = new byte[l / 2];
        for (int i = 0; i != l / 2; i++) {
            b[i] = (byte) Integer.parseInt(strhex.substring(i * 2, i * 2 + 2),
                    16);
        }
        return b;
    }
    /* 加密過程中String to Hex */
    public static String byte2hex(byte[] b) {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }
        return hs.toUpperCase();
    }
    /* 將SSK轉為128bits */
    public static String getMD5Str(String input) {
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(input.getBytes());
            byte[] md5Data = m.digest();
            return byteArrayToHex(md5Data);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
    /* byteArray To Hex */
    private static String byteArrayToHex(byte[] byteArray) {
        char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        char[] resultCharArray = new char[byteArray.length * 2];
        int index = 0;
        for (byte b : byteArray) {
            resultCharArray[index++] = hexDigits[b >>> 4 & 0xf];
            resultCharArray[index++] = hexDigits[b & 0xf];
        }
        return new String(resultCharArray);
    }
}