package com.changgou.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;

/**
 * @Auther: hftang
 * @Date: 2020/2/24 10:42
 * @Description:
 */
public class TestJwtDemo {

    @Test
    public void testJwt() {
        JwtBuilder jwtBuilder = Jwts.builder()
                .setId("888")
                .setSubject("小白")
                .setIssuedAt(new Date())
//                .setExpiration(new Date())
                .signWith(SignatureAlgorithm.HS256, "itcast");

        //自己创建更多的字段

        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("name","hftang");
        hashMap.put("sex","男");
        hashMap.put("item","中国");

        jwtBuilder.addClaims(hashMap);




        System.out.println("---->" + jwtBuilder.compact());


    }

    /**
     * 解析jwt
     */
    @Test
    public void parseJwt() {

        String token = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI4ODgiLCJzdWIiOiLlsI_nmb0iLCJpYXQiOjE1ODI1MTM1NDAsIml0ZW0iOiLkuK3lm70iLCJzZXgiOiLnlLciLCJuYW1lIjoiaGZ0YW5nIn0.5w-92TYm9SzUdv7oiwUhB84duvBeFM4E8o1qp33pxrA";
        Claims claims = Jwts.parser().setSigningKey("itcast").parseClaimsJws(token).getBody();

        System.out.println(claims);


    }


}
