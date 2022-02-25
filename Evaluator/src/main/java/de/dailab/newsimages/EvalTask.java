package de.dailab.newsimages;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;


/**
 * The Evaluator for the news Images Dataset.
 * @author andreas
 *
 */
public class EvalTask {
	
	/**
	 * The main method. Can be called by <code> mvn exec:java -Dexec.mainClass=de.dailab.newsimages.EvalTask<code>
	 * 
	 * The program expects two parameters: the groundTruth file and the predition file
	 */
    @SuppressWarnings("resource")
    public static void main(String[] args) throws IOException {

        String groundTruthFileName = "Data/content2019-04-v3.tsv";
        String predictionFileName = "randomPrediction1626036852603";
        
        // eval the params
        if (args.length >= 1) {
        	groundTruthFileName = args[0];
        }
        if (args.length >= 2) {
        	predictionFileName = args[1];
        }
        
        final Set<String> groundTruthArticleIDs = new HashSet<>();
        final Set<String> groundTruthIIDs = new HashSet<>();
        final Map<String, String> imageByArticleID  = new HashMap<>();

        // read the groundTruth
        try(Reader in = new FileReader(groundTruthFileName)) {
            Iterable<CSVRecord> records = new CSVParser(in, CSVFormat.DEFAULT.withDelimiter('\t').withHeader("article","aid","url img","iid","hashvalue","title","text","imgFile"));
            for (CSVRecord record : records) {
                String articleID = record.get(0);
                String iID = record.get(4);
                
                // ignore line not starting with a number
                try {
                    Integer.valueOf(articleID);
                } catch (Exception e) {
                    continue;
                }
                
                if (!groundTruthArticleIDs.add(articleID)) {
                    System.err.println("invalid articleID in groundTruth");
                }
                if (!groundTruthIIDs.add(iID)) {
                    System.err.println("invalid imageiID in groundTruth");
                }
                imageByArticleID.put(articleID, iID);
            }
        }
        System.out.printf("read file:%s, found %d articleIDs and %d imageIDs\n", groundTruthFileName, groundTruthArticleIDs.size(), groundTruthIIDs.size());
        
        
        // read the prediction
        final Set<String>predictedArticleIDs = new HashSet<>();
        final Set<String>predictedArticleIDsObf = new HashSet<>();
        final Map<String, Integer> predictedPositionByArticleID = new HashMap<>();
        
        int[] found = {0, 0};
        try(Reader in = new FileReader(predictionFileName)) {
            Iterable<CSVRecord> records = new CSVParser(in, CSVFormat.DEFAULT.withDelimiter('\t'));
            for (CSVRecord record : records) {
                
                // scrambledID
                String articleIdPredicedOb = record.get(0);
                predictedArticleIDsObf.add(articleIdPredicedOb);
                
                // ignore line not starting with a number
                try {
                    Integer.valueOf(articleIdPredicedOb);
                } catch (Exception e) {
                    continue;
                }
                
                // repair the ID
                String articleIdRepaired = null;
                try {
                    articleIdRepaired = articleIdPredicedOb.substring(6, 10) + articleIdPredicedOb.substring(1,6);
                } catch (Exception e) {
                    System.out.println("Invalid ArtickeID to repair: " + articleIdPredicedOb);
                }
               
                // check whether the ID is valid
                if (!groundTruthArticleIDs.contains(articleIdRepaired)) {
                    System.err.println("invalid prediced articleID");
                }
                
                // check against duplicates
                if (!predictedArticleIDs.add(articleIdRepaired)) {
                    System.err.println("doublicated ArticleID in prediction file");
                }
                
                for (int i = 1; i < 101; i++) {
                    String currentIID = record.get(i).trim();
                    
                    if (imageByArticleID.get(articleIdRepaired).equals(currentIID)) {
                        System.out.println("found for " + articleIdRepaired + " image at pos: " + i);
                        found[0]++;
                        predictedPositionByArticleID.put(articleIdRepaired, i);
                        break;
                    }
                }
                found[1]++;
            }
            
        }
        System.out.printf("predictionFile\t%s\n", predictionFileName);
        System.out.printf("correctIn100\t%d\t%d\n", found[0], found[1]);
        
        // aggregate the findings
        System.out.printf("MRR\t%d\t%.5f \n", 100, computeMRR(predictedPositionByArticleID, groundTruthIIDs.size()));
        
        // report errors
        int[] evalPoints = {5, 10, 50, 100};
        double[] meanRecallAt = computeMeanRecall(predictedPositionByArticleID, groundTruthIIDs.size(), evalPoints);
        for (int i = 0; i < meanRecallAt.length; i++) {
            System.out.printf("MeanRecallAt\t%d\t%.5f\n", evalPoints[i], meanRecallAt[i]);
            
        }
        
        //createARandomPrediction(predictedArticleIDsObf, groundTruthIIDs, "randomPrediction" + System.currentTimeMillis());
    }
    
    /**
     * Compute MRR
     * @param predictedPositionByArticleID
     * @param numberOfArticles
     * @return
     */
    public static double computeMRR(final Map<String, Integer> predictedPositionByArticleID, int numberOfArticles) {
        
        double sumReziprocalRank = 0.0d;
        for (Integer foundPosition : predictedPositionByArticleID.values()) {
            sumReziprocalRank += 1d / (double) foundPosition;
        }
        return sumReziprocalRank / (double) numberOfArticles;
    }
    
    
    /**
     * Compute MRR
     * @param predictedPositionByArticleID
     * @param numberOfArticles
     * @return
     */
    public static double[] computeMeanRecall(final Map<String, Integer> predictedPositionByArticleID, int numberOfArticles, int[] evalPoints) {
        
        double[] sumRecallAtEval = new double[evalPoints.length];
        for (Integer foundPosition : predictedPositionByArticleID.values()) {
            for (int i = 0; i < evalPoints.length; i++) {
                int currentEvalPoint = evalPoints[i];
                if (foundPosition <= currentEvalPoint) {
                    sumRecallAtEval[i] += 1.0;
                }
            }
        }
        for (int i = 0; i < sumRecallAtEval.length; i++) {
            sumRecallAtEval[i] /= (double) numberOfArticles;
        }
        return sumRecallAtEval;
    }
    
    /**
     * Create a file containing randomized predictions
     */
    public static void createARandomPrediction(final Set<String> groundTruthArticleIDs, final Set<String> groundTruthIIDs, final String fileName) {
        
        final String DELIM = "\t";
        try (BufferedWriter br = new BufferedWriter(new FileWriter(fileName))) {
            br.write("header");
            br.newLine();
            for (String articleID : groundTruthArticleIDs) {
                br.write(articleID);
                List<String> tmpIIDs = new ArrayList<>();
                tmpIIDs.addAll(groundTruthIIDs);
                Collections.shuffle(tmpIIDs);
                for (String currentIID : tmpIIDs) {
                    br.write(DELIM);
                    br.write(currentIID);
                }
                br.newLine();
            }
        } catch (Exception e) {
           e.printStackTrace();
        }
    }
}
