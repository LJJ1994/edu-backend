package com.xuecheng.manage_course.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.netflix.discovery.converters.Auto;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.client.CmsPageClient;
import com.xuecheng.manage_course.dao.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

    @Autowired
    CoursePicRepository coursePicRepository;

    @Autowired
    CmsPageClient cmsPageClient;

    @Autowired
    CoursePubRepository coursePubRepository;

    @Autowired
    TeachplanMediaRepository teachplanMediaRepository;

    @Autowired
    TeachplanRepository teachplanRepository;

    @Autowired
    TeachplanMediaPubRepository teachplanMediaPubRepository;

    // 课程详情cms-page配置信息
    @Value("${course‐publish.dataUrlPre}")
    private String publish_dataUrlPre;
    @Value("${course‐publish.pagePhysicalPath}")
    private String publish_page_physicalpath;
    @Value("${course‐publish.pageWebPath}")
    private String publish_page_webpath;
    @Value("${course‐publish.siteId}")
    private String publish_siteId;
    @Value("${course‐publish.templateId}")
    private String publish_templateId;
    @Value("${course‐publish.previewUrl}")
    private String previewUrl;

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

    // 删除课程计划
    @Transactional
    public ResponseResult deleteTeachplan(String teachplanId) {
        Optional<Teachplan> optional = courseTeachplanRepository.findById(teachplanId);
        if (!optional.isPresent()) {
            return new ResponseResult(CommonCode.FAIL);
        }
        courseTeachplanRepository.deleteById(teachplanId);
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
    public QueryResponseResult<CourseInfo> findCourseList(String companyId, int page, int size, CourseListRequest courseListRequest) {
        if (courseListRequest == null) {
            courseListRequest = new CourseListRequest();
        }
        if (page <= 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 10;
        }
        courseListRequest.setCompanyId(companyId);
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
    @Transactional
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

    // 添加课程图片
    @Transactional
    public ResponseResult addCoursePic(String courseId, String pic) {
        // 查询课程图片
        // 如果有图片则修改，没有则添加
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        CoursePic coursePic = null;
        if (optional.isPresent()) {
            coursePic = optional.get();
        }
        if (coursePic == null) {
            coursePic = new CoursePic();
        }
        coursePic.setCourseid(courseId);
        coursePic.setPic(pic);
        coursePicRepository.save(coursePic);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    // 查询课程图片信息
    public CoursePic findCoursePic(String courseId) {
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    /**
     * @Description: deleteCoursePic 删除图片，成功返回1，失败返回0
     * @param courseId
     * @return: com.xuecheng.framework.model.response.ResponseResult
     * @Author: LJJ
     * @Date: 2020/1/29 18:28
     */
    @Transactional
    public ResponseResult deleteCoursePic(String courseId) {
        long optional = coursePicRepository.deleteByCourseid(courseId);
        if (optional > 0) {
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    /**
     * @Description: getCourseView 查询课程详情页的基本信息
     *
     * @param courseId
     * @return: com.xuecheng.framework.domain.course.CourseView
     * @Author: LJJ
     * @Date: 2020/1/30 14:12
     */
    public CourseView getCourseView(String courseId) {
        CourseView courseView = new CourseView();
        // 查询课程基本信息
        Optional<CourseBase> optionalCourseBase = courseBaseRepository.findById(courseId);
        if (optionalCourseBase.isPresent()) {
            CourseBase courseBase = optionalCourseBase.get();
            courseView.setCourseBase(courseBase);
        }
        // 查询课程图片
        Optional<CoursePic> optionalCoursePic = coursePicRepository.findById(courseId);
        if (optionalCoursePic.isPresent()) {
            CoursePic coursePic = optionalCoursePic.get();
            courseView.setCoursePic(coursePic);
        }
        // 查询课程营销
        Optional<CourseMarket> optionalCourseMarket = courseMarketRepository.findById(courseId);
        if (optionalCourseMarket.isPresent()) {
            CourseMarket courseMarket = optionalCourseMarket.get();
            courseView.setCourseMarket(courseMarket);
        }
        // 查询课程计划
        TeachplanNode teachplanNode = teachplanMapper.selectList(courseId);
        courseView.setTeachplanNode(teachplanNode);
        return courseView;
    }

    // 根据courseId查询课程基本信息
    public CourseBase findCourseBaseById(String courseId) {
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    // 课程预览
    public CoursePublishResult preview(String courseId) {
        CourseBase one = this.findCourseBaseById(courseId);
        // 发布课程预览页面
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(publish_siteId);
        cmsPage.setTemplateId(publish_templateId);
        cmsPage.setPageName(courseId + ".html");
        cmsPage.setPageAliase(one.getName());
        cmsPage.setPageWebPath(publish_page_webpath);
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);
        cmsPage.setDataUrl(publish_dataUrlPre + courseId);

        // 远程请求CMS服务, 保存页面信息
        CmsPageResult cmsPageResult = cmsPageClient.save(cmsPage);
        if (!cmsPageResult.isSuccess()) {
            return new CoursePublishResult(CommonCode.FAIL, null);
        }
        // 获取到页面id
        String pageId = cmsPageResult.getCmsPage().getPageId();
        // 获取页面url
        String pageUrl = previewUrl + pageId;
        return new CoursePublishResult(CommonCode.SUCCESS, pageUrl);
    }
    // 课程发布
    @Transactional
    public CoursePublishResult publish(String courseId) {
        CourseBase one = this.findCourseBaseById(courseId);
        // 发布页面
        CmsPostPageResult cmsPostPageResult = publish_page(courseId);
        if (!cmsPostPageResult.isSuccess()) {
            return new CoursePublishResult(CommonCode.FAIL, null);
        }
        // 更新课程发布状态
        CourseBase courseBase = this.saveCoursePubState(courseId);
        // 保存课程索引信息
        CoursePub coursePub = createCoursePub(courseId);
        saveCoursePub(courseId, coursePub);
        // 保存ES课程计划媒资信息
        this.saveTeachplanMediaPub(courseId);
        String pageUrl = cmsPostPageResult.getPageUrl();
        return new CoursePublishResult(CommonCode.SUCCESS, pageUrl);
    }

    // 保存ES课程计划媒资信息
    @Transactional
    void saveTeachplanMediaPub(String courseId) {
        List<TeachplanMedia> teachplanMediaList = teachplanMediaRepository.findByCourseId(courseId);
        // 先删除表里的数据，保证更改或者更新课程信息的时候能及时修改ES中的数据
        long l = teachplanMediaPubRepository.deleteByCourseId(courseId);
        List<TeachplanMediaPub> teachplanMediaPubList = new ArrayList<>();
        for (TeachplanMedia teachplanMedia:teachplanMediaList) {
            TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
            BeanUtils.copyProperties(teachplanMedia, teachplanMediaPub);
            teachplanMediaPub.setTimestamp(new Date());
            teachplanMediaPubRepository.save(teachplanMediaPub);
        }
    }
    // 更新课程发布状态
    private CourseBase saveCoursePubState(String courseId) {
        CourseBase courseBase = this.findCourseBaseById(courseId);
        courseBase.setStatus("202002");
        CourseBase save = courseBaseRepository.save(courseBase);
        return save;
    }
    // 课程发布页面
    public CmsPostPageResult publish_page(String courseId) {
        CourseBase one = this.findCourseBaseById(courseId);
        // 发布课程预览页面
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(publish_siteId);
        cmsPage.setTemplateId(publish_templateId);
        cmsPage.setPageName(courseId + ".html");
        cmsPage.setPageAliase(one.getName());
        cmsPage.setPageWebPath(publish_page_webpath);
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);
        cmsPage.setDataUrl(publish_dataUrlPre + courseId);

        // 远程请求CMS服务, 保存页面信息
        CmsPostPageResult cmsPostPageResult = cmsPageClient.postPageQuick(cmsPage);
        return cmsPostPageResult;
    }

    // 创建CoursePub
    private CoursePub createCoursePub(String id) {
        CoursePub coursePub = new CoursePub();
        coursePub.setId(id);
        // 查询课程基本信息
        Optional<CourseBase> courseBaseOptional = courseBaseRepository.findById(id);
        if (courseBaseOptional.isPresent()) {
            CourseBase courseBase = courseBaseOptional.get();
            BeanUtils.copyProperties(courseBase, coursePub);
        }
        // 查询课程图片
        Optional<CoursePic> coursePicOptional = coursePicRepository.findById(id);
        if (coursePicOptional.isPresent()) {
            CoursePic coursePic = coursePicOptional.get();
            BeanUtils.copyProperties(coursePic, coursePub);
        }
        // 查询课程营销
        Optional<CourseMarket> courseMarketOptional = courseMarketRepository.findById(id);
        if (courseMarketOptional.isPresent()) {
            CourseMarket courseMarket = courseMarketOptional.get();
            BeanUtils.copyProperties(courseMarket, coursePub);
        }
        // 查询课程计划
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        // 转换为json字符串
        String jsonString = JSON.toJSONString(teachplanNode);
        coursePub.setTeachplan(jsonString);
        return coursePub;
    }

    // 保存coursePub到数据库
    private CoursePub saveCoursePub(String id, CoursePub coursePub) {
        if (StringUtils.isEmpty(id)) {
            ExceptionCast.cast(CourseCode.COURSE_PUBLISH_COURSEIDISNULL);
        }
        CoursePub coursePubNew = null;
        Optional<CoursePub> coursePubOptional = coursePubRepository.findById(id);
        if (coursePubOptional.isPresent()) {
            coursePubNew = coursePubOptional.get();
        } else {
            coursePubNew = new CoursePub();
        }
        BeanUtils.copyProperties(coursePub, coursePubNew);
        coursePubNew.setId(id);
        coursePub.setTimestamp(new Date());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        String format = simpleDateFormat.format(new Date());
        coursePubNew.setPubTime(format);
        coursePubRepository.save(coursePubNew);
        return coursePubNew;
    }

    // 保存课程计划对应的媒资信息
    public ResponseResult savemedia(TeachplanMedia teachplanMedia) {
        if (teachplanMedia == null) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        String teachplanId = teachplanMedia.getTeachplanId();
        Optional<Teachplan> teachplanOptional = teachplanRepository.findById(teachplanId);
        if (!teachplanOptional.isPresent()) {
            ExceptionCast.cast(CourseCode.COURSE_MEDIA_TEACHPLAN_ISNULL);
        }
        Teachplan teachplan = teachplanOptional.get();
        String grade = teachplan.getGrade();
        if (grade == null || !grade.equals("3")) {
            ExceptionCast.cast(CourseCode.COURSE_TEACHPLAN_GRADE_IS_NOT_THEAF); // 选择的节点不是课程计划叶子节点
        }
        TeachplanMedia one = null;
        Optional<TeachplanMedia> teachplanMediaOptional = teachplanMediaRepository.findById(teachplanId);
        if (teachplanMediaOptional.isPresent()) {
            one = teachplanMediaOptional.get();
        } else {
            one = new TeachplanMedia();
        }
        // 保存课程计划对应的媒资信息
        one.setTeachplanId(teachplanId);
        one.setCourseId(teachplanMedia.getCourseId());
        one.setMediaFileOriginalName(teachplanMedia.getMediaFileOriginalName());
        one.setMediaId(teachplanMedia.getMediaId());
        one.setMediaUrl(teachplanMedia.getMediaUrl());
        teachplanMediaRepository.save(one);

        return new ResponseResult(CommonCode.SUCCESS);
    }


}
