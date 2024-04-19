import java.util.*;
import java.io.*;
import java.util.regex.*;


public class Project3 {

    public static void main(String[] args) {

        Matcher wordMatcher;
        Pattern wordPattern = Pattern.compile("[a-zA-Z]+");

        // filepaths to all required files
        String docsFile = "cran.all.1400";
        String queriesFile = "cran.qry";
        String relevantDocsFile = "cranqrel";

        // generates the documents, puts them all in the folder P3docs
        genDocs(docsFile);

        // generates the document frequency of every term
        HashMap<String, Integer> docFreq = calcDocFreq();

        // calculates the tfidf of every term in every document
        HashMap<Integer, HashMap<String, Double>> tfidf = calcTfidf(docFreq);


        char action;
        String rawQuery;
        String query = "";
        Scanner in = new Scanner(System.in);

        // array to hold the results if the single query option is selected
        ArrayList<Integer> results = null;


        // do while loop for the user interface
        do {

            // gets a selection from the user
            System.out.print("\nChoose an option: [S]ingle query / [F]ile queries / [E]xit: ");
            action = in.next().toUpperCase().charAt(0);
            in.nextLine();

            switch (action) {

                // single query case
                case 'S':

                    rawQuery = "";
                    query = "";

                    // gets the query to search for
                    System.out.print("Enter a query to search for: ");
                    rawQuery = in.nextLine();
                    rawQuery = rawQuery.toLowerCase();

                    // use wordMatcher to format the query
                    wordMatcher = wordPattern.matcher(rawQuery);

                    // pulls out each word and adds it to the processed query
                    while (wordMatcher.find())
                        query += rawQuery.substring(wordMatcher.start(), wordMatcher.end()) + " ";

                    // if the query is empty after processing tell the user that it must contain a term
                    if (query.equals(""))
                        System.out.println("Query must contain at least one term.");
                    
                    // if the query contains at least one term processes the query
                    else {
                        // processes the query
                        results = processQuery(tfidf, query);

                        // checks if any document ids were returned
                        if (results.size() == 0) {
                            System.out.println("No documents found.");

                        }

                        else {
                            // displays the results as long as they aren't null
                            System.out.println("The top 10 documents for query '" + query + "' is:");

                            for(int i = 0; i < results.size(); i++) {
                                System.out.println("Document " + (i + 1) + ": DocId: " + results.get(i));
                            }
                        }
                    }
                    break;

                // query file case
                case 'F':
                    
                    // generates the query results for each query in the file
                    HashMap<Integer, ArrayList<Integer>> queryResults = processQueriesFromFile(tfidf, queriesFile);

                    // calculates the map from the query results
                    double map = calcMap(queryResults, relevantDocsFile);
                    System.out.println("The mean average precision based on the provided cran.qry and cranqrel files is: " + map);
                    break;
            }


        } while (action != 'E');

        // closes the keyboard input
        in.close();
    }


    /**
     * generates a text document for each document in the master file
     * @param filename filename of the master file
     */
    private static void genDocs(String filename) {

        System.out.print("Generating documents... ");

        String line;
        BufferedReader br = null;
        PrintWriter currentDoc = null;

        // flag used to only write the abrtract to the output
        boolean inAbstract = false;

        // if the folder P3docs doesn't exist create it
        File p3docs = new File("./P3docs");
        if (!p3docs.exists())
            p3docs.mkdir();

        try {
            // open the master file
            br = new BufferedReader(new FileReader(filename));

            // while loop to read each line of the master file
            while ((line = br.readLine()) != null) {


                // if we are at the start of the next document
                if (line.split(" ")[0].equals(".I")) {

                    // get the docId from the .I line
                    String docId = line.split(" ")[1];

                    // close the last document
                    if (currentDoc != null) 
                        currentDoc.close();

                    // open the new document
                    currentDoc = new PrintWriter(new File("./P3docs/" + docId + ".txt"));

                    // set inAbstract to false to prevent unwanted lines from being in the document file
                    inAbstract = false;
                }
                
                // if we are in the abstract write to the file
                if (inAbstract)
                    currentDoc.println(line);

                // if we are at the line above the abstract set the flag to true
                if (line.equals(".W"))
                    inAbstract = true;

            }

            // closes the last file and the master file
            currentDoc.close();
            br.close();
            
        }

        // catch file not found error
        catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

        System.out.println("Done!");

    }

