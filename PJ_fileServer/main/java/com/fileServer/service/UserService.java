package com.fileServer.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fileServer.entity.User;
import com.fileServer.mapper.UserMapper;

@Service
@Transactional
public class UserService {
   @Autowired
    UserMapper userMapper;

    public List<User> findAll(){
    	List<User> userList = new ArrayList<User>();
        userList = userMapper.findAll();
        return userList;
    }
}
