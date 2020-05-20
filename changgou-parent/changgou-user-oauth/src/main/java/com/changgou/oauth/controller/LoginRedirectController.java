package com.changgou.oauth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Auther: hftang
 * @Date: 2020/3/2 16:27
 * @Description:
 */

@Controller
@RequestMapping("/oauth")
public class LoginRedirectController {

    @RequestMapping("/login")
    public String login(@RequestParam(value = "FROM", required = false, defaultValue = "") String from, Model model) {

        model.addAttribute("from", from);
        return "login";
    }
}
