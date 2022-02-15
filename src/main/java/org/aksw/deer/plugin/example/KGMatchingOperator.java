/**
 * @author Raviteja Kanagarla
 * @author Chaitali Suhas Bagwe
 *
 */

package org.aksw.deer.plugin.example;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import org.aksw.deer.enrichments.AbstractParameterizedEnrichmentOperator;
import org.aksw.deer.vocabulary.DEER;
import org.aksw.faraday_cage.engine.ValidatableParameterMap;
import org.aksw.limes.core.io.config.Configuration;
import org.apache.jena.rdf.model.*;
import org.dkpro.similarity.algorithms.api.SimilarityException;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class calls the various similarity algorithms and LIMES approach for KG matching
 */
@Extension
public class KGMatchingOperator extends AbstractParameterizedEnrichmentOperator {

  private static final Logger logger = LoggerFactory.getLogger(KGMatchingOperator.class);
  //getting the user mentioned approach from configuration file
  public static  Property Approach=DEER.property("matchingApproach");
  public static List<Model> modelToNextGroup=new ArrayList<>();


  /**
   * Base Constructor
   */
  public KGMatchingOperator() {

    super();
  }


  @Override
  public ValidatableParameterMap createParameterMap() { // 2
    return ValidatableParameterMap.builder().declareProperty(Approach)
      .declareValidationShape(getValidationModelFor(KGMatchingOperator.class)).build();
  }

  /**
   * This method reads the configuration.ttl file and performs KG Matching
   * using the approach/algorithm specified in the file.
   * @param models
   * @return the models to submit to the next group of Ontology Matching
   */
  @Override
  protected List<Model> safeApply(List<Model> models)  { // 3
    String choice= getParameterMap().getOptional(Approach).map(RDFNode::asLiteral).map(Literal::getString)
      .orElse("LIMES");
    if (choice=="")
    {
      choice="LIMES"; // if no approach specified, Limes will get executed by default
    }

    System.out.println("your choice is "+choice);

    switch(choice)
    {
      case "LIMES":
        LimesApproach limesApp = new LimesApproach();
        Configuration con = limesApp.createLimesConfigurationFile(models);
        limesApp.callLimes(con);
        modelToNextGroup=limesApp.createSparQLEndpoint();
        break;
      case "JaccardSimilarity":
        JaccardSimilarityMeasure jaccardMeasure = new JaccardSimilarityMeasure();
        modelToNextGroup= jaccardMeasure.jaccardSimilarityFunction();
        break;
      case "WeightedJaccardSimilarity":
        WeightedJaccardSimilarityMeasure weightedJaccardMeasure = new WeightedJaccardSimilarityMeasure();
        modelToNextGroup= weightedJaccardMeasure.weightedJaccardSimilarityFunction();
        break;
      case "CosineSimilarity":
        CosineSimilarityMeasure cosSimMeasure = new CosineSimilarityMeasure();
        try {
          modelToNextGroup= cosSimMeasure.cosineSimilarityFunction();
        } catch (SimilarityException e) {
          e.printStackTrace();
        }
        break;
      case "DiceSimilarityMetrics":
        DiceSimilarityMeasure diceSimMeasure = new DiceSimilarityMeasure();
        try {
          modelToNextGroup= diceSimMeasure.diceSimilarityMetricsFunction();
        } catch (SimilarityException e) {
          e.printStackTrace();
        }
        break;
      case "BERTSimilarity":
        BertSimilarityMeasure bertMeasure = new BertSimilarityMeasure();
        try {
          modelToNextGroup= bertMeasure.calculateBertSimilarity();
        } catch (IOException e) {
          e.printStackTrace();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        break;

    }

    return modelToNextGroup;
  }

}


