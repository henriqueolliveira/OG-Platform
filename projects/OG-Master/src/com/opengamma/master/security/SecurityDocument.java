/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.joda.beans.BeanDefinition;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectMetaProperty;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.AbstractDocument;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;

/**
 * A document used to pass into and out of the security master.
 */
@PublicSPI
@BeanDefinition
public class SecurityDocument extends AbstractDocument implements Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The security unique identifier.
   * This field is managed by the master but must be set for updates.
   */
  @PropertyDefinition
  private UniqueIdentifier _uniqueId;
  /**
   * The security.
   */
  @PropertyDefinition
  private ManageableSecurity _security;

  /**
   * Creates an instance.
   */
  public SecurityDocument() {
  }

  /**
   * Creates an instance from a security.
   * @param security  the security, not null
   */
  public SecurityDocument(final ManageableSecurity security) {
    ArgumentChecker.notNull(security, "security");
    setUniqueId(security.getUniqueId());
    setSecurity(security);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the name of the security.
   * <p>
   * This is derived from the security itself.
   * @return the name, null if no name
   */
  public String getName() {
    return (getSecurity() != null ? getSecurity().getName() : null);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code SecurityDocument}.
   * @return the meta-bean, not null
   */
  public static SecurityDocument.Meta meta() {
    return SecurityDocument.Meta.INSTANCE;
  }

  @Override
  public SecurityDocument.Meta metaBean() {
    return SecurityDocument.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName) {
    switch (propertyName.hashCode()) {
      case -294460212:  // uniqueId
        return getUniqueId();
      case 949122880:  // security
        return getSecurity();
    }
    return super.propertyGet(propertyName);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue) {
    switch (propertyName.hashCode()) {
      case -294460212:  // uniqueId
        setUniqueId((UniqueIdentifier) newValue);
        return;
      case 949122880:  // security
        setSecurity((ManageableSecurity) newValue);
        return;
    }
    super.propertySet(propertyName, newValue);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the security unique identifier.
   * This field is managed by the master but must be set for updates.
   * @return the value of the property
   */
  public UniqueIdentifier getUniqueId() {
    return _uniqueId;
  }

  /**
   * Sets the security unique identifier.
   * This field is managed by the master but must be set for updates.
   * @param uniqueId  the new value of the property
   */
  public void setUniqueId(UniqueIdentifier uniqueId) {
    this._uniqueId = uniqueId;
  }

  /**
   * Gets the the {@code uniqueId} property.
   * This field is managed by the master but must be set for updates.
   * @return the property, not null
   */
  public final Property<UniqueIdentifier> uniqueId() {
    return metaBean().uniqueId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the security.
   * @return the value of the property
   */
  public ManageableSecurity getSecurity() {
    return _security;
  }

  /**
   * Sets the security.
   * @param security  the new value of the property
   */
  public void setSecurity(ManageableSecurity security) {
    this._security = security;
  }

  /**
   * Gets the the {@code security} property.
   * @return the property, not null
   */
  public final Property<ManageableSecurity> security() {
    return metaBean().security().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code SecurityDocument}.
   */
  public static class Meta extends AbstractDocument.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code uniqueId} property.
     */
    private final MetaProperty<UniqueIdentifier> _uniqueId = DirectMetaProperty.ofReadWrite(this, "uniqueId", UniqueIdentifier.class);
    /**
     * The meta-property for the {@code security} property.
     */
    private final MetaProperty<ManageableSecurity> _security = DirectMetaProperty.ofReadWrite(this, "security", ManageableSecurity.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map;

    @SuppressWarnings({"unchecked", "rawtypes" })
    protected Meta() {
      LinkedHashMap temp = new LinkedHashMap(super.metaPropertyMap());
      temp.put("uniqueId", _uniqueId);
      temp.put("security", _security);
      _map = Collections.unmodifiableMap(temp);
    }

    @Override
    public SecurityDocument createBean() {
      return new SecurityDocument();
    }

    @Override
    public Class<? extends SecurityDocument> beanType() {
      return SecurityDocument.class;
    }

    @Override
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
      return _map;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code uniqueId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UniqueIdentifier> uniqueId() {
      return _uniqueId;
    }

    /**
     * The meta-property for the {@code security} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ManageableSecurity> security() {
      return _security;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
