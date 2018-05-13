package ifis.skysim2.data.generator;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.CholeskyDecomposition;
import cern.colt.matrix.linalg.EigenvalueDecomposition;
import cern.jet.random.Normal;

// Data:
// - Marginal distributions: uniform in [0, 1]
// - For any i != j, dimension i and dimension j have a Kendall tau correlation coefficient of tau
//
// Construction:
// - Generate standard normal data set with Kendall tau as given above
// - Construct the copula of this distibution (Kendall's tau is invariant under strictly monotonic transformations!)
public class DataGeneratorCorrelatedUniform extends AbstractDataGenerator {

    private double tau;

    public DataGeneratorCorrelatedUniform(double tau) {
        super();
        this.tau = tau;
    }

    @Override
    public float[] generate(int d, int n) {
        float[] data = new float[d * n];

        // create the correlation matrix (Pearson correlation!)
        // since our marginal distributions will be standard normal variates,
        // the correlation matrix is identical to the covariance matrix
        double rho = Math.sin(0.5 * tau * Math.PI);
        // check if this rho would yield a positive semidefinite matrix
        double minTau = -2 / Math.PI * Math.asin(1.0 / (d - 1));
        if (tau < minTau) {
            // One of the eigenvalues would be negative ==> set it to zero
            rho = -1.0 / (d - 1);
            System.out.format("Tau (%.3f) is too small, setting it to %.3f%n", tau, minTau);
        }
        double[][] corr = new double[d][d];
        for (int i = 0; i < d; i++) {
            for (int j = 0; j < d; j++) {
                corr[i][j] = rho;
            }
            // Add a slight perturbation to the diagonal to enforce positive definiteness,
            // which is required by the Cholesky decomposition
            corr[i][i] = 1 + 0.0000000001;
        }
        DoubleMatrix2D corrM = new DenseDoubleMatrix2D(corr);

        // compute its Cholesky decomposition
        CholeskyDecomposition decomp = new CholeskyDecomposition(corrM);
//	if (!decomp.isSymmetricPositiveDefinite()) {
//	    corrM = findNearestCorrelationMatrix(corrM);
//	    decomp = new CholeskyDecomposition(corrM);
//	}
        DoubleMatrix2D lowerTriang = decomp.getL();

        for (int i = 0; i < n; i++) {
            int pos = d * i;

            // generate a vector of d independent standard normal variates
            Normal stdNormGen = new Normal(0, 1, re);
            double[] tmp = new double[d];
            for (int j = 0; j < d; j++) {
                tmp[j] = stdNormGen.nextDouble();
            }
            DoubleMatrix1D tmpM = new DenseDoubleMatrix1D(tmp);

            // transform the vector to get a point of the distribution we want
            DoubleMatrix1D pointM = lowerTriang.zMult(tmpM, null);
            double[] point = pointM.toArray();
            for (int j = 0; j < d; j++) {
                // copula transform
                point[j] = stdNormGen.cdf(point[j]);
                // write to output
                data[pos + j] = (float) point[j];
            }
        }

        return data;
    }

    // we are implementing the method described in
    // Higham: Computing the nearest correlation matrix—a problem from finance (2002),
    // choosing W = I
    private static DoubleMatrix2D findNearestCorrelationMatrix(DoubleMatrix2D a) {
        int n = a.rows();
        DoubleMatrix2D deltaS = new DenseDoubleMatrix2D(n, n);
        DoubleMatrix2D y = a.copy();
        for (int k = 0; k < 10; k++) {
            DoubleMatrix2D r = minus(y, deltaS);
            DoubleMatrix2D x = projectOntoS(r);
            deltaS = minus(x, r);
            y = projectOntoU(x);
        }
//	System.out.println("Distance: " + frobeniusNorm(minus(a, y)));
        return y;
    }

    private static DoubleMatrix2D minus(DoubleMatrix2D a, DoubleMatrix2D b) {
        int m = a.rows();
        int n = a.columns();
        double[][] aArray = a.toArray();
        double[][] bArray = b.toArray();
        double[][] sumArray = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                sumArray[i][j] = aArray[i][j] - bArray[i][j];
            }
        }
        return new DenseDoubleMatrix2D(sumArray);
    }

    private static DoubleMatrix2D projectOntoS(DoubleMatrix2D a) {
        EigenvalueDecomposition spectralDecomp = new EigenvalueDecomposition(a);
        DoubleMatrix2D q = spectralDecomp.getV();
        DoubleMatrix2D dPlus = spectralDecomp.getD();
        int n = dPlus.rows();
        for (int i = 0; i < n; i++) {
            double lambdaI = dPlus.get(i, i);
            if (lambdaI < 0) {
                dPlus.set(i, i, 0);
            }
        }
        DoubleMatrix2D aPlus = q.zMult(dPlus, null);
        aPlus = aPlus.zMult(q, null, 1, 0, false, true);
        return aPlus;
    }

    private static DoubleMatrix2D projectOntoU(DoubleMatrix2D a) {
        DoubleMatrix2D p = a.copy();
        int n = p.rows();
        for (int i = 0; i < n; i++) {
            p.set(i, i, 1);
        }
        return p;
    }

    private static double frobeniusNorm(DoubleMatrix2D a) {
        double norm = 0;
        int m = a.rows();
        int n = a.columns();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                double aIJ = a.get(i, j);
                norm += aIJ * aIJ;
            }
        }
        norm = Math.sqrt(norm);
        return norm;
    }

    @Override
    public String getShortName() {
        return "d_CorrUni";
    }
}

