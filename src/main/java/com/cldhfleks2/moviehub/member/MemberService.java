package com.cldhfleks2.moviehub.member;

import com.cldhfleks2.moviehub.error.ErrorService;
import com.cldhfleks2.moviehub.like.MovieLike;
import com.cldhfleks2.moviehub.like.MovieLikeRepository;
import com.cldhfleks2.moviehub.movie.Movie;
import com.cldhfleks2.moviehub.movie.MovieDTO;
import com.cldhfleks2.moviehub.movie.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
    private final MovieLikeRepository movieLikeRepository;
    private final MovieService movieService;

    @Value("${file.dir}")
    private String fileDir;

    @Value(("${file.dir.on.db}"))
    private String fileDirOnDb;

    //회원가입시 아이디 중복검사
    ResponseEntity<String> checkUsername(String username) {
        //회원인것들에서만 중복 검사
        //오류를 막기위해 회원탈퇴한 유저의 아이디를 사용 불가능
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

    //마이페이지 GET
    String getMyPage(Model model, Authentication auth) {
        String username = auth.getName();
        Optional<Member> memberObj = memberRepository.findByUsernameAndStatus(username);
        if(!memberObj.isPresent()) //유저 정보 체크
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "", "유저 정보를 찾을 수 없습니다.", String.class);

        Member member = memberObj.get();
        MemberDTO memberDTO = MemberDTO.create()
                .username(member.getUsername())
                .nickname(member.getNickname())
                .profileImage(member.getProfileImage()).build();
        model.addAttribute("member", memberDTO);

        return "member/mypage";
    }

    //유저 프로필 GET
    String getUserprofile(Long memberId, Model model) {
        Optional<Member> memberObj = memberRepository.findById(memberId);
        if(!memberObj.isPresent())
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/userprofile/", "유저 정보를 찾을 수 없습니다.", String.class);

        Member member = memberObj.get();
        MemberDTO memberDTO = MemberDTO.create()
                .username(member.getUsername())
                .nickname(member.getNickname())
                .profileImage(member.getProfileImage())
                .build();

        model.addAttribute("memberDTO", memberDTO);

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

    //내가 찜한 리스트 GET
    public String getMyWish(Model model, Authentication auth, Integer pageIdx) throws Exception{
        if(pageIdx == null) pageIdx = 1;

        String username = auth.getName();
        Optional<Member> memberObj = memberRepository.findByUsernameAndStatus(username);
        if(!memberObj.isPresent()) //유저 정보 체크
            return ErrorService.send(HttpStatus.UNAUTHORIZED.value(), "/mywish", "유저 정보를 찾을 수 없습니다.", String.class);

        //좋아요한 전체 영화 객체를 가져오기
        Page<MovieLike> movieLikePage = movieLikeRepository.findAllByUsernameAndStatus(username, PageRequest.of(pageIdx - 1, 8));
        List<Movie> movieList = new ArrayList<>();
        for (MovieLike movieLike : movieLikePage)  movieList.add(movieLike.getMovie());

        //movieDTO만들기
        List<MovieDTO> movieDTOList = new ArrayList<>();
        for(Movie movie : movieList) {
            MovieDTO movieDTO = movieService.getMovieDTO(movie);
            movieDTOList.add(movieDTO);
        }

        //페이지로 전달
        Page<MovieDTO> movieDTOPage = new PageImpl<>(
                movieDTOList,
                movieLikePage.getPageable(),
                movieLikePage.getTotalElements()
        );

        model.addAttribute("movieDTOPage", movieDTOPage);

        return "member/mywish";
    }

}
