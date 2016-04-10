/*
    Author         |Daniel Bramucci
    ------------------------------------------------------------------------------
    Date           |April 5, 2016
    ------------------------------------------------------------------------------
    Language       |Java 8
    ------------------------------------------------------------------------------
    Purpose        |Personal Project to make commenting homework faster and avoid commenting get/set methods
                   |by hand
    ------------------------------------------------------------------------------
    Course          |K-State CIS200
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;


class AutoCommenter {

    private static final String[] SET_SYNONYMS = {"set", "store"};
    private static final String[] GET_SYNONYMS = {"get"};
    private static UI userInterface;

    public static void main(String[] args) throws FileNotFoundException {
        String sourceName;
        if (args.length > 0 && args[0].equalsIgnoreCase("CLI"))
            userInterface = new CLI();
        else
            userInterface = new GUI();
        try {
            if(args[1].substring(args[1].length()-5).equals(".java"))
                sourceName = args[1].substring(0,args[1].length()-5);
            else
                sourceName = args[1];
        } catch(Exception ignored){ // Handles user not inputing a file
            sourceName = userInterface.getUserFileName();
        }
        if (!(new File(sourceName+".java").exists()))
            sourceName = userInterface.getUserFileName();
        makeBackup(sourceName); // Makes a backup of the original file in-case this breaks something
/*--------------------------------------------------Make new arrays---------------------------------------------------*/
        Scanner sourceCodeFile = new Scanner(new File(sourceName+".java"));
        String sourceCodeAsAString = "";
        while(sourceCodeFile.hasNext())
            sourceCodeAsAString += sourceCodeFile.nextLine() + "\n";
        String[] code = sourceCodeAsAString.split("\n");
        String[] addedComments = new String[code.length];
        // Fill the added comments with empty strings
        for(int i = 0; i < addedComments.length; i++)
            addedComments[i] = "";
        sourceCodeFile.close();
/*--------------------------------------------Fill the comments array-------------------------------------------------*/
        int currentScopeDepth = 0;
        int oldScopeDepth;
        for(int i = 0; i < code.length; i++){
            oldScopeDepth = currentScopeDepth;
            if (code[i].contains("{")){
                currentScopeDepth++;
            }
            if (code[i].contains("}")){
                currentScopeDepth--;
            }
//            System.out.printf("Scopedepth: %d, line is \n%s\n",currentScopeDepth, code[i]);

/*-------------------------------------Code for when a method is found------------------------------------------------*/
            if(oldScopeDepth == 1 && // When moving from class scope
                    code[i].contains("(")  && // Open parenthesis means this is a function
                    code[i].contains("{")  && // prevents false positives from function calls
                    !code[i-1].contains("*/")){ // This means a new function is declared // or an inline array.
                int indexOfOpenParen = code[i].indexOf("(");
                int indexOfCloseParen = code[i].lastIndexOf(")");
                String[] currentLineWhiteSpaceSeparated = code[i].split("\\s");
                String nameOfFunction = "";
                int positionOfFunctionName = -1;


                for(int j = 0; j < currentLineWhiteSpaceSeparated.length; j++){  // Find position of functionName in the current line
                    if(!currentLineWhiteSpaceSeparated[j].isEmpty())
                        positionOfFunctionName = j;
                    if(currentLineWhiteSpaceSeparated[j].contains("("))
                        break;
                }

                // Get function name and check for no space between functionName and '('
                if(positionOfFunctionName != -1){
                    if(!currentLineWhiteSpaceSeparated[positionOfFunctionName].split("\\(")[0].isEmpty())
                        nameOfFunction = currentLineWhiteSpaceSeparated[positionOfFunctionName].split("\\(")[0];
                    else
                        nameOfFunction = currentLineWhiteSpaceSeparated[positionOfFunctionName - 1];
                }

                // check to see if function is a get or set method
                int getFunctionWordIndex = findMatch(nameOfFunction, GET_SYNONYMS);
                boolean isGetFunction = getFunctionWordIndex != -1;
                int setFunctionWordIndex = findMatch(nameOfFunction, SET_SYNONYMS);
                boolean isSetFunction = setFunctionWordIndex != -1;

/*---------------------------------------Comments for get methods-----------------------------------------------------*/
                if(isGetFunction){ // Generates code for get methods
                    addedComments[i] = String.format("\t/**%s\r\n",nameOfFunction);
                    if(!currentLineWhiteSpaceSeparated[positionOfFunctionName - 1].equals("void")){
                        String temp = nameOfFunction.substring(GET_SYNONYMS[getFunctionWordIndex].length());
                        temp = temp.toLowerCase().charAt(0) + temp.substring(1);
                        addedComments[i] += commentify(String.format("Returns the private variable %s's value", temp));
                        addedComments[i] += commentify(String.format("@return the value of the private variable %s", temp));
                    }
                    addedComments[i] += "\t */\r\n";
                }
/*--------------------------------------Comments for set methods------------------------------------------------------*/
                else if(isSetFunction){ // generates code for set methods
                    addedComments[i] = String.format("\t/**%s\r\n",nameOfFunction);
                    String[] parameters = code[i].substring(indexOfOpenParen+1, indexOfCloseParen).split(",");
                    if(!(parameters.length < 1)){
                        for (String parameter : parameters) {
                            String temp = nameOfFunction.substring(SET_SYNONYMS[setFunctionWordIndex].length());
                            temp = temp.toLowerCase().charAt(0) + temp.substring(1);
                            addedComments[i] += commentify(String.format("Sets the private variable %s to the provided value", temp));
                            addedComments[i] += commentify(String.format("@param %s value to store into %s", parameter.split(" ")[1], temp));
                        }
                    }
                    if(!currentLineWhiteSpaceSeparated[positionOfFunctionName - 1].equals("void"))
                        addedComments[i] += String.format("\t *@return %s\r\n","thingy");
                    addedComments[i] += "\t */\r\n";
                }
/*-------------------------------------Comments for non-get/set methods-----------------------------------------------*/
                else{ // Generates comments for non-get/set methods
                    String mainDescription = "";
                    while(!mainDescription.substring(mainDescription.length()-6>=0 ?mainDescription.length()-6:0).equals("\t * \r\n")) {
                        mainDescription += commentify(userInterface.promptInput(String.format("Please describe %s\n " +
                                "type a blank line to quit", nameOfFunction)));
                    }
//                        mainDescription = mainDescription.substring(0,mainDescription.length()-5);

                    addedComments[i] = String.format("\t/**%s\r\n",nameOfFunction);
                    addedComments[i] += mainDescription;
                    String textBetweenTheParens = code[i].substring(indexOfOpenParen+1, indexOfCloseParen);
                    String[] parameters = textBetweenTheParens.split(",");
                    if(parameters[0].split("\\s").length >= 2){
                        for (String parameter : parameters) {
                            addedComments[i] += commentify(String.format("@param %s %s",parameter.split(" ")[parameter.split(" ").length > 2 ?2:1],userInterface.promptInput(String.format("Please input the description for the parameter\n%s", parameter.split(" ")[parameter.split(" ").length > 2 ?2:1]))));//parameters[j].split("\\s")[parameters[j].split("\\s").length - 1]);
                        }
                    }
                    if(!(currentLineWhiteSpaceSeparated[positionOfFunctionName - 1].equals("void") ||currentLineWhiteSpaceSeparated[positionOfFunctionName - 1].equals("public")))
                        addedComments[i] += commentify("@return "+userInterface.promptInput(String.format("Please input the description what the following function returns\n %s", nameOfFunction)));
                    addedComments[i] += "\t */\r\n";
                }

            }
        }

