
# LIMES - Link Discovery Framework for Metric Spaces.

[![Build Status](https://github.com/dice-group/LIMES/actions/workflows/run-tests.yml/badge.svg?branch=master&event=push)](https://github.com/dice-group/LIMES/actions/workflows/run-tests.yml)
[![DockerHub](https://badgen.net/badge/dockerhub/dicegroup%2Flimes/blue?icon=docker)](https://hub.docker.com/r/dicegroup/limes)
[![GNU Affero General Public License v3.0](https://badgen.net/badge/license/GNU_Affero_General_Public_License_v3.0/orange)](./LICENSE)
![Java 1.8+](https://badgen.net/badge/java/1.8+/gray?icon=maven)

# Type Driven Wombat Simple

1- The implementation of Type-Driven Wombat Simple can be found at 

***limes-core/src/main/java/org/aksw/limes/core/ml/algorithm/TypeDrivenWombatSimple.java.***

2- The class that identifies the data type of property can be found at  

***limes-core/src/main/java/org/aksw/limes/core/ml/algorithm/wombat/CheckType.java.***

# Guide: How to run example of Type Driven Wombat Simple

#### Step 1:  Clone the repository and change branch to typeDrivenWombatSimple
    git clone https://github.com/binhudakhalid/LIMES.git --branch typeDrivenWombatSimple
    
#### Step 2: Build lIMES
Go to limes-core directory and run

    cd LIMES/limes-core
    mvn clean package shade:shade -Dcheckstyle.skip=true -Dmaven.test.skip=true
    
#### Step 3: Start the lIMES as a server

    java -jar target/limes-core-1.7.6-SNAPSHOT.jar -s
    
#### Step 4: 
Go to datasets folder, In this folder, you can find limes configuration file "Type-Driven-Amazon-GoogleProducts.xml" and datasets. 

    cd LIMES/limes-core/src/main/resources/datasets
    
#### Step 5: Sumbit the config file through curl
  
    curl -F config_file=@Type-Driven-Amazon-GoogleProducts.xml http://localhost:8080/submit

After running these step you will get the result.

![Alt text](/screenshot/result.png?raw=true "Title")

## Configuration File

The sample configuration file can found at 

***limes-core/src/main/resources/datasets/Type-Driven-Amazon-GoogleProducts.xml***

![Alt text](/screenshot/configurationFile.png?raw=true "Title")


## Benchmarking
Configuration files dataset for benchmarking can be found in 

    limes-core/src/main/resources/datasetsOne
    
![Alt text](/screenshot/benchmark1.png?raw=true "Title")

## Running LIMES

To bundle LIMES as a single jar file, do

```bash
mvn clean package shade:shade -Dmaven.test.skip=true
```

Then execute it using

```bash
java -jar limes-core/target/limes-core-${current-version}.jar
```

## Using Docker

For running LIMES server in Docker, we expose port 8080. The image accepts the same arguments as the
limes-core.jar, i.e. to run a configuration at `./my-configuration`:

```bash
docker run -it --rm \
  -v $(pwd):/data \
  dicegroup/limes:latest \
    /data/my-configuration.xml
```

To run LIMES server:

```bash
docker run -it --rm \
  -p 8080:8080
  dicegroup/limes:latest \
    -s
```

## Maven

```xml

<dependencies>
    <dependency>
        <groupId>org.aksw.limes</groupId>
        <artifactId>limes-core</artifactId>
        <version>1.7.5</version>
    </dependency>
</dependencies>
```

```xml

<repositories>
    <repository>
        <id>maven.aksw.internal</id>
        <name>University Leipzig, AKSW Maven2 Internal Repository</name>
        <url>http://maven.aksw.org/repository/internal/</url>
    </repository>

    <repository>
        <id>maven.aksw.snapshots</id>
        <name>University Leipzig, AKSW Maven2 Snapshot Repository</name>
        <url>http://maven.aksw.org/repository/snapshots/</url>
    </repository>
</repositories>
```

## How to cite

```bibtex
@article{KI_LIMES_2021,
  title={{LIMES - A Framework for Link Discovery on the Semantic Web}},
  author={Axel-Cyrille {Ngonga Ngomo} and Mohamed Ahmed Sherif and Kleanthi Georgala and Mofeed Hassan and Kevin Dreßler and Klaus Lyko and Daniel Obraczka and Tommaso Soru},
  journal={KI-K{\"u}nstliche Intelligenz, German Journal of Artificial Intelligence - Organ des Fachbereichs "Künstliche Intelligenz" der Gesellschaft für Informatik e.V.},
  year={2021},
  url = {https://papers.dice-research.org/2021/KI_LIMES/public.pdf},
  publisher={Springer}
}
```

## More details

* [Demo](https://dice-research.org/LIMES)
* [User manual](http://dice-group.github.io/LIMES/#/user_manual/index)
* [Developer manual](http://dice-group.github.io/LIMES/#/developer_manual/index)
