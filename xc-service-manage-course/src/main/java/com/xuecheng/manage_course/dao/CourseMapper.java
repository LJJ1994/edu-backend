package com.xuecheng.manage_course.dao;

import com.github.pagehelper.Page;
import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
* @Description: CourseMapper
* @Author: LJJ
* @Date: 2020/1/26 21:06
* @Modified By:
*/
@Mapper
@Component
public interface CourseMapper {
   CourseBase findCourseBaseById(String id);

   Page<CourseInfo> findCourseListPage(CourseListRequest courseListRequest);
}
