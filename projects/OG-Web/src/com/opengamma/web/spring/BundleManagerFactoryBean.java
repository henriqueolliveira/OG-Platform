/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.spring;

import com.opengamma.web.bundle.BundleManager;
import com.opengamma.web.bundle.BundleParser;

/**
 * Creates a BundleManager for Production from the Bundle XML configuration file.
 */
public class BundleManagerFactoryBean extends AbstractBundleManagerFactoryBean {

  @Override
  protected BundleManager createObject() {
    BundleParser parser = new BundleParser(resolveConfigurationFile(), resolveBaseDir());
    return parser.parse();
  }

}
