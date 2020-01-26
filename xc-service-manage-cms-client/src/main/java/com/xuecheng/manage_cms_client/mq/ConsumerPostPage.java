package com.xuecheng.manage_cms_client.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.manage_cms_client.service.PageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description: 监听MQ， 接受页面发布消息
 * @Create: 2020-01-26 16:16:16
 * @Modified By:
 */
@Component
public class ConsumerPostPage {
    private static  final Logger LOGGER = LoggerFactory.getLogger(ConsumerPostPage.class);

    @Autowired
    PageService pageService;

    /**
     * @Description: postPage
     *
     * @param msg 消息队列中的消息体
     * @return: void
     * @Author: LJJ
     * @Date: 2020/1/26 16:18
     */
    @RabbitListener(queues = {"${xuecheng.mq.queue}"})
    public void postPage(String msg) {
        Map map = JSON.parseObject(msg, Map.class);
        String pageId = (String) map.get("pageId");
        CmsPage cmsPage = pageService.findCmsPageById(pageId);
        if (cmsPage == null) {
            LOGGER.error("receive postpage msg, cmsPage is null, pageId: {}", pageId);
            return;
        }
        // 调用service方法将页面从GridFs文件系统下载到服务器
        pageService.savePageToServerPath(pageId);
    }
}
