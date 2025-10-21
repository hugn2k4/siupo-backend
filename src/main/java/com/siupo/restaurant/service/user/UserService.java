package com.siupo.restaurant.service.user;

import com.siupo.restaurant.model.User;

public interface UserService {
    User getUserByEmail(String email);
}
