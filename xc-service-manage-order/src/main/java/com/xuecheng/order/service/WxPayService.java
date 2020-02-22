package com.xuecheng.order.service;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayUtil;
import com.xuecheng.framework.domain.order.XcOrders;
import com.xuecheng.framework.domain.order.XcOrdersDetail;
import com.xuecheng.framework.domain.order.XcOrdersPay;
import com.xuecheng.framework.domain.order.request.CreateQrcodeRequest;
import com.xuecheng.framework.domain.order.response.CreateOrderResult;
import com.xuecheng.framework.domain.order.response.OrderCode;
import com.xuecheng.framework.domain.order.response.PayOrderResult;
import com.xuecheng.framework.domain.order.response.PayQrcodeResult;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.order.dao.XcOrderPayRepository;
import com.xuecheng.order.dao.XcOrdersRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description:
 * @Create: 2020-02-21 19:37:37
 * @Modified By:
 */
@Service
public class WxPayService {
    @Autowired
    WXPay wxPay;

    @Autowired
    XcOrdersRepository xcOrdersRepository;

    @Autowired
    XcOrderPayRepository xcOrderPayRepository;

    @Value("${wxpay.url}")
    private String url;

    public PayQrcodeResult createWeixinQrcode(CreateQrcodeRequest request) {
        // 验证订单号
        String orderNumber = request.getOrderNumber();
        if (StringUtils.isEmpty(orderNumber)) {
            return new PayQrcodeResult(OrderCode.ORDER_ORDERNUMERROR_NULL);
        }
        // 查询订单
        XcOrders one = xcOrdersRepository.findByOrderNumber(orderNumber);
        if (one == null) {
            return new PayQrcodeResult(OrderCode.Pay_NOTFOUNDORDER);
        }

        // 获取开始时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date start = new Date();

        //判断该订单是否过期
        Date oneEndTime1 = null;
        if (one != null) {
            String oneEndTime = one.getEndTime();

            try {
                oneEndTime1 = simpleDateFormat.parse(oneEndTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        // 如果过期
        if (one != null && compareDate(oneEndTime1, start) >= 2){
            return new PayQrcodeResult(OrderCode.Pay_ORDER_EXPIRED);
        }

        // 如果没有过期并且未支付，则返回

        // 下单生成二维码
        Float price = one.getPrice();
        Map resultMap = nativePay(orderNumber, price);
        String codeUrl = (String) resultMap.get("code_url");

        PayQrcodeResult qrcodeResult = new PayQrcodeResult(CommonCode.SUCCESS);
        qrcodeResult.setCodeUrl(codeUrl);
        qrcodeResult.setMoney(price);
        qrcodeResult.setOrderNumber(orderNumber);

        return qrcodeResult;
    }

    /*
     * 日期大小比较
     * 0--相同  1--前者大  2--后者大
     */
    private int compareDate(Date sDate, Date eDate)
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

    // 微信支付下单
    private Map nativePay(String tradeId, Float money) {
        try {
            // 定义请求参数
            Map<String, String> map = new HashMap<>();
            map.put("body", "在线教育");
            map.put("out_trade_no", tradeId);
            BigDecimal payMoney = new BigDecimal("0.01");
            BigDecimal fen = payMoney.multiply(new BigDecimal("100")); //1.00
            fen = fen.setScale(0, BigDecimal.ROUND_UP); // 1
            map.put("total_fee", String.valueOf(fen)); // 以分为单位
            map.put("notify_url", url); // 回调地址
            map.put("trade_type","NATIVE");//交易类型

            // 统一下单
            Map<String, String> resultMap = wxPay.unifiedOrder(map);
            return resultMap;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 请求微信接口，查询订单状态
    public Map queryOrder(String orderNumber) {
        Map map = new HashMap();
        map.put("out_trade_no", orderNumber);
        try {
            return wxPay.orderQuery(map);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 查询数据库订单支付表
    public PayOrderResult queryOrderPay(String orderNumber) {
        XcOrdersPay one = xcOrderPayRepository.findByOrderNumber(orderNumber);
        if (one != null && one.getStatus().equals("402002")) {
            PayOrderResult payOrderResult = new PayOrderResult(CommonCode.SUCCESS);
            payOrderResult.setXcOrdersPay(one);
            return payOrderResult;
        }
        return new PayOrderResult(OrderCode.Pay_ORDER_NOT_PAY);
    }
}
