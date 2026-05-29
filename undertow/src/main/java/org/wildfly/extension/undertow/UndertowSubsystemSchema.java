/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.extension.undertow;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ResourceRegistration;
import org.jboss.as.controller.SubsystemSchema;
import org.jboss.as.controller.persistence.xml.NamedResourceRegistrationXMLElement;
import org.jboss.as.controller.persistence.xml.ResourceRegistrationXMLElement;
import org.jboss.as.controller.persistence.xml.ResourceXMLElementLocalName;
import org.jboss.as.controller.persistence.xml.ResourceXMLParticleFactory;
import org.jboss.as.controller.persistence.xml.ResourceXMLSequence;
import org.jboss.as.controller.persistence.xml.SingletonResourceRegistrationXMLElement;
import org.jboss.as.controller.persistence.xml.SubsystemResourceRegistrationXMLElement;
import org.jboss.as.controller.persistence.xml.SubsystemResourceXMLSchema;
import org.jboss.as.controller.xml.VersionedNamespace;
import org.jboss.as.controller.xml.XMLCardinality;
import org.jboss.as.controller.xml.XMLContent;
import org.jboss.as.version.Stability;
import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.IntVersion;
import org.wildfly.extension.undertow.filters.CustomFilterDefinition;
import org.wildfly.extension.undertow.filters.ErrorPageDefinition;
import org.wildfly.extension.undertow.filters.ExpressionFilterDefinition;
import org.wildfly.extension.undertow.filters.FilterDefinitions;
import org.wildfly.extension.undertow.filters.FilterRefDefinition;
import org.wildfly.extension.undertow.filters.GzipFilterDefinition;
import org.wildfly.extension.undertow.filters.ModClusterDefinition;
import org.wildfly.extension.undertow.filters.NoAffinityResourceDefinition;
import org.wildfly.extension.undertow.filters.RankedAffinityResourceDefinition;
import org.wildfly.extension.undertow.filters.RequestLimitHandlerDefinition;
import org.wildfly.extension.undertow.filters.ResponseHeaderFilterDefinition;
import org.wildfly.extension.undertow.filters.RewriteFilterDefinition;
import org.wildfly.extension.undertow.filters.SingleAffinityResourceDefinition;
import org.wildfly.extension.undertow.handlers.FileHandlerDefinition;
import org.wildfly.extension.undertow.handlers.HandlerDefinitions;
import org.wildfly.extension.undertow.handlers.ReverseProxyHandlerDefinition;
import org.wildfly.extension.undertow.handlers.ReverseProxyHandlerHostDefinition;

/**
 * Enumerates the supported Undertow subsystem schemas.
 * @author Paul Ferraro
 */
public enum UndertowSubsystemSchema implements SubsystemResourceXMLSchema<UndertowSubsystemSchema> {
/*  Unsupported, for documentation purposes only
    VERSION_1_0(1, 0),  // WildFly 8.0
    VERSION_1_1(1, 1),  // WildFly 8.1
    VERSION_1_2(1, 2),  // WildFly 8.2
    VERSION_2_0(2),     // WildFly 9
    VERSION_3_0(3, 0),  // WildFly 10.0
 */
    VERSION_3_1(3, 1),  // WildFly 10.1
    VERSION_4_0(4),     // WildFly 11
    VERSION_5_0(5),     // WildFly 12
    VERSION_6_0(6),     // WildFly 13
    VERSION_7_0(7),     // WildFly 14
    VERSION_8_0(8),     // WildFly 15-16
    VERSION_9_0(9),     // WildFly 17
    VERSION_10_0(10),   // WildFly 18-19
    VERSION_11_0(11),   // WildFly 20-22    N.B. There were no parser changes between 10.0 and 11.0 !!
    VERSION_12_0(12),   // WildFly 23-26.1, EAP 7.4
    VERSION_13_0(13),   // WildFly 27       N.B. There were no schema changes between 12.0 and 13.0!
    VERSION_14_0(14),   // WildFly 28-39
    VERSION_14_0_PREVIEW(14, 0, Stability.PREVIEW),   // WildFly 33-35
    VERSION_14_0_COMMUNITY(14, 0, Stability.COMMUNITY),   // WildFly 36-40
    VERSION_15_0(15)    // WildFly 40-present
    ;

