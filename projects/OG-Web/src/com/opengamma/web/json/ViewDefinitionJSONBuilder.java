/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.json;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.fudgemsg.wire.FudgeMsgReader;
import org.fudgemsg.wire.FudgeMsgWriter;
import org.fudgemsg.wire.json.FudgeJSONStreamReader;
import org.fudgemsg.wire.json.FudgeJSONStreamWriter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.beust.jcommander.internal.Lists;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.DeltaDefinition;
import com.opengamma.engine.view.ResultModelDefinition;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Custom JSON builder to convert ViewDefinition to JSON object and vice versa
 */
public final class ViewDefinitionJSONBuilder implements JSONBuilder<ViewDefinition> {
  
  private static final String FUDGE_ENVELOPE = "fudgeEnvelope";
  private static final String NAME_FIELD = "name";
  private static final String IDENTIFIER_FIELD = "identifier";
  private static final String UNIQUE_IDENTIFIER_FIELD = "unique";
  private static final String USER_FIELD = "user";
  private static final String MIN_DELTA_CALC_PERIOD_FIELD = "minDeltaCalcPeriod";
  private static final String MAX_DELTA_CALC_PERIOD_FIELD = "maxDeltaCalcPeriod";
  private static final String MIN_FULL_CALC_PERIOD_FIELD = "fullDeltaCalcPeriod";
  private static final String MAX_FULL_CALC_PERIOD_FIELD = "maxFullCalcPeriod";
  private static final String RESULT_MODEL_DEFINITION_FIELD = "resultModelDefinition";
  private static final String CALCULATION_CONFIGURATION_FIELD = "calculationConfiguration";
  private static final String PORTFOLIO_REQUIREMENTS_BY_SECURITY_TYPE_FIELD = "portfolioRequirementsBySecurityType";
  private static final String SECURITY_TYPE_FIELD = "securityType";
  private static final String PORTFOLIO_REQUIREMENT_FIELD = "portfolioRequirement";
  private static final String PORTFOLIO_REQUIREMENT_REQUIRED_OUTPUT_FIELD = "requiredOutput";
  private static final String PORTFOLIO_REQUIREMENT_CONSTRAINTS_FIELD = "constraints";
  
  private static final String SPECIFIC_REQUIREMENT_FIELD = "specificRequirement";
  private static final String DELTA_DEFINITION_FIELD = "deltaDefinition";
  private static final String CURRENCY_FIELD = "currency";
  private static final String DEFAULT_PROPERTIES_FIELD = "defaultProperties";
  
  private static final FudgeContext FUDGE_CONTEXT = OpenGammaFudgeContext.getInstance();
  private FudgeSerializationContext _serialization = new FudgeSerializationContext(FUDGE_CONTEXT);
  private FudgeDeserializationContext _deserialization = new FudgeDeserializationContext(FUDGE_CONTEXT);

  /**
   * Creates an instance
   */
  public ViewDefinitionJSONBuilder() {
  }

