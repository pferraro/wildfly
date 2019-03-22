/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.wildfly.clustering.web.infinispan.session.finer;

import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import org.infinispan.commons.marshall.NotSerializableException;
import org.infinispan.functional.EntryView;
import org.infinispan.functional.FunctionalMap.ReadWriteMap;
import org.wildfly.clustering.ee.Mutator;
import org.wildfly.clustering.ee.infinispan.CacheMapEntryMutator;
import org.wildfly.clustering.ee.infinispan.CacheProperties;
import org.wildfly.clustering.infinispan.spi.functional.MapGetFunction;
import org.wildfly.clustering.infinispan.spi.functional.MapPutFunction;
import org.wildfly.clustering.infinispan.spi.functional.MapRemoveFunction;
import org.wildfly.clustering.marshalling.spi.Marshaller;
import org.wildfly.clustering.web.infinispan.session.SessionAttributes;
import org.wildfly.clustering.web.session.SessionAttributeImmutability;

/**
 * Exposes session attributes for fine granularity sessions.
 * @author Paul Ferraro
 */
public class FineSessionAttributes<V> extends FineImmutableSessionAttributes<V> implements SessionAttributes {
    private final SessionAttributesKey key;
    private final ReadWriteMap<SessionAttributesKey, Map<String, V>> attributes;
    private final Map<String, Mutator> mutations = new ConcurrentHashMap<>();
    private final Marshaller<Object, V> marshaller;
    private final CacheProperties properties;
    private final BiFunction<String, EntryView.ReadWriteEntryView<SessionAttributesKey, Map<String, V>>, V> removeFunction;
    private final BiFunction<Map.Entry<String, V>, EntryView.ReadWriteEntryView<SessionAttributesKey, Map<String, V>>, V> putFunction;

    public FineSessionAttributes(SessionAttributesKey key, ReadWriteMap<SessionAttributesKey, Map<String, V>> attributes, Marshaller<Object, V> marshaller, CacheProperties properties) {
        super(key, attributes, marshaller);
        this.key = key;
        this.attributes = attributes;
        this.marshaller = marshaller;
        this.properties = properties;
        this.removeFunction = new MapRemoveFunction<>(properties.isTransactional());
        this.putFunction = new MapPutFunction<>(properties.isTransactional());
    }

    @Override
    public Object removeAttribute(String name) {
        Object result = this.read(name, this.attributes.eval(this.key, name, this.removeFunction).join());
        this.mutations.remove(name);
        return result;
    }

    @Override
    public Object setAttribute(String name, Object attribute) {
        if (attribute == null) {
            return this.removeAttribute(name);
        }
        if (this.properties.isMarshalling() && !this.marshaller.isMarshallable(attribute)) {
            throw new IllegalArgumentException(new NotSerializableException(attribute.getClass().getName()));
        }

        V value = this.marshaller.write(attribute);
        Object result = this.read(name, this.attributes.eval(this.key, new AbstractMap.SimpleImmutableEntry<>(name, value), this.putFunction).join());
        if (this.properties.isTransactional()) {
            // Add a passive mutation to prevent any subsequent mutable getAttribute(...) from triggering a redundant mutation on close.
            this.mutations.put(name, Mutator.PASSIVE);
        } else {
            // If the object is mutable, we need to indicate trigger a mutation on close
            if (SessionAttributeImmutability.INSTANCE.test(attribute)) {
                this.mutations.remove(name);
            } else {
                this.mutations.put(name, new CacheMapEntryMutator<>(this.attributes, this.key, name, value, this.properties));
            }
        }
        return result;
    }

    @Override
    public Object getAttribute(String name) {
        V value = this.attributes.eval(this.key, new MapGetFunction<>(name)).join();
        Object attribute = this.read(name, value);
        if (attribute != null) {
            // If the object is mutable, we need to trigger a mutation on close
            if (!SessionAttributeImmutability.INSTANCE.test(attribute)) {
                this.mutations.putIfAbsent(name, new CacheMapEntryMutator<>(this.attributes, this.key, name, value, this.properties));
            }
        }
        return attribute;
    }

    @Override
    public void close() {
        for (Mutator mutator : this.mutations.values()) {
            mutator.mutate();
        }
        this.mutations.clear();
    }
}
