package org.kmetrics;

import java.util.Arrays;

import javax.management.MBeanServer;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;

/**
 * @author Luiz Toscano
 *
 */
@Component
public class KafkaConsumerMetrics extends KafkaMetrics {
	private static final String CONSUMER_METRICS = "kafka.consumer";
	private static final String CONSUMER_GROUP = "consumer-metrics";
	private static final String CONSUMER_PREFFIX = "kafka.consumer";
	private static final String CONSUMER_COORDINATOR_GROUP = "consumer-coordinator-metrics";
	private static final String CONSUMER_COORDINATOR_PREFFIX = "kafka.consumer.coordinator";
	private static final String CONSUMER_FETCH_MANAGER_GROUP = "consumer-fetch-manager-metrics";
	private static final String CONSUMER_FETCH_MANAGER_PREFFIX = "kafka.consumer.fetch.manager";

	public KafkaConsumerMetrics() {
		super();
	}

	public KafkaConsumerMetrics(Iterable<Tag> tags, MBeanServer mBeanServer) {
		super(tags, mBeanServer);
	}

	public KafkaConsumerMetrics(Iterable<Tag> tags) {
		super(tags);
	}

	@Override
	public void bindTo(MeterRegistry registry) {
		registerConsumerMetrics(registry);
		registerConsumerCoordinatorMetrics(registry);
		registerConsumerFetchManagerMetrics(registry);
	}

	private void registerConsumerMetrics(MeterRegistry registry) {
		String[] metricsGauge = { "response-rate", "select-rate", "network-io-rate", "io-ratio", "io-wait-ratio",
				"outgoing-byte-rate", "successful-authentication-rate", "failed-authentication-rate",
				"incoming-byte-rate", "connection-close-rate", "request-size-max", "request-size-avg", "iotime-total",
				"connection-creation-rate", "io-wait-time-ns-avg", "io-time-ns-avg", "request-rate" };

		String[] metricsCounter = { "connection-creation-total", "connection-close-total", "request-total",
				"network-io-total", "incoming-byte-total", "response-total", "iotime-total",
				"successful-authentication-total", "connection-count", "io-waittime-total",
				"failed-authentication-total", "select-total", "outgoing-byte-total" };

		registerGauges(registry, CONSUMER_METRICS, CONSUMER_GROUP, CONSUMER_PREFFIX, Arrays.asList(metricsGauge));
		registerFunctionCounters(registry, CONSUMER_METRICS, CONSUMER_GROUP, CONSUMER_PREFFIX,
				Arrays.asList(metricsCounter));
	}

	private void registerConsumerCoordinatorMetrics(MeterRegistry registry) {
		String[] metricsGauge = { "join-time-max", "commit-latency-avg", "sync-time-avg", "join-rate",
				"assigned-partitions", "sync-rate", "commit-rate", "last-heartbeat-seconds-ago", "heartbeat-rate",
				"commit-latency-max", "join-time-avg", "sync-time-max", "heartbeat-response-time-max" };

		String[] metricsCounter = { "sync-total", "commit-total", "heartbeat-total", "join-total" };

		registerGauges(registry, CONSUMER_METRICS, CONSUMER_COORDINATOR_GROUP, CONSUMER_COORDINATOR_PREFFIX,
				Arrays.asList(metricsGauge));
		registerFunctionCounters(registry, CONSUMER_METRICS, CONSUMER_COORDINATOR_GROUP,
				CONSUMER_COORDINATOR_PREFFIX, Arrays.asList(metricsCounter));
	}

	private void registerConsumerFetchManagerMetrics(MeterRegistry registry) {

		String[] metricsGauge = { "bytes-consumed-rate", "fetch-latency-max", "fetch-rate", "fetch-throttle-time-max",
				"fetch-size-max", "fetch-latency-avg", "records-lag-max", "records-consumed-rate",
				"fetch-throttle-time-avg", "fetch-size-avg", "records-per-request-avg" };

		String[] metricsCounter = { "fetch-total", "records-consumed-total", "bytes-consumed-total" };

		registerGauges(registry, CONSUMER_METRICS, CONSUMER_FETCH_MANAGER_GROUP, CONSUMER_FETCH_MANAGER_PREFFIX,
				Arrays.asList(metricsGauge));
		registerFunctionCounters(registry, CONSUMER_METRICS, CONSUMER_FETCH_MANAGER_GROUP,
				CONSUMER_FETCH_MANAGER_PREFFIX, Arrays.asList(metricsCounter));
	}

}
