/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.as.clustering.infinispan.subsystem;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.jboss.as.clustering.controller.Attribute;
import org.jboss.as.clustering.infinispan.subsystem.remote.ConnectionPoolResourceDefinition;
import org.jboss.as.clustering.infinispan.subsystem.remote.RemoteCacheContainerResourceDefinition;
import org.jboss.as.clustering.infinispan.subsystem.remote.RemoteClusterResourceDefinition;
import org.jboss.as.clustering.infinispan.subsystem.remote.SecurityResourceDefinition;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;

/**
 * Enumerates the attributes used in the Infinispan subsystem schema.
 * @author Paul Ferraro
 * @author Richard Achmatowicz (c) 2011 RedHat Inc.
 * @author Tristan Tarrant
 */
public enum XMLAttribute {
    // must be first
    UNKNOWN((String) null),
    ACQUIRE_TIMEOUT(LockingResourceDefinition.Attribute.ACQUIRE_TIMEOUT),
    @Deprecated CAPACITY("capacity"),
    ALIASES(CacheContainerResourceDefinition.ListAttribute.ALIASES),
    @Deprecated ASYNC_MARSHALLING("async-marshalling"),
    BACKUP_FAILURE_POLICY(BackupResourceDefinition.Attribute.FAILURE_POLICY),
    @Deprecated BATCH_SIZE("batch-size"),
    @Deprecated BATCHING("batching"),
    @Deprecated BIAS_LIFESPAN(ScatteredCacheResourceDefinition.Attribute.BIAS_LIFESPAN),
    @Deprecated CACHE(RemoteStoreResourceDefinition.Attribute.CACHE),
    CAPACITY_FACTOR(DistributedCacheResourceDefinition.Attribute.CAPACITY_FACTOR),
    CHANNEL(JGroupsTransportResourceDefinition.Attribute.CHANNEL),
    CHUNK_SIZE(StateTransferResourceDefinition.Attribute.CHUNK_SIZE),
    CLASS(CustomStoreResourceDefinition.Attribute.CLASS),
    @Deprecated CLUSTER("cluster"),
    COMPLETE_TIMEOUT(TransactionResourceDefinition.Attribute.COMPLETE_TIMEOUT),
    CONCURRENCY_LEVEL(LockingResourceDefinition.Attribute.CONCURRENCY),
    @Deprecated CONSISTENT_HASH_STRATEGY("consistent-hash-strategy"),
    CREATE_ON_START(TableResourceDefinition.Attribute.CREATE_ON_START),
    DATA_SOURCE(JDBCStoreResourceDefinition.Attribute.DATA_SOURCE),
    @Deprecated DATASOURCE("datasource"),
    DEFAULT_CACHE(CacheContainerResourceDefinition.Attribute.DEFAULT_CACHE),
    @Deprecated DEFAULT_CACHE_CONTAINER("default-cache-container"),
    DIALECT(JDBCStoreResourceDefinition.Attribute.DIALECT),
    DOMAIN(RemoteClusterResourceDefinition.Attribute.DOMAIN),
    DROP_ON_STOP(TableResourceDefinition.Attribute.DROP_ON_STOP),
    @Deprecated ENABLED(BackupResourceDefinition.DeprecatedAttribute.ENABLED),
    @Deprecated EVICTION_EXECUTOR("eviction-executor"),
    @Deprecated EVICTION_TYPE("eviction-type"),
    @Deprecated EXECUTOR("executor"),
    FETCH_SIZE(TableResourceDefinition.Attribute.FETCH_SIZE),
    @Deprecated FETCH_STATE(StoreResourceDefinition.DeprecatedAttribute.FETCH_STATE),
    @Deprecated FLUSH_LOCK_TIMEOUT("flush-lock-timeout"),
    @Deprecated INDEX("index"),
    INTERVAL(ExpirationResourceDefinition.Attribute.INTERVAL),
    @Deprecated INVALIDATION_BATCH_SIZE(ScatteredCacheResourceDefinition.Attribute.INVALIDATION_BATCH_SIZE),
    ISOLATION(LockingResourceDefinition.Attribute.ISOLATION),
    @Deprecated JNDI_NAME("jndi-name"),
    KEEPALIVE_TIME(ThreadPoolResourceDefinition.values()[0].getKeepAliveTime()),
    L1_LIFESPAN(DistributedCacheResourceDefinition.Attribute.L1_LIFESPAN),
    LIFESPAN(ExpirationResourceDefinition.Attribute.LIFESPAN),
    @Deprecated LISTENER_EXECUTOR("listener-executor"),
    LOCK_TIMEOUT(JGroupsTransportResourceDefinition.Attribute.LOCK_TIMEOUT),
    LOCKING(TransactionResourceDefinition.Attribute.LOCKING),
    MARSHALLER(CacheContainerResourceDefinition.Attribute.MARSHALLER),
    MAX_BATCH_SIZE(StoreResourceDefinition.Attribute.MAX_BATCH_SIZE),
    @Deprecated MAX_ENTRIES("max-entries"),
    MAX_IDLE(ExpirationResourceDefinition.Attribute.MAX_IDLE),
    MAX_THREADS(ThreadPoolResourceDefinition.values()[0].getMaxThreads()),
    MERGE_POLICY(PartitionHandlingResourceDefinition.Attribute.MERGE_POLICY),
    MIN_THREADS(ThreadPoolResourceDefinition.values()[0].getMinThreads()),
    MODE(TransactionResourceDefinition.Attribute.MODE),
    MODIFICATION_QUEUE_SIZE(StoreWriteBehindResourceDefinition.Attribute.MODIFICATION_QUEUE_SIZE),
    @Deprecated MODULE("module"),
    MODULES(CacheContainerResourceDefinition.ListAttribute.MODULES),
    NAME(ModelDescriptionConstants.NAME),
    OUTBOUND_SOCKET_BINDING("outbound-socket-binding"),
    OWNERS(DistributedCacheResourceDefinition.Attribute.OWNERS),
    PASSIVATION(StoreResourceDefinition.Attribute.PASSIVATION),
    PATH(FileStoreResourceDefinition.DeprecatedAttribute.RELATIVE_PATH),
    PREFIX(StringTableResourceDefinition.Attribute.PREFIX),
    PRELOAD(StoreResourceDefinition.Attribute.PRELOAD),
    PURGE(StoreResourceDefinition.Attribute.PURGE),
    @Deprecated QUEUE_FLUSH_INTERVAL("queue-flush-interval"),
    QUEUE_LENGTH(ThreadPoolResourceDefinition.BLOCKING.getQueueLength()),
    @Deprecated QUEUE_SIZE("queue-size"),
    RELATIVE_TO(FileStoreResourceDefinition.DeprecatedAttribute.RELATIVE_TO),
    @Deprecated REMOTE_CACHE("remote-cache"),
    @Deprecated REMOTE_SERVERS(RemoteStoreResourceDefinition.Attribute.SOCKET_BINDINGS),
    @Deprecated REMOTE_SITE("remote-site"),
    REMOTE_TIMEOUT(ClusteredCacheResourceDefinition.Attribute.REMOTE_TIMEOUT),
    @Deprecated REPLICATION_QUEUE_EXECUTOR("replication-queue-executor"),
    SEGMENTED(StoreResourceDefinition.Attribute.SEGMENTED),
    SEGMENTS(SegmentedCacheResourceDefinition.Attribute.SEGMENTS),
    SHARED(StoreResourceDefinition.Attribute.SHARED),
    @Deprecated SHUTDOWN_TIMEOUT("shutdown-timeout"),
    @Deprecated SINGLETON("singleton"),
    SITE("site"),
    SIZE(MemoryResourceDefinition.Attribute.SIZE),
    SIZE_UNIT(MemoryResourceDefinition.SharedAttribute.SIZE_UNIT),
    @Deprecated STACK("stack"),
    @Deprecated START("start"),
    STATISTICS_ENABLED(CacheResourceDefinition.Attribute.STATISTICS_ENABLED),
    STOP_TIMEOUT(TransactionResourceDefinition.Attribute.STOP_TIMEOUT),
    STRATEGY(BackupResourceDefinition.Attribute.STRATEGY),
    STRIPING(LockingResourceDefinition.Attribute.STRIPING),
    TAKE_OFFLINE_AFTER_FAILURES(BackupResourceDefinition.TakeOfflineAttribute.AFTER_FAILURES),
    TAKE_OFFLINE_MIN_WAIT(BackupResourceDefinition.TakeOfflineAttribute.MIN_WAIT),
    @Deprecated THREAD_POOL_SIZE("thread-pool-size"),
    TIMEOUT(StateTransferResourceDefinition.Attribute.TIMEOUT),
    TYPE(TableResourceDefinition.ColumnAttribute.ID.getColumnType()),
    WHEN_SPLIT(PartitionHandlingResourceDefinition.Attribute.WHEN_SPLIT),

