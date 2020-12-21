package com.ping.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.ping.gmall.bean.UserInfo;
import com.ping.gmall.passport.config.JwtUtil;
import com.ping.gmall.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {

    @Reference
    private UserService userService;

    @RequestMapping("index")
    public String index (HttpServletRequest request){
        String originUrl = request.getParameter("originUrl");
        request.setAttribute("originUrl",originUrl);



        return "index";
    }
    @Value("${token.key}")
    String signKey;

    @RequestMapping("login")
    @ResponseBody
    public String login(UserInfo userInfo,HttpServletRequest request){
        String salt = request.getHeader("X-forwarded-for");
        if (userInfo != null){
            UserInfo loginUser = userService.login(userInfo);
            if (loginUser == null){
                return "fail";

            }else{
                Map<String, Object> map = new HashMap<>();
                map.put("userId",loginUser.getId());
                map.put("nickName",loginUser.getNickName());
                String token = JwtUtil.encode(signKey, map, salt);

                return token;
            }
        }
        return "fail";
    }
    // http://passport.ping.com/verify?token=xxx&salt=x
    @RequestMapping("verify")
    @ResponseBody
    public String verify(HttpServletRequest request){
        String token = request.getParameter("token");
        String salt = request.getParameter("salt");



        Map<String, Object> map = JwtUtil.decode(token, signKey, salt);
        if (map != null){
            String userId = (String)map.get("userId");
            UserInfo userInfo = userService.verify(userId);
            if (userInfo != null){
                return "success";
            }
        }
        return "fail";
    }

}



