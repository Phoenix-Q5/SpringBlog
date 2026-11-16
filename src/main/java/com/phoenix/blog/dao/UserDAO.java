package com.phoenix.blog.dao;

import com.phoenix.blog.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDAO extends JpaRepository<Users, Long> {
    Optional<Users> findByUsername(String username);
}
