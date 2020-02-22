package com.xuecheng.order.controller;

import com.github.wxpay.sdk.WXPayUtil;
import com.github.wxpay.sdk.WXPayXmlUtil;
import com.xuecheng.api.order.OrderControllerApi;
import com.xuecheng.framework.domain.order.XcOrders;
import com.xuecheng.framework.domain.order.request.CreateOrderRequest;
import com.xuecheng.framework.domain.order.request.CreateQrcodeRequest;
import com.xuecheng.framework.domain.order.request.OrderQueryRequest;
import com.xuecheng.framework.domain.order.response.CreateOrderResult;
import com.xuecheng.framework.domain.order.response.OrderResult;
import com.xuecheng.framework.domain.order.response.PayOrderResult;
import com.xuecheng.framework.domain.order.response.PayQrcodeResult;
import com.xuecheng.framework.utils.ConvertUtils;
import com.xuecheng.order.service.OrderService;
import com.xuecheng.order.service.WxPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description:
 * @Create: 2020-02-20 20:47:47
 * @Modified By:
 */
@RestController
@RequestMapping("/order")
public class OrderController implements OrderControllerApi {
    @Autowired
    OrderService orderService;

    @Autowired
    WxPayService wxPayService;

    @Value("${wxpay.query_url}")
    private String queryUrl;

    @Override
    @PostMapping("/create")
    public CreateOrderResult create(@RequestBody CreateOrderRequest createOrderRequest) {
        return orderService.create(createOrderRequest);
    }

    @Override
    @GetMapping("/get/{id}")
    public OrderResult getOrder(@PathVariable("id") String id) {

        return orderService.getOrder(id);
    }

    @Override
    @PostMapping("/pay/createWeixinQrcode")
    public PayQrcodeResult createWxQrcode(@RequestBody CreateQrcodeRequest request) {
        return wxPayService.createWeixinQrcode(request);
    }

    // 微信回调统治 notify_url
    @RequestMapping("/wxpay/url")
    public void fallback(HttpServletRequest request, HttpServletResponse response) {
        try {
            // 微信回调的输入流转换为字符串
            System.out.println("================获取wxpay通知========================");
            String xml = ConvertUtils.convertToString(request.getInputStream());
            Map<String, String> map = WXPayUtil.xmlToMap(xml);
            System.out.println(map);
            // 订单状态
            if (map.get("result_code").equals("SUCCESS")) {
                String out_trade_no = map.get("out_trade_no");
                Map result = wxPayService.queryOrder(out_trade_no);
                if (map.get("result_code").equals("SUCCESS")){
                    String transactionId = (String) result.get("transaction_id"); // 微信支付交易号
                    String orderNumber = (String) result.get("out_trade_no"); // 商户自定义订单号
                    // 修改订单支付表
                    orderService.savePayOrder(transactionId, orderNumber);
                    // 修改订单表
                    orderService.updateXcOrders(orderNumber);
                }
            }
            // 响应结果返回给微信
            response.setContentType("text/html");
            String data = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml> ";
            response.getWriter().write(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @GetMapping("/pay/queryWeixinPayStatus/{orderNumber}")
    public PayOrderResult queryOrder(@PathVariable("orderNumber") String orderNumber) {
        return wxPayService.queryOrderPay(orderNumber);
    }
}
