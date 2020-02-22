package com.xuecheng.api.order;

import com.xuecheng.framework.domain.order.XcOrders;
import com.xuecheng.framework.domain.order.request.CreateOrderRequest;
import com.xuecheng.framework.domain.order.request.CreateQrcodeRequest;
import com.xuecheng.framework.domain.order.request.OrderQueryRequest;
import com.xuecheng.framework.domain.order.response.CreateOrderResult;
import com.xuecheng.framework.domain.order.response.OrderResult;
import com.xuecheng.framework.domain.order.response.PayOrderResult;
import com.xuecheng.framework.domain.order.response.PayQrcodeResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Api(value = "订单接口", description = "下单、支付、返回成功结果")
public interface OrderControllerApi {
    @ApiOperation("创建订单")
    CreateOrderResult create(CreateOrderRequest createOrderRequest);

    @ApiOperation("获取订单信息")
    OrderResult getOrder(String orderId);

    @ApiOperation("生成支付二维码")
    PayQrcodeResult createWxQrcode(CreateQrcodeRequest qrcodeRequest);

    @ApiOperation("微信支付回调url")
    void fallback(HttpServletRequest request, HttpServletResponse response);

    @ApiOperation("查询订单状态")
    PayOrderResult queryOrder(String orderNumber);
}
