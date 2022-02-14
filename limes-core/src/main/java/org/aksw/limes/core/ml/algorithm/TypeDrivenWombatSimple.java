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
package org.aksw.limes.core.ml.algorithm;

import org.aksw.limes.core.datastrutures.LogicOperator;
import org.aksw.limes.core.datastrutures.Tree;
import org.aksw.limes.core.evaluation.qualititativeMeasures.PseudoFMeasure;
import org.aksw.limes.core.exceptions.UnsupportedMLImplementationException;
import org.aksw.limes.core.io.cache.ACache;
import org.aksw.limes.core.io.cache.Instance;
import org.aksw.limes.core.io.ls.LinkSpecification;
import org.aksw.limes.core.io.mapping.AMapping;
import org.aksw.limes.core.io.mapping.MappingFactory;
import org.aksw.limes.core.io.mapping.MappingFactory.MappingType;
import org.aksw.limes.core.measures.mapper.MappingOperations;
import org.aksw.limes.core.ml.algorithm.classifier.ExtendedClassifier;
import org.aksw.limes.core.ml.algorithm.wombat.AWombat;
import org.aksw.limes.core.ml.algorithm.wombat.CheckType;
import org.aksw.limes.core.ml.algorithm.wombat.LinkEntropy;
import org.aksw.limes.core.ml.algorithm.wombat.RefinementNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Simple implementation of the Type Driven Wombat simple algorithm
* Used most of the code from WombatSimple.java class (@author Mohamed Sherif (sherif@informatik.uni-leipzig.de))
* add new method called setProperties() 
* Modify findInitialClassifiers() method

* @author Khalid Bin Huda Siddiqui (khalids@campus.uni-paderborn.de)
* @author Khalid Khan (kkhan@campus.uni-paderborn.de)
 */


public class TypeDrivenWombatSimple extends AWombat {

    private static final Logger logger = LoggerFactory.getLogger(TypeDrivenWombatSimple.class);

    private static final String ALGORITHM_NAME = "Type Driven Wombat Simple";

    private static final int activeLearningRate = 3;

    private RefinementNode bestSolutionNode = null;

    private List<ExtendedClassifier> classifiers = null;

    private Tree<RefinementNode> refinementTreeRoot = null;
    
    Set<String> sourcePropertiesWithAtomicTemporalMeasures = new HashSet<>();
    Set<String> sourcePropertiesWithAtomicMeasuresString = new HashSet<>();
    Set<String> sourcePropertiesWithAtomicMeasuresTopological = new HashSet<>();
    Set<String> sourcePropertiesWithAtomicMeasuresPointSet = new HashSet<>();
    Set<String> sourcePropertiesWithAtomicMeasuresVectorSpace = new HashSet<>();

    Set<String> targetPropertiesWithAtomicTemporalMeasures =  new HashSet<>();
    Set<String> targetPropertiesWithAtomicMeasuresString =  new HashSet<>();
    Set<String> targetPropertiesWithAtomicMeasuresTopological =  new HashSet<>();
    Set<String> targetPropertiesWithAtomicMeasuresPointSet =  new HashSet<>();
    Set<String> targetPropertiesWithAtomicMeasuresVectorSpace =  new HashSet<>();

    /**
     * TypeDrivenWombatSimple constructor.
     */
    protected TypeDrivenWombatSimple() {
        super();
    }

    @Override
    protected String getName() {
        return ALGORITHM_NAME;
    }

    @Override
    protected void init(List<LearningParameter> lp, ACache sourceCache, ACache targetCache) {
        super.init(lp, sourceCache, targetCache);
        sourceUris = sourceCache.getAllUris();
        targetUris = targetCache.getAllUris();
        bestSolutionNode = null;
        classifiers = null;
    }

    @Override
    protected MLResults learn(AMapping trainingData) {
        this.trainingData = trainingData;
        fillSampleSourceTargetCaches(trainingData);
        this.sourceCache = sourceSample;
        this.targetCache = targetSample;
        return learn();
    }

