package com.ruoyi.im.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;

/**
 * 璁よ瘉鍏ュ彛鐐? * 
 * 褰撴湭璁よ瘉鐢ㄦ埛灏濊瘯璁块棶鍙椾繚鎶よ祫婧愭椂鐨勫鐞? * 
 * @author ruoyi
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint, Serializable {

    private static final long serialVersionUID = -8970718410437077606L;

    @Override
    public void commence(HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException {
        // 杩斿洖401鏈巿鏉冪姸鎬?        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }
}
