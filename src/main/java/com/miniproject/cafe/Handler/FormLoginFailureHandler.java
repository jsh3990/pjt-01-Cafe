package com.miniproject.cafe.Handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.SessionFlashMapManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class FormLoginFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException {

        // 1. 상태 코드 401 (Unauthorized) 설정
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // 2. 에러 메시지 판별
        String errorMessage = "로그인에 실패했습니다.";
        if (exception instanceof BadCredentialsException || exception instanceof UsernameNotFoundException) {
            errorMessage = "아이디 또는 비밀번호가 일치하지 않습니다.";
        } else {
            errorMessage = exception.getMessage();
        }

        // 3. JSON 응답 생성
        Map<String, Object> data = new HashMap<>();
        data.put("message", errorMessage);
        data.put("error", true);

        objectMapper.writeValue(response.getWriter(), data);
    }
}