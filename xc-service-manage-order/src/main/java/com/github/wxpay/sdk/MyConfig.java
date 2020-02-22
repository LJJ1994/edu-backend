package com.github.wxpay.sdk;

import java.io.InputStream;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description:
 * @Create: 2020-02-20 16:17:17
 * @Modified By:
 */
public class MyConfig extends WXPayConfig{
    @Override
    String getAppID() {
        return "wx8397f8696b538317";
    }

    @Override
    String getMchID() {
        return "1473426802";
    }

    @Override
    String getKey() {
        return "T6m9iK73b0kn9g5v426MKfHQH7X8rKwb";
    }

    @Override
    InputStream getCertStream() {
        return null;
    }

    @Override
    IWXPayDomain getWXPayDomain() {
        return new IWXPayDomain() {
            @Override
            public void report(String domain, long elapsedTimeMillis, Exception ex) {

            }

            @Override
            public DomainInfo getDomain(WXPayConfig config) {
                return new DomainInfo("api.mch.weixin.qq.com",true);
            }
        };
    }
}