    static final Set<UndertowSubsystemSchema> CURRENT = EnumSet.of(VERSION_15_0);
    private final VersionedNamespace<IntVersion, UndertowSubsystemSchema> namespace;
    private final ResourceXMLParticleFactory factory = ResourceXMLParticleFactory.newInstance(this);

    UndertowSubsystemSchema(int major) {
        this(new IntVersion(major));
    }

    UndertowSubsystemSchema(int major, int minor) {
        this(new IntVersion(major, minor));
    }

    UndertowSubsystemSchema(IntVersion version) {
        this(version, Stability.DEFAULT);
    }

    UndertowSubsystemSchema(final int major, final int minor, final Stability stability) {
        this(new IntVersion(major, minor), stability);
    }

    UndertowSubsystemSchema(final IntVersion version, final Stability stability) {
        this.namespace = SubsystemSchema.createLegacySubsystemURN(UndertowExtension.SUBSYSTEM_NAME, stability, version);
    }

    @Override
    public VersionedNamespace<IntVersion, UndertowSubsystemSchema> getNamespace() {
        return this.namespace;
    }

    @Override
    public SubsystemResourceRegistrationXMLElement getSubsystemXMLElement() {
        SubsystemResourceRegistrationXMLElement.Builder builder = this.factory.subsystemElement(UndertowRootDefinition.REGISTRATION)
                .addAttributes(List.of(
                        UndertowRootDefinition.DEFAULT_VIRTUAL_HOST,
                        UndertowRootDefinition.DEFAULT_SERVLET_CONTAINER,
                        UndertowRootDefinition.DEFAULT_SERVER,
                        UndertowRootDefinition.INSTANCE_ID,
                        UndertowRootDefinition.STATISTICS_ENABLED,
                        UndertowRootDefinition.DEFAULT_SECURITY_DOMAIN));
        if (this.since(VERSION_12_0)) {
            builder.addAttribute(UndertowRootDefinition.OBFUSCATE_SESSION_ROUTE);
        }

        ResourceXMLSequence.Builder contentBuilder = this.factory.sequence();
        if (this.since(VERSION_6_0)) {
            contentBuilder.addElement(this.factory.namedElement(ByteBufferPoolDefinition.REGISTRATION)
                    .addAttributes(ByteBufferPoolDefinition.ATTRIBUTES)
                    .withCardinality(XMLCardinality.Unbounded.OPTIONAL)
                    .build());
        }
        contentBuilder.addElement(this.factory.namedElement(BufferCacheDefinition.REGISTRATION)
                .addAttributes(BufferCacheDefinition.ATTRIBUTES)
                .withCardinality(XMLCardinality.Unbounded.OPTIONAL)
                .build());
        contentBuilder.addElement(this.factory.namedElement(ServerDefinition.REGISTRATION)
                .addAttributes(ServerDefinition.ATTRIBUTES)
                .withCardinality(XMLCardinality.Unbounded.REQUIRED)
                .withContent(this.factory.sequence()
                        .addElement(this.ajpListener())
                        .addElement(this.httpListener())
                        .addElement(this.httpsListener())
                        .addElement(this.host())
                        .build())
                .build());
        contentBuilder.addElement(this.servletContainer());
        contentBuilder.addElement(this.factory.singletonElement(HandlerDefinitions.REGISTRATION)
                .implyIfAbsent()
                .withElementLocalName(Constants.HANDLERS)
                .withContent(this.factory.sequence()
                        .addElement(this.factory.namedElement(FileHandlerDefinition.REGISTRATION)
                                .addAttributes(FileHandlerDefinition.ATTRIBUTES)
                                .withCardinality(XMLCardinality.Unbounded.OPTIONAL)
                                .build())
                        .addElement(this.reverseProxy())
                        .build())
                .build());
        contentBuilder.addElement(this.factory.singletonElement(FilterDefinitions.REGISTRATION)
                .implyIfAbsent()
                .withElementLocalName(Constants.FILTERS)
                .withContent(this.factory.sequence()
                        .addElement(this.factory.namedElement(RequestLimitHandlerDefinition.REGISTRATION)
                                .addAttributes(RequestLimitHandlerDefinition.ATTRIBUTES)
                                .withCardinality(XMLCardinality.Unbounded.OPTIONAL)
                                .build())
                        .addElement(this.factory.namedElement(ResponseHeaderFilterDefinition.REGISTRATION)
                                .addAttributes(ResponseHeaderFilterDefinition.ATTRIBUTES)
                                .withCardinality(XMLCardinality.Unbounded.OPTIONAL)
                                .build())
                        .addElement(this.factory.namedElement(GzipFilterDefinition.REGISTRATION)
                                .withCardinality(XMLCardinality.Unbounded.OPTIONAL)
                                .build())
                        .addElement(this.factory.namedElement(ErrorPageDefinition.REGISTRATION)
                                .addAttributes(ErrorPageDefinition.ATTRIBUTES)
                                .withCardinality(XMLCardinality.Unbounded.OPTIONAL)
                                .build())
                        .addElement(this.modCluster())
                        .addElement(this.factory.namedElement(CustomFilterDefinition.REGISTRATION)
                                .withElementLocalName(Constants.FILTER)
                                .addAttributes(CustomFilterDefinition.ATTRIBUTES)
                                .withCardinality(XMLCardinality.Unbounded.OPTIONAL)
                                .withContent(this.factory.sequence()
                                        .addElement(CustomFilterDefinition.PARAMETERS)
                                        .build())
                                .build())
                        .addElement(this.factory.namedElement(ExpressionFilterDefinition.REGISTRATION)
                                .addAttributes(ExpressionFilterDefinition.ATTRIBUTES)
                                .withCardinality(XMLCardinality.Unbounded.OPTIONAL)
                                .build())
                        .addElement(this.factory.namedElement(RewriteFilterDefinition.REGISTRATION)
                                .addAttributes(RewriteFilterDefinition.ATTRIBUTES)
                                .withCardinality(XMLCardinality.Unbounded.OPTIONAL)
                                .build())
                        .build())
                .build());
        if (this.since(VERSION_4_0)) {
            contentBuilder.addElement(this.factory.element(this.factory.resolve(Constants.APPLICATION_SECURITY_DOMAINS)).withCardinality(XMLCardinality.Single.OPTIONAL).withContent(this.factory.sequence().addElement(this.applicationSecurityDomain()).build()).build());
        }
        return builder.withContent(contentBuilder.build()).build();
    }

