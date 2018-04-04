/**
 * Copyright (C) Davinta Technologies 2017. All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Davinta Technologies. You shall not disclose such Confidential Information
 * and shall use it only in accordance with the terms and conditions
 * entered into with Davinta Technologies.
 */

package com.davinta.aeus.service.back;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import org.springframework.integration.endpoint.PollingConsumer;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.PollableChannel;
import org.springframework.scheduling.support.PeriodicTrigger;

import com.davinta.aeus.util.kafka.AsyncRequestMessageConsumer;
import com.davinta.aeus.util.logging.PlatformLogger;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

import uk.org.lidalia.sysoutslf4j.context.SysOutOverSLF4J;

/**
 * The Class ServiceCustomerApplication.
 * @author Jawid Musthafa P.A
 */
@SpringBootApplication
@EnableAutoConfiguration
@EnableConfigurationProperties
@EnableEncryptableProperties
@ComponentScan({ "com.davinta.aeus.util", "com.davinta.aeus.framework", "com.davinta.aeus.integration", "com.davinta.aeus.api", "com.davinta.aeus.service",
		"com.davinta.aeus.bom" })
public class Application {

	/** The logger. */
	PlatformLogger logger = PlatformLogger.getLogger(Application.class);

	/** The key store. */
	@Value("${rest.client.ssl.key-store:#{null}}")
	private String keyStore;

	/** The key store password. */
	@Value("${rest.client.ssl.key-store-password:#{null}}")
	private String keyStorePassword;

	/** The trust store. */
	@Value("${rest.client.ssl.trust-store:#{null}}")
	private String trustStore;

	/** The trust store password. */
	@Value("${rest.client.ssl.trust-store-password:#{null}}")
	private String trustStorePassword;

	/** The hostname verifier enabled. */
	@Value("${rest.client.ssl.hostname.verifier.enabled:true}")
	private boolean hostnameVerifierEnabled;

	@Autowired
	@Qualifier("fromKafka")
	PollableChannel fromKafka;

	@Autowired
	@Qualifier("errorChannel")
	PollableChannel errorChannel;

	/**
	 * Sets the java ssl configurations.
	 * @param sslEnabled the new java ssl configurations.
	 */
	@Value("${rest.client.ssl.enabled:false}")
	public void setJavaSslConfigurations(boolean sslEnabled) {
		if (sslEnabled) {

			logger.info("Application.setJavaSslConfigurations(): SSL is enabled");

			if (!hostnameVerifierEnabled) {
				javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier((hostname, sslSession) -> true);
				logger.info("Application.setJavaSslConfigurations(): After setting default host name verifier");

			}
			if (keyStore != null) {
				System.setProperty("javax.net.ssl.keyStore", keyStore);
				System.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword);
				logger.info("Application.setJavaSslConfigurations(): After setting key store password");
			}
			if (trustStore != null) {
				System.setProperty("javax.net.ssl.trustStore", trustStore);
				System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
				logger.info("Application.setJavaSslConfigurations(): After setting trust store password");
			}
		}
	}

	/**
	 * Create Embedded Servlet Container.
	 * @return servletContainer Bean
	 */
	@Bean
	public EmbeddedServletContainerFactory servletContainer() {
		return new TomcatEmbeddedServletContainerFactory();
	}

	@Bean
	AsyncRequestMessageConsumer asyncRequestMessageConsumer() {
		return new AsyncRequestMessageConsumer();
	}

	public MessageHandler messageHandler() {
		return (Message<?> m) -> {
			logger.info("Recieved-" + m.toString());
			asyncRequestMessageConsumer().handleMessage(m);
		};
	}

	public MessageHandler errorhandler() {
		return (Message<?> m) -> {
			logger.info("Error-" + m.toString());
			Object value = m.getHeaders().get("custom_header");
			logger.info(value + "");
		};
	}

	@Bean
	public PollingConsumer messageConsumer() {
		PollingConsumer consumer = new PollingConsumer(fromKafka, messageHandler());
		consumer.setTrigger(defaultPoller().getTrigger());
		return consumer;
	}

	@Bean
	public PollingConsumer errorConsumer() {
		PollingConsumer consumer = new PollingConsumer(errorChannel, errorhandler());
		consumer.setTrigger(defaultPoller().getTrigger());
		return consumer;
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public PollerMetadata defaultPoller() {
		PollerMetadata pollerMetadata = new PollerMetadata();
		pollerMetadata.setTrigger(new PeriodicTrigger(500));
		return pollerMetadata;
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		SysOutOverSLF4J.sendSystemOutAndErrToSLF4J();
		SpringApplication application = new SpringApplication(Application.class);
		application.run(args);
	}

}
