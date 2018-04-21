/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2018, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.test.clustering.cluster.discovery.servlet;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.as.server.CurrentServiceContainer;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.wildfly.clustering.discovery.spi.DiscoveryRequirement;
import org.wildfly.clustering.service.UnaryRequirement;
import org.wildfly.discovery.FilterSpec;
import org.wildfly.discovery.ServiceRegistration;
import org.wildfly.discovery.ServiceType;
import org.wildfly.discovery.ServiceURL;
import org.wildfly.discovery.spi.DiscoveryProvider;
import org.wildfly.discovery.spi.DiscoveryRequest;
import org.wildfly.discovery.spi.DiscoveryResult;
import org.wildfly.discovery.spi.RegistryProvider;

/**
 * @author Paul Ferraro
 */
@WebServlet(urlPatterns = { DiscoveryServlet.SERVLET_PATH })
public class DiscoveryServlet extends HttpServlet {
    private static final long serialVersionUID = -5077040370950193998L;

    public static final String SERVICE_URI = "uri";
    private static final String SERVLET_NAME = "discovery";
    static final String SERVLET_PATH = "/" + SERVLET_NAME;
    private static final String PROVIDER = "provider";
    private static final String ABSTRACT_TYPE = "servlet";

    public static URI createURI(URL baseURL, String provider) throws URISyntaxException {
        return baseURL.toURI().resolve(buildURI(provider).toString());
    }

    public static URI createURI(URL baseURL, String provider, URL serviceURL) throws URISyntaxException {
        return baseURL.toURI().resolve(appendParameter(buildURI(provider), SERVICE_URI, serviceURL.toURI().toString()).toString());
    }

    private static StringBuilder buildURI(String provider) {
        return new StringBuilder(SERVLET_NAME).append('?').append(PROVIDER).append('=').append(provider);
    }

    private static StringBuilder appendParameter(StringBuilder builder, String parameter, String value) {
        return builder.append('&').append(parameter).append('=').append(value);
    }

    @Override
    public void destroy() {
        System.out.println("Closing registrations: " + Collections.list(this.getServletContext().getAttributeNames()).stream().filter(name -> name.startsWith(SERVLET_NAME)).collect(Collectors.toList()));
        Collections.list(this.getServletContext().getAttributeNames()).stream().filter(name -> name.startsWith(SERVLET_NAME)).map(name -> (ServiceRegistration) this.getServletContext().getAttribute(name)).forEach(ServiceRegistration::close);
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String provider = request.getParameter(PROVIDER);
        if (provider == null) {
            throw new ServletException("Missing required parameter: " + PROVIDER);
        }
        String uri = request.getParameter(SERVICE_URI);
        UnaryRequirement requirement = (uri != null) ? DiscoveryRequirement.REGISTRY_PROVIDER : DiscoveryRequirement.DISCOVERY_PROVIDER;
        ServiceName serviceName = ServiceName.parse(requirement.resolve(provider));
        ServiceController<?> controller = CurrentServiceContainer.getServiceContainer().getService(serviceName);
        if (controller == null) {
            throw new ServletException(serviceName + " not found");
        }
        if (uri != null) {
            RegistryProvider registry = (RegistryProvider) controller.getValue();
            ServiceURL url = new ServiceURL.Builder().setAbstractType(ABSTRACT_TYPE).setUri(URI.create(uri)).create();
            System.out.println("Adding " + url + " to " + provider + " registry");
            ServiceRegistration registration = registry.registerService(url);
            this.getServletContext().setAttribute(String.join("-", SERVLET_NAME, uri), registration);
        } else {
            System.out.println("Querying services from " + provider + " provider");
            DiscoveryProvider discovery = (DiscoveryProvider) controller.getValue();
            DiscoveryResult result = new ServletDiscoveryResult(this.getServletContext(), response);
            synchronized (result) {
                DiscoveryRequest discoveryRequest = discovery.discover(ServiceType.of(ABSTRACT_TYPE, null), FilterSpec.all(), result);
                try {
                    result.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    discoveryRequest.cancel();
                }
            }
        }
    }

    private static class ServletDiscoveryResult implements DiscoveryResult {
        private final ServletContext context;
        private final HttpServletResponse response;

        ServletDiscoveryResult(ServletContext context, HttpServletResponse response) {
            this.context = context;
            this.response = response;
        }

        @Override
        public synchronized void complete() {
            this.notify();
        }

        @Override
        public void reportProblem(Throwable exception) {
            this.context.log(exception.getMessage(), exception);
        }

        @Override
        public void addMatch(ServiceURL url) {
            this.response.addHeader(SERVICE_URI, url.getLocationURI().toString());
        }
    }
}
