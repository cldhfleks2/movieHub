package com.cldhfleks2.moviehub.community;

import com.cldhfleks2.moviehub.error.ErrorService;
import com.cldhfleks2.moviehub.like.PostLike;
import com.cldhfleks2.moviehub.like.PostLikeRepository;
import com.cldhfleks2.moviehub.like.PostReviewLike;
import com.cldhfleks2.moviehub.like.PostReviewLikeRepository;
import com.cldhfleks2.moviehub.member.Member;
import com.cldhfleks2.moviehub.member.MemberRepository;
import com.cldhfleks2.moviehub.notification.Notification;
import com.cldhfleks2.moviehub.notification.NotificationRepository;
import com.cldhfleks2.moviehub.notification.NotificationTargetType;
import com.cldhfleks2.moviehub.notification.NotificationType;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunityService {
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final PostReviewRepository postReviewRepository;
    private final NotificationRepository notificationRepository;
    private final PostReviewLikeRepository postReviewLikeRepository;
    private final PostLikeRepository postLikeRepository;

    private static final int REVIEW_MAX_DEPTH = 1;

    //커뮤니티 페이지 GET TODO: 댓글갯수, 게시글 좋아요 갯수도 보여줘함.
    String getCommunity(Integer pageIdx, Model model, Authentication auth) {
        if(pageIdx == null) pageIdx = 1; //기본 1페이지 보여줌

        Page<Post> postPage = postRepository.findAllAndStatus(PageRequest.of(pageIdx-1, 10)); //status=1인 살아있는 게시글만 다 가져옴
        List<PostDTO> postDTOList = new ArrayList<>();
        for(Post post : postPage.getContent()) {
            Long postId = post.getId();
            //게시글 좋아요 갯수
            List<PostLike> postLikeList = postLikeRepository.findAllByPostIdAndStatus(postId);
            Long likeCount = (long) postLikeList.size();
            //댓글 총 갯수
            List<PostReview> postReviewList = postReviewRepository.findAllByPostIdAndStatus(postId);
            Long reviewCount = (long) postReviewList.size();
            PostDTO postDTO = PostDTO.create()
                    .postType(post.getPostType())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .view(post.getView())
                    .member(post.getMember())
                    .updateDate(post.getUpdateDate())
                    .postId(post.getId())
                    .likeCount(likeCount)
                    .reviewCount(reviewCount)
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

    //댓글 리스트에 전달할 DTO를 만드는 함수 : 재귀적으로 동작
    //현재댓글, 전체댓글로 재귀적으로 동작
    PostReviewDTO convertToPostReviewDTO(PostReview review, List<PostReview> allReviews, Long currentUserId) {
        if (review == null) return null;

        // 자식 댓글 찾기
        List<PostReviewDTO> children = allReviews.stream()
                .filter(r -> r.getParent() != null && r.getParent().getId().equals(review.getId()))
                .map(r -> convertToPostReviewDTO(r, allReviews, currentUserId))
                .collect(Collectors.toList());

        //해당 댓글의 모든 좋아요를 가져온다.
        List<PostReviewLike> allLikes = postReviewLikeRepository.findAllByReviewIdAndStatus(review.getId());
        Long likeCount = (long) allLikes.size();

        //현재 댓글을 내가 좋아요했는지 확인
        Optional<PostReviewLike> postReviewLikeObj = postReviewLikeRepository.findByReviewIdAndMemberIdAndStatus(review.getId(), currentUserId);
        Boolean isLiked;
        if(postReviewLikeObj.isPresent()) {
            isLiked = (postReviewLikeObj.get().getStatus() == 1);
        }else{
            isLiked = false;
        }

        return PostReviewDTO.create()
                .id(review.getId())
                .content(review.getContent())
                .parentId(review.getParent() != null ? review.getParent().getId() : null)
                .member(review.getMember())
                .updateDate(review.getUpdateDate())
                .likeCount(likeCount)
                .depth(review.getDepth())
                .children(children)
                .isAuthor(review.getMember().getId().equals(currentUserId))
                .isLiked(isLiked)
                .build();
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

        //댓글 뷰
        List<PostReview> allReviews = postReviewRepository.findAllByPostIdAndStatus(postId);
        // 부모 댓글만 필터링 (depth = 0)
        List<PostReviewDTO> parentReviews = allReviews.stream()
                .filter(review -> review.getParent() == null)
                .map(review -> convertToPostReviewDTO(review, allReviews, member.getId()))
                .collect(Collectors.toList());

        model.addAttribute("postReviewDTOList", parentReviews);

        //댓글 총 갯수 던져줌
        model.addAttribute("postReviewCnt", allReviews.size());


        //본인이 게시물 좋아요 눌렀는지 isLiked보내기
        Optional<PostLike> postLikeObj = postLikeRepository.findByPostIdAndMemberIdAndStatus(postId, member.getId());
        Boolean isLiked;
        if(postLikeObj.isPresent()) {
            isLiked = (postLikeObj.get().getStatus() == 1);
        }else{
            isLiked = false;
        }
        model.addAttribute("isLiked", isLiked);

        //게시글 좋아요 갯수 보내기
        List<PostLike> postLikeList = postLikeRepository.findAllByPostIdAndStatus(postId);
        int postLikeCount = postLikeList.size();
        model.addAttribute("postLikeCount", postLikeCount);
        
        
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
    ResponseEntity<String> deletePost(Long postId, Authentication auth){
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

    //댓글 작성 요청 : 댓글/답글 전부 처리
    @Transactional
    ResponseEntity<String> writeReview(PostReviewRequestDTO postReviewRequestDTO, Authentication auth) {
        String username = auth.getName();
        Optional<Member> memberObj = memberRepository.findByUsernameAndStatus(username);
        if(!memberObj.isPresent()) //유저 정보 체크
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/api/post/review/add", "유저 정보를 찾을 수 없습니다.", ResponseEntity.class);

        Long postId = postReviewRequestDTO.getPostId();
        Optional<Post> postObj = postRepository.findById(postId);
        if(!postObj.isPresent()) //게시글 존재 여부 체크
            return ErrorService.send(HttpStatus.NOT_FOUND.value(), "/api/post/review/add", "게시글 정보를 찾을 수 없습니다.", ResponseEntity.class);

        Member member = memberObj.get();
        Post post = postObj.get();
        PostReview postReview = new PostReview();
        postReview.setContent(postReviewRequestDTO.getContent());
        postReview.setPost(post);
        postReview.setMember(member);

        //답글인 경우
        if(postReviewRequestDTO.getParentId() != null){
            Optional<PostReview> parentObj = postReviewRepository.findById(postReviewRequestDTO.getParentId());
            if(!parentObj.isPresent())
                return ErrorService.send(HttpStatus.BAD_REQUEST.value(), "/api/post/review/add", "답글의 부모 댓글을 찾을 수 없습니다.", ResponseEntity.class);

            //답글이라면 부모 댓글이 있어야 depth 지정
            PostReview parent = parentObj.get();

            //부모 댓글이 위치한 게시물이 현재 댓글의 게시물과 다른 경우 : 댓글저장 안함
            if(parent.getPost().getId() != postId)
                return ErrorService.send(HttpStatus.BAD_REQUEST.value(), "/api/post/review/add", "답글의 부모 댓글이 유효하지 않습니다.", ResponseEntity.class);

            //부모 댓글의 depth 확인 : 답글이 가능한 상태인지 확인
            if(parent.getDepth() >= REVIEW_MAX_DEPTH) //최대 depth인 댓글에 답글을 달 수 없음
                return ErrorService.send(HttpStatus.BAD_REQUEST.value(), "/api/post/review/add", "답글에 답글을 달 수 없습니다.", ResponseEntity.class);

            //부모 댓글 작성 유저에게 알림 보냄
            Member parentMember = parent.getMember();
            String nickname = member.getNickname();
            String message = nickname + "님이 댓글에 답글을 달았습니다.";
            Notification notification = Notification.create()
                    .receiver(parentMember)
                    .sender(member)
                    .notificationType(NotificationType.COMMENT_ADDED)
                    .targetType(NotificationTargetType.COMMUNITY)
                    .targetId(postId)
                    .message(message) //메시지
                    .build();
            notificationRepository.save(notification); //새로운 알림 생성

            postReview.setParent(parent); //부모 댓글정보를 setter
            postReview.setDepth(parent.getDepth() + 1); //깊이 + 1
        }

        postReviewRepository.save(postReview); //댓글 작성
        
        return ResponseEntity.created(null).build();
    }

    //댓글 리스트만 GET : 게시물 상세 페이지의
    String getPostDetailOnReviewList(Long postId, Model model, Authentication auth, RedirectAttributes redirectAttributes) {
        Optional<Post> postObj = postRepository.findById(postId);
        if(!postObj.isPresent()) //게시글 존재 여부 체크
            return ErrorService.send(HttpStatus.NOT_FOUND.value(), "/postDetail/", "게시글 정보를 찾을 수 없습니다.", String.class);

        String username = auth.getName();
        Optional<Member> memberObj = memberRepository.findByUsernameAndStatus(username);
        if(!memberObj.isPresent()) //유저 정보 체크
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/postDetail/", "유저 정보를 찾을 수 없습니다.", String.class);

        Post post = postObj.get();
        if(post.getStatus() == 0){ //게시물이 삭제된 상태이면
            redirectAttributes.addFlashAttribute("alertMessage", "삭제된 게시물 입니다.");
            return "redirect:/community";
        }

        // 댓글 처리 로직 수정
        List<PostReview> allReviews = postReviewRepository.findAllByPostIdAndStatus(postId);
        Member member = memberObj.get();
        // 부모 댓글만 필터링 (parent가 null인 경우)
        List<PostReviewDTO> parentReviews = allReviews.stream()
                .filter(review -> review.getParent() == null)
                .map(review -> convertToPostReviewDTO(review, allReviews, member.getId()))
                .collect(Collectors.toList());

        model.addAttribute("postReviewDTOList", parentReviews);

        //댓글 총 갯수 던져줌
        model.addAttribute("postReviewCnt", allReviews.size());

        //댓글리스갱신을위해 DTO로 postId를 전달
        PostDTO postDTO = PostDTO.create().postId(postId).build();
        model.addAttribute("postDTO", postDTO);

        return "community/postDetail :: #reviewsSection"; //댓글 리스트만
    }

    //댓글 수정 요청
    @Transactional
    ResponseEntity<String> editReview(Long reviewId, String content, Authentication auth) {
        String username = auth.getName();
        Optional<Member> memberObj = memberRepository.findByUsernameAndStatus(username);
        if(!memberObj.isPresent()) //유저 정보 체크
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/api/post/review/edit/", "유저 정보를 찾을 수 없습니다.", ResponseEntity.class);

        Optional<PostReview> postReviewObj = postReviewRepository.findById(reviewId);
        if(!postReviewObj.isPresent())
            return ErrorService.send(HttpStatus.NOT_FOUND.value(), "/api/post/review/edit/", "댓글을 찾을 수 없습니다.", ResponseEntity.class);

        PostReview postReview = postReviewObj.get();
        postReview.setContent(content); //댓글 내용만 수정
        postReviewRepository.save(postReview); //댓글 수정

        return ResponseEntity.ok().build();
    }

    //재귀적으로 댓글과 그에 달린 답글들을 함께 삭제하는 메소드
    @Transactional
    void deleteReviewByRecur(PostReview parent) {
        //답글을 찾음
        List<PostReview> children = postReviewRepository.findAllByParent(parent);
        for(PostReview child : children){ //자식을 탐험하면서 함께 삭제
            deleteReviewByRecur(child);
        }
        postReviewRepository.delete(parent); //부모 삭제
    }

    //댓글 삭제 요청
    ResponseEntity<String> deleteReview(Long reviewId, Authentication auth){
        String username = auth.getName();
        Optional<Member> memberObj = memberRepository.findByUsernameAndStatus(username);
        if(!memberObj.isPresent()) //유저 정보 체크
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/api/post/review/edit/", "유저 정보를 찾을 수 없습니다.", ResponseEntity.class);

        Optional<PostReview> postReviewObj = postReviewRepository.findById(reviewId);
        if(!postReviewObj.isPresent())
            return ErrorService.send(HttpStatus.NOT_FOUND.value(), "/api/post/review/edit/", "댓글을 찾을 수 없습니다.", ResponseEntity.class);

        PostReview postReview = postReviewObj.get();
        deleteReviewByRecur(postReview); //재귀적으로 댓글과 그에 달린 답글도 함께 삭제

        return ResponseEntity.noContent().build();
    }




}
