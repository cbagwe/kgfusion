package org.aksw.deer.plugin.example;
import org.apache.jena.rdf.model.*;

import java.util.ArrayList;
import java.util.List;

public class SourceTargetMatch {
  Statement source;
  Statement target;
  String namespace; //set if source is empty
  String endpointSource, endpointTarget;
  Literal result;
  FusionStrategy fusionStrategy;

  SourceTargetMatch(Statement source, Statement target, String namespace, String endpointSource, String endpointTarget,
                    FusionStrategy fusionStrategy){
    this.source = source;
    this.target = target;
    this.namespace = namespace;
    this.endpointSource = endpointSource;
    this.endpointTarget = endpointTarget;
    this.fusionStrategy = fusionStrategy;
  }
  SourceTargetMatch(Statement source,String endpoint){
    this(source,null,null,endpoint,null,null);
  }
  SourceTargetMatch(Statement target, String endpoint, String namespace){
    this(null,target,namespace,null,endpoint,null);
  }

  public Statement getSource() {
    return source;
  }

  public void setSource(Statement source) {
    this.source = source;
  }

  public Statement getTarget() {
    return target;
  }

  public void setTarget(Statement target) {
    this.target = target;
  }

  public String getNamespace() {
    return namespace;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public String getEndpointSource() {
    return endpointSource;
  }

  public void setEndpointSource(String endpointSource) {
    this.endpointSource = endpointSource;
  }

  public String getEndpointTarget() {
    return endpointTarget;
  }

  public void setEndpointTarget(String endpointTarget) {
    this.endpointTarget = endpointTarget;
  }

  public Literal getResult() {
    return result;
  }

  public void setResult(Literal result) {
    this.result = result;
  }
  public List<Literal> getAlternatives() throws LiteralRequiredException {
    List<Literal> alternatives = new ArrayList<>();

    alternatives.add(source.getLiteral());
    alternatives.add(target.getLiteral());
    return alternatives;
  }
  public boolean isMatched(){
    return source != null && target != null;
  }

  public FusionStrategy getFusionStrategy() {
    return fusionStrategy;
  }

  public void setFusionStrategy(FusionStrategy fusionStrategy) {
    this.fusionStrategy = fusionStrategy;
  }

  public String toString(){
    return "source=" + source +
      ", target=" + target +
      ", result=" + result;
  }
  public String toStringComplete() {
    return "SourceTargetMatch{" +
      "source=" + source +
      ", target=" + target +
      ", namespace='" + namespace + '\'' +
      ", endpointSource='" + endpointSource + '\'' +
      ", endpointTarget='" + endpointTarget + '\'' +
      ", result=" + result +
      '}';
  }


}
