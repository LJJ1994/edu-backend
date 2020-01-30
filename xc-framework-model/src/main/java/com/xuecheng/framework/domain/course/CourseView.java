package com.xuecheng.framework.domain.course;

import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description:
 * @Create: 2020-01-30 14:02:02
 * @Modified By:
 */
@Data
@ToString
@NoArgsConstructor
public class CourseView implements Serializable {
    // 课程详情页的基本信息
    CourseBase courseBase;  // 课程基本信息
    CourseMarket courseMarket;  // 课程营销
    CoursePic coursePic;    // 课程图片
    TeachplanNode teachplanNode;    // 课程教学计划
}
