package com.cldhfleks2.moviehub.community;

import com.cldhfleks2.moviehub.error.ErrorService;
import com.cldhfleks2.moviehub.member.Member;
import com.cldhfleks2.moviehub.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommunityService {
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    //커뮤니티 페이지 GET
    String getCommunity(Model model, Authentication auth) {


        return "community/community";
    }

    //게시글 상세 페이지 GET : 방문시 view +1
    String getPostDetail(Long postId, Model model, Authentication auth) {
        Optional<Post> postObj = postRepository.findById(postId);
        if(!postObj.isPresent()) //게시글 존재 여부 체크
            return ErrorService.send(HttpStatus.NOT_FOUND.value(), "", "게시글 정보를 찾을 수 없습니다.", String.class);

        Post post = postObj.get();
        PostDTO postDTO = PostDTO.create()
                .postType(post.getPostType())
                .title(post.getTitle())
                .content(post.getContent())
                .view(post.getView() + 1) //게시글 방문시 조회수 +1
                .member(post.getMember())
                .updateDate(post.getUpdateDate())
                .build();

        model.addAttribute("postDTO", postDTO);

        //댓글 정보도 보냄

        return "community/postDetail";
    }

    //게시글 작성 페이지 GET
    String getPostWrite() {
        return "community/write";
    }

    //게시글 작성 요청
    ResponseEntity<String> writePost(PostDTO postDTO, Authentication auth) {
        String username = auth.getName();
        Optional<Member> memberObj = memberRepository.findByUsernameAndStatus(username);
        if(!memberObj.isPresent()) //유저 정보 체크
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/api/post/write", "유저 정보를 찾을 수 없습니다.", ResponseEntity.class);
        Member member = memberObj.get();

        Post post = new Post();
        post.setTitle(postDTO.getTitle());
        post.setContent(postDTO.getContent());
        post.setPostType(postDTO.getPostType());
        post.setMember(member);
        postRepository.save(post); //DB 저장

        return ResponseEntity.ok().build();
    }



}
