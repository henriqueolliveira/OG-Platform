/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.permission;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.livedata.entitlement.LiveDataEntitlementChecker;

/**
 * Returns instances of {@link PermissiveViewPermissionProvider}.
 */
public class PermissiveViewPermissionProviderFactory implements ViewPermissionProviderFactory {

  @Override
  public ViewPermissionProvider getViewPermissionProvider(SecuritySource securitySource,
      LiveDataEntitlementChecker entitlementChecker) {
    return new PermissiveViewPermissionProvider();
  }

}
