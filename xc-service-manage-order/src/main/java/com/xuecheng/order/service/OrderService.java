package com.xuecheng.order.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.course.CourseMarket;
import com.xuecheng.framework.domain.order.XcOrders;
import com.xuecheng.framework.domain.order.XcOrdersDetail;
import com.xuecheng.framework.domain.order.XcOrdersPay;
import com.xuecheng.framework.domain.order.request.CreateOrderRequest;
import com.xuecheng.framework.domain.order.response.CreateOrderResult;
import com.xuecheng.framework.domain.order.response.OrderCode;
import com.xuecheng.framework.domain.order.response.OrderResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.utils.Snowflake;
import com.xuecheng.framework.utils.SnowflakeIdWorker;
import com.xuecheng.order.config.CourseClient;
import com.xuecheng.order.dao.XcOrderDetailsRepository;
import com.xuecheng.order.dao.XcOrderPayRepository;
import com.xuecheng.order.dao.XcOrdersRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description:
 * @Create: 2020-02-20 20:48:48
 * @Modified By:
 */
@Service
public class OrderService {

    @Value("${time.interval}")
    int interval;

    @Autowired
    CourseClient courseClient;

    @Autowired
    XcOrderDetailsRepository xcOrderDetailsRepository;

    @Autowired
    XcOrdersRepository xcOrdersRepository;

    @Autowired
    XcOrderPayRepository xcOrderPayRepository;

    @Autowired
    StringRedisTemplate redisTemplate;

    // 创建订单
    @Transactional
    public CreateOrderResult create(CreateOrderRequest createOrderRequest){
        // 查询课程营销
        String courseId = createOrderRequest.getCourseId();
        if (StringUtils.isEmpty(courseId)) {
            ExceptionCast.cast(OrderCode.ORDER_ADD_GETCOURSEERROR);
        }
        // 获取开始时间和结束时间，间隔为15分钟
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        Date start = new Date();
        calendar.setTime(start);
        calendar.add(Calendar.MINUTE, interval);
        Date end = calendar.getTime();
        String startTime = simpleDateFormat.format(start);
        String endTime = simpleDateFormat.format(end);

        // 查询订单明细表中是否有该课程的订单，并且判断该订单是否过期
        List<XcOrdersDetail> list = xcOrderDetailsRepository.findByItemId(courseId);
        for(XcOrdersDetail one : list) {
            Date oneEndTime1 = null;
            if (one != null) {
                String oneEndTime = one.getEndTime();
                try {
                    oneEndTime1 = simpleDateFormat.parse(oneEndTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            // 判断表中的订单的结束时间 和 当前时间 进行比较, 订单不为空且没有过期则返回
            if (one != null && this.compareDate(oneEndTime1, start) >= 2){
                String orderNumber = one.getOrderNumber();
                XcOrders xcOrders = xcOrdersRepository.findByOrderNumber(orderNumber);
                return new CreateOrderResult(CommonCode.SUCCESS, xcOrders);
            }
        }

        // 远程调用course
        CourseMarket courseMarket = courseClient.getCourseMarketById(courseId);
        if (courseMarket == null) {
            ExceptionCast.cast(OrderCode.ORDER_ADD_GETCOURSEERROR);
        }
        String userId = createOrderRequest.getUserId();
        Float initial_price = courseMarket.getPrice_old(); // 订价
        Float price = courseMarket.getPrice(); // 交易价

        Snowflake idWorker = new Snowflake(1, 2);
        String orderNumber = idWorker.nextId() + ""; // 交易号

        // 填充订单表(xc-orders)
        XcOrders xcOrders = new XcOrders();
        xcOrders.setOrderNumber(orderNumber);
        xcOrders.setInitialPrice(initial_price);
        xcOrders.setPrice(price);
        xcOrders.setStartTime(startTime);
        xcOrders.setEndTime(endTime);
        xcOrders.setUserId(userId);
        xcOrders.setStatus("401001");
        XcOrders save = xcOrdersRepository.save(xcOrders);

        // 填充订单明细数据(xc-order-details)
        XcOrdersDetail ordersDetail = new XcOrdersDetail();
        ordersDetail.setItemId(courseId);
        ordersDetail.setOrderNumber(save.getOrderNumber());
        ordersDetail.setItemPrice(price);
        ordersDetail.setItemNum(1);
        ordersDetail.setStartTime(startTime);
        ordersDetail.setEndTime(endTime);
        ordersDetail.setValid("204001");
        XcOrdersDetail save1 = xcOrderDetailsRepository.save(ordersDetail);

        // 将订单明细转换为json
        List<XcOrdersDetail> detailList = new ArrayList<>();
        detailList.add(save1);
        String jsonString = JSON.toJSONString(detailList);
        XcOrders xcOrdersNew = new XcOrders();
        BeanUtils.copyProperties(save, xcOrdersNew);
        xcOrdersNew.setDetails(jsonString);
        xcOrdersRepository.save(xcOrdersNew);

        // 填充订单支付表
        XcOrdersPay xcOrdersPay = new XcOrdersPay();
        xcOrdersPay.setStatus("402001");
        xcOrdersPay.setOrderNumber(orderNumber);
        xcOrderPayRepository.save(xcOrdersPay);

        return new CreateOrderResult(CommonCode.SUCCESS, xcOrdersNew);
    }

    /*
    * 日期大小比较
    * 0--相同  1--前者大  2--后者大
     */
    public int compareDate(Date sDate, Date eDate)
    {
        int result = 0;
        //将开始时间赋给日历实例
        Calendar sC = Calendar.getInstance();
        sC.setTime(sDate);
        //将结束时间赋给日历实例
        Calendar eC = Calendar.getInstance();
        eC.setTime(eDate);
        //比较
        result = sC.compareTo(eC);
        //返回结果
        return result;
    }

    // 查询订单
    public OrderResult getOrder(String id) {
        XcOrders one = xcOrdersRepository.findByOrderNumber(id);
        if (one != null) {
            return new OrderResult(CommonCode.SUCCESS, one);
        }
        return null;
    }

    // 保存订单支付表 信息
    @Transactional
    public boolean savePayOrder(String transactionId, String orderNumber) {
        XcOrdersPay one = xcOrderPayRepository.findByOrderNumber(orderNumber);
        if (one != null) {
            XcOrdersPay xcOrdersPay = new XcOrdersPay();
            BeanUtils.copyProperties(one, xcOrdersPay);
            xcOrdersPay.setPayNumber(transactionId);
            xcOrdersPay.setStatus("402002");
            xcOrderPayRepository.save(xcOrdersPay);
            return true;
        }
        return false;
    }

    // 更改订单表支付状态为成功
    @Transactional
    public void updateXcOrders(String orderNumber) {
        XcOrders number = xcOrdersRepository.findByOrderNumber(orderNumber);
        if(number != null && "401001".equals(number.getStatus())) {
            XcOrders xcOrders = new XcOrders();
            BeanUtils.copyProperties(number, xcOrders);
            xcOrders.setStatus("401002");
            xcOrdersRepository.save(xcOrders);
        }
    }
}
