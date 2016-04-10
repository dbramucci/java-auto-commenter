import java.io.File;
import java.util.Scanner;
class CLI implements UI {
    private final Scanner keyboard = new Scanner(System.in);

    /**promptInput
     *
     * prompts the user for input and returns their answer
     *
     * @param prompt the prompt to display to the user
     * @return the users input
     */
    public String promptInput(String prompt){
        System.out.print("\033[H\033[2J");
        System.out.flush();
        System.out.print(prompt+"\t\n>>>");
        return keyboard.nextLine();
    }

    /**getUserFileName
     * displays a file selection dialog and returns and returns the base file name  as a string
     *
     * @return the base file name as a string string
     */
    public String getUserFileName() {
        while(true) {
            System.out.print("Please put in a file name: ");
            String sourceName = keyboard.nextLine();
            if (new File(sourceName+".java").exists())
                return sourceName;
            else if (new File(sourceName).exists())
                return sourceName.substring(0, sourceName.length()-5);
            else
                System.out.println("Source code does not exists (do not type the .java");
        }
    }
    /**display
     *
     * displays the provided string to the user
     *
     * @param output string to display to the user
     */
    public void display(String output){
        System.out.println(output);
    }
}
