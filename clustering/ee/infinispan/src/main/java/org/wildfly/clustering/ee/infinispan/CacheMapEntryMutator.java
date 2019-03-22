/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2019, Red Hat, Inc., and individual contributors
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

package org.wildfly.clustering.ee.infinispan;

import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

import org.infinispan.functional.EntryView;
import org.infinispan.functional.FunctionalMap.ReadWriteMap;
import org.wildfly.clustering.ee.Mutator;
import org.wildfly.clustering.infinispan.spi.functional.MapPutFunction;

/**
 * @author Paul Ferraro
 */
public class CacheMapEntryMutator<K, EK, EV> implements Mutator {

    private final ReadWriteMap<K, Map<EK, EV>> cache;
    private final K key;
    private final Map.Entry<EK, EV> entry;
    private final AtomicBoolean mutated;
    private final BiFunction<Map.Entry<EK, EV>, EntryView.ReadWriteEntryView<K, Map<EK, EV>>, EV> putFunction;

    public CacheMapEntryMutator(ReadWriteMap<K, Map<EK, EV>> cache, K key, EK entryKey, EV entryValue, CacheProperties properties) {
        this(cache, key, new AbstractMap.SimpleImmutableEntry<>(entryKey, entryValue), properties);
    }

    public CacheMapEntryMutator(ReadWriteMap<K, Map<EK, EV>> cache, K key, Map.Entry<EK, EV> entry, CacheProperties properties) {
        this.cache = cache;
        this.key = key;
        this.entry = entry;
        this.mutated = properties.isTransactional() ? new AtomicBoolean(false) : null;
        this.putFunction = new MapPutFunction<>(properties.isTransactional());
    }

    @Override
    public void mutate() {
        // We only ever have to perform a replace once within a batch
        if ((this.mutated == null) || this.mutated.compareAndSet(false, true)) {
            // Use FAIL_SILENTLY to prevent mutation from failing locally due to remote exceptions
            this.cache.eval(this.key, this.entry, this.putFunction).join();
        }
    }
}
