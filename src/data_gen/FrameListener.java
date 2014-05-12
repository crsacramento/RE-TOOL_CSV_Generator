package data_gen;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class FrameListener implements WindowListener {
    JFrame frame;

    public FrameListener(JFrame frame) {
        super();
        this.frame = frame;
    }

    void delete(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles())
                delete(c);
        }
        if (!f.delete())
            throw new FileNotFoundException("Failed to delete file: " + f);
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        int n = JOptionPane
                .showOptionDialog(
                        frame,
                        "Do you wish to erase all produced files?",
                        "Exiting", JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, null, null);
        if (n == 0) {
            File HTMLfinal = new File(System.getProperty("user.dir") + "\\HTMLfinal");
            File HTMLtemp = new File(System.getProperty("user.dir") + "\\HTMLtemp");
            try {
                delete(HTMLfinal);
                delete(HTMLtemp);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            
            ExtendedCSVGenerator.quit();
            System.exit(0);
        }else if(n == 1){
            ExtendedCSVGenerator.quit();
            System.exit(0);
        }

    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

}
