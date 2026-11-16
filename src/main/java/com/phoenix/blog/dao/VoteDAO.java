package com.phoenix.blog.dao;

import com.phoenix.blog.model.Users;
import com.phoenix.blog.model.Post;
import com.phoenix.blog.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoteDAO extends JpaRepository<Vote, Long> {
    Optional<Vote> findTopByPostAndUsersOrderByVoteIdDesc(Post post, Users currentUsers);
}
