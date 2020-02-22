package com.xuecheng.order.dao;

import com.xuecheng.framework.domain.order.XcOrdersPay;
import org.springframework.data.jpa.repository.JpaRepository;

public interface XcOrderPayRepository extends JpaRepository<XcOrdersPay, String> {
    XcOrdersPay findByOrderNumber(String orderNumber);
}
