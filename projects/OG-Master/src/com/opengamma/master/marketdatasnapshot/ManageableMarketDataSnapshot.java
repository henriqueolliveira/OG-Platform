/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.marketdatasnapshot;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.joda.beans.BeanDefinition;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.BasicMetaBean;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectMetaProperty;

import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.UnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.core.marketdatasnapshot.YieldCurveSnapshot;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.PublicSPI;

/**
 * A snapshot of market data potentially altered by hand
 */
@BeanDefinition
@PublicSPI
public class ManageableMarketDataSnapshot extends DirectBean implements StructuredMarketDataSnapshot {

  /**
   * The unique identifier of the snapshot.
   * This must be null when adding to a master and not null when retrieved from a master.
   */
  @PropertyDefinition
  private UniqueIdentifier _uniqueId;
  
  /**
   * The name of the snapshot intended for display purposes.
   * This field must not be null for the object to be valid.
   */
  @PropertyDefinition
  private String _name;

  /**
   * The name of the view on which this snapshot was based
   */
  @PropertyDefinition
  private String _basisViewName;
  
  @PropertyDefinition
  private UnstructuredMarketDataSnapshot _globalValues;

  /**
   * The yield curves in this snapshot
   */
  @PropertyDefinition
  private Map<YieldCurveKey, YieldCurveSnapshot> _yieldCurves;
  
  /**
   * Creates a snapshot
   */
  public ManageableMarketDataSnapshot() {
    super();
  }
  
