## Dataset FAQs
#### Q1: How has the relationship between news items and images been defined?
The news items have been crawled from news portals. The relation between text and image has been defined by the author of the news article. When creating the dataset, we have ensured that a 1:1 relationship between news items and images exist.

#### Q2: Why are there two files for the April batch?
The April batch is designed for testing. Thus, news texts and the image data are stored in separate files. The connection between the images and the texts has been obfuscated.

#### Q3: Can I use the URL for computing predictions.
In general, the articleURL can be used; the imageURL and the **imageName** must not be used. Since the URL is a kind of artificial feature, it would be interesting to study the impact of the URL, if the URL is used in a predictor.

#### Q4: How is the MRR (Mean Reciprocal Rank) computed?
The MRR computation considers only the first 100 predictions in order to limit the effort for computing prediction lists. MRR@N considers only the Top N elements of the list. This simplifies the evaluation, since participants have to provide only the top N elements of the candidate list.
In practice, this metric considers that editors do not want to scroll endless through a candidate list. The metric limits the list to N elements.
