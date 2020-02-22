package com.xuecheng.order.dao;

import com.xuecheng.framework.domain.order.XcOrdersDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface XcOrderDetailsRepository extends JpaRepository<XcOrdersDetail, String> {
    List<XcOrdersDetail> findByItemId(String itemId);
}
