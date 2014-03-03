package data_gen;

import java.awt.Component;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.io.File;

import javax.swing.*;

import org.eclipse.jdt.internal.compiler.lookup.UpdatedMethodBinding;

public class CounterUpdateDialog {
	static JFrame frame;
	static boolean run = true;
	static JLabel counters = new JLabel();
	public static void quit(){
		run=false;
	}
	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event-dispatching thread.
	 */
	private static void createAndShowGUI() {
        // Create and set up the window.
        frame = new JFrame("CounterUpdateDialog");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Set on close listener
        frame.addWindowListener(new FrameListener(frame));

        frame.setAlwaysOnTop(true);
        frame.getContentPane().setLayout(new GridBagLayout());
        GridBagConstraints cons = new GridBagConstraints();
        cons.fill = GridBagConstraints.HORIZONTAL;
        cons.weightx = 1;
        cons.gridx = 0;
        
        
        JButton incrementCorrelationButton = new javax.swing.JButton();
        incrementCorrelationButton.setText("Inc. corr. between Selenium steps & HTML files");
        incrementCorrelationButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        incrementCorrelationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ExtendedCSVGenerator.incrementCorrelation();
                updateLabel();
            }
        });
        
        JButton incrementHTMLButton = new javax.swing.JButton();
        incrementHTMLButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        incrementHTMLButton.setText("Inc. HTML file index");
        incrementHTMLButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ExtendedCSVGenerator.incrementHTML();
                updateLabel();
            }
        });
        
        JButton escapeProcessButton = new javax.swing.JButton();
        escapeProcessButton.setText("Terminate process");
        escapeProcessButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        escapeProcessButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	updateLabel();
            	// Search for selenium.html file
            	File input = new File(System.getProperty("user.dir") + "\\HTMLfinal\\selenium.html");
            	if(!input.exists())
            		JOptionPane.showMessageDialog(frame, "ERROR: you haven't saved the selenium.html file yet.");
            	else{
	            	frame.dispose();
	                ExtendedCSVGenerator.escapeProcess();
	                ExtendedCSVGenerator.quit();
            	}
            }
        });
        
        final String instructions = "Each time an action caused by the user is detected by Selenium IDE but\n"+
        "DOES NOT change anything in the page (for example a simple click in the\n"+
        		"Amazon search bar causes a click action to appear but does not change the page),\n"+
        "the user must increment HTML.\n\nEach time an action caused by the user is detected by Selenium\n"+
        		"IDE as TWO or more different actions, two possible actions can be performed:\n"+
	"\t- If nothing in the page was changed, the user must press the \"Inc. correlation\" key the number\n"+
        		"of times that Selenium IDE detected as actions\n"+ 
	"\t- if something has changed in the page, the user must press the \"Inc. correlation\" key the number of\n"+
        		"times that selenium IDE detected as actions MINUS ONE and then press the \"Inc. HTML\" key.\n"+
"\nFor example, if one action performed by the user reported as three actions in Selenium IDE:\n"+
	"\t- if nothing was changed in the page, the user presses \"Inc. correlation\" three times\n"+
	"\t- if something changed, the user presses \"Inc. HTML\"  two times and \"Inc. HTML\" one time\n\n"+
	"At the end of the execution, the user must save the Selenium execution trace as “selenium.html”\n"+
	" in the HTMLFinal folder that the program created. After this, the user must press the \"Terminate\" button \n"+
	"to terminate the program and start the inferrer process, that does not take more than a couple of seconds.";
        
        JButton showInstructionsButton = new javax.swing.JButton();
        showInstructionsButton.setText("Show instructions");
        showInstructionsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        showInstructionsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	JOptionPane.showMessageDialog(frame, instructions);
            }
        });
        
        updateLabel();
        
        frame.getContentPane().add(incrementCorrelationButton,cons);
        frame.getContentPane().add(incrementHTMLButton,cons);
        frame.getContentPane().add(escapeProcessButton,cons);
        frame.getContentPane().add(showInstructionsButton,cons); 
        frame.getContentPane().add(counters,cons);
        
        // Make frame appear on upper right
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
        Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
        int x = (int)rect.getMaxX()-350;
        frame.setLocation(x,0);
        
        // Display the window.
        frame.pack();
        frame.setVisible(true);
    }
	
	static void updateLabel(){
		counters.setText("               HTML Index: " + ExtendedCSVGenerator.getHTMLFileIndex() + " | Correlation counter: " + ExtendedCSVGenerator.getCorrelationBetweenSeleniumStepAndHtmlFiles());
	}

	public static void createGUI() {
        createAndShowGUI();
    }
	public static void main(String[] args) {
		createGUI();
	}
}
