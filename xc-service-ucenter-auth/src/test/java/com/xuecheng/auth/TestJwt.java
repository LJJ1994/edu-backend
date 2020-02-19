package com.xuecheng.auth;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.mapping.TextScore;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.test.context.junit4.SpringRunner;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description:
 * @Create: 2020-02-14 13:24:24
 * @Modified By:
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestJwt {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 用私钥生成jwt
    @Test
    public void testCreateJwt() {
        // 证书文件
        String key_location = "xc.keystore";
        // 密钥库密码
        String keystore_password = "xuechengkeystore";
        // 证书路径
        ClassPathResource pathResource = new ClassPathResource(key_location);
        // 密钥工厂
        KeyStoreKeyFactory keyFactory = new KeyStoreKeyFactory(pathResource, keystore_password.toCharArray());
        // 密钥的访问密码
        String keypassword = "xuecheng";
        String alias = "xckey";
        KeyPair keyPair = keyFactory.getKeyPair(alias, keypassword.toCharArray());
        RSAPrivateKey aPrivate = (RSAPrivateKey) keyPair.getPrivate();
        // 定义payload
        Map<String, Object> msg = new HashMap<>();
        msg.put("name", "zhangsan");
        String jsonString = JSON.toJSONString(msg);
        // 生成令牌
        Jwt encode = JwtHelper.encode(jsonString, new RsaSigner(aPrivate));
        // 取出令牌
        String token = encode.getEncoded();
        System.out.println(token);
    }
    //用公钥与jwt令牌进行配对
    @Test
    public void testGetJwt() {
        String jwt = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOiIxIiwidXNlcnBpYyI6bnVsbCwidXNlcl9uYW1lIjoiaXRjYXN0Iiwic2NvcGUiOlsiYXBwIl0sIm5hbWUiOiJ0ZXN0MDIiLCJ1dHlwZSI6IjEwMTAwMiIsImlkIjoiNDkiLCJleHAiOjE1ODE5Mzk1MTEsImF1dGhvcml0aWVzIjpbInhjX3RlYWNobWFuYWdlcl9jb3Vyc2VfYmFzZSIsInhjX3RlYWNobWFuYWdlcl9jb3Vyc2VfZGVsIiwieGNfdGVhY2htYW5hZ2VyX2NvdXJzZV9saXN0IiwieGNfdGVhY2htYW5hZ2VyX2NvdXJzZV9wbGFuIiwieGNfdGVhY2htYW5hZ2VyX2NvdXJzZSIsImNvdXJzZV9maW5kX2xpc3QiLCJ4Y190ZWFjaG1hbmFnZXIiLCJ4Y190ZWFjaG1hbmFnZXJfY291cnNlX21hcmtldCIsInhjX3RlYWNobWFuYWdlcl9jb3Vyc2VfcHVibGlzaCIsInhjX3RlYWNobWFuYWdlcl9jb3Vyc2VfYWRkIl0sImp0aSI6IjUxMGFiOGYzLTA3N2QtNDA0Zi04N2YxLTIzMmUwMTU1ZWE5NSIsImNsaWVudF9pZCI6IlhjV2ViQXBwIn0.JWvYxjVxvYkh48T_ldaHAFI2vczTzH-irSIE8h8WkMJkLTNc8C4MZjOIiZ5f75wN-BqZLf15GDv1Qr3qaULShuQS9Yuvq02sUnxG-xP53v_r5N2w0rZestY8eHDxu93bdAfsL0IU_gWQxaaXTZtdPT6vtLYxXbfKDHxtR7jBUJR2NwGq29iSuuQ6rfVEUw4d77G5BEd5ov1TxNGBg6w53_CxMBDNxPiEBdGqyM18qLzOjzKTX1uxAPsM9PTN6dP6QiVjYhWmO6YHpskXQDlxv6t66CIzJwKzokTAT5ani5FkSpFPshcxPj7Wa5zczofW6dkM3VApN7wZfW1F52XPwA";
        String pub = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgm+QbOoa7fnsxdt4as/DOFUsBkukX0jlIrOvBHSCauCJeLrAtqhk/IbBGPAmhCcX1fzT2twssAPyDfFTjWqrU2NMxJjClHg6RDqmYTcGCca7y3oXODmPi68SkqSwKoustOu4jbNCLHRUjq7gkwwu9K4511+6bLc6l2y4QEqKFTZlRHboCv6TXnWLUvXDYGUq35uaRAaJyRs2Uv8WJWz3wTc77LNC9iKzNMoT8VAi8P59dkvB+/rkHBMsJAgXGLn2spmtgE64YQ6wtyBIzVNeCIQaVh+sbLMPvmJCHxb32hANxyzLuGvNvR0nqbKrqxmN0RmoJ5ObaCIn5+BG2PDdswIDAQAB-----END PUBLIC KEY-----";

        Jwt verify = JwtHelper.decodeAndVerify(jwt, new RsaVerifier(pub));
        String claims = verify.getClaims();
        System.out.println(claims);
    }

    @Test
    public void testRedis() {
        String key = "user_token:76c7df20-cf7a-4d3b-83f2-dc9fd3d85b4d";
        Map<String,String> map = new HashMap<>();
        map.put("name","zhangsan");
        map.put("age","25");
        String mapJson = JSON.toJSONString(map);
        redisTemplate.boundValueOps(key).set(mapJson, 30, TimeUnit.SECONDS);

        Long expire = redisTemplate.getExpire(key);
        System.out.println(expire);
    }

    @Test
    public void testGet() {
        String key = "user_token:76c7df20-cf7a-4d3b-83f2-dc9fd3d85b4d";
        String value = redisTemplate.opsForValue().get(key);
        System.out.println(value);
    }
}
