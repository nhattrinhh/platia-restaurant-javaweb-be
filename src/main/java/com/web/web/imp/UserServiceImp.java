package com.web.web.imp;

import java.util.List;
import java.util.Optional;

import com.web.web.Dto.UserDTO;
import com.web.web.Entity.User;

public interface UserServiceImp {
    User registerUser(String username, String password, String email, String fullname,
                             String address, String phoneNumber, String... roleNames);

    List<User> getAllUsers();

    Optional<User> getUserById(Long id);

    void deleteUser(Long id);

     boolean deleteByUsername(String username);

     User findByUsername(String username);

     User createUser(UserDTO dto);

     User updateUser(String username, UserDTO dto);

     User updateOwnProfile(String username, UserDTO dto);
}
