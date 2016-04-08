import com.sun.javafx.binding.StringFormatter;

import java.io.*;
import java.util.*;


public class AutoCommenter {

    private static final String[] SET_SYNONYMS = new String[]{"set", "store"};
	private static final String[] GET_SYNONYMS = new String[]{"get"};

	public static void main(String[] args) throws FileNotFoundException {
		String sourceName;
        UI userInterface = new CLI();
		try {
            sourceName = args[0];
        } catch(Exception e){ // Handles user not inputing a file
            sourceName = userInterface.getUserFileName();
        }
//		else
//			sourceName = new Scanner(System.in).nextLine();
		makeBackup(sourceName); // Makes a backup of the original file in-case this breaks something
		/*************Make new arrays**************/
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
		/*********************Fill the comments array*******************/
//		for(int i = 0; i<code.length; i++)
//			System.out.printf("%d: %s\n", i, code[i]);
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
			//System.out.printf("Scopedepth: %d, line is \n%s\n",currentScopeDepth, code[i]);

            /*---------Code for when a method is found---------*/
            if(oldScopeDepth == 1 && // When moving from class scope
                    code[i].contains("(")  && // Open pearenthesis means this is a function
                    !code[i-1].contains("*/")){ // This means a new function is declared // or an inline array.
                int indexOfOpenParen = code[i].indexOf("(");
                int indexOfCloseParen = code[i].lastIndexOf(")");
                String[] currentLineWhiteSpaceSeparated = code[i].split("\\s");
                String nameOfFunction = "";
                int positionOfFunctionName = -1;
                for(int j = 0; j < currentLineWhiteSpaceSeparated.length; j++){
                    if(currentLineWhiteSpaceSeparated[j].length() != 0)
                        positionOfFunctionName = j;
                    if(currentLineWhiteSpaceSeparated[j].contains("("))
                        break;
                }
                if(positionOfFunctionName != -1){
                    if(currentLineWhiteSpaceSeparated[positionOfFunctionName].split("\\(")[0].length() != 0)
                        nameOfFunction = currentLineWhiteSpaceSeparated[positionOfFunctionName].split("\\(")[0];
                    else
                        nameOfFunction = currentLineWhiteSpaceSeparated[positionOfFunctionName - 1];
                }
                if(!currentLineWhiteSpaceSeparated[positionOfFunctionName - 1].equals("public")){ //if not a constructor
                    int getFunctionWordIndex = findMatch(nameOfFunction, GET_SYNONYMS);
                    boolean isGetFunction = (getFunctionWordIndex != -1);
                    int setFunctionWordIndex = findMatch(nameOfFunction, SET_SYNONYMS);
                    boolean isSetFunction = (setFunctionWordIndex != -1);
                    if(isGetFunction){
                        addedComments[i] = String.format("\t/**%s\r\n",nameOfFunction);
                        if(!currentLineWhiteSpaceSeparated[positionOfFunctionName - 1].equals("void")){
                            String temp = nameOfFunction.substring(GET_SYNONYMS[getFunctionWordIndex].length());
                            temp = temp.toLowerCase().charAt(0) + temp.substring(1);
                            addedComments[i] += commentify(String.format("Returns the private variable %s's value", temp));
                            addedComments[i] += commentify(String.format("@return the value of the private variable %s", temp));
                        }
                        addedComments[i] += "\t */\r\n";
                    }
                    else if(isSetFunction){
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
                    else{
                        System.out.printf("Please describe %s\n", nameOfFunction);
                        System.out.println("type a blank line to quit");
                        String mainDescription = "";
                        while(!mainDescription.substring((mainDescription.length()-6>=0)?mainDescription.length()-6:0).equals("\t * \r\n")) {
                            mainDescription += userInterface.promptInput(">>>");
                        }
//                        mainDescription = mainDescription.substring(0,mainDescription.length()-5);

                        addedComments[i] = String.format("\t/**%s\r\n",nameOfFunction);
                        addedComments[i] += mainDescription;
                        String[] parameters = code[i].substring(indexOfOpenParen+1, indexOfCloseParen).split(",");
                        if(parameters.length >= 2){
                            for (String parameter : parameters) {
                                addedComments[i] += commentify("@param" + parameter.split(" ")[(parameter.split(" ").length > 2)?2:1] + userInterface.promptInput(String.format("Please input the description for the parameter\n%s\n>>>", parameter.split(" ")[(parameter.split(" ").length > 2)?2:1])));//parameters[j].split("\\s")[parameters[j].split("\\s").length - 1]);
                            }
                        }
                        if(!currentLineWhiteSpaceSeparated[positionOfFunctionName - 1].equals("void"))
                            System.out.printf("Please input the description what the following function returns\n %s\n>>>", code[i]);
                            addedComments[i] += commentify("@return "+userInterface.promptInput(String.format("Please input the description what the following function returns\n %s\n>>>", code[i])));
                        addedComments[i] += "\t */\r\n";
                    }
                }
			}
		}

		/********************Save the source code with comments **************/
//		for(int i = 0; i < addedComments.length; i++)
//			System.out.println(addedComments[i]);
		PrintWriter newSourceCode = new PrintWriter(sourceName+".java");
		for(int i = 0; i < code.length; i++){
			newSourceCode.print(addedComments[i]);
			newSourceCode.println(code[i]);
		}
		newSourceCode.close();
	}

    private static void makeBackup(String originalFileName) throws FileNotFoundException{
		File backup = new File("Generic Backup File.txt");
		if (!new File(String.format("%s-backup(%d).java", originalFileName, 1)).exists())
			backup = new File(String.format("%s-backup(%d).java", originalFileName, 1));
		else
			for(int i = 1; new File(String.format("%s-backup(%d).java", originalFileName, i)).exists(); i++) // makes sure backup is a new file;
				backup = new File(String.format("%s-backup(%d).java", originalFileName, i+1));
		System.out.println(backup);
		File original = new File(originalFileName+".java");
		Scanner fileIn = new Scanner(original);
		PrintWriter fileOut = new PrintWriter(backup);
		while (fileIn.hasNext())
			fileOut.println(fileIn.nextLine());
		fileOut.close();
		fileIn.close();
	}

	private static int findMatch(String wordToCheck, String[] options){
		for(int i = 0; i<options.length; i++){
			if(wordToCheck.contains(options[i]))
				return i;
		}
		return -1;
	}

    private static String commentify(String stringToPutIntoComment){ return String.format("\t * %s\r\n", stringToPutIntoComment);}

}
