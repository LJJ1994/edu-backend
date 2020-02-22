package com.xuecheng.order.controller;

import com.alibaba.fastjson.JSON;
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
import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.utils.ConvertUtils;
import com.xuecheng.order.config.RabbitMQConfig;
import com.xuecheng.order.service.OrderService;
import com.xuecheng.order.service.TaskService;
import com.xuecheng.order.service.WxPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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

    @Autowired
    TaskService taskService;

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

    // 微信回调通知 notify_url
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
                    // 向xc_task 任务表插入选课记录
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    OrderResult orderResult = orderService.getOrder(orderNumber);
                    XcOrders xcOrders = orderResult.getXcOrders();
                    String start_time = xcOrders.getStartTime();
                    Date startTime = format.parse(start_time);
                    String routingKey = RabbitMQConfig.XC_LEARNING_ADDCHOOSECOURSE_KEY;
                    String exchange = RabbitMQConfig.EX_LEARNING_ADDCHOOSECOURSE;
                    String userId = xcOrders.getUserId();
                    String details = xcOrders.getDetails();
                    List list = JSON.parseObject(details, List.class);
                    Map mapString = (Map)list.get(0);
                    String courseId = (String) mapString.get("itemId");

                    XcTask xcTask = new XcTask();
                    xcTask.setCreateTime(startTime);
                    xcTask.setMqExchange(exchange);
                    xcTask.setMqRoutingkey(routingKey);
                    xcTask.setUpdateTime(startTime);
                    xcTask.setDeleteTime(startTime);
                    xcTask.setErrormsg("");
                    xcTask.setStatus("105001"); // 未执行
                    xcTask.setTaskType("106001"); // 选课
                    xcTask.setVersion(1);

                    Map<String, String> requestBody = new HashMap<>();
                    requestBody.put("userId", userId);
                    requestBody.put("courseId", courseId);
                    String jsonString = JSON.toJSONString(requestBody);
                    xcTask.setRequestBody(jsonString);
                    // 插入选课任务表
                    taskService.saveXcTask(xcTask);
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
