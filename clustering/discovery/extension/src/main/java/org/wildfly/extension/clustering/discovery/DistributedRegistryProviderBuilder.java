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

import java.util.concurrent.Executor;
import java.util.function.Function;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.wildfly.clustering.discovery.ServiceProviderRegistryDiscoveryProvider;
import org.wildfly.clustering.provider.ServiceProviderRegistry;
import org.wildfly.clustering.service.CompositeDependency;
import org.wildfly.clustering.service.InjectedValueDependency;
import org.wildfly.clustering.service.MappedValueService;
import org.wildfly.clustering.service.ValueDependency;
import org.wildfly.clustering.spi.ClusteringCacheRequirement;
import org.wildfly.discovery.ServiceURL;
import org.wildfly.discovery.spi.RegistryProvider;

/**
 * Builds a service providing a distributed {@link RegistryProvider}.
 * @author Paul Ferraro
 */
public class DistributedRegistryProviderBuilder extends RegistryProviderBuilder implements Function<ServiceProviderRegistry<ServiceURL>, RegistryProvider> {

    private volatile ValueDependency<ServiceProviderRegistry<ServiceURL>> registry;
    private volatile ValueDependency<Executor> executor;

    public DistributedRegistryProviderBuilder(PathAddress address) {
        super(address);
        this.executor = new InjectedValueDependency<>(this.getExecutorServiceName(), Executor.class);
    }

    public ServiceName getExecutorServiceName() {
        return this.getServiceName().append("executor");
    }

    @SuppressWarnings("unchecked")
    @Override
    public DistributedRegistryProviderBuilder configure(OperationContext context, ModelNode model) throws OperationFailedException {
        String container = DistributedRegistryProviderResourceDefinition.Attribute.CACHE_CONTAINER.resolveModelAttribute(context, model).asString();
        String cache = DistributedRegistryProviderResourceDefinition.Attribute.CACHE.resolveModelAttribute(context, model).asStringOrNull();
        this.registry = new InjectedValueDependency<>(ClusteringCacheRequirement.SERVICE_PROVIDER_REGISTRY.getServiceName(context, container, cache), (Class<ServiceProviderRegistry<ServiceURL>>) (Class<?>) ServiceProviderRegistry.class);
        return this;
    }

    @Override
    public ServiceBuilder<RegistryProvider> build(ServiceTarget target) {
        return new CompositeDependency(this.registry, this.executor).register(super.build(target));
    }

    @Override
    public Service<RegistryProvider> get() {
        return new MappedValueService<>(this, this.registry);
    }

    @Override
    public ServiceProviderRegistryDiscoveryProvider apply(ServiceProviderRegistry<ServiceURL> registry) {
        return new ServiceProviderRegistryDiscoveryProvider(registry, this.executor.getValue());
    }
}
