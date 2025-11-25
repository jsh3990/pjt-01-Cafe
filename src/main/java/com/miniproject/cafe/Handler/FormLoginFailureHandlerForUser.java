package com.miniproject.cafe.Handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class FormLoginFailureHandlerForUser implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) {

        String errorMessage = "로그인에 실패했습니다.";

        if (exception instanceof BadCredentialsException ||
                exception instanceof UsernameNotFoundException) {
            errorMessage = "아이디 또는 비밀번호가 일치하지 않습니다.";
        }

        request.getSession().setAttribute("loginError", errorMessage);

        try {
            response.sendRedirect("/home/login");
        } catch (Exception ignored) {}
    }
}