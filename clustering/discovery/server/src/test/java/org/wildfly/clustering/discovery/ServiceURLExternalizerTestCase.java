/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
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

package org.wildfly.clustering.discovery;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;

import org.junit.Test;
import org.wildfly.clustering.marshalling.ExternalizerTester;
import org.wildfly.discovery.AttributeValue;
import org.wildfly.discovery.ServiceURL;

/**
 * @author Paul Ferraro
 */
public class ServiceURLExternalizerTestCase {

    @Test
    public void test() throws ClassNotFoundException, IOException {
        ServiceURL min = new ServiceURL.Builder()
                .setUri(URI.create("http://localhost:8080"))
                .create();

        ServiceURL max = new ServiceURL.Builder()
                .setAbstractType("ejb")
                .setAbstractTypeAuthority("jboss")
                .setUri(URI.create("http://localhost:8080"))
                .setUriSchemeAuthority("test")
                .addAttribute("attr")
                .addAttribute("attr", AttributeValue.fromBytes(InetAddress.getLoopbackAddress().getAddress()))
                .addAttribute("attr", AttributeValue.fromInt(100))
                .addAttribute("attr", AttributeValue.fromString("test"))
                .addAttribute("attr", AttributeValue.TRUE)
                .create();

        ExternalizerTester<ServiceURL> tester = new ExternalizerTester<>(new ServiceURLExternalizer());
        tester.test(min);
        tester.test(max);
    }
}
