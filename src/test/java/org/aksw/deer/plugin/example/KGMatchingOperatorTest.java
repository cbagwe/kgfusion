package org.aksw.deer.plugin.example;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Scanner;

import org.aksw.deer.vocabulary.DEER;
import org.aksw.faraday_cage.engine.ValidatableParameterMap;
import org.aksw.limes.core.io.config.Configuration;
import org.apache.jena.rdf.model.*;
import org.junit.Test;

public class KGMatchingOperatorTest {

	@Test
	public void test() {
    Scanner scanner=new Scanner(System.in);

/*		 Model expected = ModelFactory.createDefaultModel();
		    expected.add(DEER.resource("examplePlugin"),
		      DEER.property("says"),
		      ResourceFactory.createPlainLiteral("Hello Students!"));
		    KGMatchingOperator eeo = new KGMatchingOperator();
		    eeo.initPluginId(ResourceFactory.createResource("urn:example-KGMatching-operator"));
		    eeo.initDegrees(1, 1);
		    ValidatableParameterMap params = eeo.createParameterMap()
		    	      .init();
		    	    eeo.initParameters(params);
		    	    Configuration result = eeo.createLimeConfigurationFile();
		    	    assertTrue("The empty model enriched by the example enrichment operator should contain exactly" +
		    	        " one triple: 'deer:examplePlugin deer:says \"Hello World!\"'.",
		    	      result.isIsomorphicWith(expected)); */
	System.out.println("entering the KGMatchingOperatorTest");
   // int choice = scanner.nextInt();

	System.out.println("exiting the KGMatchingOperatorTest"); 
	/*	if(kg.createLimeConfigurationFile()!=null )
		{
			System.out.println("Hurrah! Welcome to KGMatching Group Guys");
		}
		
		//kg.callLimes(con); */
	    
	}
}


