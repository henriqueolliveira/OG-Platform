/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.config;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinition;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.web.json.CurveSpecificationBuilderConfigurationJSONBuilder;
import com.opengamma.web.json.ViewDefinitionJSONBuilder;
import com.opengamma.web.json.YieldCurveDefinitionJSONBuilder;

/**
 * RESTful resource for a configuration document.
 * 
 */
@Path("/configs/{configId}")
public class WebConfigResource extends AbstractWebConfigResource {
  
  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebConfigResource(final AbstractWebConfigResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getHTML() {
    FlexiBean out = createRootData();
    ConfigDocument<?> doc = data().getConfig();
    out.put("configXml", createXML(doc));
    return getFreemarker().build("configs/config.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getJSON() {
    FlexiBean out = createRootData();
    ConfigDocument<?> doc = data().getConfig();
   
    String json = toJSON(doc.getValue());
    if (json != null) {
      out.put("configData", json);
    } else {
      out.put("configData", StringEscapeUtils.escapeJavaScript(createXML(doc)));
    }
    out.put("type", data().getTypeMap().inverse().get(doc.getType()));
    return getFreemarker().build("configs/jsonconfig.ftl", out);
  }

  private String toJSON(final Object config) {
    if (config.getClass().isAssignableFrom(ViewDefinition.class)) {
      return  new ViewDefinitionJSONBuilder().toJSON((ViewDefinition) config);
    }
    if (config.getClass().isAssignableFrom(YieldCurveDefinition.class)) {
      return new YieldCurveDefinitionJSONBuilder().toJSON((YieldCurveDefinition) config);
    }
    if (config.getClass().isAssignableFrom(CurveSpecificationBuilderConfiguration.class)) {
      return new CurveSpecificationBuilderConfigurationJSONBuilder().toJSON((CurveSpecificationBuilderConfiguration) config);
    }
    return null;
  }

  //-------------------------------------------------------------------------
  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Response putHTML(
      @FormParam("name") String name,
      @FormParam("configxml") String xml) {
    if (data().getConfig().isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }
    
    name = StringUtils.trimToNull(name);
    xml = StringUtils.trimToNull(xml);
    if (name == null || xml == null) {
      FlexiBean out = createRootData();
      if (name == null) {
        out.put("err_nameMissing", true);
      }
      if (xml == null) {
        out.put("err_xmlMissing", true);
      }
      String html = getFreemarker().build("configs/config-update.ftl", out);
      return Response.ok(html).build();
    }
    
    URI uri = updateConfig(name, parseXML(xml).getFirst());
    return Response.seeOther(uri).build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putJSON(
      @FormParam("name") String name,
      @FormParam("configJSON") String json) {
    if (data().getConfig().isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }
    
    name = StringUtils.trimToNull(name);
    json = StringUtils.trimToNull(json);
    // JSON allows a null config to just change the name
    if (name == null) {
      return Response.status(Status.BAD_REQUEST).build();
    }
    updateConfig(name, json != null ? parseJSON(json).getFirst() : null);
    return Response.ok().build();
  }

  @SuppressWarnings({"unchecked", "rawtypes" })
  private URI updateConfig(String name, Object newConfigValue) {
    ConfigDocument<?> oldDoc = data().getConfig();
    ConfigDocument doc = new ConfigDocument(oldDoc.getType());
    doc.setUniqueId(oldDoc.getUniqueId());
    doc.setName(name);
    if (newConfigValue != null) {  // null means only update the name
      doc.setValue(newConfigValue);
    }
    doc = data().getConfigMaster().update(doc);
    data().setConfig(doc);
    URI uri = WebConfigResource.uri(data());
    return uri;
  }

  //-------------------------------------------------------------------------
  @DELETE
  @Produces(MediaType.TEXT_HTML)
  public Response deleteHTML() {
    ConfigDocument<?> doc = data().getConfig();
    if (doc.isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }
    data().getConfigMaster().remove(doc.getUniqueId());
    URI uri = WebConfigsResource.uri(data());
    return Response.seeOther(uri).build();
  }

  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteJSON() {
    ConfigDocument<?> doc = data().getConfig();
    if (doc.isLatest()) {
      data().getConfigMaster().remove(doc.getUniqueId());
    }
    return Response.ok().build();
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    ConfigDocument<?> doc = data().getConfig();
    out.put("configDoc", doc);
    out.put("config", doc.getValue());
    out.put("deleted", !doc.isLatest());
    return out;
  }

  //-------------------------------------------------------------------------
  @Path("versions")
  public WebConfigVersionsResource findVersions() {
    return new WebConfigVersionsResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebConfigData data) {
    return uri(data, null);
  }

  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @param overrideConfigId  the override config id, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebConfigData data, final UniqueIdentifier overrideConfigId) {
    String configId = data.getBestConfigUriId(overrideConfigId);
    return data.getUriInfo().getBaseUriBuilder().path(WebConfigResource.class).build(configId);
  }

}