    /**
     * @return wrap with results, null if no result found
     */
    private MLResults learn() {
        if (bestSolutionNode == null) { // not to do learning twice
            bestSolutionNode = findBestSolution();
        }
        String bestMetricExpr = bestSolutionNode.getMetricExpression();
        if(!bestMetricExpr.equals("")){
            double threshold = Double.parseDouble(bestMetricExpr.substring(bestMetricExpr.lastIndexOf("|") + 1, bestMetricExpr.length()));
            AMapping bestMapping = bestSolutionNode.getMapping();
            LinkSpecification bestLS = new LinkSpecification(bestMetricExpr, threshold);
            double bestFMeasure = bestSolutionNode.getFMeasure();
            return new MLResults(bestLS, bestMapping, bestFMeasure, null);
        }
        // case no mapping found
        return null;
    }

    @Override
    protected MLResults learn(PseudoFMeasure pfm) {
        if(pfm != null){
            this.pseudoFMeasure = pfm;
        }else{ // use default PFM
            this.pseudoFMeasure = new PseudoFMeasure();
        }
        this.isUnsupervised = true;
        return learn();
    }

    @Override
    protected boolean supports(MLImplementationType mlType) {
        return mlType == MLImplementationType.SUPERVISED_BATCH ||
                mlType == MLImplementationType.UNSUPERVISED    ||
                mlType == MLImplementationType.SUPERVISED_ACTIVE;
    }

    @Override
    protected AMapping getNextExamples(int size) throws UnsupportedMLImplementationException {
        List<RefinementNode> bestNodes = getBestKNodes(refinementTreeRoot, activeLearningRate);
        AMapping intersectionMapping = MappingFactory.createDefaultMapping();
        AMapping unionMapping = MappingFactory.createDefaultMapping();

        for(RefinementNode sn : bestNodes){
            intersectionMapping = MappingOperations.intersection(intersectionMapping, sn.getMapping());
            unionMapping = MappingOperations.union(unionMapping, sn.getMapping());
        }
        AMapping posEntropyMapping = MappingOperations.difference(unionMapping, intersectionMapping);

        TreeSet<LinkEntropy> linkEntropy = new TreeSet<>();
        int entropyPos = 0, entropyNeg = 0;
        for(String s : posEntropyMapping.getMap().keySet()){
            for(String t : posEntropyMapping.getMap().get(s).keySet()){
                // compute Entropy(s,t)
                for(RefinementNode sn : bestNodes){
                    if(sn.getMapping().contains(s, t)){
                        entropyPos++;
                    }else{
                        entropyNeg++;
                    }
                }
                int entropy = entropyPos * entropyNeg;
                linkEntropy.add(new LinkEntropy(s, t, entropy));
            }
        }
        // get highestEntropyLinks
        List<LinkEntropy> highestEntropyLinks = new ArrayList<>();
        int i = 0;
        Iterator<LinkEntropy> itr = linkEntropy.descendingIterator();
        while(itr.hasNext() && i < size) {
            LinkEntropy next = itr.next();
            if (!trainingData.contains(next.getSourceUri(), next.getTargetUri())) {
                highestEntropyLinks.add(next);
                i++;
            }
        }
        AMapping result = MappingFactory.createDefaultMapping();
        for(LinkEntropy l: highestEntropyLinks){
            result.add(l.getSourceUri(), l.getTargetUri(), l.getEntropy());
        }
        return result;
    }

    @Override
    protected MLResults activeLearn(){
        return learn(new PseudoFMeasure());
    }

    @Override
    protected MLResults activeLearn(AMapping oracleMapping) {
        trainingData = MappingOperations.union(trainingData, oracleMapping);
        boolean hasPositiveExamples = trainingData.getMap().entrySet().stream()
                .flatMap(e -> e.getValue().values().stream())
                .anyMatch(x -> x == 1);
        if (hasPositiveExamples) {
            updateScores(refinementTreeRoot);
            isUnsupervised = false;
            bestSolutionNode = findBestSolution();
        }
        return learn();
    }


