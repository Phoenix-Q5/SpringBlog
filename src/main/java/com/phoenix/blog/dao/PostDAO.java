package com.phoenix.blog.dao;

import com.phoenix.blog.model.Subpost;
import com.phoenix.blog.model.Users;
import com.phoenix.blog.model.Post;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostDAO extends JpaRepository<Post, Long> {
    List<Post> findAllBySubpost(Subpost subpost);

    List<Post> findByUsers(Users users);
}
