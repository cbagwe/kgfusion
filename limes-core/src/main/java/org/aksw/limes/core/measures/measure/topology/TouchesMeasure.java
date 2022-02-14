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
package org.aksw.limes.core.measures.measure.topology;

import org.aksw.limes.core.io.cache.Instance;
import org.aksw.limes.core.measures.measure.AMeasure;
import org.aksw.limes.core.util.LimesWktReader;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;

/**
 * Measure that checks for the topological relation touches.
 *
 * @author kdressler
 */
public class TouchesMeasure extends AMeasure {
    @Override
    public double getSimilarity(Object object1, Object object2) {
        // expects WKT Strings
        String sWKT, tWKT;
        Geometry sGeo, tGeo;
        sWKT = object1.toString();
        tWKT = object2.toString();
        LimesWktReader reader = new LimesWktReader();
        try {
            sGeo = reader.read(sWKT);
            tGeo = reader.read(tWKT);
        } catch (ParseException e) {
            e.printStackTrace();
            return 0d;
        }
        return sGeo.touches(tGeo) ? 1d : 0d;
    }

    @Override
    public double getSimilarity(Instance instance1, Instance instance2, String property1, String property2) {
        double value = 0;
        double sim = 0;
        for (String source : instance1.getProperty(property1)) {
            for (String target : instance2.getProperty(property2)) {
                sim = getSimilarity(source, target);
                if (sim > value) {
                    value = sim;
                }
            }
        }
        return sim;
    }

    @Override
    public double getRuntimeApproximation(double mappingSize) {
        return mappingSize / 1000d;
    }

    @Override
    public String getName() {
        return "top_touches";
    }

    @Override
    public String getType() {
        return "topology";
    }
}
