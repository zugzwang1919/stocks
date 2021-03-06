package com.wolfesoftware.stocks.repository;

import com.wolfesoftware.stocks.exception.DuplicateException;
import com.wolfesoftware.stocks.exception.NotFoundException;
import com.wolfesoftware.stocks.model.User;
import com.wolfesoftware.stocks.repository.cloaked.LowLevelUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Optional;

@Repository
public class UserRepository  {

    public static final Logger logger = LoggerFactory.getLogger(UserRepository.class);

    @Resource
    LowLevelUserRepository lowLevelUserRepository;


    public Optional<User> findUserById(Long id) {
        logger.debug("Inside findUserById() where id = {}", id);
        Optional<User> userToBeReturned =  lowLevelUserRepository.findById(id);
        logger.debug("Inside findUserById() after retrieval");
        return userToBeReturned;
    }

    public Optional<User> findUserByUserName(String username) {
        logger.debug("Inside findUserByUserName() where user name = {}", username);
        Optional<User> userToBeReturned =  lowLevelUserRepository.findByUsername(username);
        logger.debug("Inside findUserByUserName() after retrieval");
        return userToBeReturned;
    }

    public Optional<User> findUserByGoogleid(String googleid) {
        logger.debug("Inside findUserByGoogleid() where googleid = {}", googleid);
        Optional<User> userToBeReturned =  lowLevelUserRepository.findByGoogleid(googleid);
        logger.debug("Inside findUserByGoogleid() after retrieval");
        return userToBeReturned;
    }

    public User createUser(User user) {
        if (lowLevelUserRepository.existsByUsername(user.getUsername()))
            throw new DuplicateException("User Name already exists.");
        User u = lowLevelUserRepository.save(user);


        return u;
    }

    public User updateUser(User user) {
        if (!lowLevelUserRepository.existsByUsername(user.getUsername()))
            throw new DuplicateException("User Name already exists.");

        return lowLevelUserRepository.save(user);
    }

    public void deleteUser(Long id) {
        if (!lowLevelUserRepository.existsById(id))
            throw new NotFoundException("User Name does not exist");

         lowLevelUserRepository.deleteById(id);
    }

}
