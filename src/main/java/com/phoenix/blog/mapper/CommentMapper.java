package com.phoenix.blog.mapper;

import com.phoenix.blog.model.Users;
import com.phoenix.blog.dto.CommentsDto;
import com.phoenix.blog.model.Comment;
import com.phoenix.blog.model.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "text", source = "commentsDto.text")
    @Mapping(target = "createdDate", expression = "java(java.time.Instant.now())")
    @Mapping(target = "post", source = "post")
    @Mapping(target = "users", source = "users")
    Comment map(CommentsDto commentsDto, Post post, Users users);

    @Mapping(target = "postId", expression = "java(comment.getPost().getPostId())")
    @Mapping(target = "userName", expression = "java(comment.getUsers().getUsername())")
    CommentsDto mapToDto(Comment comment);
}