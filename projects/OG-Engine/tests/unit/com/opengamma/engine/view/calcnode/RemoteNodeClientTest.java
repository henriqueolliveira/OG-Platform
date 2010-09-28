/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.junit.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionCompilationService;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.test.TestCalculationNode;
import com.opengamma.engine.view.cache.CacheSelectHint;
import com.opengamma.engine.view.cache.IdentifierMap;
import com.opengamma.engine.view.cache.InMemoryIdentifierMap;
import com.opengamma.engine.view.calcnode.msg.Execute;
import com.opengamma.engine.view.calcnode.msg.Failure;
import com.opengamma.engine.view.calcnode.msg.IsAlive;
import com.opengamma.engine.view.calcnode.msg.Ready;
import com.opengamma.engine.view.calcnode.msg.RemoteCalcNodeMessage;
import com.opengamma.engine.view.calcnode.msg.Result;
import com.opengamma.engine.view.calcnode.stats.FunctionInvocationStatisticsSender;
import com.opengamma.transport.CollectingFudgeMessageReceiver;
import com.opengamma.transport.DirectFudgeConnection;
import com.opengamma.util.fudge.OpenGammaFudgeContext;

/**
 * Tests RemoteNodeClient
 */
public class RemoteNodeClientTest {

  private static final FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();
  private static final long TIMEOUT = 1000L;

  protected static CalculationJob createTestCalculationJob() {
    return new CalculationJob(JobDispatcherTest.createTestJobSpec(), null, JobDispatcherTest.createTestJobItems(), CacheSelectHint.allShared());
  }

  @Test
  public void simpleInvocation() {
    final IdentifierMap identifierMap = new InMemoryIdentifierMap ();
    final DirectFudgeConnection conduit = new DirectFudgeConnection(s_fudgeContext);
    final CollectingFudgeMessageReceiver messages = new CollectingFudgeMessageReceiver();
    conduit.getEnd2().setFudgeMessageReceiver(messages);
    final RemoteNodeClient client = new RemoteNodeClient(conduit.getEnd1(), new FunctionCompilationService (new InMemoryFunctionRepository (), new FunctionCompilationContext ()), new InMemoryIdentifierMap (), new FunctionInvocationStatisticsSender ());
    final AbstractCalculationNode node = new TestCalculationNode();
    assertEquals(0, messages.getMessages().size());
    client.addNode(node);
    assertEquals(0, messages.getMessages().size());
    client.start();
    assertEquals(1, messages.getMessages().size());
    final FudgeMsgEnvelope readyMsgEnvelope = messages.getMessages().get(0);
    messages.clear();
    final FudgeDeserializationContext dcontext = new FudgeDeserializationContext(s_fudgeContext);
    final FudgeSerializationContext scontext = new FudgeSerializationContext(s_fudgeContext);
    final RemoteCalcNodeMessage readyMessage = dcontext.fudgeMsgToObject(RemoteCalcNodeMessage.class, readyMsgEnvelope.getMessage());
    assertTrue(readyMessage instanceof Ready);
    final Ready ready = (Ready) readyMessage;
    assertEquals(1, ready.getCapacity());
    final CalculationJob job = createTestCalculationJob();
    job.convertInputs(identifierMap);
    conduit.getEnd2().getFudgeMessageSender().send(
        FudgeSerializationContext.addClassHeader(scontext.objectToFudgeMsg(new Execute(job)), Execute.class, RemoteCalcNodeMessage.class));
    final FudgeMsgEnvelope resultMsgEnvelope = messages.waitForMessage(TIMEOUT);
    assertNotNull(resultMsgEnvelope);
    final RemoteCalcNodeMessage resultMessage = dcontext.fudgeMsgToObject(RemoteCalcNodeMessage.class, resultMsgEnvelope.getMessage());
    assertTrue(resultMessage instanceof Result);
    final Result result = (Result) resultMessage;
    assertEquals(job.getSpecification(), result.getResult().getSpecification());
  }
  
