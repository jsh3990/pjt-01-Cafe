package com.miniproject.cafe.Service;

import com.miniproject.cafe.VO.AdminRevenueVO;
import java.util.List;

public interface AdminRevenueService {

    List<AdminRevenueVO> getOrdersByDate(String date, String store);

    List<AdminRevenueVO> getAllOrders(String store);

    List<AdminRevenueVO> getOrdersByRange(String startDate, String endDate, String store);
}