package com.miniproject.cafe.Mapper;

import com.miniproject.cafe.VO.MenuVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface MenuMapper {

    List<MenuVO> getMenuByStoreAndCategory(@Param("storeName") String storeName,
                                           @Param("category") String category);

    List<MenuVO> getMenuByStore(String storeName);

    // 단일 파라미터 삭제
    // MenuVO getMenuById(String menuId);

    // 매장 포함 버전만 유지
    MenuVO getMenuById(@Param("menuId") String menuId,
                       @Param("storeName") String storeName);

    void insertMenu(MenuVO menuVO);
    void updateMenu(MenuVO menuVO);
    void deleteMenuByStore(String menuId, String storeName);
    String getLastMenuIdByStore(String storeName);
    void updateSalesStatus(String menuId, String storeName, String salesStatus);

    List<MenuVO> searchMenu(@Param("storeName") String storeName,
                            @Param("keyword") String keyword);

}
