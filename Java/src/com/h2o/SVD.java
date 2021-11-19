package com.h2o;

public class SVD {
    public SVD() {
    }

    ;

    public static final int compute(double[][] a, double[][] u, double[][] v, double[] d) {

        int i, its, j, jj, k, l, m, n, mn, nm, rank;
        double anorm, c, f, g, h, s, scale, x, y, z, tmp;
        boolean cancel;
        double EPSILON = 1.0e-15;

        /*     This method is a translation of the Algol procedure svd,
         *     Num. Math. 14, 403-420(1970) by Golub and Reinsch.
         *     Handbook for Auto. Comp., Vol II-Linear Algebra, 134-151(1971).
         *
         *     This method determines the singular value decomposition
         *     a=udv'  of a real m by n rectangular matrix.  Householder
         *     bidiagonalization and a variant of the QR algorithm are used.
         *
         *     on input
         *
         *        a contains the rectangular input matrix to be decomposed.
         *
         *     on output
         *
         *        a is unaltered.
         *
         *        d contains the n (non-negative) singular values of a (the
         *          diagonal elements of s).  If an
         *          error exit is made, the singular values should be correct
         *          for indices 1 .. rank.
         *
         *        u contains the matrix u (orthogonal column vectors) of the
         *          decomposition. If an error exit is made, the columns of u
         *          corresponding to indices of correct singular values should
         *          be correct.
         *
         *        v contains the matrix v (orthogonal) of the decomposition.
         *          If an error exit is made, the columns of v corresponding
         *          to indices of correct singular values should be correct.
         *
         *     calls pythag for sqrt(a*a + b*b) .
         *
         *     FORTRAN version dated August 1983.
         *
         *     Converted to Java from Algol in Wilkinson & Reinsch by Leland Wilkinson
         *
         *     ------------------------------------------------------------------
         */

        rank = 0;
        m = a.length;
        n = a[0].length;

        if (m < n)
            return 0;

        double[] rv1 = new double[n];

        for (i = 0; i < m; i++) {
            for (j = 0; j < n; j++)
                u[i][j] = a[i][j];
        }
        /*     .......... Householder reduction to bidiagonal form .......... */
        g = 0.0;
        scale = 0.0;
        anorm = 0.0;
        x = 0.0;
        l = 0;

        for (i = 0; i < n; i++) {
            l = i + 1;
            rv1[i] = scale * g;
            g = 0.0;
            s = 0.0;
            scale = 0.0;
            if (i < m) {
                for (k = i; k < m; k++)
                    scale += Math.abs(u[k][i]);

                if (scale != 0.0) {
                    for (k = i; k < m; k++) {
                        u[k][i] /= scale;
                        s += u[k][i] * u[k][i];
                    }

                    f = u[i][i];
                    g = (f < 0.0 ? Math.sqrt(s) : -Math.sqrt(s));
                    h = f * g - s;
                    u[i][i] = f - g;
                    for (j = l; j < n; j++) {
                        s = 0.0;
                        for (k = i; k < m; k++)
                            s += u[k][i] * u[k][j];
                        f = s / h;
                        for (k = i; k < m; k++)
                            u[k][j] += f * u[k][i];
                    }
                    for (k = i; k < m; k++)
                        u[k][i] *= scale;
                }
            }

            d[i] = scale * g;
            g = 0.0;
            s = 0.0;
            scale = 0.0;
            if (i < m && i < n - 1) {
                for (k = l; k < n; k++)
                    scale += Math.abs(u[i][k]);

                if (scale != 0.0) {
                    for (k = l; k < n; k++) {
                        u[i][k] /= scale;
                        s += u[i][k] * u[i][k];
                    }

                    f = u[i][l];
                    g = (f < 0.0 ? Math.sqrt(s) : -Math.sqrt(s));
                    h = f * g - s;
                    u[i][l] = f - g;

                    for (k = l; k < n; k++)
                        rv1[k] = u[i][k] / h;

                    for (j = l; j < m; j++) {
                        s = 0.0;
                        for (k = l; k < n; k++)
                            s += u[j][k] * u[i][k];
                        for (k = l; k < n; k++)
                            u[j][k] += s * rv1[k];
                    }
                    for (k = l; k < n; k++)
                        u[i][k] *= scale;
                }
            }
            x = Math.max(x, Math.abs(d[i]) + Math.abs(rv1[i]));
            if (x > anorm)
                anorm = x;
        }

        /*     .......... accumulation of right-hand transformations .......... */
        for (i = n - 1; i >= 0; i--) {
            if (i < n - 1) {
                if (g != 0.0) {
                    for (j = l; j < n; j++) {
                        /*     .......... double division avoids possible underflow .......... */
                        v[j][i] = (u[i][j] / u[i][l]) / g;
                    }

                    for (j = l; j < n; j++) {
                        s = 0.0;

                        for (k = l; k < n; k++)
                            s += u[i][k] * v[k][j];

                        for (k = l; k < n; k++)
                            v[k][j] += s * v[k][i];
                    }
                }
                for (j = l; j < n; j++) {
                    v[i][j] = 0.0;
                    v[j][i] = 0.0;
                }
            }
            v[i][i] = 1.0;
            g = rv1[i];
            l = i;
        }

        /*     .......... accumulation of left-hand transformations .......... */
        mn = n;
        if (m < n)
            mn = m;

        for (i = mn - 1; i >= 0; i--) {
            l = i + 1;
            g = d[i];
            for (j = l; j < n; j++)
                u[i][j] = 0.0;

            if (g != 0.0) {
                for (j = l; j < n; j++) {
                    s = 0.0;
                    for (k = l; k < m; k++)
                        s += u[k][i] * u[k][j];
                    f = (s / u[i][i]) / g;
                    for (k = i; k < m; k++)
                        u[k][j] += f * u[k][i];
                }
                for (j = i; j < m; j++)
                    u[j][i] /= g;
            } else {
                for (j = i; j < m; j++)
                    u[j][i] = 0.0;
            }

            u[i][i] += 1.0;
        }

        /*     .......... diagonalization of the bidiagonal form .......... */
        nm = 0;
        for (k = n - 1; k >= 0; k--) {
            for (its = 1; its <= 30; its++) {
                cancel = true;
                /*     .......... test for splitting.
                 *                for l=k step -1 until 1 do -- .......... */
                for (l = k; l >= 0; l--) {
                    nm = l - 1;
                    if (Math.abs(rv1[l] + anorm) == anorm) {
                        cancel = false;
                        break;
                    }
                    if (Math.abs(d[nm] + anorm) == anorm) {
                        break;
                    }
                }

                /*     .......... cancellation of rv1(l) if l greater than 1 .......... */
                if (cancel) {
                    c = 0.0;
                    s = 1.0;

                    for (i = l; i <= k; i++) {
                        f = s * rv1[i];
                        rv1[i] = c * rv1[i];
                        if (Math.abs(f + anorm) == anorm)
                            break;

                        g = d[i];
                        h = pythag(f, g);
                        d[i] = h;
                        c = g / h;
                        s = -f / h;
                        for (j = 0; j < m; j++) {
                            y = u[j][nm];
                            z = u[j][i];
                            u[j][nm] = y * c + z * s;
                            u[j][i] = -y * s + z * c;
                        }
                    }
                }

                /*     .......... test for convergence .......... */
                z = d[k];
                if (l == k) {
                    if (z < 0.0) {
                        d[k] = -z;
                        for (j = 0; j < n; j++)
                            v[j][k] = -v[j][k];
                    }
                    break; //  normal exit
                }
                if (its == 30) { // exceeded iterations
                    break;
                }

                x = d[l];
                nm = k - 1;
                y = d[nm];
                g = rv1[nm];
                h = rv1[k];
                f = 0.5 * (((g + z) / h) * ((g - z) / y) + y / h - h / y);
                g = pythag(f, 1.0);
                tmp = (f < 0.0 ? -g : g);
                f = x - (z / x) * z + (h / x) * (y / (f + tmp) - h);

                /*     .......... next qr transformation .......... */
                c = 1.0;
                s = 1.0;

                for (j = l; j <= nm; j++) {
                    i = j + 1;
                    g = rv1[i];
                    y = d[i];
                    h = s * g;
                    g = c * g;
                    z = pythag(f, h);
                    rv1[j] = z;
                    c = f / z;
                    s = h / z;
                    f = x * c + g * s;
                    g = -x * s + g * c;
                    h = y * s;
                    y = y * c;
                    for (jj = 0; jj < n; jj++) {
                        x = v[jj][j];
                        z = v[jj][i];
                        v[jj][j] = x * c + z * s;
                        v[jj][i] = -x * s + z * c;
                    }
                    z = pythag(f, h);
                    d[j] = z;
                    /*     .......... rotation can be arbitrary if z is zero .......... */
                    if (z != 0.0) {
                        c = f / z;
                        s = h / z;
                    }
                    f = c * g + s * y;
                    x = -s * g + c * y;
                    for (jj = 0; jj < m; jj++) {
                        y = u[jj][j];
                        z = u[jj][i];
                        u[jj][j] = y * c + z * s;
                        u[jj][i] = -y * s + z * c;
                    }
                }

                rv1[l] = 0.0;
                rv1[k] = f;
                d[k] = x;
            }
        }

        eigsort(d, u, v);

        /* compute rank */
        rank = 0;
        for (i = 0; i < n; i++) {
            if (d[i] < EPSILON)
                d[i] = 0.;
            if (d[i] > 0.)
                rank++;
        }

        return (rank);
    }

