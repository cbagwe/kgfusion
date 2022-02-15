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
import java.util.HashSet;
import java.util.List;

/**
 * This class matches KGs using Jaccard similarity measure.
 */
public class JaccardSimilarityMeasure {

  /**
   * This method computes the Jaccard similarity . Before calculating the similarity,
   * all output files are deleted, if they exist.
   * Input files are preprocessed sequentially and written to text files with tokens.
   * Jaccard coefficient is calculated on these created tokens. This is done using
   * {@link FileProcessing#createAndProcessTokens()}.
   *
   * @return the list of models in which the RDF model of Jaccard similarity is stored
   */
  public List<Model> jaccardSimilarityFunction() {
    List<Model> listOfModels = new ArrayList<>();
    String nameOfFile="JaccardSimilarityModelOutput.rdf";
    Model modelDefault = ModelFactory.createDefaultModel();

    FileProcessing processFiles = new FileProcessing();

    processFiles.methodToRemoveOutputFiles();
    final File folder = new File("HobbitFiles//");
    processFiles.listFilesForFolder(folder);

    processFiles.uniqueWords = new ArrayList[processFiles.filename.size()];
    processFiles.multiset=new Multiset[processFiles.filename.size()];
    processFiles.createAndProcessTokens();

    for (int i = 0; i < processFiles.filename.size(); i++) {
      for (int j = i + 1; j < processFiles.filename.size(); j++) {

        //find the jaccard coefficient between two datasets
        double jaccardSim = calculateJaccardCoefficient(processFiles.uniqueWords[i], processFiles.uniqueWords[j]);

        Property pop= ResourceFactory.createProperty("JaccardSimilarityValue="+jaccardSim);
        System.out.println("i = " + processFiles.filename.get(i) + " j = " + processFiles.filename.get(j) + " similarity value= " + jaccardSim);
        try {
          // write the similarity measure in a local .nt file
          FileWriter writer = new FileWriter( "JaccardSimilarityOutput" + ".nt", true);
          String data = processFiles.filename.get(i) + "\t" + jaccardSim + "\t" + processFiles.filename.get(j);
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
   * Method to compute the Jaccard similarity coefficient from two array lists
   *
   * @param a - List of all tokens in KG1
   * @param b - List of all tokens in KG2
   * @return Jaccard Similarity coefficient
   */
  public double calculateJaccardCoefficient(ArrayList<String> a, ArrayList<String> b) {
    HashSet<String> s1 = new HashSet<String>();
    HashSet<String> s2 = new HashSet<String>();

    if (a.size() < b.size()) {
      for (int i = 0; i < a.size(); i++) {
        s1.add(a.get(i));
      }
      for (int i = 0; i < a.size(); i++) {
        s2.add(b.get(i));
      }
    } else {
      for (int i = 0; i < b.size(); i++) {
        s1.add(a.get(i));
      }
      for (int i = 0; i < b.size(); i++) {
        s2.add(b.get(i));
      }
    }
    final int sa = s1.size();
    final int sb = s2.size();
    s1.retainAll(s2);
    final int intersection = s1.size();
    return 1d / (sa + sb - intersection) * intersection;
  }
}
