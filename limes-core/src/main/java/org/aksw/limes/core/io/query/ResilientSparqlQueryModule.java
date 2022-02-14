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
package org.aksw.limes.core.io.query;

import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontend;
import org.aksw.jena_sparql_api.cache.h2.CacheUtilsH2;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlServiceReference;
import org.aksw.jena_sparql_api.pagination.core.QueryExecutionFactoryPaginated;
import org.aksw.limes.core.io.cache.ACache;
import org.aksw.limes.core.io.config.KBInfo;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.DatasetDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;


/**
 * @author Mohamed Sherif (sherif@informatik.uni-leipzig.de)
 * @version Jul 12, 2016
 */
public class ResilientSparqlQueryModule extends SparqlQueryModule implements IQueryModule {

    protected Logger logger = LoggerFactory.getLogger(ResilientSparqlQueryModule.class);

    protected int retryCount = 5;
    protected int retryDelayInMS = 500;
    protected int requestDelayInMs = 50;
    protected int pageSize = 900;
    protected long timeToLive = 24l * 60l * 60l * 1000l;
    protected String cacheDirectory = System.getProperty("user.dir") + "/cache";


    public ResilientSparqlQueryModule(KBInfo kbInfo) {
        super(kbInfo);
    }


    public ResilientSparqlQueryModule(KBInfo kbinfo, Logger logger, int retryCount, int retryDelayInMS,
                                      int requestDelayInMs, int pageSize, long timeToLive, String cacheDirectory) {
        super(kbinfo);
        this.logger = logger;
        this.retryCount = retryCount;
        this.retryDelayInMS = retryDelayInMS;
        this.requestDelayInMs = requestDelayInMs;
        this.pageSize = pageSize;
        this.timeToLive = timeToLive;
        this.cacheDirectory = cacheDirectory;
    }


    /**
     * Reads from a SPARQL endpoint or a file and writes the results in a cache
     *
     * @param cache The cache in which the content on the SPARQL endpoint is to be written
     * @param sparql True if the endpoint is a remote SPARQL endpoint, else assume that is is a jena model
     */
    public void fillCache(ACache cache, boolean sparql) {
        long startTime = System.currentTimeMillis();
        String query = generateQuery();

        logger.info("Querying the endpoint.");
        //run query
        org.aksw.jena_sparql_api.core.QueryExecutionFactory qef = null;
        try {
            qef = initQueryExecution(kb);
        } catch (Exception e) {
            e.printStackTrace();
        }
        QueryExecution qe = qef.createQueryExecution(query);
        int counter = 0;
        ResultSet results = qe.execSelect();
        //write
        String uri, value;
        while (results.hasNext()) {
            QuerySolution soln = results.nextSolution();
            // process query here
            {
                try {
                    uri = soln.get(kb.getVar().substring(1)).toString();
                    int i = 1;
                    for (String propertyLabel : kb.getProperties()) {
                        if (soln.contains("v" + i)) {
                            value = soln.get("v" + i).toString();
                            cache.addTriple(uri, propertyLabel, value);
                        }
                        i++;
                    }
                    if(kb.getOptionalProperties() != null){
                        for (String propertyLabel : kb.getOptionalProperties()) {
                            if (soln.contains("v" + i)) {
                                value = soln.get("v" + i).toString();
                                cache.addTriple(uri, propertyLabel, value);
                            }
                        }
                        i++;
                    }
                } catch (Exception e) {
                    logger.warn("Error while processing: " + soln.toString());
                    logger.warn("Following exception occured: " + e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException();

                }
            }
            counter++;
        }
        logger.info("Retrieved " + counter + " triples and " + cache.size() + " entities.");
        logger.info("Retrieving statements took " + (System.currentTimeMillis() - startTime) / 1000.0 + " seconds.");
    }

    /**
     * @param kbInfo knowledge base information object
     * @return QueryExecutionFactory object
     * @throws ClassNotFoundException if class not found
     * @throws SQLException if SQL contains errors
     */
    protected QueryExecutionFactory initQueryExecution(KBInfo kbInfo) throws ClassNotFoundException, SQLException {
        QueryExecutionFactory qef;

        DatasetDescription dd = new DatasetDescription();
        if(kbInfo.getGraph() != null) {
            dd.addDefaultGraphURI(kbInfo.getGraph());
        }

        SparqlServiceReference ssr = new SparqlServiceReference(kbInfo.getEndpoint(), dd);

        qef = FluentQueryExecutionFactory
                .http(ssr)
                .config()
                .withRetry(retryCount, retryDelayInMS, TimeUnit.MILLISECONDS)
                .withDelay(requestDelayInMs, TimeUnit.MILLISECONDS)
                .withPagination(pageSize)
                .end()
                .create();

        if (cacheDirectory != null) {
            String dbName = kbInfo.getEndpoint().replaceAll("[:/]", "_");
            CacheFrontend cacheFrontend = CacheUtilsH2.createCacheFrontend(dbName, true, timeToLive);
            qef = new QueryExecutionFactoryCacheEx(qef, cacheFrontend);
        } else {
            logger.info("The cache directory has not been set. Creating an uncached SPARQL client.");
        }

        try {
            qef = new QueryExecutionFactoryPaginated(qef, pageSize);
            return qef;
        } catch (Exception e) {
            logger.warn("Couldn't create Factory with pagination. Returning Factory without pagination. Exception: " +
                    e.getLocalizedMessage());
            return qef;
        }
    }
}