    private ResourceRegistrationXMLElement ajpListener() {
        // Reproduce attribute order of the previous parser implementation
        Set<AttributeDefinition> attributes = new LinkedHashSet<>(AjpListenerResourceDefinition.ATTRIBUTES);
        if (!this.since(VERSION_15_0) && !this.since(VERSION_14_0_COMMUNITY)) {
            attributes.remove(AjpListenerResourceDefinition.ALLOWED_REQUEST_ATTRIBUTES_PATTERN);
        }
        return this.listener(AjpListenerResourceDefinition.REGISTRATION).addAttributes(attributes).build();
    }

    private ResourceRegistrationXMLElement httpListener() {
        // Reproduce attribute order of the previous parser implementation
        Set<AttributeDefinition> attributes = new LinkedHashSet<>(HttpListenerResourceDefinition.ATTRIBUTES);
        attributes.addAll(AbstractHttpListenerResourceDefinition.ATTRIBUTES);
        return this.httpListener(HttpListenerResourceDefinition.REGISTRATION, attributes).build();
    }

    private ResourceRegistrationXMLElement httpsListener() {
        // Reproduce attribute order of the previous parser implementation
        Set<AttributeDefinition> attributes = new LinkedHashSet<>(HttpsListenerResourceDefinition.ATTRIBUTES);
        attributes.addAll(AbstractHttpListenerResourceDefinition.ATTRIBUTES);
        if (!this.since(UndertowSubsystemSchema.VERSION_4_0)) {
            attributes.remove(HttpsListenerResourceDefinition.SSL_CONTEXT);
        }
        if (!this.since(UndertowSubsystemSchema.VERSION_4_0)) {
            attributes.removeAll(List.of(AbstractHttpListenerResourceDefinition.CERTIFICATE_FORWARDING, AbstractHttpListenerResourceDefinition.PROXY_ADDRESS_FORWARDING));
        }
        return this.httpListener(HttpsListenerResourceDefinition.REGISTRATION, attributes).build();
    }

