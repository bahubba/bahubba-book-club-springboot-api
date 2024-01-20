package com.bahubba.bahubbabookclub.service;

import com.bahubba.bahubbabookclub.exception.UserNotFoundException;
import com.bahubba.bahubbabookclub.model.dto.UserDTO;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;
import java.util.UUID;

public interface UserService extends OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    /**
     * Retrieve a user by ID
     *
     * @param id The user's ID
     * @return The user's info
     * @throws UserNotFoundException The user was not found
     */
    public UserDTO findByID(UUID id) throws UserNotFoundException;

    /**
     * Retrieve all users
     *
     * @return All users
     */
    // TODO - add pagination
    public List<UserDTO> findAll();

    /**
     * Remove (soft delete) a user
     *
     * @param id The user's ID
     * @return The user's updated info with a departure date
     * @throws UserNotFoundException The user was not found
     */
    public UserDTO removeUser(UUID id) throws UserNotFoundException;
}
