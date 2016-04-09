import javax.swing.*;
import java.io.File;
class GUI extends UI{

    public String promptInput(String prompt){
        return JOptionPane.showInputDialog(null, prompt);
    }
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
    public void display(String output){
        JOptionPane.showMessageDialog(null,output);
    }
}

