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
import java.util.Random;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.wildfly.clustering.marshalling.ExternalizerTester;
import org.wildfly.clustering.marshalling.spi.SerializerExternalizer;
import org.wildfly.discovery.AttributeValue;

/**
 * Unit test for {@link AttributeValueSerializer}.
 * @author Paul Ferraro
 */
public class AttributeValueExternalizerTestCase {
    private final Random random = new Random();

    @Test
    public void testOpaque() throws ClassNotFoundException, IOException {
        byte[] bytes = new byte[this.random.nextInt(Byte.MAX_VALUE)];
        this.random.nextBytes(bytes);

        AttributeValue value = AttributeValue.fromBytes(bytes);

        AttributeValueSerializer serializer = AttributeValueSerializer.of(value);
        Assert.assertSame(AttributeValueSerializer.OPAQUE, serializer);
        new ExternalizerTester<>(new SerializerExternalizer<>(AttributeValue.class, serializer)).test(value);
    }

    @Test
    public void testNumeric() throws ClassNotFoundException, IOException {
        AttributeValue value = AttributeValue.fromInt(this.random.nextInt());

        AttributeValueSerializer serializer = AttributeValueSerializer.of(value);
        Assert.assertSame(AttributeValueSerializer.NUMERIC, serializer);
        new ExternalizerTester<>(new SerializerExternalizer<>(AttributeValue.class, serializer)).test(value);
    }

    @Test
    public void testString() throws ClassNotFoundException, IOException {
        AttributeValue value = AttributeValue.fromString(UUID.randomUUID().toString());

        AttributeValueSerializer serializer = AttributeValueSerializer.of(value);
        Assert.assertSame(AttributeValueSerializer.STRING, serializer);
        new ExternalizerTester<>(new SerializerExternalizer<>(AttributeValue.class, serializer)).test(value);
    }

    @Test
    public void testFalse() throws ClassNotFoundException, IOException {
        AttributeValue value = AttributeValue.FALSE;

        AttributeValueSerializer serializer = AttributeValueSerializer.of(value);
        Assert.assertSame(AttributeValueSerializer.FALSE, serializer);
        new ExternalizerTester<>(new SerializerExternalizer<>(AttributeValue.class, serializer)).test(value);
    }

    @Test
    public void testTrue() throws ClassNotFoundException, IOException {
        AttributeValue value = AttributeValue.TRUE;

        AttributeValueSerializer serializer = AttributeValueSerializer.of(value);
        Assert.assertSame(AttributeValueSerializer.TRUE, serializer);
        new ExternalizerTester<>(new SerializerExternalizer<>(AttributeValue.class, serializer)).test(value);
    }

    @Test
    public void testNull() throws ClassNotFoundException, IOException {
        AttributeValueSerializer serializer = AttributeValueSerializer.of(null);
        Assert.assertSame(AttributeValueSerializer.NULL, serializer);
        new ExternalizerTester<>(new SerializerExternalizer<>(AttributeValue.class, serializer)).test(null);
    }
}
