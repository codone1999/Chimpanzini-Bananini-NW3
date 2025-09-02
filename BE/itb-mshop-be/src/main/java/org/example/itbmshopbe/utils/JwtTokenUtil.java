package org.example.itbmshopbe.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.security.Key;

public class JwtTokenUtil {
    private static final long EXPIRATION_MS = 15 * 60 * 1000; //15min
    private static final String SECRET_KEY = generateRandomSecret();
    private static final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());


    private static String generateRandomSecret() {
        byte[] key= new byte[32];
        new SecureRandom().nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }

    public static String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public static boolean isTokenExpired(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .getExpiration().before(new Date());
    }

    public static String getSecretKey(){
        return SECRET_KEY;
    }

    public static String generateAccessToken(Integer id,String email,String nickname,String role,String status) {
        Map<String,Object> claims = new HashMap<>();
        claims.put("id",id);
        claims.put("email",email);
        claims.put("nickname",nickname);
        claims.put("role",role);
        claims.put("status",status);
        Date now = new Date();
        Date expiration = new Date(now.getTime() + 1800000);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuer("http://intproj24.sit.kmutt.ac.th/nw3/")
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

    }
    public static Claims validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    public static String generateRefreshToken(Integer id, String email, String nickname, String role,String status) {
        Map<String,Object> claims = new HashMap<>();
        claims.put("id",id);
        claims.put("email",email);
        claims.put("nickname",nickname);
        claims.put("role",role);
        claims.put("status",status);

        Date now = new Date();
        Date expiration = new Date(now.getTime() + (24 * 60 * 60 * 1000)); // 24 hours

        return Jwts.builder()
                .setClaims(claims)
                .setIssuer("http://intproj24.sit.kmutt.ac.th/nw3/")
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    //public static String getEmailFromToken(String token) {
    //    return (String) validateToken(token).get("email");
    //}
//
    //public static String getNicknameFromToken(String token) {
    //    return (String) validateToken(token).get("nickname");
    //}
//
    //public static Integer getIdFromToken(String token) {
    //    return (Integer) validateToken(token).get("id");
    //}
//
    //public static String getRoleFromToken(String token) {
    //    return (String) validateToken(token).get("role");
    //}
}