    private static final double pythag(double a, double b) {

        /*     Pythagorean theorem with overflow protection.
         *     Finds sqrt(a^2+b^2) without overflow or destructive underflow.
         *     Converted to Java by Leland Wilkinson.
         */

        double p, r, s, t, u;

        p = Math.max(Math.abs(a), Math.abs(b));
        if (p != 0.0) {
            r = Math.pow(Math.min(Math.abs(a), Math.abs(b)) / p, 2);
            for (; ; ) {
                t = 4.0 + r;
                if (t == 4.0)
                    break;
                s = r / t;
                u = 1.0 + 2.0 * s;
                p = u * p;
                r = Math.pow(s / u, 2) * r;
            }
        }
        return (p);
    }

    private static final void eigsort(double[] a, double[][] u, double[][] v) {
        /* Sorting by eigenvalues (descending) */
        int l;
        int n = a.length;
        for (l = 1; l <= n; l = 3 * l + 1)
            ;
        while (l > 2) {
            l = l / 3;
            int k = n - l;
            for (int j = 0; j < k; j++) {
                int i = j;
                while (i >= 0) {
                    int ip1 = i + l;
                    if (a[i] < a[ip1] || Double.isNaN(a[i])) {
                        double p = a[i];
                        a[i] = a[ip1];
                        a[ip1] = p;
                        if (u != null) {
                            for (int ii = 0; ii < n; ii++) {
                                p = u[ii][i];
                                u[ii][i] = u[ii][ip1];
                                u[ii][ip1] = p;
                            }
                        }
                        if (v != null) {
                            for (int ii = 0; ii < n; ii++) {
                                p = v[ii][i];
                                v[ii][i] = v[ii][ip1];
                                v[ii][ip1] = p;
                            }
                        }
                        i = i - l;
                    } else
                        break;
                }
            }
        }
    }
}
