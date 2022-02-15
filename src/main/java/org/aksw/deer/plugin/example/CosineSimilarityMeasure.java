/**
 * @author Raviteja Kanagarla
 * @author Chaitali Suhas Bagwe
 *
 */

package org.aksw.deer.plugin.example;
import com.github.jsonldjava.shaded.com.google.common.collect.Multiset;
import org.apache.jena.rdf.model.*;
import org.dkpro.similarity.algorithms.api.SimilarityException;
import org.dkpro.similarity.algorithms.api.TextSimilarityMeasure;
import org.dkpro.similarity.algorithms.lexical.string.CosineSimilarity;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class matched KGs using Cosine similarity measure.
 */
public class CosineSimilarityMeasure {
  /**
   * Method to calculate cosine similarity using open source framework after
   * deleting previous output files and preprocessing input files using
   * {@link FileProcessing#createAndProcessTokens()}..
   * {@link CosineSimilarity} is invoked to calculate the cosine coefficient.
   *
   * @return the list of models in which the RDF model of cosine similarity is stored
   * @throws SimilarityException
   */
  public List<Model> cosineSimilarityFunction() throws SimilarityException {
    List<Model> listOfModels = new ArrayList<>();
    String nameOfFile = "CosineSimilarityModelOutput.rdf";
    Model modelDefault = ModelFactory.createDefaultModel();

    FileProcessing processFiles = new FileProcessing();

    processFiles.methodToRemoveOutputFiles();
    final File folder = new File("HobbitFiles//");
    processFiles.listFilesForFolder(folder);

    processFiles.uniqueWords = new ArrayList[processFiles.filename.size()];
    processFiles.multiset=new Multiset[processFiles.filename.size()];
    processFiles.createAndProcessTokens();

    //Initialize the cosine similarity measure
    TextSimilarityMeasure cosMeasure = new CosineSimilarity();
    for (int i = 0; i < processFiles.filename.size(); i++) {
      for (int j = i + 1; j < processFiles.filename.size(); j++) {
        Collection<String> doc1 = processFiles.uniqueWords[i];
        Collection<String> doc2 = processFiles.uniqueWords[j];
        System.out.println("i = " + processFiles.filename.get(i) + " j = " + processFiles.filename.get(j));

        // calculate the cosine similarity coefficient
        double cosSim = cosMeasure.getSimilarity(doc1, doc2);

        System.out.println("Cosine Similarity: " + cosSim);
        Property pop= ResourceFactory.createProperty("CosineSimilarityValue="+cosSim);
        try {
          //write the cosine similarity measure in a local n-triple file
          FileWriter writer = new FileWriter( "CosineSimilarityOutput"+ ".nt",true);
          String data = processFiles.filename.get(i) + "\t" + cosSim + "\t" + processFiles.filename.get(j) + " .";
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
}
