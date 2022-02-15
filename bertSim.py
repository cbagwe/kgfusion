'''
@author : Chaitali Suhas Bagwe
@author : Raviteja Kanargala
'''
import glob
import os
import pandas as pd
import numpy as np
from sentence_transformers import SentenceTransformer
from sklearn.metrics.pairwise import cosine_similarity
from sklearn.metrics.pairwise import euclidean_distances
from rdflib import URIRef, Literal
import sys

documents = []
documents_name = []

#Reading the processed datasets and storing it in array
for file in glob.glob("HobbitFiles\\*.txt"):
	with open(file, 'r') as f:
	    data = f.read()
	    documents.append(data)
	    documents_name.append(os.path.split(file)[-1])

#Converting the array of processed datasets into pandas dataframe
documents_df=pd.DataFrame({'documents':documents},index=documents_name)
print(documents_df)

#Embedding a pre-trained BERT model from HuggingFace using SentenceTransformer
sbert_model = SentenceTransformer('all-mpnet-base-v2')

#Encoding the processed datasets in pandas dataframe
document_embeddings = sbert_model.encode(documents_df['documents'])

#Find cosine similarity of the embedded documents
pairwise_similarities=cosine_similarity(document_embeddings)

f= open("BertSimilarityOutput.nt","w",encoding="utf-8")

'''

'''
for doc_id in range(0,len(documents)):
  similar_ix=np.argsort(pairwise_similarities[doc_id])[::-1]
  for ix in similar_ix:
      if ix==doc_id:
          continue
      doc1 = documents_df.index[doc_id].replace(".txt",".nt")
      doc2 = documents_df.index[ix].replace(".txt",".nt")
      f.write(doc1 + " ")
      f.write(f'{pairwise_similarities[doc_id][ix]} ')
      f.write(doc2 + " .")
      f.write("\n")



