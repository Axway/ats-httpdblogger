/*
 * Copyright 2017 Axway Software
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.axway.ats.httpdblogger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;

public class HttpSessionListener implements javax.servlet.http.HttpSessionListener {

    @Override
    public void sessionCreated(
                                final HttpSessionEvent se ) {

        HttpSession session = se.getSession();
        ServletContext context = session.getServletContext();
        context.setAttribute(session.getId(), session);
    }

    @Override
    public void sessionDestroyed(
                                  final HttpSessionEvent se ) {

        HttpSession session = se.getSession();
        ServletContext context = session.getServletContext();
        context.removeAttribute(session.getId());
    }

}
