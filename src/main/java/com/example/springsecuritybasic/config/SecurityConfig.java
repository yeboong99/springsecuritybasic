package com.example.springsecuritybasic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // 계층 권한 설정을 위해 잠시 주석처리.
//        http
//                .authorizeHttpRequests((auth) -> auth.requestMatchers("/", "/login", "/join", "/joinProc", "/main").permitAll()
//                        .requestMatchers("/admin").hasRole("ADMIN")
//                        .requestMatchers("/my/**").hasAnyRole("ADMIN", "USER")
//                        .anyRequest().authenticated()
//                );

        // 계층 권한 설정. 하단 Hierarchy 설정을 통해 A < B < C의 계층을 가진 권한설정을 해두었다.
        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/").hasAnyRole("A")
                        .requestMatchers("/manager").hasAnyRole("B")
                        .requestMatchers("/admin").hasAnyRole("C")
                        .anyRequest().authenticated()
                );

//        http
//                .formLogin((auth) -> auth.loginPage("/login")
//                        .loginProcessingUrl("/loginProc")
//                        .permitAll()
//                );

        // csrf.disable()을 주석처리해 csrf Enable됨. csrf는 토큰검증을 하기때문에 필요한 시스템을 추가해줘야함.
        // 또한 csrf설정이 Enable되었을 때부터는 로그아웃을 반드시 POST 메서드로 진행해야 하는데,
        // http
        //            .logout((auth) -> auth.logoutUrl("/logout")
        //                    .logoutSuccessUrl("/"));
        // 이런 설정을 해두고 컨트롤러에서 GET 매핑으로 아래와 같이 로그아웃을 만들어 해당 GET메서드를 가로채는 방식으로 구성하면 GET메서드로 로그아웃이 가능함.
        // @Controller
        // public class logoutController {
        //
        //    @GetMapping("/logout")
        //    public String logout(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //
        //        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //        if(authentication != null) {
        //            new SecurityContextLogoutHandler().logout(request, response, authentication);
        //        }
        //
        //        return "redirect:/";
        //    }
        // }

//        http
//                .csrf((auth) -> auth.disable());



        http
                .sessionManagement((auth) -> auth
                        .maximumSessions(1) // 하나의 아이디 당 최대 세션 수
                        .maxSessionsPreventsLogin(true));   // 다중 로그인 최대 갯수를 넘으면 어떻게 처리할 지 정함. true -> 새로운 세션 차단, false -> 기존 세션 1개 삭제

        //세션 고정 보호 - 세션 고정 공격(문서 참고)으로부터 보호할 수 있는 옵션.
        http
                .sessionManagement((auth) -> auth
                        .sessionFixation().none()); // none() : 로그인 시 세션 정보 변경 안함 (단, 세션 고정 공격에 노출됨)
                                                    // newSession() : 로그인 시 새로운 세션 생성
                                                    // changeSessionId() : 로그인 시 동일한 세션에 대한 id만 변경

        // http basic 인증 방식을 위해 상단 formLogin 비활성화. formLogin과는 다른 인증방식.
        // http basic 인증 방식 : 바디가 아닌 헤더에 아이디, 비밀번호를 담아 검사하는 방식.
        http
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    // 계층 권한 설정하기.
    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();

        // 여러 계층 간 관계를 표현할 때 개행(\n) 후 이어서 문자열 형태로 적어주면 된다. Spring Security 이므로 앞에 ROLE_ 붙여야함.
        hierarchy.setHierarchy("ROLE_C > ROLE_B\n" + "ROLE_B > ROLE_A");
        return hierarchy;
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

}