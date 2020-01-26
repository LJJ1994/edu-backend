package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.cms.CmsConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
* @Description: CmsConfigControllerApi
* @Author: LJJ
* @Date: 2020/1/23 9:48
* @Modified By:
*/
@Api(value = "cms配置管理接口", description = "cms配置管理接口,提供数据模型的管理、查询接口")
public interface CmsConfigControllerApi {
    @ApiOperation("通过id查询Cms配置信息")
    public CmsConfig getModel(String id);
}
