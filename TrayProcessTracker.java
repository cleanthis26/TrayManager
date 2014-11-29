import java.awt.*;
import java.awt.event.*;
import javax.swing.ImageIcon;
import javax.swing.Timer;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;

public class TrayProcessTracker {

	private static int checkInterval = 60000;
	private static PopupMenu popup;
    private static TrayIcon trayIcon;
    private static SystemTray tray;
    private static Image trayImgStop;
    private static Image trayImgRun;
    private static Timer checkTimer;
    private static java.util.List<ProcessDetails> allProcesses = new ArrayList<ProcessDetails>();
    private static Date lastCheck = new Date(System.currentTimeMillis());
    private static Date nextCheck = new Date(System.currentTimeMillis() + checkInterval);

    private static void setTrayStatus(Boolean running, long checkTime, long memUsage) {
    	Date lastCheck = new Date(checkTime);
    	Date nextCheck = new Date(checkTime + checkInterval);
    	String toolTipText = "TomCat: [[" + (running? "Running" : "Stopped") + "]]\n";

    	if (running) {
    		trayIcon.setImage(trayImgRun);
    		double memUsageKB = ((double) memUsage) / 1024.0;
    		toolTipText += "MemUsage: " + memUsageKB + " KB\n";
    	}
    	else {
    		trayIcon.setImage(trayImgStop);
    	}
    	toolTipText += "Last Check: " + lastCheck.toString() + "\nNext Check: " + nextCheck.toString();
    	trayIcon.setToolTip(toolTipText);
    }

    private static void checkIfProcEnabled() {
    	ProcessDetails currProc = allProcesses.get(0);
        long currTime = System.currentTimeMillis();
        setTrayStatus(ProcessHandler.checkProcess(currProc), currTime, currProc.getMemoryUsage());
    }

  	private static void checkAndKill() {
  		ProcessDetails currProc = allProcesses.get(0);
    	long currTime = System.currentTimeMillis();
        if (ProcessHandler.checkProcess(currProc)) {
        	ProcessHandler.killProcess(currProc);
        	setTrayStatus(false, currTime, 0);
        }
  	}

  	private static ActionListener processTimerAction = new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
      	checkIfProcEnabled();
      }
  	};


  	private static ActionListener trayDoubleClick = new ActionListener()
    {
        public void actionPerformed(ActionEvent e)
        {
        	checkAndKill();
        }
    };

	public static void main(String[] args) {
		// Set up process
		ProcessDetails apacheProcessDetails = new ProcessDetails("Apache Tomcat 6.0.", "java.exe");
		//apacheProcessDetails = new ProcessDetails("notepad", "notepad.exe");
		allProcesses.add(apacheProcessDetails);
 		setUpTray();
		checkIfProcEnabled();
		checkTimer = new Timer(checkInterval, processTimerAction);
		checkTimer.start();
	} // main

	private static void setUpTray() {
		popup = new PopupMenu();

		trayImgRun = null;
		trayImgStop = null;

		try {
    		trayImgRun = (new ImageIcon("TrayProcessTrackerLogoRunning.jpg", "TrayProcessTrackerRun")).getImage();
    		trayImgStop = (new ImageIcon("TrayProcessTrackerLogoStopped.jpg", "TrayProcessTrackerStop")).getImage();
		} catch (Exception e) {
			e.printStackTrace();
		}

		trayIcon =  new TrayIcon(trayImgStop);
		tray = SystemTray.getSystemTray();
		trayIcon.addActionListener(trayDoubleClick);

		MenuItem exitItem = new MenuItem("Exit");
		exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tray.remove(trayIcon);
                System.exit(0);
            }
        });

        MenuItem killAndExitItem = new MenuItem("Check, Kill and Exit");
        killAndExitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	checkAndKill();
                tray.remove(trayIcon);
                System.exit(0);
            }
        });

		MenuItem checkKillItem = new MenuItem("Check and Kill");
        	checkKillItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	checkAndKill();
            }
        });


        MenuItem checkNowItem = new MenuItem("Check Now");
        checkNowItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	checkTimer.stop();
            	checkIfProcEnabled();
            	checkTimer.start();
            }
        });

        popup.add(checkNowItem);
        popup.add(checkKillItem);
		popup.add(killAndExitItem);
		popup.addSeparator();
		popup.add(exitItem);

		trayIcon.setPopupMenu(popup);

		try {
            tray.add(trayIcon);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
	}



} // TrayProcessTracker