    /**
     * update F-Measure of the refinement tree r
     * based on either training data or PFM
     *
     * @param r refinement tree
     */
    protected void updateScores(Tree<RefinementNode> r) {
        if (r.getchildren() == null || r.getchildren().size() == 0) {
            r.getValue().setfMeasure(fMeasure(r.getValue().getMapping()));
            return;
        }
        for (Tree<RefinementNode> child : r.getchildren()) {
            if (child.getValue().getFMeasure() >= 0) {
                r.getValue().setfMeasure(fMeasure(r.getValue().getMapping()));
                updateScores(child);
            }
        }
    }
    /**
     * findInitialClassifiers 
     * It find initial classifier by only comparing same source data type properties to
     * same target data type properties with respect to their atomic measure.
     * It does not compare a string property with a date property.
     * 
     * @return list of ExtendedClassifier
     */
    @Override
    protected List<ExtendedClassifier> findInitialClassifiers() {
		logger.debug("Geting all initial classifiers ...");
		setProperties();
		List<ExtendedClassifier> initialClassifiers = new ArrayList<>();
		
		
	
		Set<String> stringMeasures = new HashSet<>(Arrays.asList("jaccard", "qgrams"));
        Set<String> temporalMeasures = new HashSet<>(Arrays.asList("tmp_predecessor", "tmp_successor"));
		Set<String> vectorSpaceMeasures = new HashSet<>(Arrays.asList("euclidean", "manhattan")); //remove geo_orthodromic 
		Set<String> pointSetMeasures = new HashSet<>(Arrays.asList("geo_centroid_indexed_hausdorff", "geo_fast_hausdorff", "geo_max", "geo_mean"));

		// String measure 
		for (String p : sourcePropertiesWithAtomicMeasuresString) {
			for (String q : targetPropertiesWithAtomicMeasuresString) {
				for (String m : stringMeasures) {
					ExtendedClassifier cp = findInitialClassifier(p, q, m);
					// only add if classifier covers all entries
					initialClassifiers.add(cp);
				}
			}
		}
		
		// Temporal measure  
				for (String p : sourcePropertiesWithAtomicTemporalMeasures) {
					for (String q : targetPropertiesWithAtomicTemporalMeasures) {
						for (String m : temporalMeasures) {
							ExtendedClassifier cp = findInitialClassifier(p, q, m);
							// only add if classifier covers all entries
							initialClassifiers.add(cp);
						}
					}
				}
		// vectorSpaceMeasures
				for (String p : sourcePropertiesWithAtomicMeasuresVectorSpace) {
					for (String q : targetPropertiesWithAtomicMeasuresVectorSpace) {
						for (String m : vectorSpaceMeasures) {
							ExtendedClassifier cp = findInitialClassifier(p, q, m);
							// only add if classifier covers all entries
							initialClassifiers.add(cp);
						}
					}
				}

				// pointSetMeasures
				for (String p : sourcePropertiesWithAtomicMeasuresPointSet) {
					for (String q : targetPropertiesWithAtomicMeasuresPointSet) {
						for (String m : pointSetMeasures) {
							ExtendedClassifier cp = findInitialClassifier(p, q, m);
							// only add if classifier covers all entries
							initialClassifiers.add(cp);
						}
					}
				}
		
		
		
		System.out.println(" *initialClassifiers* : " + initialClassifiers);
		logger.debug("Done computing all initial classifiers.");
		return initialClassifiers;
	}

    
    

