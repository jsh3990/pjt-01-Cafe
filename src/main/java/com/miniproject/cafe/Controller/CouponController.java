package com.miniproject.cafe.Controller;

import com.miniproject.cafe.Service.CouponService;
import com.miniproject.cafe.Service.RewardService;
import com.miniproject.cafe.VO.CouponVO;
import com.miniproject.cafe.VO.MemberVO;
import com.miniproject.cafe.VO.RewardVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
public class CouponController {

    @Autowired
    private CouponService couponService;

    @Autowired
    private RewardService rewardService;

    @GetMapping("/home/coupon")
    public String couponPage(Model model, HttpSession session) {

        MemberVO member = (MemberVO) session.getAttribute("member");
        if (member == null) {
            return "redirect:/home/";
        }

        String memberId = member.getId();

        List<CouponVO> coupons = couponService.getCouponsByUser(memberId);
        RewardVO reward = rewardService.getReward(memberId);

        model.addAttribute("coupons", coupons);
        model.addAttribute("reward", reward);
        model.addAttribute("IS_LOGGED_IN", true);

        if (coupons != null && !coupons.isEmpty()) {
            LocalDate earliest = coupons.stream()
                    .map(CouponVO::getExpireDate)
                    .min(LocalDate::compareTo)
                    .orElse(null);

            model.addAttribute("earliestExpireDate", earliest);
        }

        return "coupon";
    }
}
