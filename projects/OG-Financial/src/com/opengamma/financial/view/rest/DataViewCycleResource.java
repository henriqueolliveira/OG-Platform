/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.Providers;

import com.opengamma.engine.view.calc.ComputationCacheQuery;
import com.opengamma.engine.view.calc.ComputationCacheResponse;
import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for a {@link ViewCycle}
 */
public class DataViewCycleResource extends AbstractDataResource {

  //CSOFF: just constants
  public static final String PATH_UNIQUE_ID = "id";
  public static final String PATH_VIEW_PROCESS_ID = "viewProcessId";
  public static final String PATH_STATE = "state";
  public static final String PATH_DURATION = "duration";
  public static final String PATH_COMPILED_VIEW_DEFINITION = "compiledViewDefinition";
  public static final String PATH_RESULT = "result";
  public static final String PATH_QUERY_CACHES = "queryCaches";
  //CSON: just constants
  
  private final ViewCycle _cycle;
  
  public DataViewCycleResource(ViewCycle cycle) {
    _cycle = cycle;
  }
  
  @GET
  @Path(PATH_UNIQUE_ID)
  public Response getUniqueIdentifier() {
    return Response.ok(_cycle.getUniqueId()).build();
  }
  
  @GET
  @Path(PATH_VIEW_PROCESS_ID)
  public Response getViewProcessId() {
    return Response.ok(_cycle.getViewProcessId()).build();
  }
  
  @GET
  @Path(PATH_STATE)
  public Response getState() {
    return Response.ok(_cycle.getState()).build();
  }
  
  @GET
  @Path(PATH_DURATION)
  public Response getDuration() {
    return Response.ok(_cycle.getDurationNanos()).build();
  }
  
  @Path(PATH_COMPILED_VIEW_DEFINITION)
  public DataCompiledViewDefinitionResource getCompiledViewDefinition() {
    return new DataCompiledViewDefinitionResource(_cycle.getCompiledViewDefinition());
  }
  
  @GET
  @Path(PATH_RESULT)
  public Response getResult() {
    return Response.ok(_cycle.getResultModel()).build();
  }
  
  @GET
  @Path(PATH_QUERY_CACHES)
  public Response queryComputationCaches(@Context Providers providers, @QueryParam("msg") String msgBase64) {
    ComputationCacheQuery query = decodeBean(ComputationCacheQuery.class, providers, msgBase64);
    ComputationCacheResponse result = _cycle.queryComputationCaches(query);
    return Response.ok(result).build();
  }
  
  //-------------------------------------------------------------------------
  public static URI uriQuery(URI baseUri, String msgBase64) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path(PATH_QUERY_CACHES);
    if (msgBase64 != null) {
      bld.queryParam("msg", msgBase64);
    }
    return bld.build();
  }
  
}
