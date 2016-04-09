import java.io.File;
import java.util.Scanner;
class CLI extends UI{
    private final Scanner keyboard = new Scanner(System.in);

    public String promptInput(String prompt){
        System.out.print("\033[H\033[2J");
        System.out.flush();
        System.out.print(prompt+"\t\n>>>");
        return keyboard.nextLine();
    }
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
    public void display(String output){
        System.out.println(output);
    }
}
