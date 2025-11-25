package com.miniproject.cafe.Handler;

import com.miniproject.cafe.Mapper.CouponMapper;
import com.miniproject.cafe.Mapper.MemberMapper;
import com.miniproject.cafe.Mapper.OrderMapper;
import com.miniproject.cafe.Mapper.RewardMapper;
import com.miniproject.cafe.VO.CouponVO;
import com.miniproject.cafe.VO.MemberVO;
import com.miniproject.cafe.VO.RecentOrderVO;
import com.miniproject.cafe.VO.RewardVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.RememberMeServices;

import java.util.List;

@RequiredArgsConstructor
public class OAuthLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final MemberMapper memberMapper;
    private final RememberMeServices rememberMeServices;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) {

        rememberMeServices.loginSuccess(request, response, authentication);

        String email = authentication.getName();
        MemberVO member = memberMapper.findByEmail(email);

        if (member != null) {
            request.changeSessionId();
            request.getSession().setAttribute("member", member);
            request.getSession().setAttribute("LOGIN_USER_ID", member.getId());
        }

        try {
            response.sendRedirect("/home/?oauthSuccess=true&username=" +
                    java.net.URLEncoder.encode(member.getUsername(), "UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
