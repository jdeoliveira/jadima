package ve.usb.jgm.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/*
 * ProgressBarDemo2.java requires these files:
 *   LongTask.java
 *   SwingWorker.java
 */
public class ProgressBar extends JPanel {
    public final static int ONE_SECOND = 1000;

    private static JFrame frame = new JFrame("JdmProject");
    private static JProgressBar progressBar;
    private static Timer timer;
   // private JButton startButton;
    private static LongTask task;
   // private JTextArea taskOutput;
    private String newline = "\n";

    public ProgressBar() {
        super(new BorderLayout());
        task = new LongTask();

        //Create the demo's UI.
       /* startButton = new JButton("Start");
        startButton.setActionCommand("start");
        startButton.addActionListener(this);*/

        progressBar = new JProgressBar(0, task.getLengthOfTask());
        progressBar.setValue(0);

        //We call setStringPainted, even though we don't want the
        //string to show up until we switch to determinate mode,
        //so that the progress bar height stays the same whether
        //or not the string is shown.
        progressBar.setStringPainted(true); //get space for the string
        progressBar.setString("");          //but don't paint it

        /*taskOutput = new JTextArea(5, 20);
        taskOutput.setMargin(new Insets(5,5,5,5));
        taskOutput.setEditable(false);*/

        JPanel panel = new JPanel();
     //   panel.add(startButton);
        panel.add(progressBar);

        add(panel, BorderLayout.PAGE_START);
        //add(new JScrollPane(taskOutput), BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        //Create a timer.
        timer = new Timer(ONE_SECOND, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                progressBar.setValue(task.getCurrent());
                String s = task.getMessage();
                if (s != null) {
                    if (progressBar.isIndeterminate()) {
                        //progressBar.setIndeterminate(false);
                        //progressBar.setString(null); //display % string
                        progressBar.setString("Loading");
                    }
                 /*   taskOutput.append(s + newline);
                    taskOutput.setCaretPosition(
                            taskOutput.getDocument().getLength());*/
                }
                if (task.isDone()) {
                    Toolkit.getDefaultToolkit().beep();
                    timer.stop();
                   // startButton.setEnabled(true);
                    progressBar.setValue(progressBar.getMinimum());
                    progressBar.setString(""); //hide % string
                }
            }
        });
    }

    /**
     * Called when the user presses the start button.
     */
    /*public void actionPerformed(ActionEvent evt) {
        
        startButton.setEnabled(false);
        
    }*/

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        
        Toolkit tk = Toolkit.getDefaultToolkit ();
        Dimension screen = tk.getScreenSize ();
       
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        JComponent newContentPane = new ProgressBar();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setResizable(false);
        int lx =  (int) (screen.getWidth ()  * 3/8);
        int ly =  (int) (screen.getHeight () * 3/8);
        frame.setLocation(lx, ly);
        frame.setVisible(true);
        
        
        progressBar.setIndeterminate(true);
        task.go();
        timer.start();
    }
    
    public static void stopProgress(){
        
        frame.dispose();
    }

    public static void startProgress() {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
