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

import java.util.ListIterator;

import org.jboss.as.clustering.controller.ChildResourceDefinition;
import org.jboss.as.controller.AttributeMarshaller;
import org.jboss.as.controller.AttributeParser;
import org.jboss.as.controller.ModelVersion;
import org.jboss.as.controller.ObjectListAttributeDefinition;
import org.jboss.as.controller.ObjectTypeAttributeDefinition;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleOperationDefinitionBuilder;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.OperationEntry;
import org.jboss.as.controller.transform.description.ResourceTransformationDescriptionBuilder;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * Resource definition of a static provider.
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 * @author Paul Ferraro
 * @deprecated Replaced by {@link LocalRegistryProviderResourceDefinition}.
 */
@Deprecated
public class StaticDiscoveryProviderResourceDefinition extends ChildResourceDefinition<ManagementResourceRegistration> {

    static final String PATH_KEY = "static-provider";
    static final PathElement WILDCARD_PATH = pathElement(PathElement.WILDCARD_VALUE);
    static final PathElement pathElement(String name) {
        return PathElement.pathElement(PATH_KEY, name);
    }

    static final SimpleAttributeDefinition URI = new SimpleAttributeDefinitionBuilder(ModelDescriptionConstants.URI, ModelType.STRING, false).setAllowExpression(false).build();

    static final SimpleAttributeDefinition NAME = new SimpleAttributeDefinitionBuilder(ModelDescriptionConstants.NAME, ModelType.STRING, false).setAllowExpression(true).build();
    static final SimpleAttributeDefinition VALUE = new SimpleAttributeDefinitionBuilder(ModelDescriptionConstants.VALUE, ModelType.STRING, true).setAllowExpression(true).build();

    static final ObjectTypeAttributeDefinition ATTRIBUTE = new ObjectTypeAttributeDefinition.Builder(ModelDescriptionConstants.ATTRIBUTE, NAME, VALUE).build();

    static final ObjectListAttributeDefinition ATTRIBUTES = new ObjectListAttributeDefinition.Builder(ModelDescriptionConstants.ATTRIBUTES, ATTRIBUTE)
            .setAttributeMarshaller(AttributeMarshaller.UNWRAPPED_OBJECT_LIST_MARSHALLER)
            .setAttributeParser(AttributeParser.UNWRAPPED_OBJECT_LIST_PARSER)
            .build();

    static final ObjectTypeAttributeDefinition SERVICE = new ObjectTypeAttributeDefinition.Builder(ServiceResourceDefinition.WILDCARD_PATH.getKey(),
        ServiceResourceDefinition.Attribute.ABSTRACT_TYPE.getDefinition(),
        ServiceResourceDefinition.Attribute.ABSTRACT_TYPE_AUTHORITY.getDefinition(),
        ServiceResourceDefinition.Attribute.URI.getDefinition(),
        ServiceResourceDefinition.Attribute.URI_SCHEME_AUTHORITY.getDefinition(),
        ATTRIBUTES
    ).build();

    static final ObjectListAttributeDefinition SERVICES = new ObjectListAttributeDefinition.Builder("services", SERVICE).build();

    static void buildTransformation(ModelVersion version, ResourceTransformationDescriptionBuilder parent) {
        parent.discardChildResource(WILDCARD_PATH);
    }

    StaticDiscoveryProviderResourceDefinition() {
        super(WILDCARD_PATH, DiscoveryExtension.SUBSYSTEM_RESOLVER.createChildResolver(WILDCARD_PATH));
        this.setDeprecated(DiscoveryModel.VERSION_2_0_0.getVersion());
    }

