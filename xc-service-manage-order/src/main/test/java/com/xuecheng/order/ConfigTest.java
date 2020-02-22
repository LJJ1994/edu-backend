package com.xuecheng.order;

import com.github.wxpay.sdk.MyConfig;
import com.github.wxpay.sdk.WXPay;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description:
 * @Create: 2020-02-20 16:32:32
 * @Modified By:
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class ConfigTest {
    @Test
    public void test() {
        MyConfig config=new MyConfig();
        WXPay wxPay= null;
        try {
            wxPay = new WXPay(config);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map<String,String> map=new HashMap();
        map.put("body","畅购");//商品描述
        map.put("out_trade_no","5555510901");//订单号
        map.put("total_fee","1");//金额
        map.put("spbill_create_ip","127.0.0.1");//终端IP
        map.put("notify_url","http://www.baidu.com");//回调地址
        map.put("trade_type","NATIVE");//交易类型
        Map<String, String> result = null;
        try {
            result = wxPay.unifiedOrder( map );
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(result);
    }

    @Test
    public void testTime() {
        long interval = 15 * 60 * 1000;
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("YYYY-MM-dd HH:dd:ss");
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("YYYY-MM-dd HH:dd:ss");
        long time1 = new Date().getTime();
        long time2 = time1 + 15 * 60 * 1000;
        Date start = new Date(time1);
        Date end = new Date(time2);
        String startTime = simpleDateFormat1.format(start);
        String endTime = simpleDateFormat2.format(end);
    }

    @Test
    public void testTime1() throws ParseException {
        int interval = 15; // 分钟
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        Date start = new Date();
        calendar.setTime(start);
        calendar.add(Calendar.MINUTE, 15);
        Date end = calendar.getTime();
        String s = simpleDateFormat.format(start);
        String e = simpleDateFormat.format(end);

        Date startTime = simpleDateFormat.parse(s);
        Date endTime = simpleDateFormat.parse(e);

    }
}