  /**
   * Creates a snapshot
   * @param name the name of the snapshot
   * @param globalValues the snapshot for the global scope
   * @param yieldCurves the yield curves
   */
  public ManageableMarketDataSnapshot(String name, UnstructuredMarketDataSnapshot globalValues,
      Map<YieldCurveKey, YieldCurveSnapshot> yieldCurves) {
    super();
    _name = name;
    _globalValues = globalValues;
    _yieldCurves = yieldCurves;
  }


  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ManageableMarketDataSnapshot}.
   * @return the meta-bean, not null
   */
  public static ManageableMarketDataSnapshot.Meta meta() {
    return ManageableMarketDataSnapshot.Meta.INSTANCE;
  }

  @Override
  public ManageableMarketDataSnapshot.Meta metaBean() {
    return ManageableMarketDataSnapshot.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName) {
    switch (propertyName.hashCode()) {
      case -294460212:  // uniqueId
        return getUniqueId();
      case 3373707:  // name
        return getName();
      case 858810670:  // basisViewName
        return getBasisViewName();
      case -591591771:  // globalValues
        return getGlobalValues();
      case 119589713:  // yieldCurves
        return getYieldCurves();
    }
    return super.propertyGet(propertyName);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void propertySet(String propertyName, Object newValue) {
    switch (propertyName.hashCode()) {
      case -294460212:  // uniqueId
        setUniqueId((UniqueIdentifier) newValue);
        return;
      case 3373707:  // name
        setName((String) newValue);
        return;
      case 858810670:  // basisViewName
        setBasisViewName((String) newValue);
        return;
      case -591591771:  // globalValues
        setGlobalValues((UnstructuredMarketDataSnapshot) newValue);
        return;
      case 119589713:  // yieldCurves
        setYieldCurves((Map<YieldCurveKey, YieldCurveSnapshot>) newValue);
        return;
    }
    super.propertySet(propertyName, newValue);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the unique identifier of the snapshot.
   * This must be null when adding to a master and not null when retrieved from a master.
   * @return the value of the property
   */
  public UniqueIdentifier getUniqueId() {
    return _uniqueId;
  }

  /**
   * Sets the unique identifier of the snapshot.
   * This must be null when adding to a master and not null when retrieved from a master.
   * @param uniqueId  the new value of the property
   */
  public void setUniqueId(UniqueIdentifier uniqueId) {
    this._uniqueId = uniqueId;
  }

  /**
   * Gets the the {@code uniqueId} property.
   * This must be null when adding to a master and not null when retrieved from a master.
   * @return the property, not null
   */
  public final Property<UniqueIdentifier> uniqueId() {
    return metaBean().uniqueId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the name of the snapshot intended for display purposes.
   * This field must not be null for the object to be valid.
   * @return the value of the property
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the name of the snapshot intended for display purposes.
   * This field must not be null for the object to be valid.
   * @param name  the new value of the property
   */
  public void setName(String name) {
    this._name = name;
  }

  /**
   * Gets the the {@code name} property.
   * This field must not be null for the object to be valid.
   * @return the property, not null
   */
  public final Property<String> name() {
    return metaBean().name().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the name of the view on which this snapshot was based
   * @return the value of the property
   */
  public String getBasisViewName() {
    return _basisViewName;
  }

  /**
   * Sets the name of the view on which this snapshot was based
   * @param basisViewName  the new value of the property
   */
  public void setBasisViewName(String basisViewName) {
    this._basisViewName = basisViewName;
  }

  /**
   * Gets the the {@code basisViewName} property.
   * @return the property, not null
   */
  public final Property<String> basisViewName() {
    return metaBean().basisViewName().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the globalValues.
   * @return the value of the property
   */
  public UnstructuredMarketDataSnapshot getGlobalValues() {
    return _globalValues;
  }

  /**
   * Sets the globalValues.
   * @param globalValues  the new value of the property
   */
  public void setGlobalValues(UnstructuredMarketDataSnapshot globalValues) {
    this._globalValues = globalValues;
  }

  /**
   * Gets the the {@code globalValues} property.
   * @return the property, not null
   */
  public final Property<UnstructuredMarketDataSnapshot> globalValues() {
    return metaBean().globalValues().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the yield curves in this snapshot
   * @return the value of the property
   */
  public Map<YieldCurveKey, YieldCurveSnapshot> getYieldCurves() {
    return _yieldCurves;
  }

  /**
   * Sets the yield curves in this snapshot
   * @param yieldCurves  the new value of the property
   */
  public void setYieldCurves(Map<YieldCurveKey, YieldCurveSnapshot> yieldCurves) {
    this._yieldCurves = yieldCurves;
  }

  /**
   * Gets the the {@code yieldCurves} property.
   * @return the property, not null
   */
  public final Property<Map<YieldCurveKey, YieldCurveSnapshot>> yieldCurves() {
    return metaBean().yieldCurves().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ManageableMarketDataSnapshot}.
   */
  public static class Meta extends BasicMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code uniqueId} property.
     */
    private final MetaProperty<UniqueIdentifier> _uniqueId = DirectMetaProperty.ofReadWrite(this, "uniqueId", UniqueIdentifier.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofReadWrite(this, "name", String.class);
    /**
     * The meta-property for the {@code basisViewName} property.
     */
    private final MetaProperty<String> _basisViewName = DirectMetaProperty.ofReadWrite(this, "basisViewName", String.class);
    /**
     * The meta-property for the {@code globalValues} property.
     */
    private final MetaProperty<UnstructuredMarketDataSnapshot> _globalValues = DirectMetaProperty.ofReadWrite(this, "globalValues", UnstructuredMarketDataSnapshot.class);
    /**
     * The meta-property for the {@code yieldCurves} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<YieldCurveKey, YieldCurveSnapshot>> _yieldCurves = DirectMetaProperty.ofReadWrite(this, "yieldCurves", (Class) Map.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map;

    @SuppressWarnings({"unchecked", "rawtypes" })
    protected Meta() {
      LinkedHashMap temp = new LinkedHashMap();
      temp.put("uniqueId", _uniqueId);
      temp.put("name", _name);
      temp.put("basisViewName", _basisViewName);
      temp.put("globalValues", _globalValues);
      temp.put("yieldCurves", _yieldCurves);
      _map = Collections.unmodifiableMap(temp);
    }

    @Override
    public ManageableMarketDataSnapshot createBean() {
      return new ManageableMarketDataSnapshot();
    }

    @Override
    public Class<? extends ManageableMarketDataSnapshot> beanType() {
      return ManageableMarketDataSnapshot.class;
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
     * The meta-property for the {@code name} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> name() {
      return _name;
    }

    /**
     * The meta-property for the {@code basisViewName} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> basisViewName() {
      return _basisViewName;
    }

    /**
     * The meta-property for the {@code globalValues} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UnstructuredMarketDataSnapshot> globalValues() {
      return _globalValues;
    }

    /**
     * The meta-property for the {@code yieldCurves} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Map<YieldCurveKey, YieldCurveSnapshot>> yieldCurves() {
      return _yieldCurves;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
