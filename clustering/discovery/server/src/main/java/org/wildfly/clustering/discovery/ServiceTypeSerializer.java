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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.Externalizer;
import org.wildfly.clustering.marshalling.spi.Serializer;
import org.wildfly.clustering.marshalling.spi.SerializerExternalizer;
import org.wildfly.discovery.ServiceType;

/**
 * @author Paul Ferraro
 */
public enum ServiceTypeSerializer implements Serializer<ServiceType> {
    INSTANCE;

    private static void writeString(DataOutput output, String value) throws IOException {
        output.writeUTF((value != null) ? value : "");
    }

    private static String readString(DataInput input) throws IOException {
        String value = input.readUTF();
        return value.isEmpty() ? null : value;
    }

    @Override
    public void write(DataOutput output, ServiceType type) throws IOException {
        output.writeUTF(type.getAbstractType());
        writeString(output, type.getAbstractTypeAuthority());
        writeString(output, type.getUriScheme());
        if (type.getUriScheme() != null) {
            writeString(output, type.getUriSchemeAuthority());
        }
    }

    @Override
    public ServiceType read(DataInput input) throws IOException {
        String abstractType = input.readUTF();
        String abstractTypeAuthority = readString(input);
        String concreteType = readString(input);
        if (concreteType == null) {
            return ServiceType.of(abstractType, abstractTypeAuthority);
        }
        String concreteTypeAuthority = readString(input);
        return ServiceType.of(abstractType, abstractTypeAuthority, concreteType, concreteTypeAuthority);
    }

    @MetaInfServices(Externalizer.class)
    public static class ServiceTypeExternalizer extends SerializerExternalizer<ServiceType> {
        public ServiceTypeExternalizer() {
            super(ServiceType.class, INSTANCE);
        }
    }
}
