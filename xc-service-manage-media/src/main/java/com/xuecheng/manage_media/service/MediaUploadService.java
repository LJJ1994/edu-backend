package com.xuecheng.manage_media.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.config.RabbitMQConfig;
import com.xuecheng.manage_media.controller.MediaUploadController;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.print.attribute.standard.Media;
import java.io.*;
import java.util.*;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description:
 * @Create: 2020-02-11 03:07:07
 * @Modified By:
 */
@Service
public class MediaUploadService {
    private final static Logger LOGGER = LoggerFactory.getLogger(MediaUploadController.class);

    @Autowired
    MediaFileRepository mediaFileRepository;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Value("${xc-service-manage-media.upload-location}")
    String uploadLocation;

    @Value("${xc-service-manage-media.mq.queue-media-video-processor}")
    String queue_media_video_processor;

    @Value("${xc-service-manage-media.mq.routingkey-media-video}")
    String routingkey_media_video;

    /**
     * @Description: register 检查文件是否上传了
     * @param fileMd5 文件MD5
     * @param fileName 文件名
     * @param fileSize  文件大小
     * @param mimetype  媒体类型
     * @param fileExt   文件扩展名
     * @return: com.xuecheng.framework.model.response.ResponseResult
     */
    public ResponseResult register(String fileMd5, String fileName, long fileSize, String mimetype, String fileExt) {
        // 检查 文件是否存在
        String filepath = this.getFilePath(fileMd5, fileExt);
        File file = new File(filepath);
        // 查询数据库是否存在该文件信息
        Optional<MediaFile> byId = mediaFileRepository.findById(fileMd5);
        // 如果文件存在则返回
        if (file.exists() && byId.isPresent()) {
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_EXIST);
        }
        boolean fileFolder = this.createFileFold(fileMd5);
        if (!fileFolder) {
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_FAIL);
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * @Description: checkchunk 分块检查
     * @param fileMd5	文件MD5
     * @param chunk	块下标
     * @param chunkSize	块大小
     * @return: com.xuecheng.framework.domain.media.response.CheckChunkResult
     */
    public CheckChunkResult checkchunk(String fileMd5, Integer chunk, Integer chunkSize) {
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5);
        // 块的名称以chunk下标命名
        File chunkFile = new File(chunkFileFolderPath + chunk);
        if (chunkFile.exists()) {
            return new CheckChunkResult(MediaCode.CHUNK_FILE_EXIST_CHECK, true);
        } else {
            return new CheckChunkResult(MediaCode.CHUNK_FILE_EXIST_CHECK, false);
        }
    }

    /**
     * @Description: uploadchunk 上传分块
     * @param file 分块文件
     * @param chunk 块下标
     * @param fileMd5 文件MD5
     * @return: com.xuecheng.framework.model.response.ResponseResult
     */
    public ResponseResult uploadchunk(MultipartFile file, Integer chunk, String fileMd5) {
        // 创建块文件目录
        boolean chunkFileFolder = createChunkFileFolder(fileMd5);
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5);
        File chunkFile = new File(chunkFileFolderPath + chunk);
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            inputStream = file.getInputStream();
            fileOutputStream = new FileOutputStream(chunkFile);
            IOUtils.copy(inputStream, fileOutputStream);
        } catch (Exception e) {
            LOGGER.error("file chunk upload error... {}", e.getMessage());
            ExceptionCast.cast(MediaCode.CHUNK_FILE_EXIST_CHECK);
        }finally {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * @Description: mergeChunk 合并分块, 并且成功后向MQ发送视频处理消息,json格式为: {"mediaId": "xxxx"}
     * @param fileMd5
     * @param fileName
     * @param fileSize
     * @param mimetype
     * @param fileExt
     * @return: com.xuecheng.framework.model.response.ResponseResult
     */
    public ResponseResult mergeChunk(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5);
        File chunkFileFold = new File(chunkFileFolderPath);
        if (!chunkFileFold.exists()) {
            chunkFileFold.mkdirs();
        }
        // 合并文件的路径
        File mergeFile = new File(getFilePath(fileMd5, fileExt));
        //合并文件存在先删除再创建
        if (mergeFile.exists()) {
            mergeFile.delete();
        }
        boolean newFile = false;
        try {
            newFile = mergeFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!newFile) {
            ExceptionCast.cast(MediaCode.MERGE_FILE_FAIL);
        }
        // 获取块文件
        List<File> chunkFiles = getChunkFiles(new File(chunkFileFolderPath));
        // 合并文件
        mergeFile = this.mergeFile(mergeFile, chunkFiles);
        if (mergeFile == null) {
            ExceptionCast.cast(MediaCode.MERGE_FILE_FAIL);
        }
        // 校验文件MD5
//        boolean checkFileMd5 = checkFileMd5(mergeFile, fileMd5);
//        if (!checkFileMd5) {
//            ExceptionCast.cast(MediaCode.MERGE_FILE_CHECKFAIL);
//        }
        //将文件信息保存到数据库
        MediaFile mediaFile = new MediaFile();
        mediaFile.setFileId(fileMd5);
        mediaFile.setFileName(fileMd5+"."+fileExt);
        mediaFile.setFileOriginalName(fileName);
        //文件路径保存相对路径
        mediaFile.setFilePath(getFileFolderRelativePath(fileMd5,fileExt));
        mediaFile.setFileSize(fileSize);
        mediaFile.setUploadTime(new Date());
        mediaFile.setMimeType(mimetype);
        mediaFile.setFileType(fileExt);
        //状态为上传成功
        mediaFile.setFileStatus("301002");
        MediaFile save = mediaFileRepository.save(mediaFile);
        // 向MQ发送消息
        this.sendProcessVideoMsg(mediaFile.getFileId());
        return new ResponseResult(CommonCode.SUCCESS);
    }

    // 向MQ发送视频处理消息
    private ResponseResult sendProcessVideoMsg(String mediaId) {
        Optional<MediaFile> optional = mediaFileRepository.findById(mediaId);
        if (!optional.isPresent()) {
            return new ResponseResult(CommonCode.FAIL);
        }
        Map<String, String> msgMap = new HashMap<>();
        msgMap.put("mediaId", mediaId);
        // 解析为json
        String  jsonObject = JSON.toJSONString(msgMap);
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EX_MEDIA_PROCESSTASK,
                    routingkey_media_video, jsonObject);
            LOGGER.info("send media process task msg: {} to MQ", jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("{send media process task fail, msg:{}, error: {}", jsonObject, e.getMessage());
            return new ResponseResult(CommonCode.FAIL);
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * @Description: getFilePath 根据文件MD5和文件扩展名查询该文件名
     * 规则：
     * 一级目录：md5的第一个字符
     * 二级目录：md5的第二个字符
     * 三级目录：md5
     * 文件名：md5+文件扩展名
     * @param fileMd5 文件MD5
     * @param fileExt   文件扩展名
     * @return: java.lang.String
     */
    private String getFilePath(String fileMd5, String fileExt) {
        String filepath = uploadLocation + fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5
                + "/" + fileMd5 + "." + fileExt;
        return filepath;
    }

    /**
     * @Description: createFileFold 创建文件目录
     * @param fileMd5	文件MD5
     * @return: boolean
     */
    private boolean createFileFold(String fileMd5) {
        String fileFolderPath = this.getFileFolderPath(fileMd5);
        File file = new File(fileFolderPath);
        if (!file.exists()) {
            boolean mkdirs = file.mkdirs();
            return mkdirs;
        }
        return true;
    }

    /**
     * @Description: getFileFolderPath 获取文件目录
     * @param fileMd5
     * @return: java.lang.String
     */
    private String getFileFolderPath(String fileMd5) {
        String filepath = uploadLocation + fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5
                + "/";
        return filepath;
    }

    /**
     * @Description: getFileFolderRelativePath 获取文件相对目录
     * @param fileMd5
     * @param fileExt
     * @return: java.lang.String
     */
    private String getFileFolderRelativePath(String fileMd5, String fileExt) {
        String filepath = fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5
                + "/";
        return filepath;
    }

    /**
     * @Description: getChunkFileFolderPath 获取块文件目录
     * @param fileMd5	文件MD5
     * @return: java.lang.String
     */
    private String getChunkFileFolderPath(String fileMd5) {
        String chunkFilePath = getFileFolderPath(fileMd5) + "/" + "chunks" + "/";
        return chunkFilePath;
    }

    // 合并文件
    private File mergeFile(File mergeFile, List<File> chunkFileList) {
        try {
            RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw");
            byte[] b = new byte[1024];
            for (File chunkFile : chunkFileList) {
                RandomAccessFile raf_read = new RandomAccessFile(chunkFile, "r");
                int len = -1;
                while ((len=raf_read.read(b)) != -1) {
                    raf_write.write(b);
                }
                raf_read.close();
            }
            raf_write.close();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("merge file error... {}", e.getMessage());
            return null;
        }
        return mergeFile;
    }
    // 创建块文件目录
    private boolean createChunkFileFolder(String fileMd5) {
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        File chunkFilePath = new File(chunkFileFolderPath);
        if (!chunkFilePath.exists()) {
            boolean mkdirs = chunkFilePath.mkdirs();
            return mkdirs;
        }
        return true;
    }
    // 获取所有块文件
    private List<File> getChunkFiles(File chunkFileFolder) {
        // 获取路径下的所有块文件
        File[] chunkFiles = chunkFileFolder.listFiles();
        List<File> chunkFileList = new ArrayList<File>(Arrays.asList(chunkFiles));
        Collections.sort(chunkFileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (Integer.parseInt(o1.getName()) < Integer.parseInt(o2.getName())) {
                    return -1;
                }
                return 1;
            }
        });
        return chunkFileList;
    }

    private boolean checkFileMd5(File mergeFile, String fileMd5) {
        if (mergeFile == null || StringUtils.isEmpty(fileMd5)) {
            return false;
        }
        try {
            FileInputStream inputStream = new FileInputStream(mergeFile);
            String md5 = DigestUtils.md5Hex(inputStream);
            if (fileMd5.equalsIgnoreCase(md5)) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("check file md5 error, file is: {} md5: {}", mergeFile.getAbsolutePath(), fileMd5);
            return false;
        }
        return false;
    }

    // 合并完成后，删除chunks目录下的所有文件
    private boolean deleteChunks(String fileMd5) {
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5);
        File chunkFileList = new File(chunkFileFolderPath);
        if (chunkFileList.exists()) {
            boolean delete = chunkFileList.delete();
            return delete;
        }
        return true;
    }
}
