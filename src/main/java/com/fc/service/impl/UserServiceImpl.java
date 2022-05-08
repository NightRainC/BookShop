package com.fc.service.impl;

import com.fc.dao.UserMapper;
import com.fc.entity.User;
import com.fc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    //登录
    @Override
    public ModelAndView login(ModelAndView mv, HttpServletRequest request, HttpServletResponse response, User tempUser) {
        User user = userMapper.findByUserName(tempUser.getUname());
        if (user != null) {
            if (user.getUpwd().equals(tempUser.getUpwd())) {
                request.setAttribute("user", user);
//                mv.addObject("msg","登录成功");
                HttpSession session = request.getSession(true);
                session.setAttribute("user", user);
                session.setMaxInactiveInterval(60 * 30);
                Cookie cookie = new Cookie("JSESSIONID", session.getId());
                cookie.setMaxAge(30 * 60);
                mv.setViewName("redirect:/");
                response.addCookie(cookie);
            } else {
                mv.addObject("failMsg", "用户名或密码错误");
                mv.setViewName("user_login");
            }
        } else {
            mv.addObject("failMsg", "用户名不存在");
            mv.setViewName("user_login");
        }
        return mv;
    }

    @Override
    public ModelAndView logout(ModelAndView mv, HttpSession session, HttpServletResponse response) {
        //销毁session
        session.removeAttribute("user");
        Cookie cookie = new Cookie("JSESSIONID", null);
        //cookie为0  立即销毁
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        mv.setViewName("redirect:/");
        return mv;
    }

    @Override
    public ModelAndView register(User user, ModelAndView mv) {
        user.setUmark("普通用户");
        user.setUrole(1);
        try {
            userMapper.insert(user);
            mv.setViewName("redirect:user_login.jsp");
        } catch (Exception e) {
            e.printStackTrace();
            mv.addObject("msg", "用户名重复");
            mv.setViewName("user_register");
        }
        return mv;
    }

    @Override
    public ModelAndView changePassword(Integer uid, String oldupwd, String upwd, ModelAndView mv, HttpSession session) {
        User user = userMapper.findById(uid);
        if (user.getUpwd().equals(oldupwd)) {
            try {
                user.setUpwd(upwd);
                userMapper.update(user);
                session.setAttribute("user", user);
                mv.addObject("msg", "修改成功!");
            } catch (Exception e) {
                e.printStackTrace();
                mv.addObject("failMsg", "修改密码时出现错误，请确认原密码是否正确或联系管理员!");
            }
        } else {
            mv.addObject("failMsg", "原密码不正确！");
        }
        mv.setViewName("user_center");
        return mv;
    }

    @Override
    public ModelAndView change(ModelAndView mv, User user, HttpSession session) {
        try {
            userMapper.update(user);
            mv.addObject("msg", "修改成功!");
            User tempUser = (User) session.getAttribute("user");
            tempUser.setUphone(user.getUphone());
            tempUser.setUaddress(user.getUaddress());
            session.setAttribute("user", tempUser);
        } catch (Exception e) {
            e.printStackTrace();
            mv.addObject("failMsg", "服务器正忙,请稍后再试!");
        }
        mv.setViewName("user_center");
        return mv;
    }
}