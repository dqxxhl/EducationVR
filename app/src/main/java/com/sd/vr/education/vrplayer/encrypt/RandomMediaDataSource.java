package com.sd.vr.education.vrplayer.encrypt;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.SecureRandom;


import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import tv.danmaku.ijk.media.player.misc.IMediaDataSource;


public class RandomMediaDataSource implements IMediaDataSource {
    private RandomAccessFile randFile;
    private long size;
    String key = "179184524373135360";//秘钥
    int len; //加密长度

    public RandomMediaDataSource(File file, String key, int len) throws IOException{
        size = file.length();
        randFile = new RandomAccessFile(file, "r");
        this.key = key;
        this.len = len;
    }

    @Override
    public int readAt(long position, byte[] buffer, int offset, int length) throws IOException
    {
        int returnSize;
        if (randFile != null)
        {
            randFile.seek(position);

            returnSize = randFile.read(buffer, offset, length);
            if (position >=0 && position < len ){//解密
                int encryptSize = len - (int)position;
                if (length > encryptSize){
                    for (int i = 0; i < encryptSize; i++ ){
                        byte[] keyBytes = key.getBytes();
                        for(byte keyBytes0 : keyBytes){
                            buffer[i] =(byte)(buffer[i] ^ keyBytes0);
                        }
                    }
                }
            }

            /*if (position == 0 && length > 16){
                for (int i = 0; i < 16; i++ ){
                    byte[] keyBytes = key.getBytes();
                    for(byte keyBytes0 : keyBytes){
                        buffer[i] =(byte)(buffer[i] ^ keyBytes0);
                    }
                }
            }*/

            return returnSize;
        }
        else
        {
            return -1;
        }
    }

    @Override
    public long getSize() throws IOException
    {
        return size;
    }

    @Override
    public void close() throws IOException
    {
        if (randFile != null)
        {
            randFile.close();
            randFile = null;
        }
    }



    private byte[] decrypt(byte[] data, byte[] key) throws Exception {
        // 生成一个可信任的随机数源
        SecureRandom sr = new SecureRandom();

        // 从原始密钥数据创建DESKeySpec对象
        DESKeySpec dks = new DESKeySpec(key);

        // 创建一个密钥工厂，然后用它把DESKeySpec转换成SecretKey对象
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey securekey = keyFactory.generateSecret(dks);

        // Cipher对象实际完成解密操作
        Cipher cipher = Cipher.getInstance("DES");

        // 用密钥初始化Cipher对象
        cipher.init(Cipher.DECRYPT_MODE, securekey, sr);

        return cipher.doFinal(data);
    }
}