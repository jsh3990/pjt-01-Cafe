package com.miniproject.cafe.Filter;

import com.miniproject.cafe.Mapper.AdminMapper;
import com.miniproject.cafe.VO.AdminVO;
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

@RequiredArgsConstructor
public class AdminSessionSetupFilter extends OncePerRequestFilter {

    private final AdminMapper adminMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {

            HttpSession session = request.getSession(true);
            Object principal = auth.getPrincipal();
            String loginId = auth.getName();

            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (isAdmin && session.getAttribute("admin") == null) {

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
                    System.out.println("✅ [AdminFilter] 관리자 세션 복구 완료: " + admin.getId());
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}