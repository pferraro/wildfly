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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.jboss.as.clustering.controller.ResourceServiceBuilder;
import org.jboss.as.clustering.dmr.ModelNodes;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.Value;
import org.wildfly.clustering.discovery.spi.DiscoveryRequirement;
import org.wildfly.clustering.service.Builder;
import org.wildfly.clustering.service.Dependency;
import org.wildfly.clustering.service.InjectedValueDependency;
import org.wildfly.clustering.service.ValueDependency;
import org.wildfly.discovery.impl.AggregateDiscoveryProvider;
import org.wildfly.discovery.spi.DiscoveryProvider;

/**
 * Builds a service providing an {@link AggregateDiscoveryProvider}.
 * @author Paul Ferraro
 */
public class AggregateDiscoveryProviderBuilder extends DiscoveryProviderServiceNameProvider implements ResourceServiceBuilder<DiscoveryProvider>, Value<DiscoveryProvider> {

    private volatile List<ValueDependency<DiscoveryProvider>> providers;

    public AggregateDiscoveryProviderBuilder(PathAddress address) {
        super(address);
    }

    @Override
    public Builder<DiscoveryProvider> configure(OperationContext context, ModelNode model) throws OperationFailedException {
        List<ModelNode> providers = ModelNodes.optionalList(AggregateDiscoveryProviderResourceDefinition.Attribute.PROVIDERS.resolveModelAttribute(context, model)).orElse(Collections.emptyList());
        this.providers = new ArrayList<>(providers.size());
        for (ModelNode provider : providers) {
            this.providers.add(new InjectedValueDependency<>(DiscoveryRequirement.DISCOVERY_PROVIDER.getServiceName(context, provider.asString()), DiscoveryProvider.class));
        }
        return this;
    }

    @Override
    public ServiceBuilder<DiscoveryProvider> build(ServiceTarget target) {
        ServiceBuilder<DiscoveryProvider> builder = target.addService(this.getServiceName(), new ValueService<>(this));
        for (Dependency dependency : this.providers) {
            dependency.register(builder);
        }
        return builder.setInitialMode(ServiceController.Mode.ON_DEMAND);
    }

    @Override
    public DiscoveryProvider getValue() {
        DiscoveryProvider[] providers = new DiscoveryProvider[this.providers.size()];
        ListIterator<ValueDependency<DiscoveryProvider>> dependencies = this.providers.listIterator();
        while (dependencies.hasNext()) {
            int index = dependencies.nextIndex();
            providers[index] = dependencies.next().getValue();
        }
        return new AggregateDiscoveryProvider(providers);
    }
}
