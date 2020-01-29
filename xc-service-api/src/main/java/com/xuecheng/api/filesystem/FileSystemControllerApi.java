package com.xuecheng.api.filesystem;

import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.multipart.MultipartFile;

@Api(value = "文件系统管理接口", description = "文件系统管理，提供增删改查")
public interface FileSystemControllerApi {
    /**
     * @Description: upload
     * @param multipartFile 上传的文件
     * @param fileTag   文件标签
     * @param businessKey   业务键，各个子系统的业务键
     * @param metadata  文件元信息
     * @return: com.xuecheng.framework.domain.filesystem.response.UploadFileResult
     * @Author: LJJ
     * @Date: 2020/1/29 16:40
     */
    @ApiOperation("文件上传接口")
    public UploadFileResult upload(MultipartFile multipartFile,
                                   String fileTag,
                                   String businessKey,
                                   String metadata);
}
