/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Random;
import java.util.concurrent.Executors;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.engine.view.cache.InMemoryIdentifierMap;
import com.opengamma.engine.view.calcnode.msg.Execute;
import com.opengamma.engine.view.calcnode.msg.Ready;
import com.opengamma.engine.view.calcnode.msg.RemoteCalcNodeMessage;
import com.opengamma.engine.view.calcnode.msg.Result;
import com.opengamma.engine.view.calcnode.stats.FunctionCosts;
import com.opengamma.transport.DirectFudgeConnection;
import com.opengamma.transport.FudgeConnection;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.Timeout;

/**
 * Tests the RemoteNodeJobInvoker
 */
@Test
public class RemoteNodeJobInvokerTest {

  private static final Logger s_logger = LoggerFactory.getLogger(RemoteNodeJobInvokerTest.class);
  private static final FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();
  private static final long TIMEOUT = Timeout.standardTimeoutMillis();

  public void simpleInvocation() {
    final JobDispatcher jobDispatcher = new JobDispatcher();
    final Ready initialMessage = new Ready(1);
    final DirectFudgeConnection conduit = new DirectFudgeConnection(s_fudgeContext);
    final RemoteNodeJobInvoker jobInvoker = new RemoteNodeJobInvoker(Executors.newCachedThreadPool(), initialMessage, conduit.getEnd1(), new InMemoryIdentifierMap(), new FunctionCosts ());
    jobDispatcher.registerJobInvoker(jobInvoker);
    final TestJobResultReceiver resultReceiver = new TestJobResultReceiver();
    final FudgeConnection remoteNode = conduit.getEnd2();
    remoteNode.setFudgeMessageReceiver(new FudgeMessageReceiver() {
      @Override
      public void messageReceived(FudgeContext fudgeContext, FudgeMsgEnvelope msgEnvelope) {
        final FudgeDeserializationContext dcontext = new FudgeDeserializationContext(fudgeContext);
        s_logger.debug("message = {}", msgEnvelope.getMessage());
        final RemoteCalcNodeMessage message = dcontext.fudgeMsgToObject(RemoteCalcNodeMessage.class, msgEnvelope.getMessage());
        assertNotNull(message);
        s_logger.debug("request = {}", message);
        assertTrue(message instanceof Execute);
        final Execute job = (Execute) message;
        final Result result = new Result(JobDispatcherTest.createTestJobResult(job.getJob().getSpecification(), 0, "Test"));
        final FudgeSerializationContext scontext = new FudgeSerializationContext(fudgeContext);
        remoteNode.getFudgeMessageSender().send(FudgeSerializationContext.addClassHeader(scontext.objectToFudgeMsg(result), result.getClass(), RemoteCalcNodeMessage.class));
      }
    });
    jobDispatcher.dispatchJob(JobDispatcherTest.createTestJob(), resultReceiver);
    assertNotNull(resultReceiver.waitForResult(TIMEOUT));
  }

  public void saturate() {
    final JobDispatcher jobDispatcher = new JobDispatcher();
    final Ready initialMessage = new Ready(3);
    final DirectFudgeConnection conduit = new DirectFudgeConnection(s_fudgeContext);
    final RemoteNodeJobInvoker jobInvoker = new RemoteNodeJobInvoker(Executors.newCachedThreadPool(), initialMessage, conduit.getEnd1(), new InMemoryIdentifierMap(), new FunctionCosts ());
    jobDispatcher.registerJobInvoker(jobInvoker);
    final FudgeConnection remoteNode = conduit.getEnd2();
    final Random rnd = new Random();
    remoteNode.setFudgeMessageReceiver(new FudgeMessageReceiver() {
      @Override
      public void messageReceived(FudgeContext fudgeContext, FudgeMsgEnvelope msgEnvelope) {
        final FudgeDeserializationContext dcontext = new FudgeDeserializationContext(fudgeContext);
        final RemoteCalcNodeMessage message = dcontext.fudgeMsgToObject(RemoteCalcNodeMessage.class, msgEnvelope.getMessage());
        assertNotNull(message);
        assertTrue(message instanceof Execute);
        final Execute job = (Execute) message;
        try {
          Thread.sleep(rnd.nextInt(30));
        } catch (InterruptedException e) {
        }
        final Result result = new Result(JobDispatcherTest.createTestJobResult(job.getJob().getSpecification(), 0, "Test"));
        final FudgeSerializationContext scontext = new FudgeSerializationContext(fudgeContext);
        remoteNode.getFudgeMessageSender().send(FudgeSerializationContext.addClassHeader(scontext.objectToFudgeMsg(result), result.getClass(), RemoteCalcNodeMessage.class));
      }
    });
    final TestJobResultReceiver[] resultReceivers = new TestJobResultReceiver[100];
    for (int i = 0; i < resultReceivers.length; i++) {
      resultReceivers[i] = new TestJobResultReceiver();
      jobDispatcher.dispatchJob(JobDispatcherTest.createTestJob(), resultReceivers[i]);
    }
    for (int i = 0; i < resultReceivers.length; i++) {
      assertNotNull(resultReceivers[i].waitForResult(TIMEOUT));
    }
  }
  
}
