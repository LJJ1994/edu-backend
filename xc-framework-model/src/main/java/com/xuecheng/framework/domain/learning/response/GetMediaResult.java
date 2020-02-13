package com.xuecheng.framework.domain.learning.response;

import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description:
 * @Create: 2020-02-13 04:47:47
 * @Modified By:
 */
@Data
@ToString
@NoArgsConstructor
public class GetMediaResult extends ResponseResult {
    // 课程计划对应的媒体信息视频
    private String mediaUrl;
    public GetMediaResult(ResultCode resultCode, String mediaUrl) {
        super(resultCode);
        this.mediaUrl = mediaUrl;
    }
}
