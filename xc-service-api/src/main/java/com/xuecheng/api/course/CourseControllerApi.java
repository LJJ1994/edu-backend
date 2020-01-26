package com.xuecheng.api.course;

import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
* @Description: CourseControllerApi
* @Author: LJJ
* @Date: 2020/1/26 22:33
* @Modified By:
*/
@Api(value = "课程计划管理接口", description = "课程计划管理，提供增删改查")
public interface CourseControllerApi {
    @ApiOperation("课程计划查询")
    public TeachplanNode findTeachplanList(String courseId);
}