    // hotrod store
    CACHE_CONFIGURATION(HotRodStoreResourceDefinition.Attribute.CACHE_CONFIGURATION),

    // remote-cache-container
    REMOTE_CACHE_CONTAINER(RemoteCacheContainerResourceDefinition.WILDCARD_PATH),
    CONNECTION_TIMEOUT(RemoteCacheContainerResourceDefinition.Attribute.CONNECTION_TIMEOUT),
    DEFAULT_REMOTE_CLUSTER(RemoteCacheContainerResourceDefinition.Attribute.DEFAULT_REMOTE_CLUSTER),
    KEY_SIZE_ESTIMATE(RemoteCacheContainerResourceDefinition.DeprecatedAttribute.KEY_SIZE_ESTIMATE),
    MAX_RETRIES(RemoteCacheContainerResourceDefinition.Attribute.MAX_RETRIES),
    PROTOCOL_VERSION(RemoteCacheContainerResourceDefinition.Attribute.PROTOCOL_VERSION),
    SOCKET_TIMEOUT(RemoteCacheContainerResourceDefinition.Attribute.SOCKET_TIMEOUT),
    TCP_NO_DELAY(RemoteCacheContainerResourceDefinition.Attribute.TCP_NO_DELAY),
    TCP_KEEP_ALIVE(RemoteCacheContainerResourceDefinition.Attribute.TCP_KEEP_ALIVE),
    TRANSACTION_TIMEOUT(RemoteCacheContainerResourceDefinition.Attribute.TRANSACTION_TIMEOUT),
    VALUE_SIZE_ESTIMATE(RemoteCacheContainerResourceDefinition.DeprecatedAttribute.VALUE_SIZE_ESTIMATE),

