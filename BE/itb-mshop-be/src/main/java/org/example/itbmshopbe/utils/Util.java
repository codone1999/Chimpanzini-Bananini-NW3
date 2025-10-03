package org.example.itbmshopbe.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.example.itbmshopbe.entities.Seller;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class Util {


    public static String trimFirstAndLastSentence(String input) {
        return input == null ? null : input.trim();
    }

    public static String checkWebsiteUrl(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }
        input = input.replaceAll("\\.\\.+", ".");
        if (input.matches(".*\\.\\w{2,4}(\\.[a-z]{2,4})?$")) {
            int lastDotIndex = input.lastIndexOf('.');
            String tld = input.substring(lastDotIndex);
            String base = input.substring(0, lastDotIndex);

            base = base.replaceAll("\\.\\.", ".");
            tld = tld.replaceAll("\\.([a-z]{2,4}){2,}", ".$1");

            input = base + tld;
        }
        return input;
    }

    public static Integer getUserIdFromToken(HttpServletRequest request) {
        String token = extractToken(request);
        try {
            return JwtTokenUtil.getIdFromToken(token);
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }
    }

    public static Integer validateAndGetUserId(HttpServletRequest request, Integer pathId) {
        String token = extractToken(request);
        Integer tokenUserId = extractUserIdFromToken(token);

        if (pathId != null && !pathId.equals(tokenUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Request user id not matched with id in access token");
        }

        return tokenUserId;
    }

    public static Integer validateAndGetSellerUserId(HttpServletRequest request, Integer pathId) {
        String token = extractToken(request);
        Integer tokenUserId = extractUserIdFromToken(token);
        String tokenUserRole = extractRoleFromToken(token);

        if (!"SELLER".equalsIgnoreCase(tokenUserRole)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only sellers can access this resource");
        }

        if (pathId != null && !pathId.equals(tokenUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Request seller id not matched with id in access token");
        }

        return tokenUserId;
    }

    private static String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Token");
        }
        return authHeader.substring(7);
    }

    private static Integer extractUserIdFromToken(String token) {
        try {
            return JwtTokenUtil.getIdFromToken(token);
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }
    }

    private static String extractRoleFromToken(String token) {
        try {
            return JwtTokenUtil.getRoleFromToken(token);
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }
    }
}