  @Test
  public void isAlive () {
    final IdentifierMap identifierMap = new InMemoryIdentifierMap ();
    final DirectFudgeConnection conduit = new DirectFudgeConnection(s_fudgeContext);
    final CollectingFudgeMessageReceiver messages = new CollectingFudgeMessageReceiver();
    conduit.getEnd2().setFudgeMessageReceiver(messages);
    final RemoteNodeClient client = new RemoteNodeClient(conduit.getEnd1(), new FunctionCompilationService (new InMemoryFunctionRepository (), new FunctionCompilationContext ()), new InMemoryIdentifierMap (), new FunctionInvocationStatisticsSender ());
    client.start();
    assertEquals(1, messages.getMessages().size());
    final FudgeMsgEnvelope readyMsgEnvelope = messages.getMessages().get(0);
    messages.clear();
    final FudgeDeserializationContext dcontext = new FudgeDeserializationContext(s_fudgeContext);
    final FudgeSerializationContext scontext = new FudgeSerializationContext(s_fudgeContext);
    final RemoteCalcNodeMessage readyMessage = dcontext.fudgeMsgToObject(RemoteCalcNodeMessage.class, readyMsgEnvelope.getMessage());
    assertTrue(readyMessage instanceof Ready);
    final CalculationJob job1 = createTestCalculationJob();
    final CalculationJob job2 = createTestCalculationJob();
    job1.convertInputs(identifierMap);
    conduit.getEnd2().getFudgeMessageSender().send(FudgeSerializationContext.addClassHeader(scontext.objectToFudgeMsg(new Execute(job1)), Execute.class, RemoteCalcNodeMessage.class));
    conduit.getEnd2().getFudgeMessageSender().send(FudgeSerializationContext.addClassHeader(scontext.objectToFudgeMsg(new IsAlive(Arrays.asList (job1.getSpecification (), job2.getSpecification ()))), IsAlive.class, RemoteCalcNodeMessage.class));
    final FudgeMsgEnvelope resultMsgEnvelope = messages.waitForMessage(TIMEOUT);
    assertNotNull(resultMsgEnvelope);
    final RemoteCalcNodeMessage failureMessage = dcontext.fudgeMsgToObject(RemoteCalcNodeMessage.class, resultMsgEnvelope.getMessage());
    assertTrue(failureMessage instanceof Failure);
    final Failure failure = (Failure) failureMessage;
    assertEquals(job2.getSpecification(), failure.getJob());
    // No more messages - job1 is alive
    assertNull (messages.waitForMessage(TIMEOUT));
  }

  @Test
  public void errorInvocation() {
    final IdentifierMap identifierMap = new InMemoryIdentifierMap ();
    final DirectFudgeConnection conduit = new DirectFudgeConnection(s_fudgeContext);
    final CollectingFudgeMessageReceiver messages = new CollectingFudgeMessageReceiver();
    conduit.getEnd2().setFudgeMessageReceiver(messages);
    final RemoteNodeClient client = new RemoteNodeClient(conduit.getEnd1(), new FunctionCompilationService (new InMemoryFunctionRepository (), new FunctionCompilationContext ()), new InMemoryIdentifierMap (), new FunctionInvocationStatisticsSender ());
    final AbstractCalculationNode failingNode = new TestCalculationNode() {
      
      @Override
      public CalculationJobResult executeJob(CalculationJob job) {
        throw new OpenGammaRuntimeException ("Remote node not working"); 
      }
      
    };
    assertEquals(0, messages.getMessages().size());
    client.addNode(failingNode);
    assertEquals(0, messages.getMessages().size());
    client.start();
    assertEquals(1, messages.getMessages().size());
    final FudgeMsgEnvelope readyMsgEnvelope = messages.getMessages().get(0);
    messages.clear();
    final FudgeDeserializationContext dcontext = new FudgeDeserializationContext(s_fudgeContext);
    final FudgeSerializationContext scontext = new FudgeSerializationContext(s_fudgeContext);
    final RemoteCalcNodeMessage readyMessage = dcontext.fudgeMsgToObject(RemoteCalcNodeMessage.class, readyMsgEnvelope.getMessage());
    assertTrue(readyMessage instanceof Ready);
    final Ready ready = (Ready) readyMessage;
    assertEquals(1, ready.getCapacity());
    final CalculationJob job = createTestCalculationJob();
    job.convertInputs(identifierMap);
    conduit.getEnd2().getFudgeMessageSender().send(
        FudgeSerializationContext.addClassHeader(scontext.objectToFudgeMsg(new Execute(job)), Execute.class, RemoteCalcNodeMessage.class));
    final FudgeMsgEnvelope resultMsgEnvelope = messages.waitForMessage(TIMEOUT);
    assertNotNull(resultMsgEnvelope);
    final RemoteCalcNodeMessage resultMessage = dcontext.fudgeMsgToObject(RemoteCalcNodeMessage.class, resultMsgEnvelope.getMessage());
    assertTrue(resultMessage instanceof Failure);
    final Failure failure = (Failure) resultMessage;
    assertEquals(job.getSpecification(), failure.getJob ());
  }

}