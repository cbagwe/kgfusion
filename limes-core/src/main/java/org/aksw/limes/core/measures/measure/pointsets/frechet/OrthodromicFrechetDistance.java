/*
 * LIMES Core Library - LIMES – Link Discovery Framework for Metric Spaces.
 * Copyright © 2011 Data Science Group (DICE) (ngonga@uni-paderborn.de)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.aksw.limes.core.measures.measure.pointsets.frechet;

import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.distance.Frechet;
import org.aksw.limes.core.datastrutures.Point;
import org.aksw.limes.core.measures.measure.pointsets.APointsetsMeasure;

public class OrthodromicFrechetDistance extends Frechet {
    /**
     * Internal method for computing the discrete Fréchet distance using a
     * dynamic programming approach.
     *
     * @param p linestring
     * @param q linestring
     * @param i index on p
     * @param j index on q
     * @param ca  matrix containing the computed distances between the points
     *            from p and q. Its size is p*q.
     * @return the coupling measure between points i of p and j of q
     * @see #discreteFrechet(ILineString, ILineString)
     */
    @SuppressWarnings("unused")
    private static double discreteFrechetCouplingMeasure(ILineString p, ILineString q, int i, int j, double[][] ca) {
        if (ca[i][j] > -1) {
            return ca[i][j];
        }
        IDirectPosition a = p.getControlPoint(i);
        IDirectPosition b = q.getControlPoint(j);
//        double d = GeoOrthodromicMeasure.distance(a.getX(), a.getY(), b.getX(), b.getY());
        double d = APointsetsMeasure.pointToPointDistance(new Point(a.getX(), a.getY()), new Point(b.getX(), b.getY()));
        if (i == 0 && j == 0) {
            return ca[i][j] = d;
        }
        if (i > 0 && j == 0) {
            return ca[i][j] = Math.max(discreteFrechetCouplingMeasure(p, q, i - 1, j, ca), d);
        }
        if (i == 0 && j > 0) {
            return ca[i][j] = Math.max(discreteFrechetCouplingMeasure(p, q, i, j - 1, ca), d);
        }
        if (i > 0 && j > 0) {
            return ca[i][j] = Math.max(Math.min(discreteFrechetCouplingMeasure(p, q, i - 1, j, ca),
                    Math.min(discreteFrechetCouplingMeasure(p, q, i - 1, j - 1, ca),
                            discreteFrechetCouplingMeasure(p, q, i, j - 1, ca))), d);
        }
        return ca[i][j] = Double.POSITIVE_INFINITY;
    }

    public static double discreteFrechet(ILineString p, ILineString q) {
        // System.out.println("FRECHET P = " + p);
        // System.out.println("FRECHET Q = " + q);
        int sizeP = p.sizeControlPoint();
        int sizeQ = q.sizeControlPoint();
        double[][] ca = new double[sizeP][sizeQ];
        for (int i = 0; i < sizeP; i++) {
            for (int j = 0; j < sizeQ; j++) {
                ca[i][j] = -1.0;
            }
        }
        return discreteFrechetCouplingMeasure(p, q, sizeP - 1, sizeQ - 1, ca);
    }
}
