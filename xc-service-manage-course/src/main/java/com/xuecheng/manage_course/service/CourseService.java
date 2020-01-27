package com.xuecheng.manage_course.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.CourseMarket;
import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.dao.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description: 课程查询service层
 * @Create: 2020-01-26 22:54:54
 * @Modified By:
 */
@Service
public class CourseService{
    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    CourseBaseRepository courseBaseRepository;

    @Autowired
    CourseTeachplanRepository courseTeachplanRepository;

    @Autowired
    CourseMarketRepository courseMarketRepository;

    @Autowired
    CourseMapper courseMapper;

    /**
     * @Description: findTeachplanList 查询课程计划
     *
     * @param courseId	
     * @return: com.xuecheng.framework.domain.course.ext.TeachplanNode
     * @Author: LJJ
     * @Date: 2020/1/26 22:57
     */
    public TeachplanNode findTeachplanList(String courseId) {
        TeachplanNode teachplanNode = teachplanMapper.selectList(courseId);
        return teachplanNode;
    }
    
    /**
     * @Description: 获取课程根节点，如果没有则添加根节点
     *
     * @param courseId	
     * @return: java.lang.String
     * @Author: LJJ
     * @Date: 2020/1/27 12:39
     */
    public String getTeachplanRoot(String courseId) {
        // 校验课程id
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if (!optional.isPresent()) {
            return null;
        }
        CourseBase courseBase = optional.get();
        // 获取课程计划根节点
        List<Teachplan> teachplanList = courseTeachplanRepository.findByCourseidAndParentid(courseId, "0");
        if (teachplanList == null || teachplanList.size() <= 0) {
            // 新增一个根节点
            Teachplan teachplanRoot = new Teachplan();
            teachplanRoot.setCourseid(courseId);
            teachplanRoot.setParentid("0");
            teachplanRoot.setPname(courseBase.getName());
            teachplanRoot.setGrade("1");
            teachplanRoot.setPtype("0");
            courseTeachplanRepository.save(teachplanRoot);
            return teachplanRoot.getId();
        }
        Teachplan teachplan = teachplanList.get(0);
        return teachplan.getId();
    }
    /**
     * @Description: addTeachplan 添加课程计划
     *
     * @param teachplan	
     * @return: com.xuecheng.framework.model.response.ResponseResult
     * @Author: LJJ
     * @Date: 2020/1/27 12:47
     */
    @Transactional
    public ResponseResult addTeachplan(Teachplan teachplan) {
        // 校验课程id和课程名称
        if (teachplan == null ||
                StringUtils.isEmpty(teachplan.getCourseid()) ||
                    StringUtils.isEmpty(teachplan.getPname())) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        // 获取课程id
        String courseId = teachplan.getCourseid();
        // 获取父节点id
        String parentId = teachplan.getParentid();
        if (StringUtils.isEmpty(parentId)) {
            // 如果父节点id为空，那生成一个父节点
            parentId = getTeachplanRoot(courseId);
        }
        // 获取父节点信息
        Optional<Teachplan> teachplanOptional = courseTeachplanRepository.findById(parentId);
        if (!teachplanOptional.isPresent()) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        // 父节点
        Teachplan teachplanParent = teachplanOptional.get();
        // 父节点级别
        String parentGrade = teachplanParent.getGrade();
        // 新节点
        Teachplan teachplanNew = new Teachplan();
        BeanUtils.copyProperties(teachplan, teachplanNew);
        teachplanNew.setCourseid(courseId);
        teachplanNew.setParentid(parentId);
        teachplanNew.setStatus("0");
        if (parentGrade.equals("1")) {
            teachplanNew.setGrade("2");
        } else if(parentGrade.equals("2")){
            teachplanNew.setGrade("3");
        }
        courseTeachplanRepository.save(teachplanNew);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * @Description: 分页查询我的课程，使用pagehelper分页插件
     *
     * @param page
     * @param size
     * @param courseListRequest
     * @return: com.xuecheng.framework.model.response.QueryResponseResult<com.xuecheng.framework.domain.course.ext.CourseInfo>
     * @Author: LJJ
     * @Date: 2020/1/27 15:14
     */
    public QueryResponseResult<CourseInfo> findCourseList(int page, int size, CourseListRequest courseListRequest) {
        if (courseListRequest == null) {
            courseListRequest = new CourseListRequest();
        }
        if (page <= 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 10;
        }
        PageHelper.startPage(page, size);
        Page<CourseInfo> courseListPage = courseMapper.findCourseListPage(courseListRequest);
        List<CourseInfo> infoList = courseListPage.getResult();
        long total = courseListPage.getTotal();
        QueryResult<CourseInfo> queryResult = new QueryResult<>();
        queryResult.setList(infoList);
        queryResult.setTotal(total);
        return new QueryResponseResult<CourseInfo>(CommonCode.SUCCESS, queryResult);
    }

    /**
     * @Description: addCourseBase 添加课程
     *
     * @param courseBase
     * @return: com.xuecheng.framework.domain.course.response.AddCourseResult
     * @Author: LJJ
     * @Date: 2020/1/27 16:23
     */
    @Transactional
    public AddCourseResult addCourseBase(CourseBase courseBase) {
        // 设置课程为未发布状态
        courseBase.setStatus("202001");
        courseBaseRepository.save(courseBase);
        return new AddCourseResult(CommonCode.SUCCESS, courseBase.getId());
    }

    /**
     * @Description: getCourseBaseById 获取某个课程基础信息
     *
     * @param courseId
     * @return: com.xuecheng.framework.domain.course.CourseBase
     * @Author: LJJ
     * @Date: 2020/1/27 16:34
     */
    public CourseBase getCourseBaseById(String courseId) {
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    @Transactional
    public ResponseResult updateCourseBase(String courseId, CourseBase courseBase) {
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if (!optional.isPresent()) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        CourseBase one = optional.get();
        one.setName(courseBase.getName());
        one.setMt(courseBase.getMt());
        one.setSt(courseBase.getSt());
        one.setGrade(courseBase.getGrade());
        one.setStatus(courseBase.getStatus());
        one.setUsers(courseBase.getUsers());
        one.setStudymodel(courseBase.getStudymodel());
        one.setDescription(courseBase.getDescription());
        courseBaseRepository.save(one);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * @Description: getCourseMarketById 通过id查询课程营销信息
     *
     * @param courseId 课程id
     * @return: com.xuecheng.framework.domain.course.CourseMarket
     * @Author: LJJ
     * @Date: 2020/1/27 17:05
     */
    public CourseMarket getCourseMarketById(String courseId) {
        Optional<CourseMarket> optional = courseMarketRepository.findById(courseId);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    /**
     * @Description: updateCourseMarket 如果营销信息存在则修改，不存在则添加。
     *
     * @param courseId
 * @param courseMarket
     * @return: com.xuecheng.framework.domain.course.CourseMarket
     * @Author: LJJ
     * @Date: 2020/1/27 17:14
     */
    public CourseMarket updateCourseMarket(String courseId, CourseMarket courseMarket) {
        CourseMarket one = this.getCourseMarketById(courseId);
        if (one != null) {
            one.setCharge(courseMarket.getCharge());
            one.setStartTime(courseMarket.getStartTime());
            one.setEndTime(courseMarket.getEndTime());
            one.setValid(courseMarket.getValid());
            one.setPrice(courseMarket.getPrice());
            one.setQq(courseMarket.getQq());
            courseMarketRepository.save(one);

        } else {
            one = new CourseMarket();
            BeanUtils.copyProperties(courseMarket, one);
            one.setId(courseId);
            courseMarketRepository.save(one);
        }
        return one;
    }
}
