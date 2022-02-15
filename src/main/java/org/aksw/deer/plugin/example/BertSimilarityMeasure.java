/**
 * @author Raviteja Kanagarla
 * @author Chaitali Suhas Bagwe
 *
 */

package org.aksw.deer.plugin.example;
import com.github.jsonldjava.shaded.com.google.common.collect.Multiset;
import org.apache.jena.rdf.model.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class matches KGs using BERT similarity measure.
 */
public class BertSimilarityMeasure {

  /**
   * This method sends the n-triples files from the bigInput
   * file folder for preprocessing with the help of {@link FileProcessing#createAndProcessTokens()}.
   * ProcessBuilder is used to call the bertSim.py python file for finding
   * the BERT similarity. The stored output from this python file is read and
   * the values are stored in a RDF model. This RDF model is added to a list of models.
   *
   * @return the list of models in which the RDF model of Bert similarity is stored
   * @throws IOException
   * @throws InterruptedException
   */
  public List<Model> calculateBertSimilarity() throws IOException, InterruptedException {
    List<Model> listOfModels = new ArrayList<>();
    String nameOfFile="BertSimilarityModelOutput.rdf";
    Model modelDefault = ModelFactory.createDefaultModel();

    FileProcessing processFiles = new FileProcessing();

    FileProcessing.methodToRemoveOutputFiles();
    final File folder = new File("HobbitFiles//");
    processFiles.listFilesForFolder(folder);
    processFiles.uniqueWords = new ArrayList[processFiles.filename.size()];
    processFiles.multiset=new Multiset[processFiles.filename.size()];
    processFiles.createAndProcessTokens();

    //Calling the python function using ProcessBuilder
    ProcessBuilder pb = new ProcessBuilder("python", "bertSim.py").inheritIO();
    Process p = pb.start();
    p.waitFor();

    //Reading the output generated by Python File
    File file = new File( "BertSimilarityOutput.nt");
    BufferedReader br = new BufferedReader(new FileReader(file));
    String line = "";
    while((line = br.readLine()) != null)
    {
      //Copying the output generated by Python File to a Model
      String[] outputLine = line.split("\\s+");
      String file1 = outputLine[0];
      String file2 = outputLine[2];
      Property pop=ResourceFactory.createProperty("BertSimilarityValue="+outputLine[1]);
      Resource res= modelDefault.createResource(file1);
      modelDefault.add(res,pop,modelDefault.createResource(file2));
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