    /**
     * calculates the document frequency of every term
     * @return a hashmap with all the document frequencies
     */
    private static HashMap<String, Integer> calcDocFreq() {

        System.out.print("Calculating document frequencies... ");

        String line, word;
        BufferedReader br = null;

        Matcher wordMatcher;
        Pattern wordPattern = Pattern.compile("[a-zA-Z]+");

        HashMap<String, Integer> docFreq = new HashMap<String, Integer>();

        // counts the number of files in the project directory
        File directory = new File("./P3Docs/");
        int docCount = directory.list().length;

        // for loop for each document in the collection
        for (int i = 1; i <= docCount; i++) {

            // string list to store all the words already counted in the current document as to not have any duplicates 
            ArrayList<String> used = new ArrayList<String>();

            // build the filename of the current document
            String filename = "./P3docs/" + i + ".txt";

            try {
                // open the current document
                br = new BufferedReader(new FileReader(filename));
    
                // while loop to read each line of the current document
                while ((line = br.readLine()) != null) {
    
                    wordMatcher = wordPattern.matcher(line);

                    // loop for each word in the current line
                    while (wordMatcher.find()) {
                        
                        // use regex to get the next word
                        word = line.substring(wordMatcher.start(), wordMatcher.end());

                        // if we haven't counted the current word for the current document
                        if (!used.contains(word)) {
    
                        // if the word has never been seen before initialize it to 1
                        if (docFreq.get(word) == null)
                            docFreq.put(word, 1);
        
                        // if the word has been seen before add 1 to the count
                        else
                            docFreq.put(word, docFreq.get(word) + 1);

                        // mark that the word has been seen in this document
                        used.add(word);
                        }
                    }
                }
    
                // close the current document
                br.close();
            }
    
            // catch file not found error
            catch (IOException e) {
                System.out.println(e.getMessage());
                System.exit(1);
            }

        }

        System.out.println("Done!");

        // return the hashmap with all the document frequencies
        return docFreq;
    }


    /**
     * calculates the term frequencies for a certain document
     * @param docId the document to calculate the term frequencies for
     * @return a hashmap with the term frequencies for the docId provided
     */
    private static HashMap<String, Integer> calcTermFreq(int docId) {

        String line, word;
        BufferedReader br = null;

        Matcher wordMatcher;
        Pattern wordPattern = Pattern.compile("[a-zA-Z]+");

        // construct the filename for the document
        String filename = "./P3docs/" + docId + ".txt";

        HashMap<String, Integer> termFreq = new HashMap<String, Integer>();

        try {

            // open the file of the provided document
            br = new BufferedReader(new FileReader(filename));

            // while loop to read each line of the provided document
            while ((line = br.readLine()) != null) {

                wordMatcher = wordPattern.matcher(line);

                // loop for each word in the current line
                while (wordMatcher.find()) {
                    
                    // use the regex to get the next word
                    word = line.substring(wordMatcher.start(), wordMatcher.end());

                    // the the word has not been seen before initialize it to 1
                    if (termFreq.get(word) == null)
                        termFreq.put(word, 1);
    
                    // if the word has been seen before add 1 to the current count
                    else
                        termFreq.put(word, termFreq.get(word) + 1);
                }
            }

            // close the file
            br.close();
        }

        // catch file not found error
        catch (IOException ex) {
            System.err.println("File not found. Program terminated.\n");
            System.exit(1);
        }

        // return the hashmap of term frequencies
        return termFreq;
    }


