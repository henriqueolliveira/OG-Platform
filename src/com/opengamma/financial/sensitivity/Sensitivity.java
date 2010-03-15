package com.opengamma.financial.sensitivity;

import com.opengamma.financial.greeks.FirstOrder;
import com.opengamma.financial.greeks.MixedSecondOrder;
import com.opengamma.financial.greeks.Order;
import com.opengamma.financial.greeks.SecondOrder;
import com.opengamma.financial.pnl.Underlying;

public enum Sensitivity {
  VALUE_DELTA {

    @Override
    public <T> T accept(final SensitivityVisitor<T> visitor) {
      return visitor.visitValueDelta();
    }

    @Override
    public Order getOrder() {
      return new FirstOrder(Underlying.SPOT_PRICE);
    }
  },
  VALUE_GAMMA {

    @Override
    public <T> T accept(final SensitivityVisitor<T> visitor) {
      return visitor.visitValueGamma();
    }

    @Override
    public Order getOrder() {
      return new SecondOrder(Underlying.SPOT_PRICE);
    }

  },
  VALUE_VANNA {

    @Override
    public <T> T accept(final SensitivityVisitor<T> visitor) {
      return visitor.visitValueVanna();
    }

    @Override
    public Order getOrder() {
      return new MixedSecondOrder(new FirstOrder(Underlying.SPOT_PRICE), new FirstOrder(Underlying.SPOT_VOLATILITY));
    }
  },
  VALUE_VEGA {

    @Override
    public <T> T accept(final SensitivityVisitor<T> visitor) {
      return visitor.visitValueVega();
    }

    @Override
    public Order getOrder() {
      return new FirstOrder(Underlying.SPOT_VOLATILITY);
    }

  },
  VALUE_THETA {

    @Override
    public <T> T accept(final SensitivityVisitor<T> visitor) {
      return visitor.visitValueTheta();
    }

    @Override
    public Order getOrder() {
      return new FirstOrder(Underlying.TIME);
    }

  },
  DV01 {

    @Override
    public <T> T accept(final SensitivityVisitor<T> visitor) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Order getOrder() {
      // TODO Auto-generated method stub
      return null;
    }

  },
  PV01 {

    @Override
    public <T> T accept(final SensitivityVisitor<T> visitor) {
      return visitor.visitPV01();
    }

    @Override
    public Order getOrder() {
      // TODO Auto-generated method stub
      return null;
    }

  },
  CONVEXITY {

    @Override
    public <T> T accept(final SensitivityVisitor<T> visitor) {
      return visitor.visitConvexity();
    }

    @Override
    public Order getOrder() {
      // TODO Auto-generated method stub
      return null;
    }
  };

  public abstract <T> T accept(SensitivityVisitor<T> visitor);

  public abstract Order getOrder();

}
