package com.ping.gmall.config;


import com.alibaba.fastjson.JSON;
import com.ping.gmall.util.HttpClientUtil;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.Map;
@Component
public class AuthInterceptor extends HandlerInterceptorAdapter{
    // 多个拦截器执行的顺序？
    // 跟配置文件中，配置拦截器的顺序有关系 1 ，2
    // 用户进入控制器之前！
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如何获取到token ？
        // 用户在登录完成之后 会返回一个 url
        String token = request.getParameter("newToken");
        // 将token 放入cookie 中！
//        Cookie cookie = new Cookie("token",token);
//        response.addCookie(cookie);
        // 当token 不为null 时候放cookie
        if (token != null){
            CookieUtil.setCookie(request,response,"token",token,WebConst.COOKIE_MAXAGE,false);

        }
        if (token == null){
            token = CookieUtil.getCookieValue(request,"token",false);
        }
        // 从cookie 中获取token，解密token！
        if (token != null){
            // 开始解密token 获取nickName
            Map map = getUserMapByToken(token);
            // 取出用户昵称
            String nickName = (String)map.get("nickName");
            // 保存到作用域
            request.setAttribute("nickName",nickName);

        }

        // 在拦截器中获取方法上的注解！
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        // 获取方法上的注解LoginRequire
        LoginRequire methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequire.class);
        if (methodAnnotation != null){
            // 此时有注解 ，
            // 判断用户是否登录了？ 调用verify
            // http://passport.ping.com/verify?token=xxx&salt=x
            // 获取服务器上的ip 地址
            String salt = request.getHeader("X-forwarded-for");
            // 调用verify（）认证 http://passport.ping.com/verify
            String result = HttpClientUtil.doGet(WebConst.VERIFY_ADDRESS + "?token=" + token + "&salt=" + salt);

            if ("success".equals(result)){
                // 登录，认证成功！
                // 保存一下userId
                // 开始解密token 获取nickName
                Map map = getUserMapByToken(token);
                // 取出userId
                String userId = (String)map.get("userId");
                // 保存到作用域
                request.setAttribute("userId",userId);
                return true;

            }else{
                // 认证失败！并且 methodAnnotation.autoRedirect()=true; 必须登录
                if (methodAnnotation.autoRedirect()){
                    // 必须登录！跳转到页面！
                    String requestUrl = request.getRequestURL().toString();
//                    System.out.println("requestUrl:"+ requestUrl);// http://item.gmall.com/36.html
                    // 将url 进行转换
                    // http%3A%2F%2Fitem.gmall.com%2F36.html
                    String encodeUrl = URLEncoder.encode(requestUrl, "UTF-8");
//                    System.out.println("encodeUrl:"+ encodeUrl);

                    response.sendRedirect(WebConst.LOGIN_ADDRESS + "?originUrl=" + encodeUrl);
                    return false;
                }
            }
        }
        return true;
    }
    // 解密token获取map数据
    private Map getUserMapByToken(String token) {
        //eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6IkF0Z3VpZ3UiLCJ1c2VySWQiOiIxIn0.khaU2mavfLwXQN5p8B13S-K0Fs0QgKMA4U00uEWiBtc
        // 获取中间部分
        String tokenUserInfo = StringUtils.substringBetween(token,".");
        System.out.println(tokenUserInfo);
        // 将tokenUserInfo 进行base64解码
        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        // 解码之后得到byte数组
        byte[] decode = base64UrlCodec.decode(tokenUserInfo);
        // 需要先将decode 转成String
        String mapjson = null;
        try {
            mapjson = new String(decode, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 将字符串转换为map 直接返回！
        return JSON.parseObject(mapjson,Map.class);

    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
