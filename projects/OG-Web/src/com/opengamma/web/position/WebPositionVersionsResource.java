/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.position;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionHistoryRequest;
import com.opengamma.master.position.PositionHistoryResult;

/**
 * RESTful resource for all versions of a position.
 */
@Path("/positions/{positionId}/versions")
@Produces(MediaType.TEXT_HTML)
public class WebPositionVersionsResource extends AbstractWebPositionResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebPositionVersionsResource(final AbstractWebPositionResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  public String get() {
    PositionHistoryRequest request = new PositionHistoryRequest(data().getPosition().getUniqueId());
    PositionHistoryResult result = data().getPositionMaster().history(request);
    
    FlexiBean out = createRootData();
    out.put("versionsResult", result);
    out.put("versions", result.getPositions());
    return getFreemarker().build("positions/positionversions.ftl", out);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    PositionDocument doc = data().getPosition();
    out.put("positionDoc", doc);
    out.put("position", doc.getPosition());
    out.put("deleted", !doc.isLatest());
    return out;
  }

  //-------------------------------------------------------------------------
  @Path("{versionId}")
  public WebPositionVersionResource findVersion(@PathParam("versionId") String idStr) {
    data().setUriVersionId(idStr);
    PositionDocument doc = data().getPosition();
    UniqueIdentifier combined = doc.getUniqueId().withVersion(idStr);
    if (doc.getUniqueId().equals(combined) == false) {
      PositionDocument versioned = data().getPositionMaster().get(combined);
      data().setVersioned(versioned);
    } else {
      data().setVersioned(doc);
    }
    return new WebPositionVersionResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebPositionsData data) {
    String positionId = data.getBestPositionUriId(null);
    return data.getUriInfo().getBaseUriBuilder().path(WebPositionVersionsResource.class).build(positionId);
  }

}
