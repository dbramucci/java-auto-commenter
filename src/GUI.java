import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.io.File;
class GUI implements UI {
    /**promptInput
     *
     * prompts the user for input and returns their answer
     *
     * @param prompt the prompt to display to the user
     * @return the users input
     */
    public String promptInput(String prompt){
        return JOptionPane.showInputDialog(null, prompt);
    }

    /**getUserFileName
     * displays a file selection dialog and returns and returns the base file name  as a string
     *
     * @return the base file name as a string string
     */
    public String getUserFileName() {
        JFileChooser fc = new JFileChooser();
        while(true) {
            fc.showOpenDialog(null);
            String sourceName = fc.getSelectedFile().toString();
            if (new File(sourceName + ".java").exists())
                return sourceName;
            else if (new File(sourceName).exists())
                return sourceName.substring(0, sourceName.length() - 5);
            else
                JOptionPane.showMessageDialog(null, "Source code does not exists (do not type the .java");
        }
    }

    /**display
     *
     * displays the provided string to the user
     *
     * @param output string to display to the user
     */
    public void display(String output){
        JOptionPane.showMessageDialog(null,output);
    }
}

