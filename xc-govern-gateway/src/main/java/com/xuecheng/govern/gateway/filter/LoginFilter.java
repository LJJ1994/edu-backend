package com.xuecheng.govern.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.govern.gateway.service.AuthService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description:
 * @Create: 2020-02-16 04:01:01
 * @Modified By:
 */
@Component
public class LoginFilter extends ZuulFilter {
    @Autowired
    AuthService authService;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException{
        RequestContext currentContext = RequestContext.getCurrentContext();
        HttpServletRequest request = currentContext.getRequest();
        // 查询cookie令牌
        String accessToken = authService.getTokenFromCookie(request);
        if (StringUtils.isEmpty(accessToken)) {
            this.accessDenied();
            return null;
        }
        // 查询redis
        long expire = authService.getExpire(accessToken);
        if (expire<0) {
            this.accessDenied();
            return null;
        }
        // 查询header
        String jwtFromHeader = authService.getJwtFromHeader(request);
        if (StringUtils.isEmpty(jwtFromHeader)) {
            this.accessDenied();
            return null;
        }
        return null;
    }

    // 拒绝访问
    private void accessDenied() {
        RequestContext currentContext = RequestContext.getCurrentContext();
        HttpServletResponse response = currentContext.getResponse();
        currentContext.setResponseStatusCode(200);
        currentContext.setSendZuulResponse(false);
        ResponseResult responseResult = new ResponseResult(CommonCode.UNAUTHENTICATED);
        String jsonString = JSON.toJSONString(responseResult);
        currentContext.setResponseBody(jsonString);
        response.setContentType("application/json;charset=utf-8");
    }
}
