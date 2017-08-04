/*
 * Copyright (C) 2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.loader.engine.internal.metrics;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.codahale.metrics.MetricRegistry;
import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.Statement;
import com.datastax.loader.connectors.api.Record;
import com.datastax.loader.connectors.api.internal.ErrorRecord;
import com.datastax.loader.connectors.api.internal.MapRecord;
import com.datastax.loader.engine.internal.schema.UnmappableStatement;
import com.datastax.loader.executor.api.statement.BulkSimpleStatement;
import io.reactivex.Flowable;
import java.net.URL;
import java.time.Duration;
import java.util.concurrent.Executors;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.reflection.Whitebox;
import org.slf4j.LoggerFactory;

public class MetricsManagerTest {

  @Mock private Appender<ILoggingEvent> mockAppender;
  private Appender<ILoggingEvent> stdout;
  private Level oldLevel;
  private Logger root;

  private Record record1;
  private Record record2;
  private Record record3;

  private Statement stmt1;
  private Statement stmt2;
  private Statement stmt3;

  private BatchStatement batch;

  @Before
  public void prepareMocks() {
    MockitoAnnotations.initMocks(this);
    when(mockAppender.getName()).thenReturn("MOCK");
    Logger logger = (Logger) LoggerFactory.getLogger("com.datastax.loader.engine.internal.metrics");
    logger.addAppender(mockAppender);
    oldLevel = logger.getLevel();
    logger.setLevel(Level.INFO);
    root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    stdout = root.getAppender("STDOUT");
    root.detachAppender(stdout);
  }

  @After
  public void restoreAppenders() {
    Logger logger = (Logger) LoggerFactory.getLogger("com.datastax.loader.engine.internal.metrics");
    logger.detachAppender(mockAppender);
    logger.setLevel(oldLevel);
    root.addAppender(stdout);
  }

  @Before
  public void setUp() throws Exception {
    URL location1 = new URL("file:///file1.csv?line=1");
    URL location2 = new URL("file:///file2.csv?line=2");
    URL location3 = new URL("file:///file3.csv?line=3");
    String source1 = "line1\n";
    String source2 = "line2\n";
    String source3 = "line3\n";
    record1 = new MapRecord(source1, location1, "irrelevant");
    record2 = new MapRecord(source2, location2, "irrelevant");
    record3 = new ErrorRecord(source3, location3, new RuntimeException("irrelevant"));
    stmt1 = new BulkSimpleStatement<>(record1, "irrelevant");
    stmt2 = new BulkSimpleStatement<>(record2, "irrelevant");
    stmt3 = new UnmappableStatement(location3, record3, new RuntimeException("irrelevant"));
    batch = new BatchStatement().add(stmt1).add(stmt2);
  }

  @Test
  public void should_increment_records() throws Exception {
    MetricsManager manager =
        new MetricsManager(
            "test",
            Executors.newSingleThreadScheduledExecutor(),
            SECONDS,
            MILLISECONDS,
            -1,
            -1,
            false,
            Duration.ofSeconds(5));
    manager.init();
    Flowable<Record> records = Flowable.fromArray(record1, record2, record3);
    records.compose(manager.newConnectorMonitor()).blockingSubscribe();
    manager.close();
    MetricRegistry registry = (MetricRegistry) Whitebox.getInternalState(manager, "registry");
    assertThat(registry.meter("records/total").getCount()).isEqualTo(3);
    assertThat(registry.meter("records/successful").getCount()).isEqualTo(2);
    assertThat(registry.meter("records/failed").getCount()).isEqualTo(1);
  }

  @Test
  public void should_increment_mappings() throws Exception {
    MetricsManager manager =
        new MetricsManager(
            "test",
            Executors.newSingleThreadScheduledExecutor(),
            SECONDS,
            MILLISECONDS,
            -1,
            -1,
            false,
            Duration.ofSeconds(5));
    manager.init();
    Flowable<Statement> statements = Flowable.fromArray(stmt1, stmt2, stmt3);
    statements.compose(manager.newMapperMonitor()).blockingSubscribe();
    manager.close();
    MetricRegistry registry = (MetricRegistry) Whitebox.getInternalState(manager, "registry");
    assertThat(registry.meter("mappings/total").getCount()).isEqualTo(3);
    assertThat(registry.meter("mappings/successful").getCount()).isEqualTo(2);
    assertThat(registry.meter("mappings/failed").getCount()).isEqualTo(1);
  }

  @Test
  public void should_increment_batches() throws Exception {
    MetricsManager manager =
        new MetricsManager(
            "test",
            Executors.newSingleThreadScheduledExecutor(),
            SECONDS,
            MILLISECONDS,
            -1,
            -1,
            false,
            Duration.ofSeconds(5));
    manager.init();
    Flowable<Statement> statements = Flowable.fromArray(batch, stmt3);
    statements.compose(manager.newBatcherMonitor()).blockingSubscribe();
    manager.close();
    MetricRegistry registry = (MetricRegistry) Whitebox.getInternalState(manager, "registry");
    assertThat(registry.histogram("batches/size").getCount()).isEqualTo(2);
    assertThat(registry.histogram("batches/size").getSnapshot().getMean())
        .isEqualTo((2f + 1f) / 2f);
  }
}