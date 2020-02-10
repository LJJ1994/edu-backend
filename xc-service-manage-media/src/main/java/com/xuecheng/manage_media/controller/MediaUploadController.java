package com.xuecheng.manage_media.controller;

import com.netflix.discovery.converters.Auto;
import com.xuecheng.api.media.MediaUploadControllerApi;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.service.MediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description:
 * @Create: 2020-02-11 03:07:07
 * @Modified By:
 */
@RestController
@RequestMapping("/media/upload")
public class MediaUploadController implements MediaUploadControllerApi {
    @Autowired
    MediaService mediaService;

    @Override
    @PostMapping("/register")
    public ResponseResult register(String fileMd5, String fileName, long fileSize, String mimetype, String fileExt) {
        return mediaService.register(fileMd5, fileName, fileSize, mimetype, fileExt);
    }

    @Override
    @PostMapping("/checkchunk")
    public CheckChunkResult checkchunk(String fileMd5, Integer chunk, Integer chunkSize) {
        return mediaService.checkchunk(fileMd5, chunk, chunkSize);
    }

    @Override
    @PostMapping("/uploadchunk")
    public ResponseResult uploadchunk(MultipartFile file, Integer chunk, String fileMd5) {
        return mediaService.uploadchunk(file, chunk, fileMd5);
    }

    @Override
    @PostMapping("/mergechunks")
    public ResponseResult mergeChunk(String fileMd5, String fileName, long fileSize, String mimetype, String fileExt) {
        return mediaService.mergeChunk(fileMd5, fileName, fileSize, mimetype, fileExt);
    }
}
