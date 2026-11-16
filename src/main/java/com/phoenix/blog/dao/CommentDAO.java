package com.phoenix.blog.dao;

import com.phoenix.blog.model.Users;
import com.phoenix.blog.model.Comment;
import com.phoenix.blog.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentDAO extends JpaRepository<Comment, Long> {
    List<Comment> findByPost(Post post);

    List<Comment> findAllByUsers(Users user);
}
