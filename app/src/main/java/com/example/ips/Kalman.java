package com.example.ips;
/*
Not used yet. Some matrix and vector operations.
the initial purpose is to use method process() to implement kalman filter on processed data
detials remain to be filled
 */


import java.util.Map;

public class Kalman {

    private double[] state = new double[2]; // [x, y]
    private double[][] covariance = new double[][]{
            {1, 0},
            {0, 1}
    };

    private final double[][] R = new double[][]{ // 测量噪声协方差矩阵
            {0.1, 0},
            {0, 0.1}
    };

    private final double[][] Q = new double[][]{ // 过程噪声协方差矩阵
            {0.001, 0},
            {0, 0.001}
    };

    //data processing using kalman filter
    public double[] process(Map<String, Double> distances, double[] insPosition) {
        // 1. 使用信号强度数据计算位置
        double[] positionFromSignal = null;

        // 2. 测量更新
        double[][] S = addMatrices(covariance, R);
        double[][] K = multiplyMatrices(covariance, invertMatrix(S));
        double[] y = subtractVectors(positionFromSignal, state);
        double[] Ky = multiplyMatrixByVector(K, y);

        state = addVectors(state, Ky);
        covariance = multiplyMatrices(subtractMatrices(identityMatrix(2), K), covariance);

        // 3. 时间更新
        double[] stateDiff = subtractVectors(insPosition, state);
        state = addVectors(state, stateDiff); // 将差值添加到当前状态，以纠正INS的位置
        covariance = addMatrices(covariance, Q);

        return state;
    }

    //Matrix calculation
    public double[][] addMatrices(double[][] A, double[][] B) {
        int rows = A.length;
        int cols = A[0].length;
        double[][] result = new double[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = A[i][j] + B[i][j];
            }
        }

        return result;
    }

    //Matrix calculation
    public double[][] multiplyMatrices(double[][] A, double[][] B) {
        int rowsA = A.length;
        int colsA = A[0].length;
        int colsB = B[0].length;
        double[][] result = new double[rowsA][colsB];

        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {
                result[i][j] = 0;
                for (int k = 0; k < colsA; k++) {
                    result[i][j] += A[i][k] * B[k][j];
                }
            }
        }

        return result;
    }

    //Matrix calculation
    public double[][] invertMatrix(double[][] A) {
        int n = A.length;
        double[][] result = new double[n][n];
        double[][] temp = new double[n][n];

        // Copy A to temp matrix
        for (int i = 0; i < n; i++) {
            System.arraycopy(A[i], 0, temp[i], 0, n);
        }

        // Initialize result to identity matrix
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                result[i][j] = (i == j) ? 1.0 : 0.0;
            }
        }

        // Gaussian elimination
        for (int i = 0; i < n; i++) {
            // Find maximum in the current column
            double maxEl = Math.abs(temp[i][i]);
            int maxRow = i;
            for (int k = i + 1; k < n; k++) {
                if (Math.abs(temp[k][i]) > maxEl) {
                    maxEl = Math.abs(temp[k][i]);
                    maxRow = k;
                }
            }

            // Swap maximum row with current row
            double[] tmp = temp[maxRow];
            temp[maxRow] = temp[i];
            temp[i] = tmp;

            double[] tmp2 = result[maxRow];
            result[maxRow] = result[i];
            result[i] = tmp2;

            // Make all rows below this one 0 in current column
            for (int k = i + 1; k < n; k++) {
                double c = -temp[k][i] / temp[i][i];
                for (int j = i; j < n; j++) {
                    if (i == j) {
                        temp[k][j] = 0;
                    } else {
                        temp[k][j] += c * temp[i][j];
                    }
                }
                for (int j = 0; j < n; j++) {
                    result[k][j] += c * result[i][j];
                }
            }
        }

        // Solve equation Ax=b for an upper triangular matrix A
        for (int i = n - 1; i >= 0; i--) {
            for (int k = i - 1; k >= 0; k--) {
                double c = -temp[k][i] / temp[i][i];
                for (int j = 0; j < n; j++) {
                    result[k][j] += c * result[i][j];
                }
                temp[k][i] = 0;
            }
            double c = 1.0 / temp[i][i];
            for (int j = 0; j < n; j++) {
                result[i][j] *= c;
            }
            temp[i][i] = 1;
        }

        return result;
    }

    //Matrix calculation
    public double[][] identityMatrix(int size) {
        double[][] result = new double[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                result[i][j] = (i == j) ? 1.0 : 0.0;
            }
        }

        return result;
    }

    //Matrix calculation
    public double[][] subtractMatrices(double[][] A, double[][] B) {
        int rows = A.length;
        int cols = A[0].length;
        double[][] result = new double[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = A[i][j] - B[i][j];
            }
        }

        return result;
    }

    //Vector calculation
    public double[] addVectors(double[] a, double[] b) {
        int length = a.length;
        double[] result = new double[length];

        for (int i = 0; i < length; i++) {
            result[i] = a[i] + b[i];
        }

        return result;
    }

    //Vector calculation
    public double[] subtractVectors(double[] a, double[] b) {
        int length = a.length;
        double[] result = new double[length];

        for (int i = 0; i < length; i++) {
            result[i] = a[i] - b[i];
        }

        return result;
    }

    //Multiply matrix by vector
    public double[] multiplyMatrixByVector(double[][] matrix, double[] vector) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        double[] result = new double[rows];

        for (int i = 0; i < rows; i++) {
            result[i] = 0;
            for (int j = 0; j < cols; j++) {
                result[i] += matrix[i][j] * vector[j];
            }
        }

        return result;
    }


}
