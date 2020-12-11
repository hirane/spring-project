package com.fileServer.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import com.fileServer.entity.User;
import com.fileServer.service.UserService;

@Controller
public class UserController {

    @Autowired
    UserService UserService;

    @GetMapping("/userList")
    public ModelAndView getPerson(ModelAndView mav) {
    	List<User> userlist = new ArrayList<User>();;
        userlist = UserService.findAll();
        mav.addObject("userlist",userlist);
        mav.setViewName("userList_test");
        return mav;
    }
}