    // remote-cache-container -> connection-pool
    EXHAUSTED_ACTION(ConnectionPoolResourceDefinition.Attribute.EXHAUSTED_ACTION),
    MAX_ACTIVE(ConnectionPoolResourceDefinition.Attribute.MAX_ACTIVE),
    MAX_WAIT(ConnectionPoolResourceDefinition.Attribute.MAX_WAIT),
    MIN_EVICTABLE_IDLE_TIME(ConnectionPoolResourceDefinition.Attribute.MIN_EVICTABLE_IDLE_TIME),
    MIN_IDLE(ConnectionPoolResourceDefinition.Attribute.MIN_IDLE),

    // remote-cache-container -> remote-clusters
    SOCKET_BINDINGS(RemoteClusterResourceDefinition.Attribute.SOCKET_BINDINGS),

    // remote-cache-container -> security
    SSL_CONTEXT(SecurityResourceDefinition.Attribute.SSL_CONTEXT),
    ;
    private final String name;

    XMLAttribute(Attribute attribute) {
        this(attribute.getDefinition().getXmlName());
    }

    XMLAttribute(PathElement wildcardPath) {
        this(wildcardPath.getKey());
    }

    XMLAttribute(String name) {
        this.name = name;
    }

    /**
     * Get the local name of this element.
     *
     * @return the local name
     */
    public String getLocalName() {
        return this.name;
    }

    private static final Map<String, XMLAttribute> attributes;

    static {
        final Map<String, XMLAttribute> map = new HashMap<>();
        for (XMLAttribute attribute : EnumSet.allOf(XMLAttribute.class)) {
            final String name = attribute.getLocalName();
            if (name != null) {
                assert !map.containsKey(name) : attribute;
                map.put(name, attribute);
            }
        }
        attributes = map;
    }

    public static XMLAttribute forName(String localName) {
        final XMLAttribute attribute = attributes.get(localName);
        return attribute == null ? UNKNOWN : attribute;
    }
}
