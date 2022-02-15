# Knowledge Graph Matching Operator

## Technical Requirements
1. Java (version 12+)
2. Python (version 3.8+)

## How to run KG Matching Operator
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

## Hobbit Files

Currently there are only eight files on git, more files can be downloaded from Hobbit platform (https://hobbitdata.informatik.uni-leipzig.de/teaching/theses/skuhlmann/) and must be copied in ./HobbitFiles folder in working directory.