    /**
     * @return RefinementNode containing the best over all solution
     */
    public RefinementNode findBestSolution() {
        classifiers = findInitialClassifiers();
        createRefinementTreeRoot();
        Tree<RefinementNode> mostPromisingNode = getMostPromisingNode(refinementTreeRoot);
        logger.debug("Most promising node: " + mostPromisingNode.getValue());
        int i = 1;
        while ((mostPromisingNode.getValue().getFMeasure()) < getMaxFitnessThreshold()
                && refinementTreeRoot.size() <= getMaxRefinmentTreeSize()
                && i <= getMaxIterationNumber()) {
            expandNode(mostPromisingNode);
            mostPromisingNode = getMostPromisingNode(refinementTreeRoot);
            if (mostPromisingNode.getValue().getFMeasure() == -Double.MAX_VALUE) {
                break; // no better solution can be found
            }
            logger.debug("Most promising node: " + mostPromisingNode.getValue());
            i++;
        }
        RefinementNode bestSolution = getBestNode(refinementTreeRoot).getValue();
        logger.debug("Overall Best Solution: " + bestSolution);
        return bestSolution;
    }


    /**
     * @param r the root of the refinement tree
     * @param k number of best nodes
     * @return sorted list of best k tree nodes
     */
    protected List<RefinementNode> getBestKNodes(Tree<RefinementNode> r, int k) {
        TreeSet<RefinementNode> ts = new TreeSet<>();
        TreeSet<RefinementNode> sortedNodes = getSortedNodes(r, getOverAllPenaltyWeight(), ts);
        List<RefinementNode> resultList = new ArrayList<>();
        int i = 0;
        Iterator<RefinementNode> itr = sortedNodes.descendingIterator();
        while(itr.hasNext() && i < k) {
            resultList.add(itr.next());
            i++;
        }
        return resultList;
    }


    /**
     * @param r the root of the refinement tree
     * @param penaltyWeight from 0 to 1
     * @param result refinment tree
     * @return sorted list of tree nodes
     */
    protected TreeSet<RefinementNode> getSortedNodes(Tree<RefinementNode> r, double penaltyWeight, TreeSet<RefinementNode> result) {
        if (r.getchildren() == null || r.getchildren().size() == 0) {
            result.add(r.getValue());
            return result;
        }
        for (Tree<RefinementNode> child : r.getchildren()) {
            if (child.getValue().getFMeasure() >= 0) {
                result.add(r.getValue());
                return getSortedNodes(child, penaltyWeight, result);
            }
        }
        return null;
    }

    /**
     * Expand an input refinement node by applying
     * all available operators to the input refinement
     * node's mapping with all other classifiers' mappings
     *
     * @param node
     *         Refinement node to be expanded
     * @author sherif
     */
    private void expandNode(Tree<RefinementNode> node) {
        AMapping map = MappingFactory.createDefaultMapping();
        for (ExtendedClassifier c : classifiers) {
            for (LogicOperator op : LogicOperator.values()) {
                if (!node.getValue().getMetricExpression().equals(c.getMetricExpression())) { // do not create the same metricExpression again
                    if (op.equals(LogicOperator.AND)) {
                        map = MappingOperations.intersection(node.getValue().getMapping(), c.getMapping());
                    } else if (op.equals(LogicOperator.OR)) {
                        map = MappingOperations.union(node.getValue().getMapping(), c.getMapping());
                    } else if (op.equals(LogicOperator.MINUS)) {
                        map = MappingOperations.difference(node.getValue().getMapping(), c.getMapping());
                    }
                    String metricExpr = op + "(" + node.getValue().getMetricExpression() + "," + c.getMetricExpression() + ")|0";
                    RefinementNode child = createNode(map, metricExpr);
                    node.addChild(new Tree<RefinementNode>(child));
                }
            }
        }
        if (isVerbose()) {
            refinementTreeRoot.print();
        }
    }


