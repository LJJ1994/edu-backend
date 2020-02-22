package com.xuecheng.order.config;

import com.xuecheng.framework.client.XcServiceList;
import com.xuecheng.framework.domain.course.CourseMarket;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description: 远程调用用户用心服务
 * @Create: 2020-02-15 13:57:57
 * @Modified By:
 */
@FeignClient(value = XcServiceList.XC_SERVICE_MANAGE_COURSE)
public interface CourseClient {
    // 获取课程营销信息
    @GetMapping("/course/coursemarket/get/{courseId}")
    public CourseMarket getCourseMarketById(@PathVariable("courseId") String courseId);
}