    private NamedResourceRegistrationXMLElement.Builder httpListener(ResourceRegistration registration, Set<AttributeDefinition> attributes) {
        if (!this.since(UndertowSubsystemSchema.VERSION_4_0)) {
            attributes.remove(AbstractHttpListenerResourceDefinition.REQUIRE_HOST_HTTP11);
        }
        if (!this.since(UndertowSubsystemSchema.VERSION_6_0)) {
            attributes.remove(AbstractHttpListenerResourceDefinition.PROXY_PROTOCOL);
        }
        return this.listener(registration).addAttributes(attributes);
    }

    private NamedResourceRegistrationXMLElement.Builder listener(ResourceRegistration registration) {
        // Reproduce attribute order of the previous parser implementation
        Set<AttributeDefinition> attributes = new LinkedHashSet<>(ListenerResourceDefinition.ATTRIBUTES);
        if (!this.since(UndertowSubsystemSchema.VERSION_4_0)) {
            attributes.remove(ListenerResourceDefinition.RFC6265_COOKIE_VALIDATION);
        }
        if (!this.since(UndertowSubsystemSchema.VERSION_6_0)) {
            attributes.remove(ListenerResourceDefinition.ALLOW_UNESCAPED_CHARACTERS_IN_URL);
        }
        return this.factory.namedElement(registration)
                .addAttributes(attributes)
                .withCardinality(XMLCardinality.Unbounded.OPTIONAL);
    }

    private ResourceRegistrationXMLElement host() {
        NamedResourceRegistrationXMLElement.Builder builder = this.factory.namedElement(HostDefinition.REGISTRATION)
                .addAttributes(List.of(HostDefinition.ALIAS, HostDefinition.DEFAULT_WEB_MODULE, HostDefinition.DEFAULT_RESPONSE_CODE, HostDefinition.DISABLE_CONSOLE_REDIRECT))
                .withCardinality(XMLCardinality.Unbounded.REQUIRED)
                ;

        if (this.since(UndertowSubsystemSchema.VERSION_6_0)) {
            builder.addAttribute(HostDefinition.QUEUE_REQUESTS_ON_START);
        }

        NamedResourceRegistrationXMLElement filterRefElement = this.factory.namedElement(FilterRefDefinition.REGISTRATION)
                .addAttributes(FilterRefDefinition.ATTRIBUTES)
                .withCardinality(XMLCardinality.Unbounded.OPTIONAL)
                .build();

        ResourceXMLSequence.Builder contentBuilder = this.factory.sequence();
        contentBuilder.addElement(this.factory.namedElement(LocationDefinition.REGISTRATION)
                .addAttributes(LocationDefinition.ATTRIBUTES)
                .withCardinality(XMLCardinality.Unbounded.OPTIONAL)
                .withContent(this.factory.sequence()
                        .addElement(filterRefElement)
                        .build())
                .build());
        contentBuilder.addElement(this.factory.singletonElement(AccessLogDefinition.REGISTRATION)
                .addAttributes(AccessLogDefinition.ATTRIBUTES)
                .withCardinality(XMLCardinality.Single.OPTIONAL)
                .build());
        if (this.since(VERSION_9_0)) {
            contentBuilder.addElement(this.factory.singletonElement(ConsoleAccessLogDefinition.REGISTRATION)
                    .addAttributes(ConsoleAccessLogDefinition.ATTRIBUTES)
                    .withCardinality(XMLCardinality.Single.OPTIONAL)
                    .withContent(this.factory.sequence()
                            .addElement(ExchangeAttributeDefinitions.ATTRIBUTES)
                            .addElement(ConsoleAccessLogDefinition.METADATA)
                            .build())
                    .build());
        }
        contentBuilder.addElement(filterRefElement);
        contentBuilder.addElement(this.singleSignOn(List.of(), XMLContent.empty()));
        if (this.since(VERSION_4_0)) {
            contentBuilder.addElement(this.factory.singletonElement(HttpInvokerDefinition.REGISTRATION)
                    .addAttributes(HttpInvokerDefinition.ATTRIBUTES)
                    .withCardinality(XMLCardinality.Single.OPTIONAL)
                    .build());
        }
        return builder.withContent(contentBuilder.build()).build();
    }

