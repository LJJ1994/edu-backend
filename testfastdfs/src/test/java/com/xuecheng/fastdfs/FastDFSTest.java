package com.xuecheng.fastdfs;

import org.csource.common.MyException;
import org.csource.fastdfs.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description:
 * @Create: 2020-01-29 14:44:44
 * @Modified By:
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class FastDFSTest {
    @Test
    public void testUpload() {
        try {
            // 获取配置文件
            ClientGlobal.initByProperties("config/fastdfs-client.properties");
            // 创建客户端
            TrackerClient trackerClient = new TrackerClient();
            // 连接tracker服务器
            TrackerServer trackerServer = trackerClient.getConnection();
            // 连接storage服务器
            StorageServer storeStorage = trackerClient.getStoreStorage(trackerServer);
            // 创建storage客户端
            StorageClient1 storageClient1 = new StorageClient1(trackerServer, storeStorage);
            String filePath = "e:/EKbk3VoVUAENX8z.jpg";
            String fileId = storageClient1.upload_file1(filePath, "jpg", null);
            System.out.println(fileId); // /group1/M00/00/00/wKhlgF4xLZ6AOPnmAAD0_XQF1ok919.jpg
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDownload() throws IOException, MyException {
        // 获取配置文件
        ClientGlobal.initByProperties("config/fastdfs-client.properties");
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getConnection();
        StorageServer storageServer = null;
        StorageClient1 storageClient1 = new StorageClient1(trackerServer, storageServer);
        byte[] bytes = storageClient1.download_file1("group1/M00/00/00/wKhlgF4xLZ6AOPnmAAD0_XQF1ok919.jpg");
        File file = new File("e:/download.jpg");
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(bytes);
        fileOutputStream.close();
    }
}
