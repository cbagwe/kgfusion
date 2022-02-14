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
package org.aksw.limes.core.ml.algorithm.euclid;

import org.aksw.limes.core.evaluation.qualititativeMeasures.PseudoFMeasure;
import org.aksw.limes.core.exceptions.UnsupportedMLImplementationException;
import org.aksw.limes.core.io.cache.ACache;
import org.aksw.limes.core.io.mapping.AMapping;
import org.aksw.limes.core.ml.algorithm.LearningParameter;
import org.aksw.limes.core.ml.algorithm.MLAlgorithmFactory;
import org.aksw.limes.core.ml.algorithm.MLResults;
import org.aksw.limes.core.ml.algorithm.classifier.SimpleClassifier;
import org.aksw.limes.core.ml.algorithm.euclid.LinearSelfConfigurator.QMeasureType;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Class wraps around EUCLIDs boolean classifier to abide LIMES ml interface
 *
 * @author Klaus Lyko (lyko@informatik.uni-leipzig.de)
 */
public class BooleanEuclid extends LinearEuclid {

    protected static Logger logger = Logger.getLogger(BooleanEuclid.class);

    static final String ALGORITHM_NAME = MLAlgorithmFactory.EUCLID_BOOLEAN;
    public static final String MAX_THRESHOLD = "max_threshold";

    @Override
    protected void init(List<LearningParameter> learningParameters, ACache sourceCache, ACache targetCache) {
        setDefaultParameters();
        super.init(learningParameters, sourceCache, targetCache);
        lsc = new BooleanSelfConfigurator(sourceCache, targetCache);
    }

    @Override
    public void setDefaultParameters() {    // execution mode. STRICT = true leads to a strong bias towards precision by
        double max_thres = 1.0;
        super.setDefaultParameters();
        learningParameters.add(new LearningParameter(MAX_THRESHOLD, max_thres, Double.class, 0d, 1d, Double.NaN, MAX_THRESHOLD));
    }


    @Override
    protected MLResults learn(PseudoFMeasure pfm) throws UnsupportedMLImplementationException {
        // first setup EUCLID
        configureEuclid(lsc);
        lsc.setPFMType(QMeasureType.UNSUPERVISED);
        // get initial classifiers
        List<SimpleClassifier> init_classifiers = lsc.getBestInitialClassifiers();
        List<SimpleClassifier> result_classifiers  = lsc.learnClassifer(init_classifiers);
        // compute results
        MLResults result = new MLResults();
        AMapping mapping = lsc.getMapping(result_classifiers);
        result.setMapping(mapping);
        result.setQuality(lsc.computeQuality(mapping));
        result.setLinkSpecification(lsc.getLinkSpecification(result_classifiers));
        result.setClassifiers(result_classifiers);
        for(int i = 0; i<result_classifiers.size(); i++) {
            result.addDetail(i+". Classifier ", result_classifiers.get(i));
            AMapping map = lsc.executeClassifier(result_classifiers.get(i), result_classifiers.get(i).getThreshold());
            result.addDetail(i+". Mapping.size= ", map.size());
        }
        return result;
    }

    @Override
    protected AMapping predict(ACache source, ACache target, MLResults mlModel) {
        assert (mlModel.classifiersSet());
        List<SimpleClassifier> classifiers = mlModel.getClassifiers();
        assert(classifiers.size()>0);
        BooleanSelfConfigurator le = new BooleanSelfConfigurator(source, target);
        configureEuclid(le);
        AMapping map = le.getMapping(classifiers);
//		logger.info("Predict  with mlModel on Caches +"+source.size()+","+target.size()+"+ resulted in "+map.size()+" map.");
        return map;
    }

    @Override
    protected String getName() {
        return ALGORITHM_NAME;
    }
}
