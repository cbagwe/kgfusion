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
package org.aksw.limes.core.io.config.reader.xml;

import org.aksw.limes.core.io.config.Configuration;
import org.aksw.limes.core.io.config.KBInfo;
import org.aksw.limes.core.io.config.reader.rdf.RDFConfigurationReader;
import org.aksw.limes.core.ml.algorithm.LearningParameter;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertTrue;


/**
 * @author Mohamed Sherif (sherif@informatik.uni-leipzig.de)
 * @version Jan 15, 2016
 */
public class RDFConfigurationReaderTest {

    private static final String SYSTEM_DIR = System.getProperty("user.dir");
    Map<String, String> prefixes;
    LinkedHashMap<String, Map<String, String>> functions;
    KBInfo sourceInfo, targetInfo;
    Configuration testConf;

    @Before
    public void init() {
        prefixes = new HashMap<>();
        prefixes.put("geos", "http://www.opengis.net/ont/geosparql#");
        prefixes.put("lgdo", "http://linkedgeodata.org/ontology/");
        prefixes.put("geom", "http://geovocab.org/geometry#");
        prefixes.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        prefixes.put("limes", "http://limes.sf.net/ontology/");

        functions = new LinkedHashMap<>();
        Map<String, String> f = new LinkedHashMap<>();
        f.put("polygon", null);
        functions.put("geom:geometry/geos:asWKT", f);

        sourceInfo = new KBInfo(
                "linkedgeodata",                                                  //String id
                "http://linkedgeodata.org/sparql",                                //String endpoint
                null,                                                             //String graph
                "?x",                                                             //String var
                new ArrayList<String>(Arrays.asList("geom:geometry/geos:asWKT")), //List<String> properties
                new ArrayList<String>(),                                          //List<String> optionalProperties
                new ArrayList<String>(Arrays.asList("?x a lgdo:RelayBox")),       //ArrayList<String> restrictions
                functions,                                                        //LinkedHashMap<String, Map<String, String>> functions
                prefixes,                                                         //Map<String, String> prefixes
                2000,                                                             //int pageSize
                "sparql",                                                         //String type
                -1,                                                               //int minOffset
                -1                                                                //int maxoffset
        );

        targetInfo = new KBInfo(
                "linkedgeodata",                                                  //String id
                "http://linkedgeodata.org/sparql",                                //String endpoint
                null,                                                             //String graph
                "?y",                                                             //String var
                new ArrayList<String>(Arrays.asList("geom:geometry/geos:asWKT")), //List<String> properties
                new ArrayList<String>(),                                          //List<String> optionalProperties
                new ArrayList<String>(Arrays.asList("?y a lgdo:RelayBox")),       //ArrayList<String> restrictions
                functions,                                                        //LinkedHashMap<String, Map<String, String>> functions
                prefixes,                                                         //Map<String, String> prefixes
                2000,                                                             //int pageSize
                "sparql",                                                         //String type
                -1,                                                               //int minOffset
                -1                                                                //int maxoffset                
        );

        testConf = new Configuration();
        testConf.setPrefixes(prefixes);
        testConf.setSourceInfo(sourceInfo);
        testConf.setTargetInfo(targetInfo);
        testConf.setAcceptanceRelation("lgdo:near");
        testConf.setVerificationRelation("lgdo:near");
        testConf.setAcceptanceFile("lgd_relaybox_verynear.nt");
        testConf.setVerificationThreshold(0.5);
        testConf.setVerificationFile("lgd_relaybox_near.nt");
        testConf.setOutputFormat("TAB");
        testConf.setAcceptanceThreshold(0.9);

    }


    @Test
    public void testRDFReaderForMLAgorithm() {
        List<LearningParameter> mlParameters = new ArrayList<>();
        LearningParameter lp = new LearningParameter();
        lp.setName("max execution time in minutes");
        lp.setValue(60);
        mlParameters.add(lp);

        testConf.setMlAlgorithmName("wombat simple");
        testConf.setMlAlgorithmParameters(mlParameters);

//        String file = System.getProperty("user.dir") + "/resources/lgd-lgd-ml.ttl";

        String file = Thread.currentThread().getContextClassLoader().getResource("lgd-lgd-ml.ttl").getPath();
        RDFConfigurationReader c = new RDFConfigurationReader(file);
        Configuration fileConf = c.read();
        assertTrue(testConf.equals(fileConf));
    }

    @Test
    public void testRDFReaderForMetric() {
        testConf.setMetricExpression("geo_hausdorff(x.polygon, y.polygon)");
        testConf.setAcceptanceRelation("lgdo:near");
        testConf.setVerificationRelation("lgdo:near");
        testConf.setAcceptanceThreshold(0.9);
        testConf.setAcceptanceFile("lgd_relaybox_verynear.nt");
        testConf.setVerificationThreshold(0.5);
        testConf.setVerificationFile("lgd_relaybox_near.nt");
        testConf.setOutputFormat("TAB");
        testConf.setOptimizationTime(1000);
        testConf.setExpectedSelectivity(0.8);

//        String file = System.getProperty("user.dir") + "/resources/lgd-lgd.ttl";
        String file = Thread.currentThread().getContextClassLoader().getResource("lgd-lgd.ttl").getPath();

        RDFConfigurationReader c = new RDFConfigurationReader(file);
        Configuration fileConf = c.read();
        assertTrue(testConf.equals(fileConf));
    }

    @Test
    public void testRDFReaderForOptionalProperties() {
        testConf.setMetricExpression("geo_hausdorff(x.polygon, y.polygon)");

        sourceInfo.setOptionalProperties(Arrays.asList("rdfs:label"));
        targetInfo.setOptionalProperties(Arrays.asList("rdfs:label"));

        String file = SYSTEM_DIR + "/resources/lgd-lgd-optional-properties.ttl";
        RDFConfigurationReader c = new RDFConfigurationReader(file);
        Configuration fileConf = c.read();

        assertTrue(testConf.equals(fileConf));
    }

    @Test
    public void test1() {
        //Thread.currentThread().getContextClassLoader().getResource("lgd-lgd.ttl").getPath();
        String filename = Thread.currentThread().getContextClassLoader().getResource("lgd-lgd.ttl").getPath();
        RDFConfigurationReader reader = new RDFConfigurationReader(filename);
        Configuration config = reader.read();

        Set<String> parameters = config.getConfigurationParametersNames();
        assertTrue(parameters.contains("optimizationTime"));
        assertTrue(parameters.contains("expectedSelectivity"));

        assertTrue(config.getExpectedSelectivity() == 0.8);
        assertTrue(config.getOptimizationTime() == 1000);


    }

    @Test
    public void test2() {
        String filename = Thread.currentThread().getContextClassLoader().getResource("lgd-lgd2.ttl").getPath();
        RDFConfigurationReader reader = new RDFConfigurationReader(filename);
        Configuration config = reader.read();


        assertTrue(config.getExpectedSelectivity() == 1.0);
        assertTrue(config.getOptimizationTime() == 0);


    }
}
