/*
 * Copyright (C) 2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.loader.executor.api.ccm;

import com.datastax.loader.executor.api.BulkExecutor;
import com.datastax.loader.executor.api.DefaultRxJavaBulkExecutor;
import com.datastax.loader.executor.api.listener.ExecutionListener;
import com.datastax.loader.tests.ccm.annotations.CCMTest;
import com.datastax.driver.core.Session;

@CCMTest
public class RxJavaReadWriteIT extends AbstractReadWriteIT {

  protected BulkExecutor getBulkExecutor(ExecutionListener listener, Session session) {
    return DefaultRxJavaBulkExecutor.builder(session).withExecutionListener(listener).build();
  }

}