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

package org.wildfly.extension.clustering.discovery;

import java.util.function.Supplier;

import org.jboss.as.clustering.controller.ResourceServiceBuilder;
import org.jboss.as.clustering.function.Consumers;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.wildfly.clustering.discovery.CommandDispatcherDiscoveryProvider;
import org.wildfly.clustering.discovery.spi.DiscoveryRequirement;
import org.wildfly.clustering.dispatcher.CommandDispatcher;
import org.wildfly.clustering.dispatcher.CommandDispatcherFactory;
import org.wildfly.clustering.service.Builder;
import org.wildfly.clustering.service.CompositeDependency;
import org.wildfly.clustering.service.InjectedValueDependency;
import org.wildfly.clustering.service.SuppliedValueService;
import org.wildfly.clustering.service.ValueDependency;
import org.wildfly.clustering.spi.ClusteringRequirement;
import org.wildfly.discovery.spi.DiscoveryProvider;

/**
 * Builds a service providing an {@link RegistryProvider}.
 * @author Paul Ferraro
 */
public class DistributedDiscoveryProviderBuilder implements ResourceServiceBuilder<DiscoveryProvider>, Supplier<CommandDispatcher<DiscoveryProvider>> {

    private final ServiceName name;

    private volatile ValueDependency<DiscoveryProvider> provider;
    private volatile ValueDependency<CommandDispatcherFactory> dispatcherFactory;

    public DistributedDiscoveryProviderBuilder(PathAddress address) {
        this.name = DistributedDiscoveryProviderResourceDefinition.Capability.DISCOVERY_PROVIDER.getServiceName(address);
    }

    @Override
    public ServiceName getServiceName() {
        return this.name;
    }

    @Override
    public Builder<DiscoveryProvider> configure(OperationContext context, ModelNode model) throws OperationFailedException {
        String channel = DistributedDiscoveryProviderResourceDefinition.Attribute.CHANNEL.resolveModelAttribute(context, model).asStringOrNull();
        this.dispatcherFactory = new InjectedValueDependency<>(ClusteringRequirement.COMMAND_DISPATCHER_FACTORY.getServiceName(context, channel), CommandDispatcherFactory.class);
        String provider = DistributedDiscoveryProviderResourceDefinition.Attribute.PROVIDER.resolveModelAttribute(context, model).asString();
        this.provider = new InjectedValueDependency<>(DiscoveryRequirement.DISCOVERY_PROVIDER.getServiceName(context, provider), DiscoveryProvider.class);
        return this;
    }

    @Override
    public ServiceBuilder<DiscoveryProvider> build(ServiceTarget target) {
        Service<DiscoveryProvider> service = new SuppliedValueService<>(CommandDispatcherDiscoveryProvider::new, this, Consumers.close());
        ServiceBuilder<DiscoveryProvider> builder = target.addService(this.name, service).setInitialMode(ServiceController.Mode.ON_DEMAND);
        return new CompositeDependency(this.provider, this.dispatcherFactory).register(builder);
    }

    @Override
    public CommandDispatcher<DiscoveryProvider> get() {
        return this.dispatcherFactory.getValue().createCommandDispatcher(this.getServiceName(), this.provider.getValue());
    }
}
