package com.ams.service;

import com.ams.dto.LoginResponse;
import com.ams.entity.User;

public interface IUserService {
     LoginResponse getCurrentUser(String email);
}
