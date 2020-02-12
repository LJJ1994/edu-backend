package com.xuecheng.manage_media;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.mapping.TextScore;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;
import java.util.*;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description:
 * @Create: 2020-02-11 00:50:50
 * @Modified By:
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestMedia {
    @Test
    public void testChunk() throws IOException {
        File sourceFile = new File("E:\\program\\java\\eduFront\\xc-ui-pc-static-portal\\video\\ffmpegtest\\video\\lucene.avi");
        long chunkSize = 1024*1024;
        long chunkNum = (long) Math.ceil(sourceFile.length()*1.0/chunkSize);
        String chunkPath = "E:\\program\\java\\eduFront\\xc-ui-pc-static-portal\\video\\ffmpegtest\\video\\chunk\\";
        File chunkFolder = new File(chunkPath);
        if (!chunkFolder.exists()) {
            chunkFolder.mkdirs();
        }
        byte[] b = new byte[1024];
        int len = -1;
        RandomAccessFile raf_read = new RandomAccessFile(sourceFile, "r");
        for (int i = 0; i < chunkNum; i++) {
            File file = new File(chunkPath+i);
            boolean newFile = file.createNewFile();
            if (newFile) {
                RandomAccessFile raf_write = new RandomAccessFile(file, "rw");
                while((len=raf_read.read(b))!=-1) {
                    raf_write.write(b, 0, len);
                    if (file.length()>chunkSize) {
                        break;
                    }
                }
                raf_write.close();
            }
        }
        raf_read.close();
    }

    @Test
    public void testMerge() throws IOException {
        File chunkFolder = new File("E:\\program\\java\\eduFront\\xc-ui-pc-static-portal\\video\\ffmpegtest\\video\\chunk\\");
        File mergeFile = new File("E:\\program\\java\\eduFront\\xc-ui-pc-static-portal\\video\\ffmpegtest\\video\\lucene_merge.avi");
        boolean newFile = mergeFile.createNewFile();
        RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw");
        raf_write.seek(0);
        File[] files = chunkFolder.listFiles();
        List<File> fileList = new ArrayList<File>(Arrays.asList(files));
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (Integer.parseInt(o1.getName()) < Integer.parseInt(o2.getName())) {
                    return -1;
                }
                return 1;
            }
        });
        byte[] b = new byte[1024];
        for (File chunkFile:fileList) {
            RandomAccessFile raf_read = new RandomAccessFile(chunkFile, "r");
            int len = -1;
            while ((len=raf_read.read(b)) != -1) {
                raf_write.write(b, 0, len);
            }
            raf_read.close();
        }
        raf_write.close();
    }

    @Test
    public void testMd5() throws IOException {
        String md5 = "5fbb79a2016c0eb609ecd0cd3dc48016";
        String filePath = "E:\\program\\java\\eduFront\\xc-ui-pc-static-portal\\video\\3\\a\\3a65b7d43dd71eec33a1c0c1e50c379d\\3a65b7d43dd71eec33a1c0c1e50c379d.avi";
        File file = new File(filePath);
        for(int i = 0; i < 3; i++) {
            FileInputStream inputStream = new FileInputStream(file);
            String md5Hex = DigestUtils.md5Hex(inputStream);
            System.out.println(md5Hex);
        }
    }

    @Test
    public void testMD5String() {
        String name = "myname";
        for (int i=0;i<5;i++) {
            String s = DigestUtils.md5Hex(name);
            System.out.println(s);
        }
    }

    @Test
    public void testMD5Lucene() throws IOException {
        String path = "E:\\program\\java\\eduFront\\xc-ui-pc-static-portal\\video\\lucene.avi";
        String filePath = "E:\\program\\java\\eduFront\\xc-ui-pc-static-portal\\video\\c\\5\\c5c75d70f382e6016d2f506d134eee11\\c5c75d70f382e6016d2f506d134eee11.avi";
        File file = new File(filePath);
        File file1 = new File(path);
        FileInputStream inputStream = new FileInputStream(file);
        FileInputStream inputStream1 = new FileInputStream(file1);
        String md5Hex = DigestUtils.md5Hex(inputStream);
        String md5Hex1 = DigestUtils.md5Hex(inputStream1);
        System.out.println(md5Hex);
        System.out.println(md5Hex1);
    }

    @Test
    public void testMD5Byte() throws IOException {
        String path = "E:\\program\\java\\eduFront\\xc-ui-pc-static-portal\\video\\lucene.avi";
        String filePath = "E:\\program\\java\\eduFront\\xc-ui-pc-static-portal\\video\\c\\5\\c5c75d70f382e6016d2f506d134eee11\\c5c75d70f382e6016d2f506d134eee11.avi";
        File file = new File(filePath);
        File file1 = new File(path);
        FileInputStream inputStream = new FileInputStream(file);
        FileInputStream inputStream1 = new FileInputStream(file1);
        String md5Hex = DigestUtils.md5Hex(inputStream);
        String md5Hex1 = DigestUtils.md5Hex(inputStream1);
        System.out.println(md5Hex);
        System.out.println(md5Hex1);
    }
}
