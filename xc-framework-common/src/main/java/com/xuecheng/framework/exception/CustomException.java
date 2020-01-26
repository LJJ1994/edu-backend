package com.xuecheng.framework.exception;

import com.xuecheng.framework.model.response.ResultCode;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description: 自定义异常类
 * @Create: 2020-01-22 10:11:11
 * @Modified By:
 */
public class CustomException extends RuntimeException{
    ResultCode resultCode;

    public CustomException(ResultCode resultCode) {
        // 异常信息为: 错误代码+错误信息
        super("错误代码：" + resultCode.code() + "错误信息: " + resultCode.message());
        this.resultCode = resultCode;
    }

    public ResultCode getResultCode() {
        return this.resultCode;
    }
}
