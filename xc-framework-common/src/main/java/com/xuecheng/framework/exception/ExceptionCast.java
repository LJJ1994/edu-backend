package com.xuecheng.framework.exception;

import com.xuecheng.framework.model.response.ResultCode;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description: 自定义抛出异常类
 * @Create: 2020-01-22 10:14:14
 * @Modified By:
 */
public class ExceptionCast {
    // 使用静态方法抛出自定义异常
    public static void cast(ResultCode resultCode) {
        throw new CustomException(resultCode);
    }
}
