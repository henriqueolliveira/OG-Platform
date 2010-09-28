/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.greeks;

/**
 * @param <T> Return type of methods
 */
public interface GreekVisitor<T> {

  T visitPrice();

  T visitZeta();

  T visitCarryRho();

  T visitDelta();

  T visitDriftlessTheta();

  T visitDZetaDVol();

  T visitElasticity();

  T visitPhi();

  T visitRho();

  T visitStrikeDelta();

  T visitTheta();

  T visitVarianceVega();

  T visitVega();

  T visitVegaP();

  T visitZetaBleed();

  T visitVarianceVanna();

  T visitDeltaBleed();

  T visitGamma();

  T visitGammaP();

  T visitStrikeGamma();

  T visitVanna();

  T visitVarianceVomma();

  T visitVegaBleed();

  T visitVomma();

  T visitVommaP();

  T visitDVannaDVol();

  T visitGammaBleed();

  T visitGammaPBleed();

  T visitSpeed();

  T visitSpeedP();

  T visitUltima();

  T visitVarianceUltima();

  T visitZomma();

  T visitZommaP();
}