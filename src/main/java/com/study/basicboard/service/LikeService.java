package com.study.basicboard.service;

import com.study.basicboard.domain.entity.Board;
import com.study.basicboard.domain.entity.Like;
import com.study.basicboard.domain.entity.User;
import com.study.basicboard.repository.BoardRepository;
import com.study.basicboard.repository.LikeRepository;
import com.study.basicboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;

    @Transactional
    public void addLike(String loginId, Long boardId) {
        Board board = boardRepository.findById(boardId).get();
        User loginUser = userRepository.findByLoginId(loginId).get();
        User boardUser = board.getUser();

        //글쓴이가 자신이 아니라면 글쓴이의 좋아요 갯수를 +1 한다.
        if (!boardUser.equals(loginUser)) {
            boardUser.likeChange(boardUser.getReceivedLikeCnt() + 1);
        }

        board.likeChange(board.getLikeCnt() + 1);

        likeRepository.save(Like.builder()
                .user(loginUser)
                .board(board)
                .build());
    }

    @Transactional
    public void deleteLike(String loginId, Long boardId) {
        Board board = boardRepository.findById(boardId).get();
        User loginUser = userRepository.findByLoginId(loginId).get();
        User boardUser = board.getUser();

        //글쓴이가 자신이 아니라면 글쓴이의 좋아요 갯수를 -1 한다.
        if (!boardUser.equals(loginUser)) {
            boardUser.likeChange(boardUser.getReceivedLikeCnt() - 1);
        }

        board.likeChange(board.getLikeCnt() - 1);
        likeRepository.deleteByUserLoginIdAndBoardId(loginId, boardId);
    }

    public Boolean checkLike(String loginId, Long boardId) {
        return likeRepository.existsByUserLoginIdAndBoardId(loginId, boardId);
    }
}
