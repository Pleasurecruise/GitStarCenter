package cn.yiming1234.gitstarcenter.config;

import cn.yiming1234.gitstarcenter.secruity.CustomAuthenticationEntryPoint;
import cn.yiming1234.gitstarcenter.secruity.TokenAuthenticationFilter;
import cn.yiming1234.gitstarcenter.secruity.oauth2.CustomOAuth2UserService;
import cn.yiming1234.gitstarcenter.secruity.oauth2.OAuth2LoginSuccessHandler;
import cn.yiming1234.gitstarcenter.secruity.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import cn.yiming1234.gitstarcenter.secruity.oauth2.OAuth2LoginFailureHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
public class SecurityConfig {
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oauth2LoginFailureHandler;

    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService,
                      CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
                      OAuth2LoginSuccessHandler oauth2LoginSuccessHandler,
                      OAuth2LoginFailureHandler oauth2LoginFailureHandler) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
        this.oauth2LoginSuccessHandler = oauth2LoginSuccessHandler;
        this.oauth2LoginFailureHandler = oauth2LoginFailureHandler;
    }

    /**
     * 使用cookie存储OAuth2授权请求
     */
    @Bean
    public HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository() {
        return new HttpCookieOAuth2AuthorizationRequestRepository();
    }

    /**
     * 注入TokenAuthenticationFilter
     */
    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter();
    }

    /**
     * 密码加密
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 注入AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 配置安全过滤器链
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/", "/login", "/error", "/favicon.ico", "/oauth2/authorize", "/oauth2/callback/*").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(authorization -> authorization
                    .baseUri("/oauth2/authorize")
                    .authorizationRequestRepository(cookieAuthorizationRequestRepository())
                )
                .redirectionEndpoint(redirection -> redirection
                    .baseUri("/oauth2/callback/*")
                )
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
                .successHandler(oauth2LoginSuccessHandler)
                .failureHandler(oauth2LoginFailureHandler)
            )
            .exceptionHandling((exceptions) -> exceptions
                     .authenticationEntryPoint(customAuthenticationEntryPoint)
            );
        http.addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