  @Override
  public ViewDefinition fromJSON(String json) {
    ArgumentChecker.notNull(json, "JSON document");
    
    ViewDefinition viewDefinition = null;
    try {
      JSONObject viewJSON = new JSONObject(json).getJSONObject(FUDGE_ENVELOPE);
      UniqueIdentifier portfolioIdentifier = null;
      if (viewJSON.opt(IDENTIFIER_FIELD) != null) {
        portfolioIdentifier = convertJsonToObject(UniqueIdentifier.class, viewJSON.getJSONObject(IDENTIFIER_FIELD));
      }
      String name = viewJSON.getString(NAME_FIELD);
      UserPrincipal liveDataUser = convertJsonToObject(UserPrincipal.class, viewJSON.getJSONObject(USER_FIELD));
      ResultModelDefinition resultModelDefinition = convertJsonToObject(ResultModelDefinition.class, viewJSON.getJSONObject(RESULT_MODEL_DEFINITION_FIELD));
      viewDefinition = new ViewDefinition(name, portfolioIdentifier, liveDataUser, resultModelDefinition);
      
      if (viewJSON.opt(CURRENCY_FIELD) != null) {
        String isoCode = viewJSON.getString(CURRENCY_FIELD);
        viewDefinition.setDefaultCurrency(Currency.of(isoCode));
      }

      if (viewJSON.opt(MIN_DELTA_CALC_PERIOD_FIELD) != null) {
        viewDefinition.setMinDeltaCalculationPeriod(viewJSON.getLong(MIN_DELTA_CALC_PERIOD_FIELD));
      }
      if (viewJSON.opt(MAX_DELTA_CALC_PERIOD_FIELD) != null) {
        viewDefinition.setMaxDeltaCalculationPeriod(viewJSON.getLong(MAX_DELTA_CALC_PERIOD_FIELD));
      }
      if (viewJSON.opt(MIN_FULL_CALC_PERIOD_FIELD) != null) {
        viewDefinition.setMinFullCalculationPeriod(viewJSON.getLong(MIN_FULL_CALC_PERIOD_FIELD));
      }
      if (viewJSON.opt(MAX_FULL_CALC_PERIOD_FIELD) != null) {
        viewDefinition.setMaxFullCalculationPeriod(viewJSON.getLong(MAX_FULL_CALC_PERIOD_FIELD));
      }
      if (viewJSON.opt(CALCULATION_CONFIGURATION_FIELD) != null) {
        JSONArray calConfigs = viewJSON.getJSONArray(CALCULATION_CONFIGURATION_FIELD);
        for (int i = 0; i < calConfigs.length(); i++) {
          JSONObject calcConfigJSON = calConfigs.getJSONObject(i);
          ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(viewDefinition, calcConfigJSON.getString(NAME_FIELD));
          if (calcConfigJSON.opt(PORTFOLIO_REQUIREMENTS_BY_SECURITY_TYPE_FIELD) != null) {
            JSONArray securityTypeRequirements = calcConfigJSON.getJSONArray(PORTFOLIO_REQUIREMENTS_BY_SECURITY_TYPE_FIELD);
            for (int j = 0; j < securityTypeRequirements.length(); j++) {
              JSONObject securityTypeRequirement = securityTypeRequirements.getJSONObject(j);
              String securityType = securityTypeRequirement.getString(SECURITY_TYPE_FIELD);
              Set<Pair<String, ValueProperties>> requirements = new HashSet<Pair<String, ValueProperties>>();
              if (securityTypeRequirement.opt(PORTFOLIO_REQUIREMENT_FIELD) != null) {
                JSONArray portfolioRequirements = securityTypeRequirement.getJSONArray(PORTFOLIO_REQUIREMENT_FIELD);
                for (int k = 0; k < portfolioRequirements.length(); k++) {
                  JSONObject portfolioRequirement = portfolioRequirements.getJSONObject(k);
                  String requiredOutput = portfolioRequirement.getString(PORTFOLIO_REQUIREMENT_REQUIRED_OUTPUT_FIELD);
                  if (portfolioRequirement.opt(PORTFOLIO_REQUIREMENT_CONSTRAINTS_FIELD) != null) {
                    ValueProperties constraints = convertJsonToObject(ValueProperties.class, portfolioRequirement.getJSONObject(PORTFOLIO_REQUIREMENT_CONSTRAINTS_FIELD));
                    requirements.add(Pair.of(requiredOutput, constraints));
                  }
                }
                calcConfig.addPortfolioRequirements(securityType, requirements);
              }
            }
          }
          if (calcConfigJSON.opt(SPECIFIC_REQUIREMENT_FIELD) != null) {
            JSONArray specificRequirements = calcConfigJSON.getJSONArray(SPECIFIC_REQUIREMENT_FIELD);
            for (int j = 0; j < specificRequirements.length(); j++) {
              JSONObject specificRequirement = specificRequirements.getJSONObject(j);
              calcConfig.addSpecificRequirement(convertJsonToObject(ValueRequirement.class, specificRequirement));
            }
          }
          if (calcConfigJSON.opt(DELTA_DEFINITION_FIELD) != null) {
            calcConfig.setDeltaDefinition(convertJsonToObject(DeltaDefinition.class, calcConfigJSON.getJSONObject(DELTA_DEFINITION_FIELD)));
          }
          if (calcConfigJSON.opt(DEFAULT_PROPERTIES_FIELD) != null) {
            calcConfig.setDefaultProperties(convertJsonToObject(ValueProperties.class, calcConfigJSON.getJSONObject(DEFAULT_PROPERTIES_FIELD)));
          }
          viewDefinition.addViewCalculationConfiguration(calcConfig);
        }
      }
      if (viewJSON.opt(UNIQUE_IDENTIFIER_FIELD) != null) {
        viewDefinition.setUniqueId(convertJsonToObject(UniqueIdentifier.class, viewJSON.getJSONObject(UNIQUE_IDENTIFIER_FIELD)));
      }
    } catch (JSONException ex) {
      throw new OpenGammaRuntimeException("Unable to create ViewDefinition", ex);
    }
    return viewDefinition;
  }
  
