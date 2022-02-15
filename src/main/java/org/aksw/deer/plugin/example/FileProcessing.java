/**
 * @author Raviteja Kanagarla
 * @author Chaitali Suhas Bagwe
 *
 */

package org.aksw.deer.plugin.example;
import com.aliasi.tokenizer.EnglishStopTokenizerFactory;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.github.jsonldjava.shaded.com.google.common.collect.HashMultiset;
import com.github.jsonldjava.shaded.com.google.common.collect.Multiset;
import org.apache.jena.iri.impl.Main;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;
import org.jetbrains.annotations.NotNull;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * This class processes the Hobbit files. This processed files are used by various
 * document matching similarities to match KGs. array
 */
public class FileProcessing {
  static String hobbitFiles = "HobbitFiles//";
  static ArrayList<String>[] uniqueWords;
  static ArrayList<String> filename = new ArrayList<String>();
  public static List<Model> modelToNextGroup=new ArrayList<>();
  static Multiset<String>[] multiset;
  public static Model modelDefault=ModelFactory.createDefaultModel();

  /**
   * Method to find the literals in dataset.
   * Literals are found using {@link Query} {@link QueryFactory}
   * and {@link QueryExecutionFactory} classes.
   * The found literals are cleaned and preprocessed later.
   */
  public void createAndProcessTokens() {
    // TODO Auto-generated method stub
    FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());
    for (int i = 0; i < filename.size(); i++) {
      System.out.println("Processing File " + filename.get(i));
      uniqueWords[i] = new ArrayList<>();
      multiset[i] = HashMultiset.create();
      Model model = FileManager.get().loadModel(hobbitFiles + filename.get(i));

      // sparql query to find literals in the datasets
      String queryString =
        "SELECT DISTINCT ?o {"
          + "          ?s ?p ?o"
          + "      FILTER isLiteral(?o)"
          + "}";
      Query query = QueryFactory.create(queryString);
      QueryExecution qexec = QueryExecutionFactory.create(query,model);

      //get the literals and send it for preprocessing
      try {
        ResultSet results = qexec.execSelect();
        while (results.hasNext()) {
          QuerySolution soln = results.nextSolution();
          String soltn = soln.toString().replace("( ?o = \"", "");
          int ind = soltn.indexOf("^^");
          if (ind > 0) {
            soltn = soltn.substring(0, ind - 1);
            removeStopWords(soltn, multiset[i]);
          } else {
            soltn = soltn.replace("\" (", "");
            removeStopWords(soltn, multiset[i]);
          }
        }
      } finally {
        qexec.close();
      }

      Multiset<String> multiset1 = HashMultiset.create();
      multiset1.addAll(multiset[i]);

      //sort the tokens in order of their frequency and store them in a ArrayList
      while (multiset1.size() > 0) {
        String key = getMax(multiset1);
        uniqueWords[i].add(key);
        multiset1.remove(key, multiset1.count(key));
      }

      // write the tokens of each file in .txt formal
      try {
        FileWriter writer = new FileWriter(hobbitFiles + filename.get(i).replace(".nt", "") + ".txt", false);
        writer.write(uniqueWords[i].toString());
        writer.write("\n");
        writer.close();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  /**
   * Method for cleaning a String. The string is cleared of stop words,
   * words with length <=2, special characters.
   *
   * @param soltn - Literal as string
   * @param multiset - Set of clean literal strings
   */
  private void removeStopWords(String soltn, Multiset<String> multiset) {
    soltn = soltn.toLowerCase().trim();
    soltn = soltn.replaceAll("[^a-zA-Z0-9]", " ");
    soltn = soltn.replaceAll("http.*?\\s", " ");
    soltn = soltn.replaceAll("\\w*\\d+\\w*\\s*", " ");
    TokenizerFactory factory = IndoEuropeanTokenizerFactory.INSTANCE;
    factory = new EnglishStopTokenizerFactory(factory);
    Tokenizer tokenizer = factory.tokenizer(soltn.toCharArray(), 0, soltn.length());

    for (String word : tokenizer) {
      if ((word.length() > 2) && !(word.matches("[0-9]+"))) {
        multiset.add(word);
      }
    }
  }

  /**
   * Method to get the token with maximum count from a set.
   *
   * @param multiset - Set of tokens (preprocessed literals)
   * @return - token with maximum count
   */
  private String getMax(@NotNull Multiset<String> multiset) {
    int max = 0;
    String maxKey = null;
    for (String word : multiset.elementSet()) {
      if (multiset.count(word) > max) {
        max = multiset.count(word);
        maxKey = word;
      }
    }
    return maxKey;
  }

  /**
   * Method is to list all the files in the input directory.
   *
   * @param  folder - Input directory
   */
  public void listFilesForFolder(final @NotNull File folder) {
    for (final File fileEntry : folder.listFiles()) {
      if (fileEntry.isDirectory()) {
        listFilesForFolder(fileEntry);
      } else {
        if (fileEntry.getName().endsWith(".nt"))
          filename.add(fileEntry.getName());
      }
    }
  }

  /**
   * Method to delete pre-existing output files.
   */
  public static void methodToRemoveOutputFiles()
  {
    String fileNames[] =
      {"JaccardSimilarityOutput", "WeightedJaccardSimilarityOutput",
        "DiceSimilarityMetricsOutput", "CosineSimilarityOutput"
      };
    for(int i=0; i<fileNames.length; i++)
    {
      Path path1 = Paths.get(hobbitFiles + fileNames[i] + ".nt");
      try {
        Files.deleteIfExists(path1);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

}




