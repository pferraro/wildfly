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
import java.lang.reflect.Field;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import org.wildfly.clustering.marshalling.spi.IndexSerializer;
import org.wildfly.clustering.marshalling.spi.Serializer;
import org.wildfly.discovery.AttributeValue;
import org.wildfly.security.manager.WildFlySecurityManager;

/**
 * Serializers for {@link AttributeValue} types.
 * @author Paul Ferraro
 */
public enum AttributeValueSerializer implements Serializer<AttributeValue> {
    NUMERIC {
        @Override
        public void write(DataOutput output, AttributeValue value) throws IOException {
            output.writeInt(value.asInt());
        }

        @Override
        public AttributeValue read(DataInput input) throws IOException {
            return AttributeValue.fromInt(input.readInt());
        }
    },
    OPAQUE {
        @Override
        public void write(DataOutput output, AttributeValue value) throws IOException {
            // Content is not exposed - attribute value is truly opaque!
            try {
                byte[] content = (byte[]) WildFlySecurityManager.doUnchecked(new DeclaredFieldAccessor(value, "content"));
                IndexSerializer.VARIABLE.writeInt(output, content.length);
                output.write(content);
            } catch (PrivilegedActionException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public AttributeValue read(DataInput input) throws IOException {
            byte[] value = new byte[IndexSerializer.VARIABLE.readInt(input)];
            input.readFully(value);
            return AttributeValue.fromBytes(value);
        }
    },
    STRING {
        @Override
        public void write(DataOutput output, AttributeValue value) throws IOException {
            output.writeUTF(value.toString());
        }

        @Override
        public AttributeValue read(DataInput input) throws IOException {
            return AttributeValue.fromString(input.readUTF());
        }
    },
    FALSE {
        @Override
        public AttributeValue read(DataInput input) throws IOException {
            return AttributeValue.FALSE;
        }
    },
    TRUE {
        @Override
        public AttributeValue read(DataInput input) throws IOException {
            return AttributeValue.TRUE;
        }
    },
    NULL {
        @Override
        public AttributeValue read(DataInput input) throws IOException {
            return null;
        }
    },
    ;
    @Override
    public void write(DataOutput output, AttributeValue value) throws IOException {
        // Write nothing
    }

    static AttributeValueSerializer of(AttributeValue value) {
        if (value == null) return NULL;
        if (value.isOpaque()) return OPAQUE;
        if (value.isNumeric()) return NUMERIC;
        if (value.isString()) return STRING;
        if (value.isBoolean()) {
            return value == AttributeValue.TRUE ? TRUE : FALSE;
        }
        throw new IllegalArgumentException(value.toString());
    }

    private static class DeclaredFieldAccessor implements PrivilegedExceptionAction<Object> {
        private final Object object;
        private final String fieldName;

        DeclaredFieldAccessor(Object object, String fieldName) {
            this.object = object;
            this.fieldName = fieldName;
        }

        @Override
        public Object run() throws Exception {
            Field field = this.object.getClass().getDeclaredField(this.fieldName);
            field.setAccessible(true);
            try {
                return field.get(this.object);
            } finally {
                field.setAccessible(false);
            }
        }
    }
}