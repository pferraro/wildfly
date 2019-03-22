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

package org.wildfly.clustering.infinispan.spi.functional;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Paul Ferraro
 */
public enum MapOperations implements Operations<Map<Object, Object>> {
    COPY_ON_WRITE() {
        @Override
        public Map<Object, Object> apply(Map<Object, Object> map) {
            return new HashMap<>(map);
        }

        @Override
        public Map<Object, Object> get() {
            return new HashMap<>();
        }
    },
    CONCURRENT() {
        @Override
        public Map<Object, Object> apply(Map<Object, Object> map) {
            return map;
        }

        @Override
        public Map<Object, Object> get() {
            return new ConcurrentHashMap<>();
        }
    },
    ;

    @SuppressWarnings("unchecked")
    private <K, V> Operations<Map<K, V>> cast() {
        return (Operations<Map<K, V>>) (Operations<?>) this;
    }

    public static <K, V> Operations<Map<K, V>> of(boolean transactional) {
        return (transactional ? COPY_ON_WRITE : CONCURRENT).cast();
    }
}
