/**
 * @author Link Parrish
 * Takes a posting list as an input and outputs a variable byte gap list
 */
import java.util.InputMismatchException;
import java.util.Scanner;

public class VarByte {

    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);
        byte[] varByteList;

        boolean again = true;
        int len, temp;

        // while loop until a correct posting list is created
        while (again) {
            // try catch for non integer input
            try {

                // gets the length of the posting list
                System.out.print("Posting list length: ");
                len = in.nextInt();
                in.nextLine();

                // error checking for invald length
                if (len <= 0) 
                    System.out.println("Error: length must be greater than zero.\n");
                
                else {

                    // creates a list with the length receieved above
                    int[] postList = new int[len];

                    // loop to get all the values for the list
                    for (int i = 0; i < len; i++) {
                        System.out.print("Enter value " + (i+1) + ": ");
                        temp = in.nextInt();
                        in.nextLine();

                        // error checking for doc id of zero
                        if (temp <= 0) {
                            System.out.println("Error: document value must be greater than zero.\n");
                            i--;
                        }

                        // error checking for doc id less than the last one
                        else if (i != 0 && postList[i-1] >= temp) {
                            System.out.println("Error: document value must be greater than the last one.\n");
                            i--;
                        }

                        else
                            postList[i] = temp;
                    }

                    // method to convert to a variable byte list
                    varByteList = getVarByte(postList);

                    // prints the list out
                    System.out.println("\nVarible Byte List:");

                    for (int i = 0; i < varByteList.length; i++)
                        System.out.print(varByteList[i] + " ");

                    again = false;
                }
            }

            // if any non integer characters are inputed the program will start again from the top
            catch (InputMismatchException e) {
                System.out.println("Error: non-integer entered, restarting...\n");
                in.nextLine();
            }
        }

        in.close();
    }



    /**
     * method to actually convert the posting list to a variable byte list
     * @param postL the posting list to convert
     * @return the variable byte list
     */
    public static byte[] getVarByte(int[] postL) {

        // create a list to store the gap values
        int[] gapL = new int[postL.length];

        // calculate the gap values
        gapL[0] = postL[0];
        for (int i = 1; i < postL.length; i++)
                gapL[i] = postL[i] -postL[i-1];

        // creates an array that contains the number of bytes needed for each value of the gap list
        int totalSize = 0;
        int[] size = new int[gapL.length];

        // uses the byteSize method to calculate the values for the size array
        for (int i = 0; i < gapL.length; i++) {
            size[i] = byteSize(gapL[i]);
            totalSize += size[i];
        }

        // create the variable byte list
        byte[] byteL = new byte[totalSize];
        int currentSize = 0;

        // loop to fill the variable byte list one gap value at a time
        for (int i = 0; i < gapL.length; i++) {

            // loop to create all the bytes for each value of the gap list
            byte[] oneGap = new byte[size[i]];
            for (int j = size[i] - 1; j >= 0; j--) {

                // removes the first seven digits of the binary value
                oneGap[j] = (byte)(gapL[i] % 128);

                // if this is the last value replace the front digit with a 1
                if (j == size[i] - 1)
                    oneGap[j] = (byte)(oneGap[j] | 0x80);

                // add the byte to the final byte array
                byteL[currentSize + j] = oneGap[j];
            }
            currentSize += size[i];
        }
        return byteL;
    }

    /**
     * calculates the number of seven digit binary values needed to store a number
     * @param x the number that needs to be stored in binary
     * @return the number of 7 digit binary numbers
     */
    public static int byteSize(int x) {

        int b = 0;

        // count the number of binary digits needed
        do {
            x = x / 2;
            b++;
        } while (x != 0);

        // if the number is divisable by 7 return b / 7 otherwise return b / 7 + 1
        if (b % 7 == 0)
            return b / 7;
        else
            return b / 7 + 1;
    }
}