/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat, Inc., and individual contributors
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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.infinispan.Cache;
import org.infinispan.context.Flag;
import org.infinispan.functional.FunctionalMap.ReadWriteMap;
import org.infinispan.functional.impl.FunctionalMapImpl;
import org.infinispan.functional.impl.ReadWriteMapImpl;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntriesEvicted;
import org.infinispan.notifications.cachelistener.event.CacheEntriesEvictedEvent;
import org.wildfly.clustering.ee.infinispan.CacheProperties;
import org.wildfly.clustering.infinispan.spi.distribution.Key;
import org.wildfly.clustering.marshalling.spi.InvalidSerializedFormException;
import org.wildfly.clustering.marshalling.spi.Marshaller;
import org.wildfly.clustering.web.infinispan.logging.InfinispanWebLogger;
import org.wildfly.clustering.web.infinispan.session.SessionAttributes;
import org.wildfly.clustering.web.infinispan.session.SessionAttributesFactory;
import org.wildfly.clustering.web.infinispan.session.SessionCreationMetaDataKey;
import org.wildfly.clustering.web.session.ImmutableSessionAttributes;

/**
 * {@link SessionAttributesFactory} for fine granularity sessions.
 * A given session's attributes are mapped to N+1 co-located cache entries, where N is the number of session attributes.
 * A separate cache entry stores the activate attribute names for the session.
 * @author Paul Ferraro
 */
@Listener(sync = false)
public class FineSessionAttributesFactory<V> implements SessionAttributesFactory<Map<String, V>> {

    private final ReadWriteMap<SessionAttributesKey, Map<String, V>> attributes;
    private final Cache<SessionAttributesKey, Map<String, V>> cache;
    private final Marshaller<Object, V> marshaller;
    private final CacheProperties properties;

    public FineSessionAttributesFactory(Cache<SessionAttributesKey, Map<String, V>> cache, Marshaller<Object, V> marshaller, CacheProperties properties) {
        this.cache = cache;
        this.attributes = ReadWriteMapImpl.create(FunctionalMapImpl.create(cache.getAdvancedCache()));
        this.marshaller = marshaller;
        this.properties = properties;
    }

    @Override
    public Map<String, V> createValue(String id, Void context) {
        Map<String, V> attributes = this.properties.isTransactional() ? new HashMap<>() : new ConcurrentHashMap<>();
        this.cache.put(new SessionAttributesKey(id), attributes);
        return attributes;
    }

    @Override
    public Map<String, V> findValue(String id) {
        Map<String, V> attributes = this.cache.get(new SessionAttributesKey(id));
        if (attributes != null) {
            // We need to validate that all session attributes can be unmarshalled
            for (Map.Entry<String, V> entry : attributes.entrySet()) {
                try {
                    this.marshaller.read(entry.getValue());
                    continue;
                } catch (InvalidSerializedFormException e) {
                    InfinispanWebLogger.ROOT_LOGGER.failedToActivateSessionAttribute(e, id, entry.getKey());
                    this.remove(id);
                    return null;
                }
            }
        }
        return attributes;
    }

    @Override
    public boolean remove(String id) {
        return this.cache.getAdvancedCache().withFlags(Flag.FORCE_SYNCHRONOUS).remove(new SessionAttributesKey(id)) != null;
    }

    @Override
    public SessionAttributes createSessionAttributes(String id, Map<String, V> attributes) {
        return new FineSessionAttributes<>(new SessionAttributesKey(id), this.attributes, this.marshaller, this.properties);
    }

    @Override
    public ImmutableSessionAttributes createImmutableSessionAttributes(String id, Map<String, V> names) {
        return new FineImmutableSessionAttributes<>(new SessionAttributesKey(id), this.attributes, this.marshaller);
    }

    @CacheEntriesEvicted
    public void evicted(CacheEntriesEvictedEvent<Key<String>, ?> event) {
        if (!event.isPre()) {
            Cache<SessionAttributesKey, Map<String, V>> cache = this.cache.getAdvancedCache().withFlags(Flag.SKIP_LISTENER_NOTIFICATION);
            for (Key<String> key : event.getEntries().keySet()) {
                // Workaround for ISPN-8324
                if (key instanceof SessionCreationMetaDataKey) {
                    cache.evict(new SessionAttributesKey(key.getValue()));
                }
            }
        }
    }
}
