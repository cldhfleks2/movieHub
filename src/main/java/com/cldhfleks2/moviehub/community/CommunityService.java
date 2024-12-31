package com.cldhfleks2.moviehub.community;

import com.cldhfleks2.moviehub.error.ErrorService;
import com.cldhfleks2.moviehub.member.Member;
import com.cldhfleks2.moviehub.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommunityService {
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    //커뮤니티 페이지 GET TODO: 댓글갯수, 게시글 좋아요 갯수도 보여줘함.
    String getCommunity(Integer pageIdx, Model model, Authentication auth) {
        if(pageIdx == null) pageIdx = 1; //기본 1페이지 보여줌
        
        Page<Post> postPage = postRepository.findAllAndStatus(PageRequest.of(pageIdx-1, 10)); //status=1인 살아있는 게시글만 다 가져옴
        List<PostDTO> postDTOList = new ArrayList<>();
        for(Post post : postPage.getContent()) {
            PostDTO postDTO = PostDTO.create()
                    .postType(post.getPostType())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .view(post.getView())
                    .member(post.getMember())
                    .updateDate(post.getUpdateDate())
                    .postId(post.getId())
                    .likeCount(0L)
//                  .likeCount(0L) //TODO : 게시글 좋아요 갯수를 보내야함
                    .reviewCount(0L)
//                  .reviewCount() //TODO: 댓글 갯수를 보내줘라.
                    .build();
            postDTOList.add(postDTO);
        }

        Page<PostDTO> postDTOPage = new PageImpl<>(
                postDTOList,
                postPage.getPageable(),
                postPage.getTotalElements()
        );

        model.addAttribute("postDTOPage", postDTOPage);

        return "community/community";
    }

    //게시글 상세 페이지 GET : 방문시 view +1
    @Transactional
    String getPostDetail(Long postId, Model model, Authentication auth, RedirectAttributes redirectAttributes) {
        Optional<Post> postObj = postRepository.findById(postId);
        if(!postObj.isPresent()) //게시글 존재 여부 체크
            return ErrorService.send(HttpStatus.NOT_FOUND.value(), "/postDetail/", "게시글 정보를 찾을 수 없습니다.", String.class);
        
        String username = auth.getName();
        Optional<Member> memberObj = memberRepository.findByUsernameAndStatus(username);
        if(!memberObj.isPresent()) //유저 정보 체크
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/postDetail/", "유저 정보를 찾을 수 없습니다.", String.class);
        
        Member member = memberObj.get();
        Post post = postObj.get();
        if(post.getStatus() == 0){ //게시물이 삭제된 상태이면
            redirectAttributes.addFlashAttribute("alertMessage", "삭제된 게시물 입니다.");
            return "redirect:/community";
        }
        PostDTO postDTO = PostDTO.create()
                .postType(post.getPostType())
                .title(post.getTitle())
                .content(post.getContent())
                .view(post.getView() + 1)  //게시글 방문시 조회수 +1
                .member(post.getMember())
                .updateDate(post.getUpdateDate())
                .postId(postId)
                .isAuthor(member.getId() == post.getMember().getId()) //글 작성자 본인인지 bool 전달
                .build();
        postRepository.incrementView(postId); //view + 1 수정

        model.addAttribute("postDTO", postDTO);

        //댓글 정보도 보내야함~

        return "community/postDetail";
    }

    //게시글 작성 페이지 GET
    String getPostWrite() {
        return "community/write";
    }

    //게시글 작성 요청
    @Transactional
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
    @Transactional
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
                .postId(postId)
                .build();

        model.addAttribute("postDTO", postDTO);

        return "community/edit";
    }

    //게시글 수정 요청
    @Transactional
    ResponseEntity<String> editPost(PostDTO postDTO, Authentication auth) {
        String username = auth.getName();
        Optional<Member> memberObj = memberRepository.findByUsernameAndStatus(username);
        if(!memberObj.isPresent()) //유저 정보 체크
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/api/post/edit", "유저 정보를 찾을 수 없습니다.", ResponseEntity.class);

        Optional<Post> postObj = postRepository.findById(postDTO.getPostId());
        if(!postObj.isPresent()) //게시글 존재 여부 체크
            return ErrorService.send(HttpStatus.NOT_FOUND.value(), "/api/post/edit", "게시글 정보를 찾을 수 없습니다.", ResponseEntity.class);

        Member member = memberObj.get();
        Post post = postObj.get();

        if(member.getId() != post.getMember().getId())
            return ErrorService.send(HttpStatus.FORBIDDEN.value(), "/api/post/edit", "본인 게시물이 아니면 수정할 수 없습니다.", ResponseEntity.class);

        post.setTitle(postDTO.getTitle());
        post.setContent(postDTO.getContent());
        post.setPostType(postDTO.getPostType());
        postRepository.save(post); //게시글 수정

        return ResponseEntity.ok().build();
    }

    //게시글 삭제 요청
    @Transactional
    ResponseEntity<String> deletePost(Long postId, Authentication auth, RedirectAttributes redirectAttributes){
        String username = auth.getName();
        Optional<Member> memberObj = memberRepository.findByUsernameAndStatus(username);
        if(!memberObj.isPresent()) //유저 정보 체크
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/api/post/delete", "유저 정보를 찾을 수 없습니다.", ResponseEntity.class);

        Optional<Post> postObj = postRepository.findById(postId);
        if(!postObj.isPresent()) //게시글 존재 여부 체크
            return ErrorService.send(HttpStatus.NOT_FOUND.value(), "/api/post/delete", "게시글 정보를 찾을 수 없습니다.", ResponseEntity.class);

        Member member = memberObj.get();
        Post post = postObj.get();

        if(member.getId() != post.getMember().getId())
            return ErrorService.send(HttpStatus.FORBIDDEN.value(), "/api/post/edit", "본인 게시물이 아니면 삭제할 수 없습니다.", ResponseEntity.class);

        postRepository.delete(post); //게시물 삭제 요청

        return ResponseEntity.noContent().build();
    }

}
