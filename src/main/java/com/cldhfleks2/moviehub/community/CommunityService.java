package com.cldhfleks2.moviehub.community;

import com.cldhfleks2.moviehub.error.ErrorService;
import com.cldhfleks2.moviehub.member.Member;
import com.cldhfleks2.moviehub.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    @Transactional
    String getPostDetail(Long postId, Model model, Authentication auth) {
        Optional<Post> postObj = postRepository.findById(postId);
        if(!postObj.isPresent()) //게시글 존재 여부 체크
            return ErrorService.send(HttpStatus.NOT_FOUND.value(), "/postDetail/", "게시글 정보를 찾을 수 없습니다.", String.class);

        String username = auth.getName();
        Optional<Member> memberObj = memberRepository.findByUsernameAndStatus(username);
        if(!memberObj.isPresent()) //유저 정보 체크
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/postDetail/", "유저 정보를 찾을 수 없습니다.", String.class);
        Member member = memberObj.get();

        Post post = postObj.get();
        post.setView(post.getView() + 1);  //게시글 방문시 조회수 +1
        PostDTO postDTO = PostDTO.create()
                .postType(post.getPostType())
                .title(post.getTitle())
                .content(post.getContent())
                .view(post.getView())
                .member(post.getMember())
                .updateDate(post.getUpdateDate())
                .postId(postId)
                .isAuthor(member.getId() == post.getMember().getId()) //글 작성자 본인인지 bool 전달
                .build();
        postRepository.save(post); //조회수 수정

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


    //게시글 수정 페이지 GET : 본인만 가능
    String getPostEdit(Long postId, Model model, Authentication auth, RedirectAttributes redirectAttributes) {
        Optional<Post> postObj = postRepository.findById(postId);
        if(!postObj.isPresent()){ //게시글 존재 여부 체크
            ErrorService.send(HttpStatus.NOT_FOUND.value(), "/postEdit/", "게시글 정보를 찾을 수 없습니다.", String.class);
            redirectAttributes.addFlashAttribute("alertMessage", "게시글이 존재하지 않습니다.");
            return "redirect:/main";
        }
        Post post = postObj.get();
        if(post.getStatus() == 0){
            redirectAttributes.addFlashAttribute("alertMessage", "삭제된 게시물 입니다.");
            return "redirect:/main";
        }

        String username = auth.getName();
        Optional<Member> memberObj = memberRepository.findByUsernameAndStatus(username);
        if(!memberObj.isPresent()) //유저 정보 체크
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/postEdit/", "유저 정보를 찾을 수 없습니다.", String.class);

        Member member = memberObj.get();

        if(post.getMember().getId() != member.getId()){
            ErrorService.send(HttpStatus.NOT_FOUND.value(), "/postEdit/", "본인 게시물만 수정 가능합니다.", String.class);
            redirectAttributes.addFlashAttribute("alertMessage", "본인 게시물만 수정 가능합니다.");
            return "redirect:/main";
        }

        PostDTO postDTO = PostDTO.create()
                .postType(post.getPostType())
                .title(post.getTitle())
                .content(post.getContent())
                .build();

        model.addAttribute("postDTO", postDTO);

        return "community/edit";
    }


}
