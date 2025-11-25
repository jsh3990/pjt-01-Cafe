package com.miniproject.cafe.Controller;


import com.miniproject.cafe.Service.MemberService;
import com.miniproject.cafe.VO.MemberVO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    // ⭐ 이메일 중복 체크 API
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {

        boolean idDuplicate = memberService.isIdDuplicate(email);
        boolean emailDuplicate = memberService.isEmailDuplicate(email);

        if (idDuplicate || emailDuplicate) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "이미 사용 중인 이메일입니다."));
        }

        return ResponseEntity.ok(Map.of("message", "사용 가능한 이메일입니다."));
    }

    // ⭐ 회원가입 API
    @PostMapping("/signup")
    public void signup(@ModelAttribute MemberVO vo, HttpServletResponse response) throws IOException {

        if (vo.getId() == null || vo.getId().isEmpty()) {
            vo.setId(vo.getEmail());
        }

        String result = memberService.registerMember(vo);

        if ("SUCCESS".equals(result)) {
            String encodedName = URLEncoder.encode(vo.getUsername(), StandardCharsets.UTF_8);
            response.sendRedirect("/home/login?signupSuccess=true&username=" + encodedName);

        } else {
            String message = switch (result) {
                case "PASSWORD_MISMATCH" -> "비밀번호가 일치하지 않습니다.";
                case "ID_DUPLICATE", "EMAIL_DUPLICATE" -> "이미 존재하는 계정입니다.";
                default -> "회원가입 중 오류가 발생했습니다.";
            };
            String encodedMsg = URLEncoder.encode(message, StandardCharsets.UTF_8);
            response.sendRedirect("/home/login?loginError=" + encodedMsg);
        }
    }
}