    private ResourceRegistrationXMLElement singleSignOn(Iterable<? extends Supplier<AttributeDefinition>> attributeProviders, XMLContent<Map.Entry<PathAddress, Map<PathAddress, ModelNode>>, ModelNode> content) {
        return this.factory.singletonElement(SingleSignOnDefinition.REGISTRATION)
                .provideAttributes(attributeProviders)
                .provideAttributes(EnumSet.allOf(SingleSignOnDefinition.Attribute.class))
                .withCardinality(XMLCardinality.Single.OPTIONAL)
                .withContent(content)
                .build();
    }

    private ResourceRegistrationXMLElement servletContainer() {
        NamedResourceRegistrationXMLElement.Builder builder = this.factory.namedElement(ServletContainerDefinition.REGISTRATION)
                .addAttributes(List.of(
                        ServletContainerDefinition.ALLOW_NON_STANDARD_WRAPPERS,
                        ServletContainerDefinition.DEFAULT_BUFFER_CACHE,
                        ServletContainerDefinition.STACK_TRACE_ON_ERROR,
                        ServletContainerDefinition.DEFAULT_ENCODING,
                        ServletContainerDefinition.USE_LISTENER_ENCODING,
                        ServletContainerDefinition.IGNORE_FLUSH,
                        ServletContainerDefinition.EAGER_FILTER_INIT,
                        ServletContainerDefinition.DEFAULT_SESSION_TIMEOUT,
                        ServletContainerDefinition.DISABLE_CACHING_FOR_SECURED_PAGES,
                        ServletContainerDefinition.DIRECTORY_LISTING,
                        ServletContainerDefinition.PROACTIVE_AUTHENTICATION,
                        ServletContainerDefinition.SESSION_ID_LENGTH,
                        ServletContainerDefinition.MAX_SESSIONS))
                .withCardinality(XMLCardinality.Unbounded.OPTIONAL);

        if (this.since(VERSION_4_0)) {
            builder.addAttributes(List.of(ServletContainerDefinition.DISABLE_FILE_WATCH_SERVICE, ServletContainerDefinition.DISABLE_SESSION_ID_REUSE));
        }
        if (this.since(VERSION_5_0)) {
            builder.addAttributes(List.of(ServletContainerDefinition.FILE_CACHE_METADATA_SIZE, ServletContainerDefinition.FILE_CACHE_MAX_FILE_SIZE, ServletContainerDefinition.FILE_CACHE_TIME_TO_LIVE));
        }
        if (this.since(VERSION_6_0)) {
            builder.addAttribute(ServletContainerDefinition.DEFAULT_COOKIE_VERSION);
        }
        if (this.since(VERSION_10_0)) {
            builder.addAttribute(ServletContainerDefinition.PRESERVE_PATH_ON_FORWARD);
        }
        if (this.since(VERSION_14_0)) {
            builder.addAttribute(ServletContainerDefinition.ORPHAN_SESSION_ALLOWED);
        }

        ResourceXMLSequence.Builder contentBuilder = this.factory.sequence();
        contentBuilder.addElement(this.factory.singletonElement(JspDefinition.REGISTRATION)
                .withElementLocalName(Constants.JSP_CONFIG)
                .addAttributes(JspDefinition.ATTRIBUTES)
                .withCardinality(XMLCardinality.Single.OPTIONAL)
                .build());
        if (this.since(VERSION_14_0)) {
            contentBuilder.addElement(this.factory.singletonElement(AffinityCookieDefinition.REGISTRATION)
                    .addAttributes(AffinityCookieDefinition.ATTRIBUTES)
                    .withCardinality(XMLCardinality.Single.OPTIONAL)
                    .build());
        }
        contentBuilder.addElement(this.factory.singletonElement(SessionCookieDefinition.REGISTRATION)
                .addAttributes(SessionCookieDefinition.ATTRIBUTES)
                .withCardinality(XMLCardinality.Single.OPTIONAL)
                .build());
        contentBuilder.addElement(this.factory.singletonElement(PersistentSessionsDefinition.REGISTRATION)
                .addAttributes(PersistentSessionsDefinition.ATTRIBUTES)
                .withCardinality(XMLCardinality.Single.OPTIONAL)
                .build());
        contentBuilder.addElement(this.websockets());
        contentBuilder.addElement(this.factory.element(this.resolve("mime-mappings"))
                .withCardinality(XMLCardinality.Single.OPTIONAL)
                .withContent(this.factory.sequence()
                        .addElement(this.factory.namedElement(MimeMappingDefinition.REGISTRATION).addAttribute(MimeMappingDefinition.VALUE).build())
                        .build())
                .build());
        contentBuilder.addElement(this.factory.element(this.resolve("welcome-files"))
                .withCardinality(XMLCardinality.Single.OPTIONAL)
                .withContent(this.factory.sequence()
                        .addElement(this.factory.namedElement(WelcomeFileDefinition.REGISTRATION).build())
                        .build())
                .build());
        contentBuilder.addElement(this.factory.singletonElement(CrawlerSessionManagementDefinition.REGISTRATION)
                .addAttributes(CrawlerSessionManagementDefinition.ATTRIBUTES)
                .withCardinality(XMLCardinality.Single.OPTIONAL)
                .build());
        return builder.withContent(contentBuilder.build()).build();
    }

