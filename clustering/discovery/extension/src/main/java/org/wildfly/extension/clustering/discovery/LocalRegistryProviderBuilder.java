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

package org.wildfly.extension.clustering.discovery;

import org.jboss.as.controller.PathAddress;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.Value;
import org.wildfly.discovery.impl.LocalRegistryAndDiscoveryProvider;
import org.wildfly.discovery.spi.RegistryProvider;

/**
 * Builds a service providing a local {@link RegistryProvider}.
 * @author Paul Ferraro
 */
public class LocalRegistryProviderBuilder extends RegistryProviderBuilder implements Value<RegistryProvider> {

    public LocalRegistryProviderBuilder(PathAddress address) {
        super(address);
    }

    @Override
    public Service<RegistryProvider> get() {
        return new ValueService<>(this);
    }

    @Override
    public RegistryProvider getValue() {
        return new LocalRegistryAndDiscoveryProvider();
    }
}
