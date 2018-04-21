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

import java.util.function.UnaryOperator;

import org.jboss.as.clustering.controller.ChildResourceDefinition;
import org.jboss.as.clustering.controller.ResourceDescriptor;
import org.jboss.as.clustering.controller.ResourceServiceHandler;
import org.jboss.as.clustering.controller.SimpleResourceRegistration;
import org.jboss.as.clustering.controller.UnaryRequirementCapability;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.capability.RuntimeCapability;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.wildfly.clustering.discovery.spi.DiscoveryRequirement;
import org.wildfly.clustering.service.UnaryRequirement;

/**
 * Abstract resource definition of a discovery provider.
 * @author Paul Ferraro
 */
public abstract class DiscoveryProviderResourceDefinition extends ChildResourceDefinition<ManagementResourceRegistration> implements UnaryOperator<ResourceDescriptor> {

    enum Capability implements org.jboss.as.clustering.controller.Capability {
        DISCOVERY_PROVIDER(DiscoveryRequirement.DISCOVERY_PROVIDER),
        ;
        private final RuntimeCapability<Void> definition;

        Capability(UnaryRequirement requirement) {
            this.definition = new UnaryRequirementCapability(requirement).getDefinition();
        }

        @Override
        public RuntimeCapability<?> getDefinition() {
            return this.definition;
        }
    }

    private final ResourceServiceHandler handler;

    DiscoveryProviderResourceDefinition(PathElement path, ResourceServiceHandler handler) {
        super(path, DiscoveryExtension.SUBSYSTEM_RESOLVER.createChildResolver(path));
        this.handler = handler;
    }

    @Override
    public ManagementResourceRegistration register(ManagementResourceRegistration parent) {
        ManagementResourceRegistration registration = parent.registerSubModel(this);

        ResourceDescriptor descriptor = this.apply(new ResourceDescriptor(this.getResourceDescriptionResolver())).addCapabilities(Capability.class);
        new SimpleResourceRegistration(descriptor, this.handler).register(registration);

        return registration;
    }
}
