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

import org.jboss.as.clustering.controller.ResourceDescriptor;
import org.jboss.as.clustering.controller.SimpleResourceRegistration;
import org.jboss.as.clustering.controller.SubsystemResourceDefinition;
import org.jboss.as.controller.ModelVersion;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.operations.common.GenericSubsystemDescribeHandler;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.transform.description.ResourceTransformationDescriptionBuilder;
import org.jboss.as.controller.transform.description.TransformationDescription;
import org.jboss.as.controller.transform.description.TransformationDescriptionBuilder;

/**
 * Resource definition of the discovery subsystem.
 * @author Paul Ferraro
 */
public class DiscoveryResourceDefinition extends SubsystemResourceDefinition<SubsystemRegistration> {

    static final PathElement PATH = PathElement.pathElement(ModelDescriptionConstants.SUBSYSTEM, DiscoveryExtension.SUBSYSTEM_NAME);

    @SuppressWarnings("deprecation")
    static TransformationDescription buildTransformers(ModelVersion version) {
        ResourceTransformationDescriptionBuilder builder = TransformationDescriptionBuilder.Factory.createSubsystemInstance();

        StaticDiscoveryProviderResourceDefinition.buildTransformation(version, builder);
        LocalRegistryProviderResourceDefinition.buildTransformation(version, builder);
        AggregateDiscoveryProviderResourceDefinition.buildTransformation(version, builder);
        DistributedDiscoveryProviderResourceDefinition.buildTransformation(version, builder);
        DistributedRegistryProviderResourceDefinition.buildTransformation(version, builder);

        return builder.build();
    }

    DiscoveryResourceDefinition() {
        super(PATH, DiscoveryExtension.SUBSYSTEM_RESOLVER);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void register(SubsystemRegistration parent) {
        ManagementResourceRegistration registration = parent.registerSubsystemModel(this);

        registration.registerOperationHandler(GenericSubsystemDescribeHandler.DEFINITION, GenericSubsystemDescribeHandler.INSTANCE);

        ResourceDescriptor descriptor = new ResourceDescriptor(this.getResourceDescriptionResolver());
        new SimpleResourceRegistration(descriptor, new DiscoveryServiceHandler()).register(registration);

        new StaticDiscoveryProviderResourceDefinition().register(registration);
        new LocalRegistryProviderResourceDefinition().register(registration);
        new AggregateDiscoveryProviderResourceDefinition().register(registration);
        new DistributedDiscoveryProviderResourceDefinition().register(registration);
        new DistributedRegistryProviderResourceDefinition().register(registration);
    }
}
