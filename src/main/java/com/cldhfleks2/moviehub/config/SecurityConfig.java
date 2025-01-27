package com.cldhfleks2.moviehub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        //csrf 공격 방어기능 OFF
        http.csrf((csrf) -> csrf.disable());

        //클라이언트 요청에 대해 권한 부여
        http.authorizeHttpRequests((authorize) ->
                //자원에 대한 요구도 처리 가능
                authorize.requestMatchers("/css/**", "/js/**", "/image/**", "/static/**").permitAll()
                        .requestMatchers("/", "/main").permitAll()
                        .requestMatchers("/register", "/login").permitAll()
                        .requestMatchers("/api/checkUsername").permitAll() //회원가입시 아이디 중복확인 요청 API
                        .requestMatchers("/api/manager/**").hasRole("ADMIN") //관리자 기능
                        .requestMatchers("/manager/**").hasRole("ADMIN") //관리자 기능
//                        .requestMatchers("/detail/**").permitAll()
                        .anyRequest().authenticated() //그 외 모든건 로그인을 필요로함
        );

        //로그인 설정
        http.formLogin((formLogin)  ->
                formLogin.loginPage("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/main")// 로그인 성공시 갈 url
        );

        // 로그아웃은
        http.logout(logout -> logout.logoutUrl("/logout"));

        return http.build();
    }
}