/*----------------------------------------Save the source code with comments -----------------------------------------*/
        PrintWriter newSourceCode = new PrintWriter(sourceName+".java");
        for(int i = 0; i < code.length; i++){
            newSourceCode.print(addedComments[i]);
            newSourceCode.println(code[i]);
        }
        newSourceCode.close();
    }

	/**makeBackup
	 * Makes a backup of the provided file and puts it in a file 
	 * called filename-backup(1).java where 1 will be incremented if a file with that name 
	 * already exists
	 * 
	 * @param originalFileName the base name for the file in string form
	 */
    private static void makeBackup(String originalFileName) throws FileNotFoundException{
        File backup = new File("Generic Backup File.txt");
        if (!new File(String.format("%s-backup(%d).java", originalFileName, 1)).exists())
            backup = new File(String.format("%s-backup(%d).java", originalFileName, 1));
        else
            for(int i = 1; new File(String.format("%s-backup(%d).java", originalFileName, i)).exists(); i++) // makes sure backup is a new file;
                backup = new File(String.format("%s-backup(%d).java", originalFileName, i+1));
        userInterface.display(backup.toString());
        File original = new File(originalFileName+".java");
        Scanner fileIn = new Scanner(original);
        PrintWriter fileOut = new PrintWriter(backup);
        while (fileIn.hasNext())
            fileOut.println(fileIn.nextLine());
        fileOut.close();
        fileIn.close();
    }

	/**findMatch
	 * finds if the wordToCheck is in a given array of strings and returns the index of the element of the array where a match was found.
	 * if no match is found it returns -1
	 * 
	 * @param wordToCheck the word that you wish to find in the array options
	 * @param options the array you are searching in
	 * @return the index of the element in options that matches word to check or -1 if no match is found
	 */
    private static int findMatch(String wordToCheck, String[] options){
        for(int i = 0; i<options.length; i++){
            if(wordToCheck.contains(options[i]))
                return i;
        }
        return -1;
    }

	/**commentify
	 * puts a string in a wrapper that makes it a comment
	 *
	 * @param stringToPutIntoComment the string to place into the comment
	 * @return a commented form of the string
	 */
    private static String commentify(String stringToPutIntoComment){ return String.format("\t * %s\r\n", stringToPutIntoComment);}

}
