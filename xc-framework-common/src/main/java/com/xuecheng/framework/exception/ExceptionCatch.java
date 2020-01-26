package com.xuecheng.framework.exception;

import com.google.common.collect.ImmutableMap;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description: 捕获自定义异常
 * @Create: 2020-01-22 10:16:16
 * @Modified By:
 */
@ControllerAdvice
public class ExceptionCatch {
    // 日志记录
    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionCatch.class);

    // 使用ImmutableMap存放错误类型和错误代码，该Map特点是一旦创建就不可改变，并且线程安全
    public static ImmutableMap<Class<? extends Throwable>, ResultCode> Exceptions;

    // 使用builder来构建一个异常类型和错误代码的异常
    protected static ImmutableMap.Builder<Class<? extends Throwable>, ResultCode> builder = ImmutableMap.builder();
    // 捕获自定义异常
    @ExceptionHandler(CustomException.class)
    @ResponseBody
    public ResponseResult customException(CustomException e) {
        // 日志记录异常
        LOGGER.error("Catch Exception: {}\r\nException", e.getMessage());
        ResultCode resultCode = e.getResultCode();
        ResponseResult responseResult = new ResponseResult(resultCode);
        return responseResult;
    }

    // 捕获框架或者第三方异常
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseResult exception(Exception e) {
        LOGGER.error("Catch Exception: {}\r\n Exception: ", e.getMessage(), e);
        if (Exceptions == null) {
            Exceptions = builder.build();
        }
        final ResultCode resultCode = Exceptions.get(e.getClass());
        final ResponseResult responseResult;
        if (resultCode != null) {
            responseResult = new ResponseResult(resultCode);
        } else {
            responseResult = new ResponseResult(CommonCode.SERVER_ERROR);
        }
        return responseResult;
    }
    static {
        builder.put(HttpMessageNotReadableException.class, CommonCode.INVALID_PARAM);
    }
}
