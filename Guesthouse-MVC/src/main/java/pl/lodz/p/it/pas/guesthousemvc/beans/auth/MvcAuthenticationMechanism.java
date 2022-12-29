package pl.lodz.p.it.pas.guesthousemvc.beans.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.security.enterprise.AuthenticationException;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.authentication.mechanism.http.AutoApplySession;
import javax.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import javax.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

@ApplicationScoped
@AutoApplySession
public class MvcAuthenticationMechanism implements HttpAuthenticationMechanism {

    @Inject
    private SessionBean session;


    @Override
    public AuthenticationStatus validateRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, HttpMessageContext httpMessageContext) throws AuthenticationException {
        if (session.getJwt() == null) {
            System.out.println("Session authentication as ANONYMOUS");
            return httpMessageContext.notifyContainerAboutLogin("anonymous", Collections.singleton("ANONYMOUS"));
        }
        String jwt = session.getJwt();
        Claims claims = parseJWTWithoutSign(jwt);

        String username = claims.getSubject();
        ArrayList<String> roles = claims.get("roles", ArrayList.class);

        System.out.println(claims);
        return httpMessageContext.notifyContainerAboutLogin(username, new HashSet<>(roles));
    }

    public Claims parseJWTWithoutSign(String jwt) {
        String withoutSign = removeSign(jwt);
        return Jwts.parser()
                .parseClaimsJwt(withoutSign)
                .getBody();
    }

    private String removeSign(String jwtToken) {
        if (jwtToken == null) return null;

        final int index = jwtToken.indexOf('.');
        final int index2 = jwtToken.indexOf('.', index + 1);
        if (index2 > 0) return jwtToken.substring(0, index2 + 1);

        return jwtToken;
    }
}