  private <T> T convertJsonToObject(Class<T> clazz, JSONObject json) {
    FudgeMsg fudgeMsg = convertJSONToFudgeMsg(json);
    return _deserialization.fudgeMsgToObject(clazz, fudgeMsg);
  }

  private FudgeMsg convertJSONToFudgeMsg(final JSONObject uniqueIdentifier) {
    final FudgeMsgReader fmr = new FudgeMsgReader(new FudgeJSONStreamReader(FUDGE_CONTEXT, new StringReader(uniqueIdentifier.toString())));
    FudgeMsg fudgeMsg = fmr.nextMessage();
    return fudgeMsg;
  }

  @Override
  public String toJSON(ViewDefinition viewDefinition) {
    ArgumentChecker.notNull(viewDefinition, "viewDefinition");
    JSONObject result = new JSONObject();
    JSONObject jsonObject = new JSONObject();
    try {
      jsonObject.put(NAME_FIELD, viewDefinition.getName());
      if (viewDefinition.getPortfolioId() != null) {
        jsonObject.put(IDENTIFIER_FIELD, toJSONObject(viewDefinition.getPortfolioId()));
      }
      jsonObject.put(USER_FIELD, toJSONObject(viewDefinition.getLiveDataUser()));
      jsonObject.put(RESULT_MODEL_DEFINITION_FIELD, toJSONObject(viewDefinition.getResultModelDefinition()));
      Currency defaultCurrency = viewDefinition.getDefaultCurrency();
      if (defaultCurrency != null) {
        jsonObject.put(CURRENCY_FIELD, defaultCurrency.getCode());
      }
      if (viewDefinition.getMinDeltaCalculationPeriod() != null) {
        jsonObject.put(MIN_DELTA_CALC_PERIOD_FIELD, viewDefinition.getMinDeltaCalculationPeriod());
      }
      if (viewDefinition.getMaxDeltaCalculationPeriod() != null) {
        jsonObject.put(MAX_DELTA_CALC_PERIOD_FIELD, viewDefinition.getMaxDeltaCalculationPeriod());
      }
      if (viewDefinition.getMinFullCalculationPeriod() != null) {
        jsonObject.put(MIN_FULL_CALC_PERIOD_FIELD, viewDefinition.getMinFullCalculationPeriod());
      }
      if (viewDefinition.getMaxFullCalculationPeriod() != null) {
        jsonObject.put(MAX_FULL_CALC_PERIOD_FIELD, viewDefinition.getMaxFullCalculationPeriod());
      }
      Map<String, ViewCalculationConfiguration> calculationConfigurations = viewDefinition.getAllCalculationConfigurationsByName();
      List<JSONObject> calConfigJSONList = Lists.newArrayList();
      for (ViewCalculationConfiguration calcConfig : calculationConfigurations.values()) {
        JSONObject calcConfigJSON = new JSONObject();
        calcConfigJSON.put(NAME_FIELD, calcConfig.getName());
        List<JSONObject> portfolioRequirementsBySecurityType = Lists.newArrayList();
        for (Map.Entry<String, Set<Pair<String, ValueProperties>>> securityTypeRequirements : calcConfig.getPortfolioRequirementsBySecurityType().entrySet()) {
          JSONObject securityTypeRequirementJSON = new JSONObject();
          securityTypeRequirementJSON.put(SECURITY_TYPE_FIELD, securityTypeRequirements.getKey());
          List<JSONObject> portfolioRequirement = Lists.newArrayList();
          for (Pair<String, ValueProperties> requirement : securityTypeRequirements.getValue()) {
            JSONObject reqJSON = new JSONObject();
            reqJSON.put(PORTFOLIO_REQUIREMENT_REQUIRED_OUTPUT_FIELD, requirement.getFirst());
            reqJSON.put(PORTFOLIO_REQUIREMENT_CONSTRAINTS_FIELD, toJSONObject(requirement.getSecond()));
            portfolioRequirement.add(reqJSON);
          }
          if (!portfolioRequirement.isEmpty()) {
            securityTypeRequirementJSON.put(PORTFOLIO_REQUIREMENT_FIELD, portfolioRequirement);
          }
          portfolioRequirementsBySecurityType.add(securityTypeRequirementJSON);
        }
        if (!portfolioRequirementsBySecurityType.isEmpty()) {
          calcConfigJSON.put(PORTFOLIO_REQUIREMENTS_BY_SECURITY_TYPE_FIELD, portfolioRequirementsBySecurityType);
        }
        List<JSONObject> specificRequirementList = Lists.newArrayList();
        for (ValueRequirement specificRequirement : calcConfig.getSpecificRequirements()) {
          specificRequirementList.add(toJSONObject(specificRequirement));
        }
        if (!specificRequirementList.isEmpty()) {
          calcConfigJSON.put(SPECIFIC_REQUIREMENT_FIELD, specificRequirementList);
        }
        calcConfigJSON.put(DELTA_DEFINITION_FIELD, toJSONObject(calcConfig.getDeltaDefinition()));
        calcConfigJSON.put(DEFAULT_PROPERTIES_FIELD, toJSONObject(calcConfig.getDefaultProperties()));
        calConfigJSONList.add(calcConfigJSON);
      }  
      if (!calConfigJSONList.isEmpty()) {
        jsonObject.put(CALCULATION_CONFIGURATION_FIELD, calConfigJSONList);
      }
      jsonObject.put(UNIQUE_IDENTIFIER_FIELD, toJSONObject(viewDefinition.getUniqueId()));
      
      result.put(FUDGE_ENVELOPE, jsonObject);
      
    } catch (JSONException ex) {
      throw new OpenGammaRuntimeException("unable to convert view definition to JSON", ex);
    }
    
    return result.toString();
  }

  private JSONObject toJSONObject(Object obj) throws JSONException {
    MutableFudgeMsg fudgeMsg = _serialization.objectToFudgeMsg(obj);
    StringWriter buf = new StringWriter(1024);  
    FudgeMsgWriter writer = new FudgeMsgWriter(new FudgeJSONStreamWriter(FUDGE_CONTEXT, buf));
    writer.writeMessage(removeRedundantFields(fudgeMsg));
    JSONObject jsonObject = new JSONObject(buf.toString());
    return jsonObject;
  }

  private FudgeMsg removeRedundantFields(final FudgeMsg message) {
    MutableFudgeMsg result = FUDGE_CONTEXT.newMessage();
    for (FudgeField fudgeField : message) {
      if (fudgeField.getName() != null || (fudgeField.getOrdinal() != null && fudgeField.getOrdinal() != 0)) {
        if (fudgeField.getValue() instanceof FudgeMsg) {
          FudgeMsg subMsg = (FudgeMsg) fudgeField.getValue();
          subMsg = removeRedundantFields(subMsg);
          result.add(fudgeField.getName(), subMsg);
        } else {
          result.add(fudgeField);
        }
      }
    }
    return result;
  }
}