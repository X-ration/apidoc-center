package com.adam.apidoc_center.security.oauth2;

import org.apache.catalina.util.ParameterMap;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.HashMap;
import java.util.Map;

public class HuaweiRewriteAuthorizationCodeRequestWrapper extends HttpServletRequestWrapper {
    private HttpServletRequest request;
    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request The request to wrap
     * @throws IllegalArgumentException if the request is null
     */
    public HuaweiRewriteAuthorizationCodeRequestWrapper(HttpServletRequest request) {
        super(request);
        this.request = request;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String,String[]> parameterMap = this.request.getParameterMap();
        String authorizationCode = this.request.getParameter("authorization_code");
        String[] authorizationCodeArray = new String[]{authorizationCode};
        Map<String, String[]> newDelegateMap = new HashMap<>(parameterMap);
        newDelegateMap.put(OAuth2ParameterNames.CODE, authorizationCodeArray);
        ParameterMap<String, String[]> newMap = new ParameterMap<>(newDelegateMap);
        newMap.setLocked(true);
        return newMap;
    }
}
