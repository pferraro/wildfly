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

import java.net.URI;
import java.util.Collections;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jboss.as.clustering.controller.ResourceServiceBuilder;
import org.jboss.as.clustering.dmr.ModelNodes;
import org.jboss.as.clustering.function.Consumers;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.wildfly.clustering.service.Builder;
import org.wildfly.clustering.service.InjectedValueDependency;
import org.wildfly.clustering.service.SuppliedValueService;
import org.wildfly.clustering.service.ValueDependency;
import org.wildfly.discovery.AttributeValue;
import org.wildfly.discovery.ServiceRegistration;
import org.wildfly.discovery.ServiceURL;
import org.wildfly.discovery.spi.RegistryProvider;

/**
 * Builds a service providing a {@link ServiceRegistration}.
 * @author Paul Ferraro
 */
public class ServiceRegistrationBuilder implements ResourceServiceBuilder<ServiceRegistration>, Supplier<ServiceRegistration> {

    private final ServiceName name;
    private final ValueDependency<RegistryProvider> provider;

    private volatile ServiceURL url;

    public ServiceRegistrationBuilder(PathAddress address) {
        String name = address.getLastElement().getValue();
        ServiceName registryProviderServiceName = RegistryProviderResourceDefinition.Capability.REGISTRY_PROVIDER.getServiceName(address.getParent());
        this.name = registryProviderServiceName.append(name);
        this.provider = new InjectedValueDependency<>(registryProviderServiceName, RegistryProvider.class);
    }

    @Override
    public ServiceName getServiceName() {
        return this.name;
    }

    @Override
    public Builder<ServiceRegistration> configure(OperationContext context, ModelNode model) throws OperationFailedException {
        String uri = ServiceResourceDefinition.Attribute.URI.resolveModelAttribute(context, model).asString();
        ServiceURL.Builder builder = new ServiceURL.Builder().setUri(URI.create(uri));
        String uriSchemeAuthority = ServiceResourceDefinition.Attribute.URI_SCHEME_AUTHORITY.resolveModelAttribute(context, model).asStringOrNull();
        if (uriSchemeAuthority != null) {
            builder.setUriSchemeAuthority(uriSchemeAuthority);
        }
        String abstractType = ServiceResourceDefinition.Attribute.ABSTRACT_TYPE.resolveModelAttribute(context, model).asStringOrNull();
        if (abstractType != null) {
            builder.setAbstractType(abstractType);
        }
        String abstractTypeAuthority = ServiceResourceDefinition.Attribute.ABSTRACT_TYPE_AUTHORITY.resolveModelAttribute(context, model).asStringOrNull();
        if (abstractTypeAuthority != null) {
            builder.setAbstractTypeAuthority(abstractTypeAuthority);
        }
        for (Property property : ModelNodes.optionalPropertyList(ServiceResourceDefinition.Attribute.ATTRIBUTES.resolveModelAttribute(context, model)).orElse(Collections.emptyList())) {
            builder.addAttribute(property.getName(), AttributeValue.fromString(property.getValue().asString()));
        }
        this.url = builder.create();
        return this;
    }

    @Override
    public ServiceBuilder<ServiceRegistration> build(ServiceTarget target) {
        Service<ServiceRegistration> service = new SuppliedValueService<>(Function.identity(), this, Consumers.close());
        return this.provider.register(target.addService(this.getServiceName(), service).setInitialMode(ServiceController.Mode.PASSIVE));
    }

    @Override
    public ServiceRegistration get() {
        return this.provider.getValue().registerService(this.url);
    }
}
