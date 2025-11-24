package com.miniproject.cafe.Controller;

import com.miniproject.cafe.Mapper.AdminMapper;
import com.miniproject.cafe.Service.AdminRevenueService;
import com.miniproject.cafe.VO.AdminRevenueVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminRevenueController {

    @Autowired
    private AdminRevenueService adminRevenueService;

    @Autowired
    private AdminMapper adminMapper;

    @GetMapping("/revenue")
    public String adminRevenue(HttpSession session, Model model,
                               @RequestParam(required=false) String date,
                               @RequestParam(required=false) String store) {

        boolean isLoggedIn = session.getAttribute("admin") != null;
        model.addAttribute("isLoggedIn", isLoggedIn);
        model.addAttribute("activePage", "revenue");

        if (!isLoggedIn) {
            return "redirect:/admin/login";
        }

        if(date == null || date.isEmpty()) {
            java.time.LocalDate today = java.time.LocalDate.now();
            date = today.toString();
        }

        if(store == null) store = "";

        List<AdminRevenueVO> orderDetailVO =
                adminRevenueService.getOrdersByDate(date, store);

        model.addAttribute("orderDetailVO", orderDetailVO);
        model.addAttribute("selectedDate", date);
        model.addAttribute("selectedStore", store);

        List<String> storeList = adminMapper.getStoreList();
        model.addAttribute("storeList", storeList);

        return "admin_revenue";
    }

    @GetMapping("/revenue/orders")
    @ResponseBody
    public List<AdminRevenueVO> getRevenueOrders(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String store) {

        if(startDate != null && !startDate.isEmpty() &&
                endDate != null && !endDate.isEmpty()) {

            return adminRevenueService.getOrdersByRange(startDate, endDate, store);
        }

        else if(date != null && !date.isEmpty()) {
            return adminRevenueService.getOrdersByDate(date, store);
        }

        else {
            return adminRevenueService.getAllOrders(store);
        }
    }
}
