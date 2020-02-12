package com.xuecheng.api.media;

import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Api(value = "媒资管理接口", description = "提供文件上传，文件处理等接口", tags = {"媒资管理接口o"})
public interface MediaUploadControllerApi {
    @ApiOperation("文件上传注册")
    public ResponseResult register(String fileMd5,
                                   String fileName,
                                   long fileSize,
                                   String mimetype,
                                   String fileExt);

    @ApiOperation("分块检查")
    public CheckChunkResult checkchunk(String fileMd5,
                                       Integer chunk,
                                       Integer chunkSize);

    @ApiOperation("上传分块")
    public ResponseResult uploadchunk(MultipartFile file,
                                      Integer chunk,
                                      String fileMd5);

    @ApiOperation("合并分块")
    public ResponseResult mergeChunk(String fileMd5,
                                     String fileName,
                                     Long fileSize,
                                     String mimetype,
                                     String fileExt);
}
