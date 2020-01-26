package com.xuecheng.test;

import com.sun.corba.se.spi.ior.ObjectId;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description: test1
 * @Create: 2020-01-23 08:57:57
 * @Modified By:
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class FreemarkerTest {
    // 基于模板生成静态化html
    @Test
    public void runTestTemplate() throws IOException, TemplateException {
        // 创建配置类
        Configuration configuration = new Configuration(Configuration.getVersion());
        // 设置模板路径
        String classpath = this.getClass().getResource("/").getPath();
        configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
        // 设置字符集
        configuration.setDefaultEncoding("utf-8");
        // 加载模板
        Template template = configuration.getTemplate("hello.ftl");
        // 数据模型
        Map<String, Object> map = new HashMap<>();
        map.put("name", "hello world");
        // 静态化
        String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
        // 静态化内容
        System.out.println(content);
        InputStream inputStream = IOUtils.toInputStream(content);
        FileOutputStream fileOutputStream = new FileOutputStream(new File("e:/hello.html"));
        int copy = IOUtils.copy(inputStream, fileOutputStream);
    }

    // 基于字符串生成静态化html
    @Test
    public void runTestString() throws TemplateException, IOException{
        Configuration configuration = new Configuration(Configuration.getVersion());
        // 模板内容
        String templateString="" +
                "<html>\n" +
                " <head></head>\n" +
                " <body>\n" +
                " 名称：${name}\n" +
                " </body>\n" +
                "</html>";
        // 模板加载器
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        stringTemplateLoader.putTemplate("template", templateString);
        configuration.setTemplateLoader(stringTemplateLoader);
        // 获取模板
        Template template = configuration.getTemplate("template", "utf-8");

        // 模板数据
        Map<String, Object> map = new HashMap<>();
        map.put("name", "hello string");
        // 静态化
        String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
        //静态化内容
        System.out.println(content);
        InputStream inputStream = IOUtils.toInputStream(content);
        FileOutputStream fileOutputStream = new FileOutputStream(new File("e:/hello1.html"));
        IOUtils.copy(inputStream, fileOutputStream);
    }

    @Test
    public void testGridFs() throws FileNotFoundException {
        File file = new File("e:/index_banner.html");
        FileInputStream fileInputStream = new FileInputStream(file);
    }
}
