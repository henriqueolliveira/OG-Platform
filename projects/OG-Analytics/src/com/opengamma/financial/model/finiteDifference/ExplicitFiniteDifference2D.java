package com.opengamma.financial.model.finiteDifference;

import com.opengamma.math.cube.Cube;

public class ExplicitFiniteDifference2D {

  public double[][] solve(ConvectionDiffusion2DPDEDataBundle pdeData, final int tSteps, final int xSteps, final int ySteps, final double tMax, BoundaryCondition xLowerBoundary,
      BoundaryCondition xUpperBoundary, BoundaryCondition yLowerBoundary, BoundaryCondition yUpperBoundary, final Cube<Double, Double, Double, Double> freeBoundary) {

    double dt = tMax / (tSteps);
    double dx = (xUpperBoundary.getLevel() - xLowerBoundary.getLevel()) / (xSteps);
    double dy = (yUpperBoundary.getLevel() - yLowerBoundary.getLevel()) / (ySteps);
    double dtdx2 = dt / dx / dx;
    double dtdx = dt / dx;
    double dtdy2 = dt / dy / dy;
    double dtdy = dt / dy;
    double dtdxdy = dt / dy / dx;

    double[][] v = new double[xSteps + 1][ySteps + 1];
    double[] x = new double[xSteps + 1];
    double[] y = new double[ySteps + 1];

    double currentX = 0;
    double currentY = 0;

    for (int j = 0; j <= ySteps; j++) {
      currentY = yLowerBoundary.getLevel() + j * dy;
      y[j] = currentY;
    }
    for (int i = 0; i <= xSteps; i++) {
      currentX = xLowerBoundary.getLevel() + i * dx;
      x[i] = currentX;
      for (int j = 0; j <= ySteps; j++) {
        v[i][j] = pdeData.getInitialValue(x[i], y[j]);
      }
    }

    double sum;
    double t = 0.0;
    for (int k = 0; k < tSteps; k++) {
      double[][] vNew = new double[xSteps + 1][ySteps + 1];
      for (int i = 1; i < xSteps; i++) {
        for (int j = 1; j < ySteps; j++) {
          double a = pdeData.getA(t, x[i], y[j]);
          double b = pdeData.getB(t, x[i], y[j]);
          double c = pdeData.getC(t, x[i], y[j]);
          double d = pdeData.getD(t, x[i], y[j]);
          double e = pdeData.getE(t, x[i], y[j]);
          double f = pdeData.getF(t, x[i], y[j]);

          sum = v[i][j];
          sum += a * dtdx2 * (v[i + 1][j] - 2 * v[i][j] + v[i - 1][j]);
          sum += b * dtdx / 2 * (v[i + 1][j] - v[i - 1][j]);
          sum += c * dt * v[i][j];
          sum += d * dtdy2 * (v[i][j + 1] - 2 * v[i][j] + v[i][j - 1]);
          sum += e * dtdxdy / 4 * (v[i + 1][j + 1] + v[i - 1][j - 1] - v[i + 1][j - 1] - v[i - 1][j + 1]);
          sum += f * dtdy / 2 * (v[i][j + 1] - v[i][j - 1]);

          vNew[i][j] = sum;
        }
      }
      
      for (int i = 0; i <= xSteps; i++) {
        vNew[i][0] = v[i][0];
        vNew[i][ySteps] = v[i][ySteps];
      }
      for (int j = 0; j <= ySteps; j++) {
        vNew[0][j] = v[0][j];
        vNew[xSteps][j] = v[xSteps][j];
      }
      
      

//      double[] temp = xLowerBoundary.getRightMatrixCondition(pdeData, t);
//      sum = 0;
//      for (int n = 0; n < temp.length; n++) {
//        sum += temp[n] * v[n];
//      }
//      double q = sum + lowerBoundary.getConstant(pdeData, t);
//
//      sum = 0;
//      temp = lowerBoundary.getLeftMatrixCondition(pdeData, t);
//      for (int k = 1; k < temp.length; k++) {
//        sum += temp[k] * fNew[k];
//      }
//      fNew[0] = (q - sum) / temp[0];
//
//      temp = upperBoundary.getRightMatrixCondition(pdeData, t);
//      sum = 0;
//      for (int k = 0; k < temp.length; k++) {
//        sum += temp[k] * v[xSteps + k + 1 - temp.length];
//      }
//      q = sum + upperBoundary.getConstant(pdeData, t);
//
//      sum = 0;
//      temp = upperBoundary.getLeftMatrixCondition(pdeData, t);
//      for (int k = 0; k < temp.length - 1; k++) {
//        sum += temp[k] * fNew[xSteps + k + 1 - temp.length];
//      }
//
//      fNew[xSteps] = (q - sum) / temp[temp.length - 1];
//
//      // TODO American payoff
      t += dt;
      v = vNew;
    }

    return v;

  }

}