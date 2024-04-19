import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;

public class Project4 {
	
	// no more than this many input files needs to be processed
    final static int MAX_NUMBER_OF_INPUT_FILES = 100;
    
    // an array to hold corpus file names
    static String[] inputFileNames = new String[MAX_NUMBER_OF_INPUT_FILES];
    
    static int fileCount = 0;
    	
    
	public static void main(String[] args) {

		// location of the input files, change this if the files are somewhere else
		String inputFiles = "./docs";

		String query;
		char again = 'y';
		Scanner in = new Scanner(System.in);
		
		try {
			
			// initialises the standard analyser, index path, and directory to store the index in
			Analyzer analyzer = new StandardAnalyzer();
			Path indexPath = Files.createTempDirectory("tempIndex");
			Directory directory = FSDirectory.open(indexPath);
			
			// builds the index
			buildIndex(analyzer, directory, inputFiles);
			
			// do while to process queries until the user is done
			do {
				// gets a query from the user
				System.out.print("\nEnter a query: ");
				query = in.nextLine();
				
				// processes the query
				ScoreDoc[] results = processQuery(analyzer, directory, query);
				
				// checks that there are at least one result
				if (results.length == 0)
					System.out.println("No documents found.");
				
				// displays the results if there are any
				else {
					System.out.println("Top 5 documents found:");
					for (int i = 0; i < results.length; i++) {
						System.out.println((i + 1) + ". Doc id: " + results[i].doc + " Score: " + results[i].score);
					}
				}
				
				// asks the user if they want to process another query
				System.out.print("Do you want to process another query(y/n)?: ");
				again = in.nextLine().toLowerCase().charAt(0);
				
			} while (again == 'y');
			in.close();
		}
		
		// catches any IO Errors
		catch (IOException e) {
			System.exit(1);
		}
	}
	
	 /**
     * creates the array of file names
     * taken from Ngrams.java provided in project 1
     * @param path the path to start searching for
     */
    public static void listFilesInPath(final File path) {
        for (final File fileEntry : path.listFiles()) {

            if (fileEntry.isDirectory())
                listFilesInPath(fileEntry);

            else if (fileEntry.getName().endsWith((".txt")))
                inputFileNames[fileCount++] = fileEntry.getPath();
        }
    }
    
	/**
	 * builds the index from the file path passed to it
	 * @param analyzer Lucene StandardAnalyzer
	 * @param directory Temporary directory for storing the index
	 * @param input directory storing all input files
	 */
	private static void buildIndex(Analyzer analyzer, Directory directory, String input) {
		
		// sets up the file paths for all the input documents
        listFilesInPath(new File(input));
		
		try {
			
			// Initialises the index writer
			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			IndexWriter iwriter = new IndexWriter(directory, config);
			
			// for loop for each file in the input directory
			for (int i = 0; i < fileCount; i++) {
				// converts the string file path to a Path
				Path inputFile = Paths.get(inputFileNames[i]);
				
				Document doc = new Document();
				
				// adds a path field to the current document
				doc.add(new StringField("path", inputFile.toString(), Field.Store.YES));
				
				// gets the contents of the current document file and adds it to the document
				InputStream stream = Files.newInputStream(inputFile);
				doc.add(new TextField("contents", new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))));
				
				iwriter.addDocument(doc);
			}
			iwriter.close();
		} 
		
		// catches IO errors
		catch (IOException e) {
			System.exit(1);
		}
	}
	
	/**
	 * Processes a single query
	 * @param analyzer Lucene StandardAnalyzer
	 * @param directory temp directory holding the index
	 * @param queryText The query to process
	 * @return an array of results
	 */
	private static ScoreDoc[] processQuery(Analyzer analyzer, Directory directory, String queryText) {
		
		try {
			// initialises the directory reader, index searcher, and query parser
			DirectoryReader ireader = DirectoryReader.open(directory);
			IndexSearcher isearcher = new IndexSearcher(ireader);
			QueryParser parser = new QueryParser("contents", analyzer);
			
			// parses the query from the given string
			Query query = parser.parse(queryText);
			
			// returns the array of results
			return isearcher.search(query, 5).scoreDocs;
		}
		
		// catches any IO errors or query parsing errors
		catch (IOException | ParseException e) {
			System.exit(1);
			
			// I don't know why but there are build errors without this return statement even though it doesn't do anything
			return null;
		}
	}
}
