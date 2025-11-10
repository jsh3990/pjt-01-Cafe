package com.miniproject.cafe.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemVO {
    private int cartItemId;
    private int cartId;
    private int menuId;

    private String menuName; // 메뉴 이름
    private int menuBasePrice; // 메뉴 기본가격
    private String menuTemp; // 메뉴 온도(HOT/ICE)
    private String menuImage; // 메뉴 사진

    private int quantity; // 개수
    private int optionPrice; // 옵션가격
    private String option; // 옵션 내용
}
