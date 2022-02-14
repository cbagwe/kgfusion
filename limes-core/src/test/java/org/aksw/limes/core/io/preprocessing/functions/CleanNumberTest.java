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
package org.aksw.limes.core.io.preprocessing.functions;

import org.aksw.limes.core.exceptions.IllegalNumberOfParametersException;
import org.aksw.limes.core.io.cache.Instance;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;

public class CleanNumberTest {

    public static final String TEST_INSTANCE = "http://dbpedia.org/resource/Ibuprofen";

    // =============== EXPECTED VALUES ==================================
    public static final String CLEAN_NUMBER_EXPECTED = "10";
    public static final String CLEAN_NUMBER_EXPECTED2 = "223";

    // =============== PROPERTIES =======================================
    public static final String PROP_NUMBER = "number";
    public static final String PROP_NUMBER_NOT_PARSEABLE = "numbernotparseable";
    public static final String PROP_LABEL = "rdfs:label";

    // =============== VALUES ===========================================
    public static final String PROP_NUMBER_VALUE = "10^^http://www.w3.org/2001/XMLSchema#positiveInteger";
    public static final String PROP_NUMBER_VALUE2 = "223";
    public static final String PROP_NUMBER_NOT_PARSEABLE_VALUE = "10.0.0.1^^http://www.w3.org/2001/XMLSchema#positiveInteger";
    public static final String PROP_LABEL_VALUE1 = "Ibuprofen@de";
    public static final String PROP_LABEL_VALUE2 = "Ibuprofen@en";

    public Instance testInstance;

    @Before
    public void prepareData() {
        testInstance = new Instance(TEST_INSTANCE);

        TreeSet<String> labels = new TreeSet<>();
        labels.add(PROP_LABEL_VALUE1);
        labels.add(PROP_LABEL_VALUE2);
        testInstance.addProperty(PROP_LABEL, labels);
        testInstance.addProperty(PROP_NUMBER, PROP_NUMBER_VALUE);
        testInstance.addProperty(PROP_NUMBER, PROP_NUMBER_VALUE2);
        testInstance.addProperty(PROP_NUMBER_NOT_PARSEABLE, PROP_NUMBER_NOT_PARSEABLE_VALUE);
    }

    @Test
    public void testCleanNumber() throws IllegalNumberOfParametersException {
        new CleanNumber().applyFunction(testInstance, PROP_NUMBER);
        Iterator<String> it = testInstance.getProperty(PROP_NUMBER).iterator();
        assertEquals(CLEAN_NUMBER_EXPECTED, it.next());
        assertEquals(CLEAN_NUMBER_EXPECTED2, it.next());
    }

    @Test
    public void testCleanNumberNotParseable() throws IllegalNumberOfParametersException {
        new CleanNumber().applyFunction(testInstance, PROP_NUMBER_NOT_PARSEABLE);
        assertEquals("0", testInstance.getProperty(PROP_NUMBER_NOT_PARSEABLE).first());
    }

    @Test
    public void testCleanNumberNoNumberPresent() throws IllegalNumberOfParametersException {
        new CleanNumber().applyFunction(testInstance, PROP_LABEL);
        assertEquals("0", testInstance.getProperty(PROP_LABEL).first());
    }
}
