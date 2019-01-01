package org.kmetrics;

import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MBeanServerDelegate;
import javax.management.MBeanServerFactory;
import javax.management.MBeanServerNotification;
import javax.management.MalformedObjectNameException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;

/**
 * @author Luiz Toscano
 *
 * Based on TomcatMetrics (credits to Clint Checketts and Jon Schneider)
 */
public abstract class KafkaMetrics implements MeterBinder {
	private final Logger logger = LoggerFactory.getLogger(KafkaMetrics.class);
	private final Iterable<Tag> tags;
	private final MBeanServer mBeanServer;

	protected KafkaMetrics() {
		this(Tags.empty());
	}

	protected KafkaMetrics(Iterable<Tag> tags) {
		this(tags, getMBeanServer());
	}

	protected KafkaMetrics(Iterable<Tag> tags, MBeanServer mBeanServer) {
		this.tags = tags;
		this.mBeanServer = mBeanServer;
	}

	protected void registerGauges(MeterRegistry registry, String objName, String group, String preffix, List<String> metrics) {
		registerMetricsEventually(objName, "type", group, (name, allTags) -> {
			String clientId = name.getKeyProperty("client-id");

			metrics.forEach(metric -> {
				String metricName = preffix + "." + metric;

				Gauge.builder(metricName, mBeanServer, s -> safeDouble(() -> s.getAttribute(name, metric)))
						.tags(Tags.of("cliendId", clientId)).register(registry);

			});
		});
	}

	protected void registerFunctionCounters(MeterRegistry registry, String objName, String group, String preffix,
			List<String> metrics) {
		registerMetricsEventually(objName, "type", group, (name, allTags) -> {
			String clientId = name.getKeyProperty("client-id");

			metrics.forEach(metric -> {
				String metricName = preffix + "." + metric;

				FunctionCounter.builder(metricName, mBeanServer, s -> safeDouble(() -> s.getAttribute(name, metric)))
						.tags(Tags.of("clientId", clientId)).register(registry);
			});
		});
	}

	protected void registerMetricsEventually(String objName, String key, String value,
			BiConsumer<ObjectName, Iterable<Tag>> perObject) {
		try {
			Set<ObjectName> objs = mBeanServer.queryNames(new ObjectName(objName + ":" + key + "=" + value + ",*"),
					null);
			if (!objs.isEmpty()) {
				// MBean is present, so we can register metrics now.
				objs.forEach(o -> perObject.accept(o, Tags.concat(tags, nameTag(o))));
				return;
			}
		} catch (MalformedObjectNameException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException("Error registering Kafka JMX based metrics", e);
		}

		// MBean isn't yet registered, so we'll set up a notification to wait for them
		// to be present and register metrics later
		NotificationListener notificationListener = (notification, handback) -> {
			MBeanServerNotification mbs = (MBeanServerNotification) notification;
			ObjectName obj = mbs.getMBeanName();
			perObject.accept(obj, Tags.concat(tags, nameTag(obj)));
		};

		NotificationFilter filter = (NotificationFilter) notification -> {
			if (!MBeanServerNotification.REGISTRATION_NOTIFICATION.equals(notification.getType()))
				return false;

			// we can safely downcast now
			ObjectName obj = ((MBeanServerNotification) notification).getMBeanName();
			return obj.getDomain().equals(objName) && obj.getKeyProperty(key).equals(value);

		};

		try {
			mBeanServer.addNotificationListener(MBeanServerDelegate.DELEGATE_NAME, notificationListener, filter, null);
		} catch (InstanceNotFoundException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException("Error registering MBean listener", e);
		}
	}

	private static MBeanServer getMBeanServer() {
		List<MBeanServer> mBeanServers = MBeanServerFactory.findMBeanServer(null);
		if (!mBeanServers.isEmpty()) {
			return mBeanServers.get(0);
		}
		return ManagementFactory.getPlatformMBeanServer();
	}

	private double safeDouble(Callable<Object> callable) {
		try {
			return Double.parseDouble(callable.call().toString());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return 0.0;
		}
	}

	private Iterable<Tag> nameTag(ObjectName name) {
		if (name.getKeyProperty("name") != null) {
			return Tags.of("name", name.getKeyProperty("name").replaceAll("\"", ""));
		} else {
			return Collections.emptyList();
		}
	}

}
