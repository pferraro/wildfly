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
import org.wildfly.discovery.FilterSpec;

/**
 * {@link org.wildfly.clustering.marshalling.spi.Serializer} for a {@link FilterSpec}.
 * @author Paul Ferraro
 */
public enum FilterSpecSerializer implements Serializer<FilterSpec> {
    INSTANCE;

    @Override
    public void write(DataOutput output, FilterSpec filter) throws IOException {
        output.writeUTF((filter != null) ? filter.toString() : "");
    }

    @Override
    public FilterSpec read(DataInput input) throws IOException {
        String value = input.readUTF();
        return value.isEmpty() ? null : FilterSpec.fromString(value);
    }

    @MetaInfServices(Externalizer.class)
    public static class FilterSpecExternalizer extends SerializerExternalizer<FilterSpec> {
        public FilterSpecExternalizer() {
            super(FilterSpec.class, INSTANCE);
        }
    }
}
