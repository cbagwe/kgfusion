/**
 * @author Raviteja Kanagarla
 * @author Chaitali Suhas Bagwe
 *
 */

package org.aksw.deer.plugin.example;
import com.github.jsonldjava.shaded.com.google.common.collect.Multiset;
import org.apache.jena.rdf.model.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class matches KGs using Weighted Jaccard similarity measure.
 */
public class WeightedJaccardSimilarityMeasure {
  /**
   * Method to calculate weighted Jaccard similarity after
   * deleting previous output files and preprocessing input files using
   * {@link FileProcessing#createAndProcessTokens()}..
   *
   * @return the list of models in which the RDF model of weighted jaccard is stored
   */

  public List<Model> weightedJaccardSimilarityFunction()
  {
    List<Model> listOfModels = new ArrayList<>();
    String nameOfFile="WeightedJaccardSimilarityModelOutput.rdf";
    Model modelDefault = ModelFactory.createDefaultModel();

    FileProcessing processFiles = new FileProcessing();

    processFiles.methodToRemoveOutputFiles();
    final File folder = new File("HobbitFiles//");
    processFiles.listFilesForFolder(folder);

    processFiles.uniqueWords = new ArrayList[processFiles.filename.size()];
    processFiles.multiset=new Multiset[processFiles.filename.size()];
    processFiles.createAndProcessTokens();

    for(int i=0;i< processFiles.filename.size();i++)
    {
      for (int j=i+1;j<processFiles.filename.size();j++)
      {
        // find the weighted jaccard coefficient between two datasets
        double wjaccardSim = calculateWeightedJaccardCoefficient(processFiles.multiset[i],processFiles.multiset[j],i,j);

        Property pop= ResourceFactory.createProperty("WeightedJaccardSimilarityValue="+wjaccardSim);
        System.out.println("i = " + processFiles.filename.get(i)+ " j = " + processFiles.filename.get(j));
        System.out.println("Weighted Jaccard similarity value= " + wjaccardSim);
        try {
          // write the similarity measure in a local .nt file
          FileWriter writer = new FileWriter( "WeightedJaccardSimilarityOutput"+ ".nt",true);
          String data = processFiles.filename.get(i) + "\t" + wjaccardSim + "\t" + processFiles.filename.get(j) + " .";
          writer.write(data);
          writer.write("\n");

          //populate the modelDefault
          Resource res= modelDefault.createResource(processFiles.filename.get(i));
          modelDefault.add(res,pop,modelDefault.createResource(processFiles.filename.get(j)));

          writer.close();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }

    //Writing into a Model
    try {
      FileWriter out=new FileWriter(nameOfFile);
      modelDefault.write(out, "TTL");
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    listOfModels.add(modelDefault);

    return listOfModels;
  }

  /**
   * Method to calculate weighted Jaccard similarity coefficient between two multisets
   * which contains processed token sorted according to their frequency.
   *
   * @param mul1 KG1 tokens, sorted according to frequency
   * @param mul2 KG2 tokens, sorted according to frequency
   * @param i reference to KG1
   * @param j reference to KG2
   * @return computed weighted jaccard similarity coefficient
   */
  public double calculateWeightedJaccardCoefficient(Multiset<String> mul1, Multiset<String> mul2,int i,int j)
  {
    FileProcessing processFiles = new FileProcessing();

    double max = 0;
    double min = 0;

    int count = Math.min(processFiles.uniqueWords[i].size(),processFiles.uniqueWords[j].size());

    for(int k=0; k<count; k++)
    {
      String word1 = processFiles.uniqueWords[i].get(k);
      String word2 = processFiles.uniqueWords[j].get(k);
      int count1 = mul1.count(word1);
      int count2 = mul2.count(word2);
      if( count1 < count2 )
      {
        min = min + count1;
        max = max + count2;
      }
      else
      {
        min = min + count2;
        max = max + count1;
      }
    }
    double jaccard = min/max;
    return jaccard;
  }
}
