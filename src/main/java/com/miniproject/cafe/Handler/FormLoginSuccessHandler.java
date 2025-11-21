package com.miniproject.cafe.Handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miniproject.cafe.Mapper.MemberMapper;
import com.miniproject.cafe.VO.MemberVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.SessionFlashMapManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class FormLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final MemberMapper memberMapper;
    private final RememberMeServices rememberMeServices;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        // 1. Remember-Me 쿠키 생성
        rememberMeServices.loginSuccess(request, response, authentication);

        String email = authentication.getName();
        MemberVO member = memberMapper.findByEmail(email);

        if(member != null) {
            request.getSession().setAttribute("member", member);
        }

        response.setStatus(HttpServletResponse.SC_OK); // 200 OK
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> data = new HashMap<>();
        data.put("message", "로그인 성공");
        data.put("username", member != null ? member.getUsername() : "고객");

        // JSON 데이터를 응답 본문에 씀
        objectMapper.writeValue(response.getWriter(), data);
    }
}