    @Override
    public ManagementResourceRegistration register(ManagementResourceRegistration parent) {
        ManagementResourceRegistration registration = parent.registerSubModel(this);

        // Delegate add operation to local-provider
        OperationStepHandler addHandler = new OperationStepHandler() {
            @Override
            public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
                SERVICES.validateOperation(operation);

                // Add placeholder resource
//                context.addResource(PathAddress.EMPTY_ADDRESS, PlaceholderResource.INSTANCE);

                PathAddress address = context.getCurrentAddress();
                PathAddress providerAddress = address.getParent().append(LocalRegistryProviderResourceDefinition.pathElement(address.getLastElement().getValue()));
                ModelNode providerOperation = Util.createAddOperation(providerAddress);
                context.addStep(providerOperation, context.getRootResourceRegistration().getOperationHandler(providerAddress, ModelDescriptionConstants.ADD), OperationContext.Stage.MODEL);
                if (operation.hasDefined(SERVICES.getName())) {
                    ListIterator<ModelNode> services = operation.get(SERVICES.getName()).asList().listIterator();
                    while (services.hasNext()) {
                        // Generate a name for the service resource
                        String name = "service" + services.nextIndex();
                        ModelNode service = services.next();
                        PathAddress serviceAddress = providerAddress.append(ServiceResourceDefinition.pathElement(name));
                        ModelNode serviceOperation = Util.createAddOperation(serviceAddress);
                        serviceOperation.get(ServiceResourceDefinition.Attribute.URI.getName()).set(service.get(ServiceResourceDefinition.Attribute.URI.getName()));
                        if (service.hasDefined(ServiceResourceDefinition.Attribute.ABSTRACT_TYPE.getName())) {
                            serviceOperation.get(ServiceResourceDefinition.Attribute.ABSTRACT_TYPE.getName()).set(service.get(ServiceResourceDefinition.Attribute.ABSTRACT_TYPE.getName()));
                        }
                        if (service.hasDefined(ServiceResourceDefinition.Attribute.ABSTRACT_TYPE_AUTHORITY.getName())) {
                            serviceOperation.get(ServiceResourceDefinition.Attribute.ABSTRACT_TYPE_AUTHORITY.getName()).set(service.get(ServiceResourceDefinition.Attribute.ABSTRACT_TYPE_AUTHORITY.getName()));
                        }
                        if (service.hasDefined(ServiceResourceDefinition.Attribute.URI_SCHEME_AUTHORITY.getName())) {
                            serviceOperation.get(ServiceResourceDefinition.Attribute.URI_SCHEME_AUTHORITY.getName()).set(service.get(ServiceResourceDefinition.Attribute.URI_SCHEME_AUTHORITY.getName()));
                        }
                        if (service.hasDefined(ServiceResourceDefinition.Attribute.ATTRIBUTES.getName())) {
                            for (ModelNode attribute : service.get(ServiceResourceDefinition.Attribute.ATTRIBUTES.getName()).asList()) {
                                String attributeName = attribute.get(NAME.getName()).asString();
                                ModelNode attributeValue = attribute.get(VALUE.getName());
                                serviceOperation.get(ServiceResourceDefinition.Attribute.ATTRIBUTES.getName()).add(attributeName, attributeValue);
                            }
                        }
                        context.addStep(serviceOperation, context.getRootResourceRegistration().getOperationHandler(serviceAddress, ModelDescriptionConstants.ADD), OperationContext.Stage.MODEL);
                    }
                }
            }
        };
        registration.registerOperationHandler(new SimpleOperationDefinitionBuilder(ModelDescriptionConstants.ADD, this.getResourceDescriptionResolver()).addParameter(SERVICES).withFlag(OperationEntry.Flag.RESTART_NONE).build(), addHandler);

        // Delegate remove operation to local-provider
        OperationStepHandler removeHandler = new OperationStepHandler() {
            @Override
            public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
//                context.removeResource(PathAddress.EMPTY_ADDRESS);

                PathAddress address = context.getCurrentAddress();
                PathAddress providerAddress = address.getParent().append(LocalRegistryProviderResourceDefinition.pathElement(address.getLastElement().getValue()));
                ModelNode providerOperation = Util.createRemoveOperation(providerAddress);
                context.addStep(providerOperation, context.getRootResourceRegistration().getOperationHandler(providerAddress, ModelDescriptionConstants.REMOVE), OperationContext.Stage.MODEL);
            }
        };
        registration.registerOperationHandler(new SimpleOperationDefinitionBuilder(ModelDescriptionConstants.REMOVE, this.getResourceDescriptionResolver()).withFlag(OperationEntry.Flag.RESTART_NONE).build(), removeHandler);

        // Delegate read-attribute operation to child service resources
        OperationStepHandler readHandler = new OperationStepHandler() {
            @Override
            public void execute(OperationContext context, ModelNode operation) {
            }
        };
        // Delegate write-attribute operation to child service resources
        OperationStepHandler writeHandler = new OperationStepHandler() {
            @Override
            public void execute(OperationContext context, ModelNode operation) {
            }
        };
        registration.registerReadWriteAttribute(SERVICES, readHandler, writeHandler);

        return registration;
    }
}