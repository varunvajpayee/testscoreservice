package com.smodelware.smartcfa.util;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CORSFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("Filter initiated");
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;

        System.out.println("Request: " + request.getMethod());

        // Just ACCEPT and REPLY OK if OPTIONS
        HttpServletResponse resp = (HttpServletResponse) servletResponse;
        System.out.print(request.getHeader("Access-Control-Allow-Origin"));
        if(request.getHeader("Origin") != null && (request.getHeader("Origin").equals("http://localhost") ||request.getHeader("Origin").equals("http://localhost:4200") ||request.getHeader("Origin").equals("http://localhost:1841") || request.getHeader("Origin").equals("https://testscoreservice.appspot.com") || request.getHeader("Origin").equals("https://brightanalyst.com")|| request.getHeader("Origin").equals("https://www.brightanalyst.com")))
        {
            resp.addHeader("Access-Control-Allow-Origin",request.getHeader("Origin"));
        }

        resp.addHeader("Access-Control-Allow-Methods","GET,POST");
        resp.addHeader("Access-Control-Allow-Headers","Origin, X-Requested-With, Content-Type, Accept, 0");
        resp.addHeader("Access-Control-Allow-Credentials","true");

        if ( request.getMethod().equals("OPTIONS") ) {
            resp.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        chain.doFilter(request, servletResponse);
    }

    @Override
    public void destroy() {}
}
