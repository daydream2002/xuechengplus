package com.xuecheng.media;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @Author daydream
 * @Description
 * @Date 2024/8/2
 */

public class BigFileTest {
    @Test
    public void testChunk() throws IOException {
        File sourceFile = new File("D:\\Users\\Desktop\\doudizhu.pdf");
        String chunkFilePath = "D:\\Users\\Desktop\\chunk\\";
        int chunkSize = 1024 * 1024;
        long chunkNum = (long) Math.ceil(sourceFile.length() * 1.0 / chunkSize);
        RandomAccessFile raf_r = new RandomAccessFile(sourceFile, "r");
        byte[] bytes = new byte[1024];
        for (int i = 0; i < chunkNum; i++) {
            File chunkFile = new File(chunkFilePath + i);
            RandomAccessFile raf_rw = new RandomAccessFile(chunkFile, "rw");
            int len = -1;
            while ((len = raf_r.read(bytes)) != -1) {
                raf_rw.write(bytes, 0, len);
                if (chunkFile.length() >= chunkSize) {
                    break;
                }
            }
            raf_rw.close();
        }
        raf_r.close();
    }

    @Test
    public void merge() throws Exception {
        File sourceFile = new File("D:\\Users\\Desktop\\doudizhu.pdf");
        File mergeFile = new File("D:\\Users\\Desktop\\doudizhu2.pdf");
        String chunkFilePath = "D:\\Users\\Desktop\\chunk\\";
        File chunkFile = new File(chunkFilePath);
        File[] files = chunkFile.listFiles();
        List<File> list = Arrays.asList(files);
        list.sort(Comparator.comparingInt(o -> Integer.parseInt(o.getName())));
        RandomAccessFile raf_rw = new RandomAccessFile(mergeFile, "rw");
        byte[] bytes = new byte[1024];
        list.forEach(file -> {
            try {
                RandomAccessFile raf_r = new RandomAccessFile(file, "r");
                int len;
                while ((len = raf_r.read(bytes)) != -1) {
                    raf_rw.write(bytes, 0, len);
                }
                raf_r.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        raf_rw.close();
        FileInputStream fileInputStream_merge = new FileInputStream(mergeFile);
        FileInputStream fileInputStream_source = new FileInputStream(sourceFile);
        String md5_merge = DigestUtils.md5Hex(fileInputStream_merge);
        String md5_source = DigestUtils.md5Hex(fileInputStream_source);
        System.out.println(md5_merge);
        System.out.println(md5_source);
    }
}
