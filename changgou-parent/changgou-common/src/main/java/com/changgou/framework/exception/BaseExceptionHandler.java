package com.changgou.framework.exception;

import entity.Result;
import entity.StatusCode;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Auther: hftang
 * @Date: 2019/12/23 15:25
 * @Description:
 */

@ControllerAdvice
public class BaseExceptionHandler {


    /***
     * 异常处理
     * @param e
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Result error(Exception e) {
        e.printStackTrace();
        String message = e.getMessage();
        System.out.println("获取到的异常：" + message);

        return new Result(false, StatusCode.ERROR, e.getMessage());
    }
}
