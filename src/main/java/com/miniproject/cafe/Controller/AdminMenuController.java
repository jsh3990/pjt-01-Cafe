package com.miniproject.cafe.Controller;

import com.miniproject.cafe.Service.MenuService;
import com.miniproject.cafe.VO.AdminVO;
import com.miniproject.cafe.VO.MenuVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminMenuController {

    @Autowired
    private MenuService menuService;

    /** ================= 메뉴 관리 리스트 ================= */
    @GetMapping("/menu")
    public String menuManagement(Model model, HttpSession session) {
        AdminVO admin = (AdminVO) session.getAttribute("admin");
        if (admin == null) return "redirect:/admin/login";

        String storeName = admin.getStoreName();

        model.addAttribute("isLoggedIn", true);
        model.addAttribute("activePage", "menu");
        model.addAttribute("menuList", menuService.getMenuByStore(storeName));

        Boolean updated = (Boolean) session.getAttribute("updateSuccess");
        model.addAttribute("updateSuccess", updated);
        session.removeAttribute("updateSuccess");

        return "admin_menu_management";
    }


    /** ================= 신규 메뉴 등록 ================= */
    @PostMapping("/insertMenu")
    public String insertMenu(
            MenuVO vo,
            @RequestParam(value = "menuImgFile", required = false) MultipartFile file,
            @RequestParam("temperature") String temperature,
            HttpSession session
    ) {

        AdminVO admin = (AdminVO) session.getAttribute("admin");
        if (admin == null) throw new RuntimeException("관리자 로그인 정보가 없습니다.");

        String storeName = admin.getStoreName();
        vo.setStoreName(storeName);

        String prefix = getStorePrefix(storeName);
        String lastId = menuService.getLastMenuIdByStore(storeName);

        String newMenuId = generateNextId(prefix, lastId);
        vo.setMenuId(newMenuId);

        vo.setHotAvailable("AVAILABLE".equals(temperature) ? 1 : 0);

        // 기본 판매 상태
        if (vo.getSalesStatus() == null) vo.setSalesStatus("판매중");

        // 이미지 처리
        if (file != null && !file.isEmpty()) {
            String originalName = file.getOriginalFilename();
            String ext = originalName.substring(originalName.lastIndexOf("."));
            String fileName = java.util.UUID.randomUUID().toString() + ext;

            String filePath = "C:/upload/menuImg/";

            try {
                java.io.File dir = new java.io.File(filePath);
                if (!dir.exists()) dir.mkdirs();
                file.transferTo(new java.io.File(filePath + fileName));
                vo.setMenuImg(fileName);
            } catch (Exception e) {
                e.printStackTrace();
                vo.setMenuImg("default.png");
            }
        } else {
            vo.setMenuImg("default.png");
        }

        menuService.insertMenu(vo);
        return "redirect:/admin/menu";
    }

    /** ================= 매장 prefix ================= */
    private String getStorePrefix(String storeName) {
        switch (storeName) {
            case "강남중앙점": return "GN";
            case "역삼중앙점": return "YS";
            case "선릉중앙점": return "SL";
            default: return "MN";
        }
    }

    /** ================= 안전한 ID 생성 ================= */
    private String generateNextId(String prefix, String lastId) {
        if (lastId == null || lastId.length() < prefix.length() + 1) {
            return prefix + "001";
        }

        String numberPart;
        try {
            numberPart = lastId.substring(prefix.length());
        } catch (Exception e) {
            return prefix + "001";
        }

        int nextNum;
        try {
            nextNum = Integer.parseInt(numberPart) + 1;
        } catch (Exception e) {
            return prefix + "001";
        }

        return prefix + String.format("%03d", nextNum);
    }

    /** ================= 수정 페이지 ================= */
    @GetMapping("/updateMenu/{menuId}")
    public String updateMenuPage(@PathVariable String menuId, Model model, HttpSession session) {

        AdminVO admin = (AdminVO) session.getAttribute("admin");
        if (admin == null) return "redirect:/admin/login";

        MenuVO menu = menuService.getMenuById(menuId, admin.getStoreName());

        model.addAttribute("isLoggedIn", true);
        model.addAttribute("activePage", "menu");
        model.addAttribute("menu", menu);
        model.addAttribute("menuList", menuService.getMenuByStore(admin.getStoreName()));

        Boolean updated = (Boolean) session.getAttribute("updateSuccess");
        model.addAttribute("updateSuccess", updated);
        session.removeAttribute("updateSuccess");

        return "admin_menu_management";
    }

    /** ================= 수정 처리 ================= */
    @PostMapping("/updateMenu")
    public String updateMenu(
            MenuVO vo,
            @RequestParam(value="menuImgFile", required=false) MultipartFile file,
            @RequestParam("temperature") String temperature,
            HttpSession session
    ) {

        AdminVO admin = (AdminVO) session.getAttribute("admin");
        if (admin == null) throw new RuntimeException("관리자 로그인 필요");

        vo.setStoreName(admin.getStoreName());
        vo.setHotAvailable("AVAILABLE".equals(temperature) ? 1 : 0);

        MenuVO original = menuService.getMenuById(vo.getMenuId(), admin.getStoreName());

        /** ⭐ 판매상태 값 없으면 기존값 유지 */
        if (vo.getSalesStatus() == null) {
            vo.setSalesStatus(original.getSalesStatus());
        }

        // 이미지 적용
        if (file != null && !file.isEmpty()) {
            String originalName = file.getOriginalFilename();
            String ext = originalName.substring(originalName.lastIndexOf("."));
            String fileName = java.util.UUID.randomUUID().toString() + ext;

            String filePath = "C:/upload/menuImg/";

            try {
                java.io.File dir = new java.io.File(filePath);
                if (!dir.exists()) dir.mkdirs();
                file.transferTo(new java.io.File(filePath + fileName));
                vo.setMenuImg(fileName);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            vo.setMenuImg(original.getMenuImg());
        }

        menuService.updateMenu(vo);
        session.setAttribute("updateSuccess", true);

        return "redirect:/admin/menu";
    }

    /** ================= 삭제 ================= */
    @DeleteMapping("/deleteMenu/{id}")
    @ResponseBody
    public String deleteMenu(@PathVariable("id") String menuId, HttpSession session) {
        AdminVO admin = (AdminVO) session.getAttribute("admin");
        if (admin == null) return "fail";

        menuService.deleteMenuByStore(menuId, admin.getStoreName());
        return "success";
    }

    /** ================= 선택 삭제 ================= */
    @PostMapping("/deleteMenuBatch")
    @ResponseBody
    public String deleteMenuBatch(@RequestBody List<String> ids, HttpSession session) {
        AdminVO admin = (AdminVO) session.getAttribute("admin");
        if (admin == null) return "fail";

        for (String id : ids) {
            menuService.deleteMenuByStore(id, admin.getStoreName());
        }
        return "success";
    }

    /** ================= 판매 상태 변경 ================= */
    @PostMapping("/updateStatus")
    @ResponseBody
    public String updateMenuStatus(@RequestBody Map<String, String> data, HttpSession session) {
        AdminVO admin = (AdminVO) session.getAttribute("admin");
        if (admin == null) return "fail";

        menuService.updateSalesStatus(data.get("menuId"), admin.getStoreName(), data.get("status"));
        return "success";
    }
}
