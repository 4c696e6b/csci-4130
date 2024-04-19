/*
 * @author Link Parrish
 * Reads files from the /docs folder and creates an inverted index, then allows the user to search for a single term or a intersection of terms
 */
import java.util.*;
import java.io.*;
import java.util.regex.*;

public class Main {

    // no more than this many input files needs to be processed
    final static int MAX_NUMBER_OF_INPUT_FILES = 100;
    // an array to hold Gutenberg corpus file names
    static String[] inputFileNames = new String[MAX_NUMBER_OF_INPUT_FILES];
    static int fileCount = 0;


    public static void main(String[] args) {

        // sets up the file paths for all the documents
        String inputFileDirName = "./docs";
        listFilesInPath(new File (inputFileDirName));

        char action;
        String s1, s2;
        Scanner in = new Scanner(System.in);

        // uses a hash map as the dictionary, with string as a key and a linked list as the value
        HashMap<String, LinkedList<Integer>> dict = new HashMap<String, LinkedList<Integer>>();

        for(int i = 0; i < fileCount; i++) {
            dict = readFile(dict, i);
        }

        System.out.println("\nDictionary Created!\n");

        // do while loop for the main menu
        do {
            System.out.print("choose an option: [S]ingle term / [I]ntersection / [E]xit: ");
            action = in.next().toUpperCase().charAt(0);
            in.nextLine();

            switch(action) {

                // single term case
                case 'S':
                    // get the term
                    System.out.print("Enter a term to search for: ");
                    s1 = in.next();
                    in.nextLine();

                    // handles if the term can't be found
                    if (dict.get(s1) == null) {
                        System.out.println(s1 + " not found\n");
                    }

                    // prints the posting list of the term
                    else {
                        System.out.println(dict.get(s1) + "\n");
                    }
                    break;
                
                // intersection case
                case 'I':

                    // get the terms
                    System.out.print(   "Enter the first term: ");
                    s1 = in.next();
                    in.nextLine();

                    System.out.print("Enter the second term: ");
                    s2 = in.next();
                    in.nextLine();

                    // handles if one of the terms cannot be found
                    if (dict.get(s1) == null || dict.get(s2) == null) {
                        System.out.println("one or more terms not found\n");
                    }

                    else {
                        // get the intersection
                        LinkedList<Integer> result = intersection(dict.get(s1), dict.get(s2));

                        // handles if there's no intersection
                        if (result.size() == 0)
                            System.out.println("No intersection found between terms '" + s1 + "' and '" + s2 +"'.");

                        // prints the intersection
                        else
                        System.out.println(result + "\n");
                    }
                    break;

            }
        } while (action != 'E');

        in.close();
    }

    /**
     * creates the array of file names
     * taken from Ngrams.java provided
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
     * Reads a single file and adds the terms to the inverted index
     * Mostly taken from Ngrams.java
     * @param dict the dictionary
     * @param i the file index to read
     * @return the updated dictionary
     */
    public static HashMap<String, LinkedList<Integer>> readFile(HashMap<String, LinkedList<Integer>> dict, int i) {

        String line, word;
        BufferedReader br = null;
        Matcher wordMatcher;
        Pattern wordPattern = Pattern.compile("[a-zA-Z]+");

        try {
            // get a BufferedReader object, which encapsulates
            // access to a (disk) file
            br = new BufferedReader(new FileReader(inputFileNames[i]));
            // as long as we have more lines to process, read a line
            // the following line is doing two things: makes an assignment
            // and serves as a boolean expression for while test
            while ((line = br.readLine()) != null) {
                // process the line by extracting words using the wordPattern
                wordMatcher = wordPattern.matcher(line);
                // process one word at a time
                while ( wordMatcher.find() ) {
                    // extract the word
                    word = line.substring(wordMatcher.start(), wordMatcher.end());
                    // System.out.println(word);
                    // // convert the word to lowercase, and write to word file
                    word = word.toLowerCase();


                    // if the term doesn't exist, adds it to dictionary
                    if (dict.get(word) == null) {
                        dict.put(word, new LinkedList<>());
                        dict.get(word).add(i+1);
                    }

                    // if the term does exist, adds it to the corresponding posting list
                    else
                        dict.get(word).add(i+1);

                }
            }
        }

        catch (IOException ex) {
            System.err.println("File " + inputFileNames[i] + " not found. Program terminated.\n");
            System.exit(1);
        }
        return dict;
    }


    /**
     * finds the intersection of two posting lists
     * @param l1 the first posting list
     * @param l2 the second posting list
     * @return the intersection of the two lists
     */
    public static LinkedList<Integer> intersection(LinkedList<Integer> l1, LinkedList<Integer> l2) {
        LinkedList<Integer> result = new LinkedList<Integer>();
        int p1 = 0;
        int p2 = 0;
        while (p1 < l1.size() && p2 < l2.size()) {

            if (l1.get(p1) == l2.get(p2)) {
                result.add(l1.get(p1));
                p1++;
                p2++;
            }

            else if (l1.get(p1) < l2.get(p2))
                p1++;

            else
                p2++;
        }
        return result;
    }
}
