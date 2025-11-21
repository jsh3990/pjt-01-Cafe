package com.miniproject.cafe.Config;

import com.miniproject.cafe.Filter.SessionSetupFilter;
import com.miniproject.cafe.Handler.FormLoginFailureHandler;
import com.miniproject.cafe.Handler.FormLoginSuccessHandler;
import com.miniproject.cafe.Handler.OAuth2FailureHandler;
import com.miniproject.cafe.Handler.OAuthLoginSuccessHandler;
import com.miniproject.cafe.Mapper.AdminMapper;
import com.miniproject.cafe.Mapper.MemberMapper;
import com.miniproject.cafe.Service.AdminUserDetailsService;
import com.miniproject.cafe.Service.CustomOAuth2UserService;
import com.miniproject.cafe.Service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationProvider;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.oidc.authentication.OidcAuthorizationCodeAuthenticationProvider;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.security.web.context.SecurityContextHolderFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomUserDetailsService customUserDetailsService;
    private final AdminUserDetailsService adminUserDetailsService;

    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final FormLoginFailureHandler formLoginFailureHandler;

    private final MemberMapper memberMapper;
    private final AdminMapper adminMapper;

    private static final String REMEMBER_ME_KEY = "secure-key";

    // ============================================================
    // 0. AuthenticationProvider 명시적 등록
    // ============================================================

    @Bean
    public AuthenticationProvider adminAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(adminUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationProvider userAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationProvider oauth2AuthenticationProvider() {
        OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient = new DefaultAuthorizationCodeTokenResponseClient();
        return new OAuth2LoginAuthenticationProvider(accessTokenResponseClient, customOAuth2UserService);
    }

    @Bean
    public AuthenticationProvider oidcAuthenticationProvider() {
        OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient = new DefaultAuthorizationCodeTokenResponseClient();
        OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService = new OidcUserService();
        return new OidcAuthorizationCodeAuthenticationProvider(accessTokenResponseClient, oidcUserService);
    }


    @Bean
    public RememberMeServices rememberMeServices() {
        TokenBasedRememberMeServices services = new TokenBasedRememberMeServices(
                REMEMBER_ME_KEY,
                customUserDetailsService
        );
        services.setAlwaysRemember(false);
        services.setTokenValiditySeconds(60 * 60 * 24 * 14);
        services.setCookieName("remember-me");
        services.setParameter("remember-me");
        return services;
    }

    @Bean
    public RememberMeServices oauthRememberMeServices() {
        TokenBasedRememberMeServices services = new TokenBasedRememberMeServices(
                REMEMBER_ME_KEY,
                customUserDetailsService
        );
        services.setAlwaysRemember(true);
        services.setTokenValiditySeconds(60 * 60 * 24 * 14);
        services.setCookieName("remember-me");
        return services;
    }

    @Bean
    public RememberMeServices adminRememberMeServices() {
        TokenBasedRememberMeServices services = new TokenBasedRememberMeServices(
                REMEMBER_ME_KEY,
                adminUserDetailsService
        );
        services.setAlwaysRemember(false);
        services.setTokenValiditySeconds(60 * 60 * 24 * 14);
        services.setCookieName("remember-me-admin");
        services.setParameter("remember-me");
        return services;
    }

    // ============================================================
    // 2. Handlers
    // ============================================================

    @Bean
    public OAuthLoginSuccessHandler oAuthLoginSuccessHandler() {
        return new OAuthLoginSuccessHandler(memberMapper, oauthRememberMeServices());
    }

    @Bean
    public FormLoginSuccessHandler formLoginSuccessHandler() {
        return new FormLoginSuccessHandler(memberMapper, rememberMeServices());
    }

    // ============================================================
    // 3. Security Filter Chains
    // ============================================================

    @Bean
    @Order(1)
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/admin/**")
                .authenticationProvider(adminAuthenticationProvider()) // 관리자용 Provider
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/login", "/admin/signup", "/admin/joinForm", "/admin/checkId", "/admin/css/**", "/admin/js/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/admin/login")
                        .loginProcessingUrl("/admin/perform_login_process")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/admin/login")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "remember-me-admin")
                )
                .rememberMe(r -> r
                        .rememberMeServices(adminRememberMeServices())
                )
                .addFilterAfter(new SessionSetupFilter(memberMapper, adminMapper), SecurityContextHolderFilter.class);

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain userFilterChain(HttpSecurity http) throws Exception {
        http
                // [핵심] 사용자용(Form) + OAuth2용 Provider들을 모두 등록해줘야 합니다.
                .authenticationProvider(userAuthenticationProvider())
                .authenticationProvider(oauth2AuthenticationProvider()) // [추가] OAuth2 Provider
                .authenticationProvider(oidcAuthenticationProvider())   // [추가] OIDC Provider (구글 등)

                .csrf(csrf -> csrf.disable())
                .headers(h -> h.frameOptions(f -> f.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/css/**", "/js/**", "/images/**", "/upload/**").permitAll()
                        .requestMatchers("/api/member/**", "/oauth2/**", "/login").permitAll()
                        .requestMatchers("/menu/**").permitAll()
                        .requestMatchers("/home/saveRegion", "/home/getRegion").permitAll()
                        .requestMatchers("/home/login").permitAll()
                        .requestMatchers("/home/**").authenticated()
                        .anyRequest().permitAll()
                )
                .formLogin(f -> f
                        .loginPage("/home/login")
                        .loginProcessingUrl("/login")
                        .successHandler(formLoginSuccessHandler())
                        .failureHandler(formLoginFailureHandler)
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/home/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuthLoginSuccessHandler())
                        .failureHandler(oAuth2FailureHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/home/login")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID", "remember-me")
                )
                .rememberMe(r -> r
                        .rememberMeServices(rememberMeServices())
                )
                .addFilterAfter(new SessionSetupFilter(memberMapper, adminMapper), SecurityContextHolderFilter.class);

        return http.build();
    }

    @Bean
    public static BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}