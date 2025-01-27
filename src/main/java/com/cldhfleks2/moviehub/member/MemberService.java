package com.cldhfleks2.moviehub.member;

import com.cldhfleks2.moviehub.community.*;
import com.cldhfleks2.moviehub.error.ErrorService;
import com.cldhfleks2.moviehub.like.post.PostLike;
import com.cldhfleks2.moviehub.like.post.PostLikeRepository;
import com.cldhfleks2.moviehub.like.postreview.PostReviewLike;
import com.cldhfleks2.moviehub.like.postreview.PostReviewLikeRepository;
import com.cldhfleks2.moviehub.moviereview.MovieReview;
import com.cldhfleks2.moviehub.moviereview.MovieReviewDTO;
import com.cldhfleks2.moviehub.moviereview.MovieReviewRepository;
import com.cldhfleks2.moviehub.postreview.PostReview;
import com.cldhfleks2.moviehub.postreview.PostReviewDTO;
import com.cldhfleks2.moviehub.postreview.PostReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostReviewRepository postReviewRepository;
    private final PostReviewLikeRepository postReviewLikeRepository;
    private final MovieReviewRepository movieReviewRepository;

    @Value("${file.dir}")
    private String fileDir;

    @Value(("${file.dir.on.db}"))
    private String fileDirOnDb;

    //회원가입시 아이디 중복검사
    ResponseEntity<String> checkUsername(String username) {
        //회원인것들에서만 username 중복 검사
        //오류를 막기위해 회원탈퇴한 유저의 아이디도 사용 불가능
        Optional<Member> memberObj = memberRepository.findByUsername(username);

        if(memberObj.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); //중복됨을 전달
        }else{
            return ResponseEntity.status(HttpStatus.OK).build(); //사용가능을 전달
        }
    }

    //로그인 페이지 GET
    String getLogin(Authentication auth){
        //인증정보가 있거나 로그인된 상태이면 메인 화면으로 이동
        if(auth != null && auth.isAuthenticated()) return "redirect:/main";
        return "member/login";
    }

    //회원가입 페이지 GET
    String getRegister (Authentication auth) {
        //인증정보가 있거나 로그인된 상태이면 메인 화면으로 이동
        if(auth != null && auth.isAuthenticated()) return "redirect:/main";
        return "member/register";
    }

    //회원가입 : DB에 저장
    @Transactional
    ResponseEntity<String> register (Member member) {
        //아이디 중복확인은 이미 거친 상태

        //비밀번호 암호화
        String passwordEncoded = passwordEncoder.encode(member.getPassword());
        member.setPassword(passwordEncoded);
        member.setProfileImage("/image/blank.png"); //기본적 프로필 지정

        //DB저장
        memberRepository.save(member);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    //마이페이지 내 게시글 관리 뷰 GET :
    String getMyPage(Model model, Authentication auth, String keyword, Integer pageIdx, String sort, String category) {
        if(keyword == null) keyword = "";
        if(pageIdx == null) pageIdx = 1;
        if(sort == null) sort = "latest";
        if(category == null) category = "ALL";

        String username = auth.getName();
        Optional<Member> memberObj = memberRepository.findByUsernameAndStatus(username);
        if(!memberObj.isPresent()) //유저 정보 체크
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/mypage", "유저 정보를 찾을 수 없습니다.", String.class);

        //개인정보 수정 란
        Member member = memberObj.get();
        Long memberId = member.getId();
        MemberDTO memberDTO = MemberDTO.create()
                .username(member.getUsername())
                .nickname(member.getNickname())
                .profileImage(member.getProfileImage()).build();
        model.addAttribute("member", memberDTO);

        //내 게시글 관리 : 5개씩 보여줌
        int pageSize = 5;
        Page<Post> postPage;
        if(category.equals("ALL"))
            postPage = postRepository.findAllByKeywordAndMemberIdAndStatus(keyword, memberId, PageRequest.of(pageIdx - 1, pageSize));
        else{
            PostType postType = PostType.valueOf(category.toUpperCase());
            postPage = postRepository.findAllByKeywordAndMemberIdAndCategoryAndStatus(keyword, memberId, postType, PageRequest.of(pageIdx - 1, pageSize));
        }

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

        //정렬
        postDTOList = CommunityService.sortPostDTOList(postDTOList, sort);

        //페이지로 전달
        Page<PostDTO> postDTOPage = new PageImpl<>(
                postDTOList,
                postPage.getPageable(),
                postPage.getTotalElements() == 0 ? 1 : postPage.getTotalElements() //totalElements가 0이면 totalPages를 최소 1로 설정
        );

        model.addAttribute("postDTOPage", postDTOPage);

        //에러방지용
        model.addAttribute("postReviewDTOPage", null);

        return "member/mypage";
    }

    //마이페이지 내 댓글 관리뷰 GET
    String getMyPageReview(Model model, Authentication auth, String keyword, Integer pageIdx, String sort){
        if(keyword == null) keyword = "";
        if(pageIdx == null) pageIdx = 1;
        if(sort == null) sort = "latest";

        String username = auth.getName();
        Optional<Member> memberObj = memberRepository.findByUsernameAndStatus(username);
        if(!memberObj.isPresent()) //유저 정보 체크
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/mypage/review", "유저 정보를 찾을 수 없습니다.", String.class);

        Member member = memberObj.get();
        Long memberId = member.getId();
        int pageSize = 5; //5개씩 보여줄거임
        Page<PostReview> postReviewPage = postReviewRepository.findAllByKeywordAndStatus(keyword, memberId, PageRequest.of(pageIdx - 1, pageSize));
        List<PostReviewDTO> postReviewDTOList = new ArrayList<>();
        for(PostReview postReview : postReviewPage.getContent()) {
            Long postReviewId = postReview.getId();
            List<PostReviewLike> postReviewLikeList = postReviewLikeRepository.findAllByReviewIdAndStatus(postReviewId);
            Long likeCount = (long) postReviewLikeList.size();

            PostReviewDTO postReviewDTO = PostReviewDTO.create()
                    .id(postReviewId)
                    .content(postReview.getContent())
                    .parent(postReview.getParent()) //부모 댓글
                    .parentId(postReview.getParent() != null ? postReview.getParent().getId() : null)
                    .post(postReview.getPost())
                    .member(postReview.getMember())
                    .updateDate(postReview.getUpdateDate())
                    .likeCount(likeCount)
                    .depth(postReview.getDepth()) //isAuthor 제외 isLiked 제외 children 제외함
                    .build();
            postReviewDTOList.add(postReviewDTO);
        }

        //정렬 코드 추가해라

        //페이지로 전달
        Page<PostReviewDTO> postReviewDTOPage = new PageImpl<>(
                postReviewDTOList,
                postReviewPage.getPageable(),
                postReviewPage.getTotalElements() == 0 ? 1 : postReviewPage.getTotalElements()
        );

        model.addAttribute("postReviewDTOPage", postReviewDTOPage);


        return "member/mypage :: #reviewSection";
    }

    //유저 프로필 GET
    String getUserprofile(Long memberId, Integer pageIdx, Model model) {
        if(pageIdx == null) pageIdx = 1;
        Optional<Member> memberObj = memberRepository.findById(memberId);
        if(!memberObj.isPresent())
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/userprofile/", "유저 정보를 찾을 수 없습니다.", String.class);

        Member member = memberObj.get();
        MemberDTO memberDTO = MemberDTO.create()
                .id(memberId)
                .username(member.getUsername())
                .nickname(member.getNickname())
                .profileImage(member.getProfileImage())
                .build();

        model.addAttribute("memberDTO", memberDTO);
        
        //게시글 목록
        int postPageSize = 10;
        Page<Post> postPage = postRepository.findAllByMemberIdAndStatus(memberId, PageRequest.of(pageIdx-1, postPageSize));
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
                    .postId(postId)
                    .postType(post.getPostType())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .likeCount(likeCount)
                    .reviewCount(reviewCount)
                    .view(post.getView())
                    .updateDate(post.getUpdateDate())
                    .build();
            postDTOList.add(postDTO);
        }
        //페이지로 전달
        Page<PostDTO> postDTOPage = new PageImpl<>(
                postDTOList,
                postPage.getPageable(),
                postPage.getTotalElements()
        );
        model.addAttribute("postDTOPage", postDTOPage);

        //전체 게시글 갯수
        List<Post> postList = postRepository.findAllByMemberIdAndStatus(memberId, Pageable.unpaged()).getContent();
        Long postCount = (long) postList.size();
        model.addAttribute("postCount", postCount);


        //영화 리뷰 목록 : 10개씩
        int reviewPageSize = 10;
        Page<MovieReview> movieReviewPage = movieReviewRepository.findAllByMemberIdAndStatus(memberId, PageRequest.of(pageIdx-1, reviewPageSize));
        List<MovieReviewDTO> movieReviewDTOList = new ArrayList<>();
        for(MovieReview movieReview : movieReviewPage.getContent()) {
            MovieReviewDTO movieReviewDTO = MovieReviewDTO.builder()
                    .moviePosterURL(movieReview.getMovie().getPosterURL())
                    .movieNm(movieReview.getMovie().getMovieNm())
                    .content(movieReview.getContent())
                    .ratingValue(movieReview.getRatingValue())
                    .reviewUpdateDate(movieReview.getUpdateDate())
                    .build();
            movieReviewDTOList.add(movieReviewDTO);
        }
        Page<MovieReviewDTO> movieReviewDTOPage = new PageImpl<>(
                movieReviewDTOList,
                movieReviewPage.getPageable(),
                movieReviewPage.getTotalElements()
        );
        model.addAttribute("movieReviewDTOPage", movieReviewDTOPage);

        //전체 영화리뷰 갯수
        List<MovieReview> movieReviewList = movieReviewRepository.findAllByMemberIdAndStatus(memberId, Pageable.unpaged()).getContent();
        Long movieReviewCount = (long) movieReviewList.size();
        model.addAttribute("movieReviewCount", movieReviewCount);

        return "member/userprofile";
    }

    //유저 프로필 수정 요청
    @Transactional
    ResponseEntity<String> editUserprofile(String nickname, String password, MultipartFile profileImage, Authentication auth) throws Exception{
        String username = auth.getName();
        Optional<Member> memberObj = memberRepository.findByUsernameAndStatus(username);
        if(!memberObj.isPresent()) //유저 정보 체크
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/api/user/profile", "유저 정보를 찾을 수 없습니다.", ResponseEntity.class);

        String uploadDir = System.getProperty("user.dir") + fileDir;

        //저장할 파일이름과 확장명을 구함
        String uuid = UUID.randomUUID().toString();
        String ext = profileImage.getOriginalFilename().substring(profileImage.getOriginalFilename().lastIndexOf("."));
        //실제 저장할 위치에 폴더가 없으면 생성
        File dir = new File(uploadDir);
        if(!dir.exists()){
            dir.mkdir();
        }

        //프로젝트 로컬에 실제 파일 저장
        String fileName = uuid + ext; //새로 생성된 파일이름
        String filePath = uploadDir + fileName; //실제 파일이 저장될 경로
        profileImage.transferTo(new File(filePath));

        //DB에 저장될 파일 경로
        String filePathOnDB = fileDirOnDb + fileName; // /image/ + uuid + ext

        //비밀번호 암호화
        String passwordEncoded = passwordEncoder.encode(password);
        //DB member 수정
        Member member = memberObj.get();
        member.setNickname(nickname);
        member.setPassword(passwordEncoded);
        member.setProfileImage(filePathOnDB);
        memberRepository.save(member); //수정

        //다른 페이지에서 바로 사용가능하게 auth 업데이트 진행
        MemberDetail ordinaryMemberDetail = (MemberDetail) auth.getPrincipal();
        String role = ordinaryMemberDetail.getRole();
        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        MemberDetail memberDetail = new MemberDetail(username, passwordEncoded, authorities);
        memberDetail.setUsername(username);
        memberDetail.setNickname(nickname);
        memberDetail.setRole(role);
        memberDetail.setProfileImage(filePathOnDB); //프로필 이미지 변경

        //새로운 auth 생성
        Authentication newAuth = new UsernamePasswordAuthenticationToken(memberDetail, null, authorities);

        //auth 업데이트
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