    /**
     * calculates the tfidf of each term in every document and stores it in a hashmap of hashmaps
     * @param docFreq hashmap of document frequencies
     * @param docCount the number of documents in the collection
     * @return a hashmap of hashmaps containing all the tfidf values
     */
    private static HashMap<Integer, HashMap<String, Double>> calcTfidf(HashMap<String, Integer> docFreq) {

        System.out.print("Generating tfidf values... ");

        // create the master hashmap
        HashMap<Integer, HashMap<String, Double>> tfidfMaster = new HashMap<Integer, HashMap<String, Double>>();

        // counts the number of files in the project directory
        File directory = new File("./P3Docs/");
        int docCount = directory.list().length;

        // for loop for each document in the collection
        for (int i = 1; i <= docCount; i++) {

            // uses the termFreq method to get the term frequencies for the current document
            HashMap<String, Integer> termFreq = calcTermFreq(i);

            // creates a hashmap for the current documents tfidf values
            HashMap<String, Double> tfidfCurrentDoc = new HashMap<String, Double>();

            // for each loop for each term in the current document
            for (Map.Entry<String, Integer> term : termFreq.entrySet()) {

                // calculates the idf, then the tfidf for the current term
                double idf = Math.log10(docCount / (double)docFreq.get(term.getKey()));
                double tfidf = term.getValue() * idf;

                // adds the tfidf to the current documents hashmap
                tfidfCurrentDoc.put(term.getKey(), tfidf);
            }

            // adds the current tfidf hashmap to the master hashmap
            tfidfMaster.put(i, tfidfCurrentDoc);

        }

        System.out.println("Done!");

        // return the master list of tfidf values
        return tfidfMaster;
    }


    /**
     * processes a single query
     * @param tfidf the hashmap of all tfidf values
     * @param query the query to search for
     * @return an array of docIds
     */
    private static ArrayList<Integer> processQuery(HashMap<Integer, HashMap<String, Double>> tfidf, String query) {
        
        // two array lists to store the best tfidf values and the document they correspond to
        ArrayList<Double> resultsKey = new ArrayList<Double>();
        ArrayList<Integer> resultsValue = new ArrayList<Integer>();

        // number of results to display
        int numOfResults = 10;
        int minKey, maxKey;

        // counts the number of files in the project directory
        File directory = new File("./P3Docs/");
        int docCount = directory.list().length;

        // splits the query into an array of terms
        String[] queryTerms = query.split(" ");

        // for loop to calculate the weight for each document
        for (int i = 1; i <= docCount; i++) {

            double result = 0.0;

            // for each loop for each term in the query 
            for (String qTerm : queryTerms) {

                // if the term exists in the document add its tfidf to the result
                if (tfidf.get(i).get(qTerm) != null)
                    result += tfidf.get(i).get(qTerm);
            }


            // if there are not enough results yet add it to the list as long as the result is not zero
            if (resultsKey.size() < numOfResults && result != 0) {
                resultsKey.add(result);
                resultsValue.add(i);
            }

            // else if we already have 10 results
            else if (resultsKey.size() == numOfResults) {

                // calculate the weakest result in the list
                minKey = 0;

                for (int j = 0; j < resultsKey.size(); j++) {
                    if (resultsKey.get(j) < resultsKey.get(minKey))
                        minKey = j;
                }
            
                // if the weakest result in the list is worse than the new result replace the weakest with the current
                if (minKey < result) {
                    resultsKey.set(minKey, result);
                    resultsValue.set(minKey, i);
                }
            }
        }

        // an array list to hold the sorted docIds
        ArrayList<Integer> docIds = new ArrayList<Integer>();

        // gets the current size of the result list
        numOfResults = resultsKey.size(); 

        // our results are currently unsorted, this loop sorts them by tfidf
        for (int i = 0; i < numOfResults; i++) {

            maxKey = 0;

            // finds the strongest result
            for (int j = 0; j < resultsKey.size(); j++) {
                if (resultsKey.get(j) > resultsKey.get(maxKey))
                    maxKey = j;
            }

            // adds the best result to the new list and removes it from the lists
            docIds.add(resultsValue.get(maxKey));
            resultsKey.remove(maxKey);
            resultsValue.remove(maxKey);
        }

        return docIds;
    }


