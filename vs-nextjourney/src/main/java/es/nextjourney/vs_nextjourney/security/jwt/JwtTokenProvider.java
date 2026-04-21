package es.nextjourney.vs_nextjourney.security.jwt;

import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtTokenProvider {

    private final SecretKey jwtSecret = Jwts.SIG.HS256.key().build();
    private final JwtParser jwtParser = Jwts.parser().verifyWith(jwtSecret).build();

    public String tokenStringFromHeaders(HttpServletRequest req) {
        String bearerToken = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        throw new IllegalArgumentException("Cabecera de autorización no válida");
    }

    private String tokenStringFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (TokenType.ACCESS.cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        throw new IllegalArgumentException("No se encontró la cookie del token");
    }

    public Claims validateToken(HttpServletRequest req, boolean fromCookie) {
        String token = fromCookie ? tokenStringFromCookies(req) : tokenStringFromHeaders(req);
        return validateToken(token);
    }

    public Claims validateToken(String token) {
        return jwtParser.parseSignedClaims(token).getPayload();
    }

    public String generateAccessToken(UserDetails userDetails) {
        return buildToken(TokenType.ACCESS, userDetails).compact();
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(TokenType.REFRESH, userDetails).compact();
    }

    private JwtBuilder buildToken(TokenType tokenType, UserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = Date.from(now.toInstant().plus(tokenType.duration));
        return Jwts.builder()
                .claim("roles", userDetails.getAuthorities())
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(jwtSecret);
    }
}