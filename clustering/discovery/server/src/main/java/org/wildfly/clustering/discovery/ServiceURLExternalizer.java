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

package org.wildfly.clustering.discovery;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.URI;
import java.util.List;
import java.util.Set;

import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.Externalizer;
import org.wildfly.clustering.marshalling.spi.DefaultExternalizer;
import org.wildfly.clustering.marshalling.spi.EnumExternalizer;
import org.wildfly.clustering.marshalling.spi.IndexSerializer;
import org.wildfly.discovery.AttributeValue;
import org.wildfly.discovery.ServiceURL;

/**
 * @author Paul Ferraro
 */
@MetaInfServices(Externalizer.class)
public class ServiceURLExternalizer implements Externalizer<ServiceURL> {

    private static final Externalizer<AttributeValueSerializer> TYPE_EXTERNALIZER = new EnumExternalizer<>(AttributeValueSerializer.class);
    private static final Externalizer<URI> URI_EXTERNALIZER = DefaultExternalizer.URI.cast(URI.class);

    private static void writeString(ObjectOutput output, String value) throws IOException {
        output.writeUTF((value != null) ? value : "");
    }

    private static String readString(ObjectInput input) throws IOException {
        String value = input.readUTF();
        return value.isEmpty() ? null : value;
    }

    @Override
    public void writeObject(ObjectOutput output, ServiceURL url) throws IOException {
        writeString(output, url.getAbstractType());
        writeString(output, url.getAbstractTypeAuthority());
        URI_EXTERNALIZER.writeObject(output, url.getLocationURI());
        writeString(output, url.getUriSchemeAuthority());

        Set<String> attributes = url.getAttributeNames();
        IndexSerializer.UNSIGNED_BYTE.writeInt(output, attributes.size());
        for (String attribute : attributes) {
            output.writeUTF(attribute);
            List<AttributeValue> values = url.getAttributeValues(attribute);
            IndexSerializer.UNSIGNED_BYTE.writeInt(output, values.size());
            for (AttributeValue value : values) {
                AttributeValueSerializer type = AttributeValueSerializer.of(value);
                TYPE_EXTERNALIZER.writeObject(output, type);
                type.write(output, value);
            }
        }
    }

    @Override
    public ServiceURL readObject(ObjectInput input) throws IOException, ClassNotFoundException {
        ServiceURL.Builder builder = new ServiceURL.Builder()
                .setAbstractType(readString(input))
                .setAbstractTypeAuthority(readString(input))
                .setUri(URI_EXTERNALIZER.readObject(input))
                .setUriSchemeAuthority(readString(input))
                ;
        int attributeCount = IndexSerializer.UNSIGNED_BYTE.readInt(input);
        for (int i = 0; i < attributeCount; ++i) {
            String attribute = input.readUTF();
            int valueCount = IndexSerializer.UNSIGNED_BYTE.readInt(input);
            for (int j = 0; j < valueCount; ++j) {
                AttributeValueSerializer type = TYPE_EXTERNALIZER.readObject(input);
                AttributeValue value = type.read(input);
                if (value != null) {
                    builder.addAttribute(attribute, value);
                } else {
                    builder.addAttribute(attribute);
                }
            }
        }
        return builder.create();
    }

    @Override
    public Class<ServiceURL> getTargetClass() {
        return ServiceURL.class;
    }
}
