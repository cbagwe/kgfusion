/**
 * @author Raviteja Kanagarla
 * @author Chaitali Suhas Bagwe
 *
 */

package org.aksw.deer.plugin.example;
import org.aksw.limes.core.controller.Controller;
import org.aksw.limes.core.controller.LimesResult;
import org.aksw.limes.core.io.config.Configuration;
import org.aksw.limes.core.io.config.KBInfo;
import org.aksw.limes.core.io.serializer.ISerializer;
import org.aksw.limes.core.io.serializer.SerializerFactory;
import org.apache.jena.rdf.model.*;
import java.io.*;
import java.util.*;

/**
 * This class uses LIMES framework to match different Knowledge Graphs
 * from Linked Open Data (LOD) Cloud
 */
public class LimesApproach {

  public static List<Model> modelToNextGroup=new ArrayList<>();
  public static Model modelDefault= ModelFactory.createDefaultModel();
  public static Property pop= ResourceFactory.createProperty("http://example.com/test#matches");

  /**
   * Method to populate the Limes configuration file
   * @param models
   * @return Configuration file
   * @throws IllegalArgumentException
   */
  public Configuration createLimesConfigurationFile(List<Model> models) throws IllegalArgumentException {
    // Creating Limes configuration Object
    Configuration conf = new Configuration();
    // adding prefix
    conf.addPrefix("ns1", "https://example.com/test#");
    conf.addPrefix("owl", "http://www.w3.org/2002/07/owl#");
    conf.addPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");

    // configuration for source KG
    KBInfo src = new KBInfo();
    src.setId("sourceId");
    src.setEndpoint("jsontordfoutput.ttl");
    src.setVar("?o");
    src.setPageSize(1000);
    src.setType("TURTLE");
    src.setRestrictions(new ArrayList<String>(Arrays.asList(new String[]{"?s ns1:dataset ?o"})));
    src.setProperties(Arrays.asList(new String[]{"ns1:keywords", "ns1:domain"}));
    Map<String, String> prefixes = new HashMap<String, String>();
    prefixes.put("ns1", "https://example.com/test#");
    prefixes.put("owl", "http://www.w3.org/2002/07/owl#");
    prefixes.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
    src.setPrefixes(prefixes);
    HashMap<String, String> tempHashMap = new HashMap<String, String>();
    tempHashMap.put("ns1:keywords", "");
    LinkedHashMap<String, Map<String, String>> functions = new LinkedHashMap<String, Map<String, String>>();
    functions.put("ns1:keywords", tempHashMap);
    src.setFunctions(functions);
    conf.setSourceInfo(src);

    // configuration for target KG
    KBInfo target = new KBInfo();
    target.setId("targetId");
    target.setEndpoint("jsontordfoutput.ttl");
    target.setType("TURTLE");
    target.setVar("?v");
    target.setPageSize(1000);
    target.setRestrictions(new ArrayList<String>(Arrays.asList(new String[]{"?t ns1:dataset ?v"})));
    target.setProperties(Arrays.asList(new String[]{"ns1:keywords", "ns1:domain"}));
    target.setPrefixes(prefixes);
    target.setFunctions(functions);
    conf.setTargetInfo(target);

    //metrics to match the two KGs
    conf.setMetricExpression("AND(cosine(o.ns1:keywords,v.ns1:keywords)|0.9,exactmatch(o.ns1:domain,v.ns1:domain)|0.8)");
    conf.setAcceptanceThreshold(0.8);
    conf.setAcceptanceFile("accepted.nt");
    conf.setAcceptanceRelation("owl:sameAs");

    // Review
    conf.setVerificationThreshold(0.8);
    conf.setVerificationFile("reviewme.nt");
    conf.setVerificationRelation("owl:sameAs");

    // EXECUTION
    conf.setExecutionRewriter("default");
    conf.setExecutionPlanner("default");
    conf.setExecutionEngine("default");


    // Output format CSV etc
    conf.setOutputFormat("TTL");
    //callLimes(conf);
    System.out.println("successfully executed created configuration function");


    return conf;
  }

  /**
   * Method to call LIMES using the created configuration file
   * The output KGs are populated in accepted.nt file
   * @param config - configuration file
   * @throws IllegalArgumentException
   */
  public void callLimes(Configuration config) throws IllegalArgumentException {

    String limesOutputLocation = new File("").getAbsolutePath();
    LimesResult mappings = Controller.getMapping(config);
    String outputFormat = config.getOutputFormat();
    ISerializer output = SerializerFactory.createSerializer(outputFormat);

    output.setPrefixes(config.getPrefixes());

    String workingDir = limesOutputLocation;
    File verificationFile = new File(workingDir, config.getVerificationFile());
    File acceptanceFile = new File(workingDir, config.getAcceptanceFile());

    System.out.println(acceptanceFile.getAbsolutePath());
    System.out.println(verificationFile.getAbsolutePath());
    System.out.println(acceptanceFile.getPath());
    System.out.println(verificationFile.getPath());

    output.writeToFile(mappings.getVerificationMapping(), config.getVerificationRelation(),
      verificationFile.getPath().trim());
    output.writeToFile(mappings.getAcceptanceMapping(), config.getAcceptanceRelation(),
      acceptanceFile.getPath().trim());

  }


  /**
   * Method to insert sparql endpoints according to the accepted.nt file
   * and generate the output models
   *
   * @return the list of models in which the RDF model of the document matching is stored
   */
  public List<Model> createSparQLEndpoint() {
    FileReader file1 = null;
    String nameOfFile="limesmodeloutput.rdf";

    try {
      file1 = new FileReader("accepted.nt");
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block

    }


    try (BufferedReader br = new BufferedReader(file1)) {
      String line;
      while ((line = br.readLine()) != null) {
        // process the line.
        String[] split = line.split(" ");
        if (!split[0].equals("@prefix")) {
          int len1 = split[0].length();
          String KG1 = split[0].substring(26, len1 - 1);
          int len2 = split[2].length();
          String KG2 = split[2].substring(26, len2 - 1);
          insertSparQLEndpoint(KG1, KG2);
        }
      }
      modelToNextGroup.add(modelDefault);
      System.out.println(" -Completed- ");
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    try {
      FileWriter out=new FileWriter(nameOfFile);
      modelDefault.write(out, "TTL");
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    return modelToNextGroup;
  }

  /**
   * Method to fetch sparql endpoints of matched datasets and save them into RDF model
   * @param kG1 - name of first dataset
   * @param kG2 - name of second dataset
   */
  private void insertSparQLEndpoint(String kG1, String kG2) {
    // TODO Auto-generated method stub
    FileWriter writer;
    String kG1Sparql = getSparQLEndpoint(kG1);
    String kG2Sparql1 = getSparQLEndpoint(kG2);

    if (kG1Sparql != null && kG2Sparql1 != null && !(kG1Sparql.equals(kG2Sparql1))) {
      try {
        Resource res = modelDefault.createResource(kG1Sparql);
        modelDefault.add(res,pop,modelDefault.createResource(kG2Sparql1));
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();

      }
    }

  }


  /**
   * Method to read the sparql endpoints from the file which contains the endpoints for all datasets.
   *
   * @param kG1 - Name of dataset
   * @return sparql endpoint
   */
  private String getSparQLEndpoint(String kG1) {
    // TODO Auto-generated method stub
    FileReader file = null;
    try {
      file = new FileReader("SparQLList.txt");
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    try (BufferedReader br = new BufferedReader(file)) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] split = line.split(" : ");
        if (split[0].equals(kG1))
          return split[1];
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }
}