    private ResourceRegistrationXMLElement websockets() {
        SingletonResourceRegistrationXMLElement.Builder builder = this.factory.singletonElement(WebsocketsDefinition.REGISTRATION)
                .addAttributes(List.of(WebsocketsDefinition.BUFFER_POOL, WebsocketsDefinition.WORKER, WebsocketsDefinition.DISPATCH_TO_WORKER))
                .withCardinality(XMLCardinality.Single.OPTIONAL);

        if (this.since(VERSION_4_0)) {
            builder.addAttributes(List.of(WebsocketsDefinition.PER_MESSAGE_DEFLATE, WebsocketsDefinition.DEFLATER_LEVEL));
        }
        return builder.build();
    }

    private ResourceRegistrationXMLElement reverseProxy() {
        NamedResourceRegistrationXMLElement.Builder builder = this.factory.namedElement(ReverseProxyHandlerDefinition.REGISTRATION)
                .addAttributes(List.of(
                        ReverseProxyHandlerDefinition.CONNECTIONS_PER_THREAD,
                        ReverseProxyHandlerDefinition.SESSION_COOKIE_NAMES,
                        ReverseProxyHandlerDefinition.PROBLEM_SERVER_RETRY,
                        ReverseProxyHandlerDefinition.REQUEST_QUEUE_SIZE,
                        ReverseProxyHandlerDefinition.MAX_REQUEST_TIME,
                        ReverseProxyHandlerDefinition.CACHED_CONNECTIONS_PER_THREAD,
                        ReverseProxyHandlerDefinition.CONNECTION_IDLE_TIMEOUT))
                .withCardinality(XMLCardinality.Unbounded.OPTIONAL);

        if (this.since(VERSION_4_0)) {
            builder.addAttribute(ReverseProxyHandlerDefinition.MAX_RETRIES);
        }
        if (this.since(VERSION_14_0_COMMUNITY) || this.since(VERSION_15_0)) {
            builder.addAttributes(List.of(ReverseProxyHandlerDefinition.REUSE_X_FORWARDED_HEADER, ReverseProxyHandlerDefinition.REWRITE_HOST_HEADER));
        }
        ResourceXMLSequence content = this.factory.sequence()
                .addElement(this.reverseProxyHost())
                .build();
        return builder.withContent(content).build();
    }

    private ResourceRegistrationXMLElement reverseProxyHost() {
        NamedResourceRegistrationXMLElement.Builder builder = this.factory.namedElement(ReverseProxyHandlerHostDefinition.REGISTRATION)
                .addAttributes(List.of(ReverseProxyHandlerHostDefinition.OUTBOUND_SOCKET_BINDING, ReverseProxyHandlerHostDefinition.SCHEME, ReverseProxyHandlerHostDefinition.INSTANCE_ID, ReverseProxyHandlerHostDefinition.PATH, ReverseProxyHandlerHostDefinition.SECURITY_REALM))
                .withCardinality(XMLCardinality.Unbounded.OPTIONAL);

        if (this.since(VERSION_4_0)) {
            builder.addAttributes(List.of(ReverseProxyHandlerHostDefinition.SSL_CONTEXT, ReverseProxyHandlerHostDefinition.ENABLE_HTTP2));
        }
        return builder.build();
    }

