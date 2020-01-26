package com.xuecheng.test.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description: a
 * @Create: 2020-01-22 15:57:57
 * @Modified By:
 */
@Controller
public class FreemarkerController {
    @Autowired
    RestTemplate restTemplate;

    @GetMapping("/test")
    public String welcome(Map<String, String> map) {
        map.put("name", "Java程序员");
        return "hello";
    }

    @GetMapping("/banner")
    public String getBanner(Map map) {
        ResponseEntity<Map> forEntity = restTemplate.getForEntity("http://localhost:31001/cms/config/getmodel/5a791725dd573c3574ee333f", Map.class);
        Map body = forEntity.getBody();
        map.putAll(body);
        return "index_banner";
    }
}
