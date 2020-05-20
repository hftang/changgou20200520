package com.changgou.user.feign;

import com.changgou.user.pojo.User;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/****
 * @Author:hftang
 * @Description:
 * @Date 2019/6/18 13:58
 *****/
@FeignClient(name = "user")
@RequestMapping("/user")
public interface UserFeign {

    @GetMapping("/load/{id}")
    public Result<User> findByUsername(@PathVariable(name = "id") String id);

    /**
     * 给用户添加积分
     *
     * @param points
     * @return
     */
    @GetMapping(value = "/points/add")
    public Result addUserPoints(@RequestParam("points") Integer points) ;



}