/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.view.filter;

import com.cghislai.organiseurilesdepaix.view.Views;
import com.cghislai.organiseurilesdepaix.view.control.AuthController;
import java.io.IOException;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author cghislai
 */
@WebFilter(urlPatterns = {"/tools/*", "/user/*"})
public class LoginFilter implements Filter {

    @Inject
    private AuthController authController;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        String requestURI = httpServletRequest.getRequestURI();
        String contextPath = httpServletRequest.getContextPath();
        requestURI = requestURI.replace(contextPath, "");

        if (authController == null) {
            chain.doFilter(request, response);
            return;
        }
        if (requestURI.startsWith("/user/")) {
            if (!requestURI.startsWith("/user/login")
                    && !requestURI.startsWith("/user/register")) {
                if (!authController.isUserAuthenticated()) {
                    httpServletResponse.sendRedirect(contextPath + Views.INDEX);
                    return;
                }
            }
        } else if (requestURI.startsWith("/tools/")) {
            if (!authController.isAdminAuthenticated()) {
                httpServletResponse.sendRedirect(contextPath + Views.INDEX);
                return;
            }
        }
        chain.doFilter(request, response);

    }

    @Override
    public void init(FilterConfig config) throws ServletException {
        // Nothing to do here!
    }

    @Override
    public void destroy() {
        // Nothing to do here!
    }

}
