# Ontology Matching Operator

 1. Clone this Project in local        
 2. If you get any error/warning in Eclipse (Plugin execution not covered by lifecycle configuration:) in POM.xml  <br /> Go to Windows --> Preferences --> Maven --> Errors/Warnings --> Change to "Warning" or "Ignore" for Plugin Execution.
 3. For Jars Installation, use `mvn initialize` or manually install using below commands: <br/>
      a. **LogMap Jar Installation in Maven ::** 
      ```cmd 
      mvn install:install-file -Dfile=[path-to-jar-file-in-repo] -DgroupId=uk.ox.logmap -DartifactId=logmap-matcher -Dversion=4.0 -Dpackaging=jar
      ```
      b. **FCA-Map Jar Installation in Maven ::**
      ```cmd 
      mvn install:install-file -Dfile="[path-to-jar-file-in-repo]" -DgroupId=cn.ac.amss.semanticweb -DartifactId=FCA-Map -Dversion=1.1.0 -Dpackaging=jar
      ```
4. Use `mvn clean package` in parent folder to generate the plugin under
`./target/plugin-starter-${version}-plugin.jar`.
Copy the plugin into a folder named `plugins/` in the working directory from which you
want to invoke DEER and it will automatically be loaded.   

5. To invoke it via DEER cli :
```cmd
 java -jar deer-cli/target/deer-cli-${current-version}.jar path_to_config.ttl
 ``` 

# Configuration file

The DEER Framework looks for parameter like matching_Library for selecting one of the implementation i.e LogMap or FCA-Map.
It uses typeOfMap parameter for choosing different combination of Mappings. 

![Alt text](/screenshot/conf.png?raw=true "Output")


## For Windows

### Docker command for Windows cmd
```cmd
docker run -it --rm  -v %cd%/plugins:/plugins -v %cd%/src/test/resources:/config dicegroup/deer:latest /config/configuration.ttl
```




