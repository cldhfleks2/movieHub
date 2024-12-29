package com.cldhfleks2.moviehub.member;

import com.cldhfleks2.moviehub.role.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberDetailService implements UserDetailsService {
    private final MemberRepository memberRepository;
    private final RoleService roleService;

    //유저 정보를 전달해주는 서비스
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Member> member = memberRepository.findByUsernameAndStatus(username);

        if(member.isEmpty())
            throw new UsernameNotFoundException("유저를 찾을 수 없습니다.");

        var loginMember = member.get();
        List<GrantedAuthority> authorities = new ArrayList<>();

        //여기서 멤버들의 등급을 정함
        String role = roleService.getUserRole(loginMember.getUsername()).getRoleName();
        authorities.add(new SimpleGrantedAuthority(role));

        //유저 정보 반환
        MemberDetail realMember = new MemberDetail(loginMember.getUsername(), loginMember.getPassword(), authorities);
        realMember.setUsername(loginMember.getUsername());
        realMember.setNickname(loginMember.getNickname()); //닉네임 전달
        realMember.setRole(role);
        realMember.setProfileImage(loginMember.getProfileImage());  // 프로필 이미지 설정
        return realMember;
    }

}
