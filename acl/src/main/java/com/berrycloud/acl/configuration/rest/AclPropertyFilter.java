package com.berrycloud.acl.configuration.rest;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

public class AclPropertyFilter extends OncePerRequestFilter{
	public static final String ACL_PROPERTY_HEADER_NAME="_aclProperty";
	public static final String ACL_PROPERTY_HEADER_VALUE="true";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		 HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request) {
		        @Override
		        public String getHeader(String name) {
		            if(ACL_PROPERTY_HEADER_NAME.equals(name)) {
		            	return ACL_PROPERTY_HEADER_VALUE;
		            }
		            return super.getHeader(name);
		        }
		    };
		    filterChain.doFilter(wrapper, response);
		
	}

}