    private ResourceRegistrationXMLElement modCluster() {
        NamedResourceRegistrationXMLElement.Builder builder = this.factory.namedElement(ModClusterDefinition.REGISTRATION)
                .addAttributes(List.of(
                        ModClusterDefinition.MANAGEMENT_SOCKET_BINDING,
                        ModClusterDefinition.ADVERTISE_SOCKET_BINDING,
                        ModClusterDefinition.SECURITY_KEY,
                        ModClusterDefinition.ADVERTISE_PROTOCOL,
                        ModClusterDefinition.ADVERTISE_PATH,
                        ModClusterDefinition.ADVERTISE_FREQUENCY,
                        ModClusterDefinition.HEALTH_CHECK_INTERVAL,
                        ModClusterDefinition.BROKEN_NODE_TIMEOUT,
                        ModClusterDefinition.WORKER,
                        ModClusterDefinition.MAX_REQUEST_TIME,
                        ModClusterDefinition.MANAGEMENT_ACCESS_PREDICATE,
                        ModClusterDefinition.CONNECTIONS_PER_THREAD,
                        ModClusterDefinition.CACHED_CONNECTIONS_PER_THREAD,
                        ModClusterDefinition.CONNECTION_IDLE_TIMEOUT,
                        ModClusterDefinition.REQUEST_QUEUE_SIZE,
                        ModClusterDefinition.SECURITY_REALM,
                        ModClusterDefinition.USE_ALIAS,
                        ModClusterDefinition.ENABLE_HTTP2,
                        ModClusterDefinition.MAX_AJP_PACKET_SIZE,
                        ModClusterDefinition.HTTP2_MAX_HEADER_LIST_SIZE,
                        ModClusterDefinition.HTTP2_MAX_FRAME_SIZE,
                        ModClusterDefinition.HTTP2_MAX_CONCURRENT_STREAMS,
                        ModClusterDefinition.HTTP2_INITIAL_WINDOW_SIZE,
                        ModClusterDefinition.HTTP2_HEADER_TABLE_SIZE,
                        ModClusterDefinition.HTTP2_ENABLE_PUSH))
                .withCardinality(XMLCardinality.Unbounded.OPTIONAL);
        if (this.since(VERSION_4_0)) {
            builder.addAttributes(List.of(ModClusterDefinition.FAILOVER_STRATEGY, ModClusterDefinition.SSL_CONTEXT, ModClusterDefinition.MAX_RETRIES));
        }
        if (this.since(VERSION_10_0)) {
            builder.withContent(this.factory.choice()
                    .addElement(this.factory.singletonElement(NoAffinityResourceDefinition.REGISTRATION).withElementLocalName("no-affinity").build())
                    .addElement(this.factory.singletonElement(SingleAffinityResourceDefinition.REGISTRATION).withElementLocalName(ResourceXMLElementLocalName.VALUE_KEY).build())
                    .addElement(this.factory.singletonElement(RankedAffinityResourceDefinition.REGISTRATION).withElementLocalName(ResourceXMLElementLocalName.VALUE_KEY).addAttribute(RankedAffinityResourceDefinition.Attribute.DELIMITER.getDefinition()).build())
                    .build());
        }
        return builder.build();
    }

    private ResourceRegistrationXMLElement applicationSecurityDomain() {
        NamedResourceRegistrationXMLElement.Builder builder = this.factory.namedElement(ApplicationSecurityDomainDefinition.REGISTRATION)
                .addAttributes(List.of(ApplicationSecurityDomainDefinition.HTTP_AUTHENTICATION_FACTORY, ApplicationSecurityDomainDefinition.OVERRIDE_DEPLOYMENT_CONFIG, ApplicationSecurityDomainDefinition.ENABLE_JACC))
                .withCardinality(XMLCardinality.Unbounded.OPTIONAL)
                ;

        if (this.since(VERSION_7_0)) {
            builder.addAttribute(ApplicationSecurityDomainDefinition.SECURITY_DOMAIN);
        }
        if (this.since(VERSION_8_0)) {
            builder.addAttributes(List.of(ApplicationSecurityDomainDefinition.ENABLE_JASPI, ApplicationSecurityDomainDefinition.INTEGRATED_JASPI));
        }

        ResourceXMLSequence content = this.factory.sequence()
                .addElement(this.singleSignOn(EnumSet.allOf(ApplicationSecurityDomainSingleSignOnDefinition.Attribute.class), XMLContent.of(this.factory.sequence()
                        .addElement(ApplicationSecurityDomainSingleSignOnDefinition.Attribute.CREDENTIAL.get())
                        .build())))
                .build();
        return builder.withContent(content).build();
    }
}
