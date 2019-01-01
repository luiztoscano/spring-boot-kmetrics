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
public class KafkaProducerMetrics extends KafkaMetrics {
	private static final String PRODUCER_METRICS = "kafka.producer";
	private static final String PRODUCER_GROUP = "producer-metrics";
	private static final String PRODUCER_PREFFIX = "kafka.producer";

	public KafkaProducerMetrics() {
		super();
	}

	public KafkaProducerMetrics(Iterable<Tag> tags, MBeanServer mBeanServer) {
		super(tags, mBeanServer);
	}

	public KafkaProducerMetrics(Iterable<Tag> tags) {
		super(tags);
	}

	@Override
	public void bindTo(MeterRegistry registry) {
		registerProducerMetrics(registry);
	}

	private void registerProducerMetrics(MeterRegistry registry) {
		String[] metricsGauge = { "produce-throttle-time-max", "select-rate", "outgoing-byte-rate", "batch-size-max",
				"produce-throttle-time-avg", "batch-split-rate", "request-rate", "buffer-available-bytes",
				"buffer-exhausted-rate", "response-rate", "record-send-rate", "record-queue-time-avg", "metadata-age",
				"network-io-rate", "io-ratio", "io-wait-ratio", "successful-authentication-rate", "request-size-max",
				"failed-authentication-rate", "record-queue-time-max", "waiting-threads", "incoming-byte-rate",
				"bufferpool-wait-ratio", "connection-close-rate", "request-size-avg", "records-per-request-avg",
				"connection-creation-rate", "record-size-avg", "request-latency-avg", "io-wait-time-ns-avg",
				"record-error-rate", "requests-in-flight", "io-time-ns-avg", "compression-rate-avg",
				"record-retry-rate", "request-latency-max", "record-size-max", "buffer-total-bytes", "batch-size-avg" };
		
		String[] metricsCounter = { "connection-creation-total", "bufferpool-wait-time-total", "batch-split-total",
				"connection-close-total", "record-send-total", "iotime-total", "successful-authentication-total",
				"io-waittime-total", "buffer-exhausted-total", "outgoing-byte-total", "request-total",
				"network-io-total", "incoming-byte-total", "response-total", "record-retry-total", "connection-count",
				"record-error-total", "failed-authentication-total", "select-total" };

		registerGauges(registry, PRODUCER_METRICS, PRODUCER_GROUP, PRODUCER_PREFFIX, Arrays.asList(metricsGauge));
		registerFunctionCounters(registry, PRODUCER_METRICS, PRODUCER_GROUP, PRODUCER_PREFFIX, Arrays.asList(metricsCounter));
	}

}
