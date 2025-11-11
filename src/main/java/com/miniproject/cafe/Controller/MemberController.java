package com.miniproject.cafe.Controller;

import com.miniproject.cafe.Service.MemberService;
import com.miniproject.cafe.VO.MemberVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/home")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("memberVO", new MemberVO());
        return "register";
    }

    @GetMapping("/login")
    public String login(){
        return "/login";
    }

    @PostMapping("/register")
    public String registerMember(
            @ModelAttribute MemberVO memberVO,
            @RequestParam("pw-confirm") String pwConfirm,
            RedirectAttributes ra) {

        if (!memberVO.getPassword().equals(pwConfirm)) {
            ra.addFlashAttribute("passwordError", "비밀번호가 서로 일치하지 않습니다.");
            ra.addFlashAttribute("memberVO", memberVO);
            return "redirect:/home/register";
        }

        if (memberService.isEmailDuplicate(memberVO.getEmail())) {
            ra.addFlashAttribute("emailError", "이미 존재하는 이메일입니다.");
            ra.addFlashAttribute("memberVO", memberVO);
            return "redirect:/home/register";
        }

        if (memberService.isIdDuplicate(memberVO.getId())) {
            ra.addFlashAttribute("idError", "이미 존재하는 아이디입니다.");
            ra.addFlashAttribute("memberVO", memberVO);
            return "redirect:/home/register";
        }

        memberService.MemberRegister(memberVO);

        ra.addFlashAttribute("msg", "회원가입이 완료되었습니다.");
        return "redirect:/home/login";
    }

    @PostMapping("/login")
    public String loginMember(@ModelAttribute MemberVO memberVO, RedirectAttributes ra) {
        MemberVO loginResultVO = memberService.MemberLogin(memberVO);

        if(loginResultVO != null && loginResultVO.getPassword().equals(memberVO.getPassword())) {
            ra.addFlashAttribute("msg", loginResultVO.getUsername() + "님!\n환영합니다.");
            return "redirect:/home/"; // 성공 시 메인으로
        } else {
            ra.addFlashAttribute("error", "아이디 또는 비밀번호가 일치하지 않습니다.");
            return "redirect:/home/login"; // 실패 시 다시 로그인 폼으로
        }
    }
}
