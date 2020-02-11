package com.xuecheng.manage_media_process.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.MediaFileProcess_m3u8;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.utils.HlsVideoUtil;
import com.xuecheng.framework.utils.Mp4VideoUtil;
import com.xuecheng.manage_media_process.dao.MediaFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description: 视频处理MQ
 * @Create: 2020-02-12 01:15:15
 * @Modified By:
 */
@Component
public class MediaProcessTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(MediaProcessTask.class);

    //ffmpge绝对路径
    @Value("${xc-service-manage-media.ffmpeg-path}")
    String ffmpgePath;

    // 上传文件根路径
    @Value("${xc-service-manage-media.video-location}")
    String serverPath;

    @Autowired
    MediaFileRepository mediaFileRepository;

    /**
     * @Description: RabbitMQ监听视频处理消息队列, 消费队列里的视频上传信息
     * @param msg {"mediaId": "XXXXXX"}, mediaId表示该文件id，md5表示
     * @return: void
     */
    @RabbitListener(queues = "${xc-service-manage-media.mq.queue-media-video-processor}", containerFactory = "customContainerFactory")
    public void receiveMediaProcessTask(String msg) {
        Map msgMap = JSON.parseObject(msg, Map.class);
        LOGGER.info("receive media process tas msg: {}", msgMap);
        String mediaId = (String) msgMap.get("mediaId");
        // 判断媒资信息是否存在
        Optional<MediaFile> byId = mediaFileRepository.findById(mediaId);
        if (!byId.isPresent()) {
            return;
        }
        MediaFile mediaFile = byId.get();
        String fileType = mediaFile.getFileType();
        if (fileType == null || !fileType.equals("avi")) { //如果不是avi文件
            mediaFile.setProcessStatus("303004"); // 更改处理状态为无需处理
            mediaFileRepository.save(mediaFile);
            return;
        } else {
            mediaFile.setProcessStatus("303001");// 状态为未处理
            mediaFileRepository.save(mediaFile);
        }

        // 生成mp4文件
        String videoPath = serverPath + mediaFile.getFilePath() + mediaFile.getFileName();
        String mp4Name = mediaFile.getFileId() + ".mp4";
        String mp4FileFolder = serverPath + mediaFile.getFilePath();
        Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(ffmpgePath, videoPath, mp4Name, mp4FileFolder);
        String result = mp4VideoUtil.generateMp4();
        if (!result.equals("success")) {
            mediaFile.setProcessStatus("303003"); // 状态为处理失败
            MediaFileProcess_m3u8 mediaFileProcessM3u8 = new MediaFileProcess_m3u8();
            mediaFileProcessM3u8.setErrormsg(result);
            mediaFile.setMediaFileProcess_m3u8(mediaFileProcessM3u8);
            mediaFileRepository.save(mediaFile);
            return;
        }

        // 根据mp4生成m3u8文件(业内推荐)
        // mp4文件
        String mp4FilePath = serverPath + mediaFile.getFilePath() + mp4Name;
        String m3u8FolderPath = serverPath + mediaFile.getFilePath() + "hls/";
        String m3u8Name = mediaFile.getFileId() + ".m3u8";
        HlsVideoUtil hlsVideoUtil = new HlsVideoUtil(ffmpgePath, mp4FilePath, m3u8Name, m3u8FolderPath);
        String m3u8 = hlsVideoUtil.generateM3u8();
        if (!m3u8.equals("success")) {
            mediaFile.setProcessStatus("303003"); // 状态为处理失败
            MediaFileProcess_m3u8 mediaFileProcessM3u8 = new MediaFileProcess_m3u8();
            mediaFileProcessM3u8.setErrormsg(result);
            mediaFile.setMediaFileProcess_m3u8(mediaFileProcessM3u8);
            mediaFileRepository.save(mediaFile);
            return;
        }
        // 获取m3u8列表，并保存在mongodb
        List<String> m3u8List = hlsVideoUtil.get_ts_list();
        mediaFile.setProcessStatus("303002"); // 状态为处理成功
        MediaFileProcess_m3u8 mediaFileProcessM3u8 = new MediaFileProcess_m3u8();
        mediaFileProcessM3u8.setTslist(m3u8List);
        mediaFile.setMediaFileProcess_m3u8(mediaFileProcessM3u8);
        // 以后前端访问视频文件的url
        String fileUrl = mediaFile.getFilePath() + "hls/" + m3u8Name;
        mediaFile.setFileUrl(fileUrl);
        mediaFileRepository.save(mediaFile);
        return;
    }
}
