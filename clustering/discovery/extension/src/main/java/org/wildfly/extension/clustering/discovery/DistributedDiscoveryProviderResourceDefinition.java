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

package org.wildfly.extension.clustering.discovery;

import java.util.function.UnaryOperator;

import org.jboss.as.clustering.controller.CapabilityReference;
import org.jboss.as.clustering.controller.ResourceCapabilityReference;
import org.jboss.as.clustering.controller.ResourceDescriptor;
import org.jboss.as.clustering.controller.SimpleResourceServiceHandler;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.ModelVersion;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.as.controller.transform.description.ResourceTransformationDescriptionBuilder;
import org.jboss.dmr.ModelType;
import org.wildfly.clustering.discovery.spi.DiscoveryRequirement;
import org.wildfly.clustering.spi.ClusteringDefaultRequirement;
import org.wildfly.clustering.spi.ClusteringRequirement;

/**
 * Resource definition of a distributed discovery provider.
 * @author Paul Ferraro
 */
public class DistributedDiscoveryProviderResourceDefinition extends DiscoveryProviderResourceDefinition {

    static final String PATH_KEY = "distributed-provider";
    static final PathElement WILDCARD_PATH = pathElement(PathElement.WILDCARD_VALUE);
    static PathElement pathElement(String name) {
        return PathElement.pathElement(PATH_KEY, name);
    }

    enum Attribute implements org.jboss.as.clustering.controller.Attribute, UnaryOperator<SimpleAttributeDefinitionBuilder> {
        CHANNEL("channel", ModelType.STRING) {
            @Override
            public SimpleAttributeDefinitionBuilder apply(SimpleAttributeDefinitionBuilder builder) {
                return builder.setRequired(false)
                        .setCapabilityReference(new CapabilityReference(Capability.DISCOVERY_PROVIDER, ClusteringRequirement.COMMAND_DISPATCHER_FACTORY))
                        ;
            }
        },
        PROVIDER("provider", ModelType.STRING) {
            @Override
            public SimpleAttributeDefinitionBuilder apply(SimpleAttributeDefinitionBuilder builder) {
                return builder.setRequired(true)
                        .setCapabilityReference(new CapabilityReference(Capability.DISCOVERY_PROVIDER, DiscoveryRequirement.DISCOVERY_PROVIDER))
                        ;
            }
        },
        ;
        private final AttributeDefinition definition;

        Attribute(String name, ModelType type) {
            this.definition = this.apply(new SimpleAttributeDefinitionBuilder(name, type)
                    .setAllowExpression(false)
                    .setFlags(AttributeAccess.Flag.RESTART_RESOURCE_SERVICES)
                    ).build();
        }

        @Override
        public AttributeDefinition getDefinition() {
            return this.definition;
        }
    }

    static void buildTransformation(ModelVersion version, ResourceTransformationDescriptionBuilder parent) {
        if (DiscoveryModel.VERSION_2_0_0.requiresTransformation(version)) {
            parent.discardChildResource(WILDCARD_PATH);
        }
    }

    DistributedDiscoveryProviderResourceDefinition() {
        super(WILDCARD_PATH, new SimpleResourceServiceHandler<>(DistributedDiscoveryProviderBuilder::new));
    }

    @Override
    public ResourceDescriptor apply(ResourceDescriptor descriptor) {
        return descriptor.addAttributes(Attribute.class).addResourceCapabilityReference(new ResourceCapabilityReference(Capability.DISCOVERY_PROVIDER, ClusteringDefaultRequirement.COMMAND_DISPATCHER_FACTORY));
    }
}
