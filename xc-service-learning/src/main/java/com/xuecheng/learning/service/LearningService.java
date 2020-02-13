package com.xuecheng.learning.service;

import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.learning.response.GetMediaResult;
import com.xuecheng.framework.domain.learning.response.LearningCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.learning.client.CourseSearchClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description:
 * @Create: 2020-02-13 04:54:54
 * @Modified By:
 */
@Service
public class LearningService {
    @Autowired
    CourseSearchClient  courseSearchClient;

    public GetMediaResult getMedia(String courseId, String teachplanId) {
        // 远程调用搜索服务
        TeachplanMediaPub teachplanMediaPub = courseSearchClient.getmedia(teachplanId);
        if (teachplanMediaPub==null || StringUtils.isEmpty(teachplanMediaPub.getMediaUrl())) {
            ExceptionCast.cast(LearningCode.LEARNING_GETMEDIA_ERROR);
        }
        String mediaUrl = teachplanMediaPub.getMediaUrl();
        return new GetMediaResult(CommonCode.SUCCESS, mediaUrl);
    }
}
