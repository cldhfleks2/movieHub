package com.cldhfleks2.moviehub.community;

import com.cldhfleks2.moviehub.postreview.PostReviewRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class CommunityController {
    private final CommunityService communityService;

    //커뮤니티 페이지 GET : 페이지번호, 카테고리탭, 정렬기준 선택
    @GetMapping("/community")
    String getCommunity(Integer pageIdx, String keyword, String category, String sort, Model model, Authentication auth) {
        return communityService.getCommunity(pageIdx, keyword, category, sort, model, auth);
    }

    //게시글 상세 페이지 GET : 방문시 view +1
    @GetMapping("/postDetail/{postId}")
    String getPostDetail(@PathVariable Long postId, Model model, Authentication auth, RedirectAttributes redirectAttributes) {
        return communityService.getPostDetail(postId, model, auth, redirectAttributes);
    }

    //게시글 작성 페이지 GET
    @GetMapping("/postWrite")
    String getPostWrite() {
        return communityService.getPostWrite();
    }

    //게시글 작성 요청
    @PostMapping("/api/post/write")
    ResponseEntity<String> writePost(@RequestBody PostDTO postDTO, Authentication auth) {
        return communityService.writePost(postDTO, auth);
    }

    //게시글 수정 페이지 GET : 본인만 가능
    @GetMapping("/postEdit/{postId}")
    String getPostEdit(@PathVariable Long postId,Model model, Authentication auth, RedirectAttributes redirectAttributes) {
        return communityService.getPostEdit(postId, model, auth, redirectAttributes);
    }

    //게시글 수정 요청
    @PostMapping("/api/post/edit")
    ResponseEntity<String> editPost(@RequestBody PostDTO postDTO, Authentication auth) {
        return communityService.editPost(postDTO, auth);
    }

    //게시글 삭제 요청
    @DeleteMapping("/api/post/delete")
    ResponseEntity<String> deletePost(Long postId, Authentication auth){
        return communityService.deletePost(postId, auth);
    }

    //댓글 작성 요청 : 댓글/답글 전부 처리
    @PostMapping("/api/post/review/add")
    ResponseEntity<String> writeReview(@RequestBody PostReviewRequestDTO postReviewRequestDTO, Authentication auth) {
        return communityService.writeReview(postReviewRequestDTO, auth);
    }

    //댓글 리스트만 GET : 게시물 상세 페이지의
    @GetMapping("/api/post/review/list/{postId}")
    String getPostDetailOnReviewList(@PathVariable Long postId, Model model, Authentication auth, RedirectAttributes redirectAttributes) {
        return communityService.getPostDetailOnReviewList(postId, model, auth, redirectAttributes);
    }

    //댓글 수정 요청
    @PatchMapping("/api/post/review/edit/{reviewId}")
    ResponseEntity<String> editReview(@PathVariable Long reviewId, String content, Authentication auth) {
        return communityService.editReview(reviewId, content, auth);
    }

    //댓글 삭제 요청
    @DeleteMapping("/api/post/review/delete/{reviewId}")
    ResponseEntity<String> deleteReview(@PathVariable Long reviewId, Authentication auth){
        return communityService.deleteReview(reviewId, auth);
    }



}
