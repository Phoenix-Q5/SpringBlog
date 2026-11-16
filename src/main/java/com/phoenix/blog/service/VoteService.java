package com.phoenix.blog.service;

import com.phoenix.blog.dto.VoteDto;
import com.phoenix.blog.exceptions.PostNotFoundException;
import com.phoenix.blog.exceptions.SpringPostException;
import com.phoenix.blog.model.Post;
import com.phoenix.blog.model.Vote;
import com.phoenix.blog.dao.PostDAO;
import com.phoenix.blog.dao.VoteDAO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.phoenix.blog.model.VoteType.UPVOTE;

@Service
@AllArgsConstructor
public class VoteService {

    private VoteDAO voteDAO;
    private PostDAO postDAO;
    private AuthService authService;

    @Transactional
    public void vote(VoteDto voteDto) {
        Post post = postDAO.findById(voteDto.getPostId())
                .orElseThrow(() -> new PostNotFoundException("Post not found with ID - " + voteDto.getPostId()));
        Optional<Vote> voteByPostAndUser = voteDAO.findTopByPostAndUsersOrderByVoteIdDesc(post, authService.getCurrentUser());
        if (voteByPostAndUser.isPresent() &&
                voteByPostAndUser.get().getVoteType()
                        .equals(voteDto.getVoteType())) {
            throw new SpringPostException("You have already "
                    + voteDto.getVoteType() + "'d for this post");
        }
        if (UPVOTE.equals(voteDto.getVoteType())) {
            post.setVoteCount(post.getVoteCount() + 1);
        } else {
            post.setVoteCount(post.getVoteCount() - 1);
        }
        voteDAO.save(mapToVote(voteDto, post));
        postDAO.save(post);
    }

    private Vote mapToVote(VoteDto voteDto, Post post) {
        return Vote.builder()
                .voteType(voteDto.getVoteType())
                .post(post)
                .users(authService.getCurrentUser())
                .build();
    }
}
