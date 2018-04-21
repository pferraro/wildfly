/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat, Inc., and individual contributors
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

import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.wildfly.clustering.provider.ServiceProviderRegistration;
import org.wildfly.clustering.provider.ServiceProviderRegistry;
import org.wildfly.discovery.AttributeValue;
import org.wildfly.discovery.FilterSpec;
import org.wildfly.discovery.ServiceRegistration;
import org.wildfly.discovery.ServiceType;
import org.wildfly.discovery.ServiceURL;
import org.wildfly.discovery.spi.DiscoveryProvider;
import org.wildfly.discovery.spi.DiscoveryRequest;
import org.wildfly.discovery.spi.DiscoveryResult;
import org.wildfly.discovery.spi.RegistryProvider;

/**
 * Unit test for {@link AsynchronousDiscoveryProvider}.
 * @author Paul Ferraro
 */
public class ServiceProviderRegistryDiscoveryProviderTestCase {
    @Test
    public void discover() throws Exception {
        ServiceProviderRegistry<ServiceURL> registry = mock(ServiceProviderRegistry.class);
        DiscoveryProvider provider = new ServiceProviderRegistryDiscoveryProvider(registry, task -> task.run());

        ServiceType type = ServiceType.of("type", "auth");
        FilterSpec filter = FilterSpec.equal("attr", "value");
        DiscoveryResult result = mock(DiscoveryResult.class);

        Set<ServiceURL> services = new HashSet<>();

        ServiceURL.Builder builder = new ServiceURL.Builder();
        builder.setUri(URI.create("java://uri0"));
        builder.setAbstractType("type");
        builder.setAbstractTypeAuthority("auth");
        builder.addAttribute("attr", AttributeValue.fromString("value"));
        ServiceURL url0 = builder.create();
        services.add(url0);

        builder = new ServiceURL.Builder();
        builder.setUri(URI.create("java://uri1"));
        builder.setAbstractType("type");
        builder.setAbstractTypeAuthority("auth");
        builder.addAttribute("attr", AttributeValue.fromString("other-value"));
        ServiceURL url1 = builder.create();
        services.add(url1);

        builder = new ServiceURL.Builder();
        builder.setUri(URI.create("java://uri2"));
        builder.setAbstractType("type");
        builder.setAbstractTypeAuthority("other-auth");
        builder.addAttribute("attr", AttributeValue.fromString("other-value"));
        ServiceURL url2 = builder.create();
        services.add(url2);

        builder = new ServiceURL.Builder();
        builder.setUri(URI.create("java://uri3"));
        builder.setAbstractType("other-type");
        builder.setAbstractTypeAuthority("auth");
        ServiceURL url3 = builder.create();
        services.add(url3);

        when(registry.getServices()).thenReturn(services);

        DiscoveryRequest request = provider.discover(type, filter, result);

        Assert.assertNotNull(request);

        verify(result).addMatch(url0);
        verify(result, never()).addMatch(url1);
        verify(result, never()).addMatch(url2);
        verify(result, never()).addMatch(url3);

        reset(result);

        request = provider.discover(type, null, result);

        Assert.assertNotNull(request);

        verify(result).addMatch(url0);
        verify(result).addMatch(url1);
        verify(result, never()).addMatch(url2);
        verify(result, never()).addMatch(url3);
    }

    @Test
    public void register() throws Exception {
        ServiceProviderRegistry<ServiceURL> registry = mock(ServiceProviderRegistry.class);
        RegistryProvider provider = new ServiceProviderRegistryDiscoveryProvider(registry, task -> task.run());

        ServiceURL.Builder builder = new ServiceURL.Builder();
        builder.setUri(URI.create("java://uri"));
        builder.setAbstractType("type");
        builder.setAbstractTypeAuthority("auth");
        builder.addAttribute("attr", AttributeValue.fromString("value"));
        ServiceURL url = builder.create();

        ServiceProviderRegistration<ServiceURL> registration = mock(ServiceProviderRegistration.class);

        when(registry.register(url)).thenReturn(registration);

        try (ServiceRegistration result = provider.registerService(url)) {
            Assert.assertNotNull(result);
        }

        verify(registration).close();
    }
}
