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

package org.wildfly.clustering.discovery;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

import org.junit.Test;
import org.wildfly.clustering.dispatcher.CommandDispatcher;
import org.wildfly.clustering.group.Node;
import org.wildfly.discovery.AttributeValue;
import org.wildfly.discovery.FilterSpec;
import org.wildfly.discovery.ServiceType;
import org.wildfly.discovery.ServiceURL;
import org.wildfly.discovery.spi.DiscoveryProvider;
import org.wildfly.discovery.spi.DiscoveryResult;

/**
 * @author Paul Ferraro
 */
public class CommandDispatcherDiscoveryProviderTestCase {
    @Test
    public void discover() throws Exception {
        CommandDispatcher<DiscoveryProvider> dispatcher = mock(CommandDispatcher.class);
        DiscoveryProvider provider = new CommandDispatcherDiscoveryProvider(dispatcher);

        ServiceType type = ServiceType.of("type", "auth");
        FilterSpec filter = FilterSpec.equal("attr", "value");
        DiscoveryResult result = mock(DiscoveryResult.class);

        Queue<ServiceURL> services = new LinkedList<>();

        ServiceURL.Builder builder = new ServiceURL.Builder();
        builder.setUri(URI.create("java://uri"));
        builder.setAbstractType("type");
        builder.setAbstractTypeAuthority("auth");
        builder.addAttribute("attr", AttributeValue.fromString("value"));
        ServiceURL url = builder.create();
        services.add(url);

        Node member = mock(Node.class);

        when(dispatcher.executeOnGroup(any(DiscoveryCommand.class))).thenReturn(Collections.singletonMap(member, CompletableFuture.completedFuture(services)));

        provider.discover(type, filter, result);

        verify(result).addMatch(url);
    }
}
