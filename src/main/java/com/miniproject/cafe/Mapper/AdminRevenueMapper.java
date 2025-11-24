package com.miniproject.cafe.Mapper;

import com.miniproject.cafe.VO.AdminRevenueVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminRevenueMapper {

    List<AdminRevenueVO> getOrdersByDate(@Param("date") String date,
                                         @Param("store") String store);

    List<AdminRevenueVO> getOrdersByRange(@Param("startDate") String startDate,
                                          @Param("endDate") String endDate,
                                          @Param("store") String store);

    List<AdminRevenueVO> getAllOrders(@Param("store") String store);
}
