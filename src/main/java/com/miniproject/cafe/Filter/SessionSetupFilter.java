package com.miniproject.cafe.Filter;

import com.miniproject.cafe.Mapper.*;
import com.miniproject.cafe.Service.CustomUserDetails;
import com.miniproject.cafe.VO.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class SessionSetupFilter extends OncePerRequestFilter {

    private final MemberMapper memberMapper;
    private final AdminMapper adminMapper;
    private final RewardMapper rewardMapper;
    private final CouponMapper couponMapper;
    private final OrderMapper orderMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {

            HttpSession session = request.getSession(true);
            Object principal = auth.getPrincipal();
            String loginId = auth.getName();

            // 1. 관리자 (ROLE_ADMIN)
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (isAdmin) {
                if (session.getAttribute("admin") == null) {
                    AdminVO admin = null;
                    if (principal instanceof AdminVO) {
                        admin = (AdminVO) principal;
                    } else if (principal instanceof UserDetails) {
                        admin = adminMapper.findById(((UserDetails) principal).getUsername());
                    } else {
                        admin = adminMapper.findById(loginId);
                    }

                    if (admin != null) {
                        session.setAttribute("admin", admin);
                        session.setAttribute("STORE_NAME", admin.getStoreName());
                    }
                }
            }
            // 2. 일반 회원 (ROLE_USER 등)
            else {
                if (session.getAttribute("member") == null) {
                    MemberVO member = null;

                    // Case A: 로그인 직후 (객체)
                    if (principal instanceof MemberVO) {
                        member = (MemberVO) principal;
                    }
                    // Case B: CustomUserDetails (일반적인 시큐리티 로그인)
                    else if (principal instanceof CustomUserDetails) {
                        member = ((CustomUserDetails) principal).getMemberVO();
                    }
                    // Case C: Remember-Me 복구 직후 (UserDetails or String)
                    else if (principal instanceof UserDetails) {
                        String email = ((UserDetails) principal).getUsername();
                        member = memberMapper.findByEmail(email);
                    }
                    else {
                        member = memberMapper.findByEmail(loginId);
                    }

                    // 세션 복구
                    if (member != null) {
                        session.setAttribute("member", member);
                        session.setAttribute("LOGIN_USER_ID", member.getId());
                        System.out.println("✅ [Filter] 사용자 세션 복구 완료: " + member.getEmail());
                        RewardVO rewardVO = rewardMapper.findByMemberId(member.getId());
                        List<CouponVO> coupons = couponMapper.getCouponsByUser(member.getId());
                        List<RecentOrderVO> orders = orderMapper.getRecentOrders(member.getId());
                        if(rewardVO != null) {
                            session.setAttribute("reward", rewardVO.getStamps());
                            session.setAttribute("coupon", coupons);
                            session.setAttribute("recentOrder", orders);
                        }
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}