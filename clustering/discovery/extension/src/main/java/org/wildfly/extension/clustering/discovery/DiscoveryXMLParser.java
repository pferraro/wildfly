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

import static org.jboss.as.controller.PersistentResourceXMLDescription.builder;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import org.jboss.as.clustering.controller.Attribute;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.AttributeMarshaller;
import org.jboss.as.controller.AttributeParser;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.PersistentResourceXMLDescription;
import org.jboss.as.controller.PersistentResourceXMLDescription.PersistentResourceXMLBuilder;
import org.jboss.as.controller.PersistentResourceXMLParser;

/**
 * @author Paul Ferraro
 */
public class DiscoveryXMLParser extends PersistentResourceXMLParser {

    private final DiscoverySchema schema;

    public DiscoveryXMLParser(DiscoverySchema schema) {
        this.schema = schema;
    }

    @SuppressWarnings("deprecation")
    @Override
    public PersistentResourceXMLDescription getParserDescription() {
        PersistentResourceXMLBuilder builder = builder(DiscoveryResourceDefinition.PATH, DiscoverySchema.CURRENT.getNamespaceUri());

        if (this.schema.since(DiscoverySchema.VERSION_2_0)) {
            builder.addChild(registryBuilder(LocalRegistryProviderResourceDefinition.WILDCARD_PATH));
        } else {
            builder.addChild(builder(StaticDiscoveryProviderResourceDefinition.WILDCARD_PATH).addAttribute(StaticDiscoveryProviderResourceDefinition.SERVICES, AttributeParser.UNWRAPPED_OBJECT_LIST_PARSER, AttributeMarshaller.UNWRAPPED_OBJECT_LIST_MARSHALLER));
        }

        builder.addChild(discoveryBuilder(AggregateDiscoveryProviderResourceDefinition.WILDCARD_PATH, AggregateDiscoveryProviderResourceDefinition.Attribute.class));

        if (this.schema.since(DiscoverySchema.VERSION_2_0)) {
            builder.addChild(discoveryBuilder(DistributedDiscoveryProviderResourceDefinition.WILDCARD_PATH, DistributedDiscoveryProviderResourceDefinition.Attribute.class))
                    .addChild(registryBuilder(DistributedRegistryProviderResourceDefinition.WILDCARD_PATH, DistributedRegistryProviderResourceDefinition.Attribute.class))
                    ;
        }
        return builder.build();
    }

    private static <A extends Enum<A> & Attribute> PersistentResourceXMLBuilder discoveryBuilder(PathElement path, Class<A> attributeClass) {
        return addAttributes(builder(path), attributeClass);
    }

    private static PersistentResourceXMLBuilder registryBuilder(PathElement path) {
        return builder(path).addChild(serviceBuilder());
    }

    private static <A extends Enum<A> & Attribute> PersistentResourceXMLBuilder registryBuilder(PathElement path, Class<A> attributeClass) {
        return discoveryBuilder(path, attributeClass).addChild(serviceBuilder());
    }

    private static PersistentResourceXMLBuilder serviceBuilder() {
        return addAttributes(builder(ServiceResourceDefinition.WILDCARD_PATH), ServiceResourceDefinition.Attribute.class);
    }

    private static <A extends Enum<A> & Attribute> PersistentResourceXMLBuilder addAttributes(PersistentResourceXMLBuilder builder, Class<A> attributeClass) {
        // Ensure element-based attributes are processed last
        List<AttributeDefinition> elementAttributes = new LinkedList<>();
        for (Attribute attribute : EnumSet.allOf(attributeClass)) {
            AttributeDefinition definition = attribute.getDefinition();
            if (definition.getParser().isParseAsElement()) {
                elementAttributes.add(definition);
            } else {
                builder.addAttribute(definition, definition.getParser(), definition.getMarshaller());
            }
        }
        for (AttributeDefinition attribute : elementAttributes) {
            builder.addAttribute(attribute, attribute.getParser(), attribute.getMarshaller());
        }
        return builder;
    }
}
