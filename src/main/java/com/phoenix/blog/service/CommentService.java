package com.phoenix.blog.service;

import com.phoenix.blog.mapper.CommentMapper;
import com.phoenix.blog.model.*;
import com.phoenix.blog.dto.CommentsDto;
import com.phoenix.blog.exceptions.PostNotFoundException;
import com.phoenix.blog.dao.CommentDAO;
import com.phoenix.blog.dao.PostDAO;
import com.phoenix.blog.dao.UserDAO;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@AllArgsConstructor
public class CommentService {
    private static final String POST_URL = "";
    private PostDAO postDAO;
    private UserDAO userDAO;
    private AuthService authService;
    private CommentMapper commentMapper;
    private CommentDAO commentDAO;
    private MailContentBuilder mailContentBuilder;
    private MailService mailService;

    public void save(CommentsDto commentsDto) {
        Post post = postDAO.findById(commentsDto.getPostId())
                .orElseThrow(() -> new PostNotFoundException(commentsDto.getPostId().toString()));
        Comment comment = commentMapper.map(commentsDto, post, authService.getCurrentUser());
        commentDAO.save(comment);

        String message = mailContentBuilder.build(post.getUsers().getUsername() + " posted a comment on your post." + POST_URL);
        sendCommentNotification(message, post.getUsers());
    }

    private void sendCommentNotification(String message, Users users) {
        mailService.sendMail(new NotificationEmail(users.getUsername() + " Commented on your post", users.getEmail(), message));
    }

    public List<CommentsDto> getAllCommentsForPost(Long postId) {
        Post post = postDAO.findById(postId).orElseThrow(() -> new PostNotFoundException(postId.toString()));
        return commentDAO.findByPost(post)
                .stream()
                .map(commentMapper::mapToDto).collect(toList());
    }

    public List<CommentsDto> getAllCommentsForUser(String userName) {
        Users users = userDAO.findByUsername(userName)
                .orElseThrow(() -> new UsernameNotFoundException(userName));
        return commentDAO.findAllByUsers(users)
                .stream()
                .map(commentMapper::mapToDto)
                .collect(toList());
    }
}
