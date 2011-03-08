/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import java.net.URI;

import javax.ws.rs.Path;

/**
 * RESTful resource for all bundles
 */
@Path("/bundles")
public class WebBundlesResource extends AbstractWebBundleResource {
  
  /**
   * Creates a WebBundlesResource
   * 
   * @param bundleManager        the development bundle manager, not null
   * @param compressedBundleSource  the compressed bundle source, not null
   * @param mode                    the deployment mode, DEV or PROD
   */
  public WebBundlesResource(BundleManager bundleManager, CompressedBundleSource compressedBundleSource, DeployMode mode) {
    super(bundleManager, compressedBundleSource, mode);
  }
  
  @Path("fm/{file : .*}")
  public WebFreemarkerResource findFreemarker() {
    return new WebFreemarkerResource(this);
  }
  
  @Path("dev/{bundleId}")
  public WebDevBundleResource findDevBundle() {
    return new WebDevBundleResource(this);
  }
  
  @Path("prod/{bundleId}")
  public WebProdBundleResource findProdBundle() {
    return new WebProdBundleResource(this);
  }
  
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebBundlesData data) {
    return data.getUriInfo().getBaseUriBuilder().path(WebBundlesResource.class).build();
  }

}