    /**
     * initiate the refinement tree as a root node  with set of
     * children nodes containing all initial classifiers
     *
     */
    protected void createRefinementTreeRoot() {
        RefinementNode initialNode = new RefinementNode(-Double.MAX_VALUE, MappingFactory.createMapping(MappingType.DEFAULT), "");
        refinementTreeRoot = new Tree<RefinementNode>(null, initialNode, null);
        for (ExtendedClassifier c : classifiers) {
            RefinementNode n = new RefinementNode(c.getfMeasure(), c.getMapping(), c.getMetricExpression());
            refinementTreeRoot.addChild(new Tree<RefinementNode>(refinementTreeRoot, n, null));
        }
        if (isVerbose()) {
            refinementTreeRoot.print();
        }
    }
     /*
     * This method take an instance from source & target data set and
     * then go through to all the properties and classifiy their data type(String, Number etc)
     */
    protected void setProperties(){
     		Set<String> sourceProperties = sourceCache.getNextInstance().getAllProperties();
    		Instance instance = sourceCache.getNextInstance();
    		
    		for (String s1 : sourceProperties) {
    			System.out.println("----------------------------");
    			String val = " ";
    			try {
    				val = instance.getProperty(s1).first();
    			} catch (Exception e) {
    				// TODO: handle exception
    			}
    			
    			
    			System.out.println("Property Name: " + s1);
    			System.out.println("Property value: " + val);
    			System.out.println("Data type: " + CheckType.check(val));
    			System.out.println("----------------------------");

    			if (CheckType.check(val) == "date") {
    				sourcePropertiesWithAtomicTemporalMeasures.add(s1);
    			} else if (CheckType.check(val) == "point") {
    				sourcePropertiesWithAtomicMeasuresPointSet.add(s1);
    			} else if (CheckType.check(val) == "number") {
    				sourcePropertiesWithAtomicMeasuresVectorSpace.add(s1);
    			} else {
    				sourcePropertiesWithAtomicMeasuresString.add(s1);
    			}
    		}
    		
    		System.out.println("sourcePropertiesWithAtomicTemporalMeasures :" + sourcePropertiesWithAtomicTemporalMeasures);
    		System.out.println("sourcePropertiesWithAtomicMeasuresPointSet :" + sourcePropertiesWithAtomicMeasuresPointSet);
    		System.out.println("sourcePropertiesWithAtomicMeasuresVectorSpace :" + sourcePropertiesWithAtomicMeasuresVectorSpace);
    		System.out.println("sourcePropertiesWithAtomicMeasuresString :" + sourcePropertiesWithAtomicMeasuresString);

    		Set<String> targetProperties = targetCache.getAllProperties();
    		Instance ins = targetCache.getNextInstance();
    		for (String t1 : targetProperties) {
    			System.out.println("----------------------------");
    			System.out.println("t1:" + t1);
    			String val = " ";
 
    			try {
    			val =	ins.getProperty(t1).first();
    				System.out.println("+++++++++++ val : " + val );
    			} catch (Exception e) {
    				System.out.println("----------killed------------------");
    			}
    			
    			System.out.println("Property Name: ." + t1);
    			System.out.println("Property value: ." + val);
    			System.out.println("T1 : " + CheckType.check(val));
    			System.out.println("----------------------------");

    			if (CheckType.check(val) == "date") {
    				targetPropertiesWithAtomicTemporalMeasures.add(t1);
    			} else if (CheckType.check(val) == "point") {
    				targetPropertiesWithAtomicMeasuresPointSet.add(t1);
    			} else if (CheckType.check(val) == "number") {
    				targetPropertiesWithAtomicMeasuresVectorSpace.add(t1);
    			} else {
    				targetPropertiesWithAtomicMeasuresString.add(t1);
    			}
    		}
    		
    		System.out.println("targetPropertiesWithAtomicTemporalMeasures :" + targetPropertiesWithAtomicTemporalMeasures);
    		System.out.println("targetPropertiesWithAtomicMeasuresPointSet :" + targetPropertiesWithAtomicMeasuresPointSet);
    		System.out.println("targetPropertiesWithAtomicMeasuresVectorSpace :" + targetPropertiesWithAtomicMeasuresVectorSpace);
    		System.out.println("targetPropertiesWithAtomicMeasuresString :" + targetPropertiesWithAtomicMeasuresString);
    		
    	}
}
