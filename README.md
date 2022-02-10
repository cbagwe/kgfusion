# Updated branch: inputFile

# DEER Parameters

The deer:IntanceMatchingOperator has two paremeters (deer:coverage, deer:maxLimit ).

**deer:coverage** can be use to set the level of Coverage of the Properties. Calulated by "propteryCount/TotalInstanceCount" 

example: deer:coverage "8.66"; 

**deer:maxLimit** will set the maximum number of Properties. 

example: deer:maxLimit "10";

# Input requirement for Ontology Operator

<source, enpointType, file>                 // <source, enpointType, url>

<source, endpoint, www.abc.com/fileOne>     

<source, restriction, www.abc.com/Person>   


<target, enpointType, file>

<target, endpoint, www.abc.com/fileTwo>

<target, restriction, www.abc.com/Actor>

# Ouptut from ontology operator
Only one Jena model will be return.

### Sample Ouput


``` 
1 [https://w3id.org/deer/datasetSource, https://w3id.org/deer/path, data/data_nobelprize_org.nt]
2 [https://w3id.org/deer/datasetTarget, https://w3id.org/deer/path, data/lov_linkeddata_es_dataset_lov.nt] 

3 [http://data.nobelprize.org/resource/laureate/958, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://xmlns.com/foaf/0.1/Person]
4 [http://data.nobelprize.org/resource/laureate/448, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://xmlns.com/foaf/0.1/Person] 
5 [http://sparql.cwrc.ca/ontologies/cwrc#26ad3610-a0bb-4e62-8fbc-d6be9ccbbdf6-partof-327d5213ef, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://xmlns.com/foaf/0.1/Person] 
6 [http://sparql.cwrc.ca/ontologies/cwrc#a5686049-f450-415c-9ca6-deee26e30899-b3f841f5c0, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://xmlns.com/foaf/0.1/Person] 
 

7 [http://data.nobelprize.org/resource/laureate/448, http://www.w3.org/2002/07/owl#sameAs, http://sparql.cwrc.ca/ontologies/cwrc#26ad3610-a0bb-4e62-8fbc-d6be9ccbbdf6-partof-327d5213ef] 
8 [http://data.nobelprize.org/resource/laureate/958, http://www.w3.org/2002/07/owl#sameAs, http://sparql.cwrc.ca/ontologies/cwrc#a5686049-f450-415c-9ca6-deee26e30899-b3f841f5c0]
```

### Explaination

The line 1 and 2 is telling about the path of source and target datasets. Here <strong> https://w3id.org/deer/path</strong> can be a <strong> *file path*</strong> or a <strong> *Url*</strong> or  a <strong> *knowledge graph endpoint*</strong>.  

Line 3, 4, 5 and 6 is telling the *rdf type* of entities.

Line 7 and 8 is telling which entities are similar/sameAs.

For the above example you can find the source and target dataset in project folder called "data".

# Instance Matching Operator

Using `mvn clean package` in this folder will generate the plugin under
`./target/plugin-starter-${version}-plugin.jar`.
Copy the plugin into a folder named `plugins/` in the working directory from which you
want to invoke DEER and it will automatically be loaded.

In order to invoke DEER, either download it from GitHub or use our Docker image:

```bash
docker run -it --rm \
   -v $(pwd)/plugins:/plugins -v $(pwd)/src/test/resources:/config dicegroup/deer:latest \
   /config/configuration.ttl
```

## For Windows

1. you need to create a folder `plugins` inside deer-plugin-starter directory.

2. copy the newly generated plugin under `./target/plugin-starter-${version}-plugin.jar` to `plugins` folder.

3. run the docker command from the deer-plugin-starter directory.

### Docker command for Windows cmd
```cmd
docker run -it --rm  -v %cd%/plugins:/plugins -v %cd%/src/test/resources:/config dicegroup/deer:latest /config/configuration.ttl
```
