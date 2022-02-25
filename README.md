# NewsImages
The NewsImages dataset provides news articles and links to images. It is designed for studying the relation between news items and the embedded images. The dataset has been designed for news text - news images rematching. Based on training samples, the patterns describing the relation between news snippets and texts should be learned. For the evaluation of the learned strategies, a test set is provided, consisiting of a set of images and a set of news snippets. The task consists in assigning the best image for every news item.

## Resource Description
The resource consists of da dataset and an evaluation component enabling researches to measure the quality of predictions. The evaluator computes the metrices "Mean Recall @ n" (n in {5, 10, 50, 100}) and Mean Reciprocal Rank (MRR). 


## Dataset structure
The dataset is organized in batches containing the news data collected Jan-April 2019. The batches Jan-Mar should be used for training, the batch April for testing.
For all dataset items a 1:1 news text-image relation exists. That means that every news item is linked to exactly one image; each image is assigned exactly to one news item.
The following features are provided:
  * article	aid: The article ID
  * url: The original URL of the newsItem
  * img: The image ID
  * iid: The image ID
  * hashvalue: A hashvalue for the image (for internal use, orinally used for duplicate checks)
  * title: The news item headline
  * text: The news snippet (max 256 chars)
  * imgFile: A hashed imageFileName (internal use only)

Each news items consists of a news headline, a snippet, and a link to the embedded image. The news topics cover a wide spectrum from local news to sports and politics. The news snippets and headlines are in German.

Due to legal restrictions, images must be downloaded from the original web portals. A script for downloading is provided.

## Evaluator
The evaluator is a simple JAVA program. The program can be compiled using MAVEN. Run the evalutor using `mvn exec:java -Dexec.mainClass=de.dailab.newsimages.EvalTask`. The evaluator expects two parameters: The groundTruthFile and the predictionFile.