    /**
     * reads the cran.qry file and processes the queries inside of it
     * @param tfidf the hashmap of all tfidf values
     * @param filename the filename of cran.qry
     * @return a hashmap of array lists containing the results from each query
     */
    private static HashMap<Integer, ArrayList<Integer>> processQueriesFromFile(HashMap<Integer, HashMap<String, Double>> tfidf, String filename) {

        // hashmap to store the results from each query based on query id
        HashMap<Integer, ArrayList<Integer>> queryResults = new HashMap<Integer, ArrayList<Integer>>();

        String line;
        BufferedReader br = null;

        Matcher wordMatcher;
        Pattern wordPattern = Pattern.compile("[a-zA-Z]+");

        String query = "";
        int queryId = 0;

        // flag used to only get the query for processing
        boolean inQuery = false;

        try {
            // open the query file
            br = new BufferedReader(new FileReader(filename));

            // while loop to read each line of the query file
            while ((line = br.readLine()) != null) {


                // if we are at the start of the next query
                if (line.split(" ")[0].equals(".I")) {

                    // check for the first time the loop is ran as to not call process query on a empty string
                    if (!query.equals(""))
                        queryResults.put(queryId, processQuery(tfidf, query));

                    // increment query id and reset the query as if we hit this point we are at the end of this query
                    queryId++;
                    query = "";

                    // set inQuery to false to prevent unwanted lines from being added to the query
                    inQuery = false;
                }
                
                // if we are in the query text
                if (inQuery) {

                    wordMatcher = wordPattern.matcher(line);

                    // add each word to the query without any unwanted characters
                    while (wordMatcher.find())
                        query += line.substring(wordMatcher.start(), wordMatcher.end()) + " ";
                }

                // if we are at the line above the query set the flag to true
                if (line.equals(".W"))
                    inQuery = true;

            }

            // process the last query as it is not handled by the loop
            queryResults.put(queryId, processQuery(tfidf, query));

            // closes the query file
            br.close();
            
        }

        // catch file not found error
        catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

        // return the hashmap of results
        return queryResults;
    }


    /**
     * Calculates the mean average precision for each query, calculates precision for each document for each query and averages it
     * @param queryResults a hashmap of arrays containing the doc ids from the processQuery method
     * @param filename the filename of the query relevance file
     * @return the mean average precision
     */
    private static double calcMap(HashMap<Integer, ArrayList<Integer>> queryResults, String filename) {

        String line;
        BufferedReader br = null;

        double averagePrecisionSum = 0;

        try {
            // open the query relevance file
            br = new BufferedReader(new FileReader(filename));

            // for each loop for each query
            for (Map.Entry<Integer, ArrayList<Integer>> query : queryResults.entrySet()) {

                // array list to store the relevant documents for the current query
                ArrayList<Integer> relevantDocs = new ArrayList<Integer>();
                int precisionCount = 0;
                double precisionSum = 0;

                // while loop runs while there are still lines in the query relevance file and the current line is not the last line of the current query
                while ((line = br.readLine()) != null && !line.split(" ")[2].equals("-1"))
                    relevantDocs.add(Integer.parseInt(line.split(" ")[1]));

                // for loop for processing each document in the current query
                for (int i = 0; i < query.getValue().size(); i++) {

                    // gets the ith document from the query
                    int currentDoc = query.getValue().get(i);

                    // checks if the ith document is relevant
                    if (relevantDocs.contains(currentDoc))
                        precisionCount++;

                    // calculates the current precision and adds it to the sum
                    precisionSum += (double)precisionCount / (i+1);

                }

                // calculates the average precision and adds it to the sum
                averagePrecisionSum += precisionSum / query.getValue().size();
            }

            // closes the query relevance file
            br.close();
        }

         // catch file not found error
         catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

        // returns the mean average precision
        return averagePrecisionSum / queryResults.size();
    }
}
