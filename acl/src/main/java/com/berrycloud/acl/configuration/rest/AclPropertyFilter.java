/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.berrycloud.acl.configuration.rest;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

/**
 * A filter for adding an extra header to all incoming request. The purpose of this filter is to 'hack' the
 * dispatcherServer make it dispatch data-rest request to the modified (ACL compatible) controller methods.
 * 
 * @author István Rátkai (Selindek)
 */
public class AclPropertyFilter extends OncePerRequestFilter {
    public static final String ACL_PROPERTY_HEADER_NAME = "_aclProperty";
    public static final String ACL_PROPERTY_HEADER_VALUE = "true";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request) {
            @Override
            public String getHeader(String name) {
                if (ACL_PROPERTY_HEADER_NAME.equals(name)) {
                    return ACL_PROPERTY_HEADER_VALUE;
                }
                return super.getHeader(name);
            }
        };
        filterChain.doFilter(wrapper, response);

    }

}
