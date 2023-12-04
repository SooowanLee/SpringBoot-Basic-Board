package com.study.basicboard.config;

import com.study.basicboard.config.auth.MyAccessDeniedHandler;
import com.study.basicboard.config.auth.MyAuthenticationEntryPoint;
import com.study.basicboard.config.auth.MyLoginSuccessHandler;
import com.study.basicboard.config.auth.MyLogoutSuccessHandler;
import com.study.basicboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository userRepository;

    //로그인하지 않은 유저들만 접근 가능한 URL
    private static final String[] anonymousUserUrl = {"/users/login", "/users/join"};

    //로그인한 유저들만 접근 가능한 URL
    private static final String[] authenticatedUserUrl = {"/boards/**/**/edit", "/boards/**/**/delete", "/likes/**", "/users/myPage/**", "/users/edit", "/users/delete"};

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .authorizeHttpRequests(
                        httpRequest -> httpRequest
                                .requestMatchers(anonymousUserUrl).anonymous()
                                .requestMatchers(authenticatedUserUrl).authenticated()
                                .requestMatchers(new AntPathRequestMatcher("/boards/greeting/write")).hasAnyAuthority("BRONZE", "ADMIN")
                                .requestMatchers(HttpMethod.POST, "/boards/greeting").hasAnyAuthority("BRONZE", "ADMIN")
                                .requestMatchers(new AntPathRequestMatcher("/boards/free/write")).hasAnyAuthority("SILVER", "GOLD", "ADMIN")
                                .requestMatchers(HttpMethod.POST, "/boards/free").hasAnyAuthority("SILVER", "GOLD", "ADMIN")
                                .requestMatchers(new AntPathRequestMatcher("/boards/gold/**")).hasAnyAuthority("GOLD", "ADMIN")
                                .requestMatchers(new AntPathRequestMatcher("/users/admin/**")).hasAuthority("ADMIN")
                                .requestMatchers(new AntPathRequestMatcher("/comments/**")).hasAnyAuthority("BRONZE", "SILVER", "GOLD", "ADMIN")
                                .anyRequest().permitAll())
                .exceptionHandling(
                        exceptionHandling -> exceptionHandling
                                .accessDeniedHandler(new MyAccessDeniedHandler(userRepository))
                                .authenticationEntryPoint(new MyAuthenticationEntryPoint())
                )
                .formLogin(
                        login -> login
                                .loginPage("/users/login")      //로그인 페이지
                                .usernameParameter("loginId")   //로그인에 사용될 id
                                .passwordParameter("password")  //로그인에 사용될 password
                                .failureUrl("/users/login?fail")    //로그인에 실패 시 redirect 될 URL => 실패 메세지 출력
                                .successHandler(new MyLoginSuccessHandler(userRepository))   //로그인 성공 시 실행 될 Handler
                )
                .logout(
                        logout -> logout
                                .logoutUrl("/users/logout") //로그아웃 URL
                                .invalidateHttpSession(true).deleteCookies("JSESSIONID")
                                .logoutSuccessHandler(new MyLogoutSuccessHandler())
                ).build();
    }
}
