import java.awt.*;
import java.awt.event.*;
import javax.swing.ImageIcon;
import javax.swing.Timer;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class TrayProcessTracker {

    private static int checkInterval = 60000;
    private static PopupMenu popup;
    private static TrayIcon trayIcon;
    private static SystemTray tray;
    private static Image trayImgStop;
    private static Image trayImgRun;
    private static Image trayImgCheck;
    private static Timer checkTimer;
    private static java.util.List<ProcessDetails> allProcesses = new ArrayList<ProcessDetails>();
    private static OSCheck.OSType ostype;

    private static void setTrayStatus(Boolean running, long checkTime, long memUsage) {
        Date lastCheck = new Date(checkTime);
        Date nextCheck = new Date(checkTime + checkInterval);

        if (checkTimer.isRunning()) {
            checkTimer.stop();
        }

        String toolTipText = "TomCat: [[" + (running ? "Running" : "Stopped") + "]]\n";

        if (running) {
            trayIcon.setImage(trayImgRun);
            double memUsageKB = ((double) memUsage) / 1024.0;
            toolTipText += "MemUsage: " + memUsageKB + " KB\n";
        } else {
            trayIcon.setImage(trayImgStop);
        }

        checkTimer.start();
        toolTipText += "Last Check: " + lastCheck.toString() + "\nNext Check: " + nextCheck.toString();
        trayIcon.setToolTip(toolTipText);
    }

    private static void checkIfProcEnabled() {
        trayIcon.setImage(trayImgCheck);
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

    private static ActionListener trayDoubleClick = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            checkAndKill();
        }
    };

    public static void main(String[] args) {
        // Check if tray is supported
        if (!SystemTray.isSupported()) {
            System.out.println("Your operating system does not support the System Tray. System exiting...");
            System.exit(0);
        }

        // Check operating system
        ostype = OSCheck.getOperatingSystemType();

        System.out.println("OS Detected: " + ostype.toString());
        if (ostype.equals(OSCheck.OSType.Windows)) {
            System.out.println("WIN OK");
        } else {
            System.out.println("Your operating system is not (yet) supported. System exiting...");
            System.exit(0);
        }

        // Set up process
        ProcessDetails apacheProcessDetails = new ProcessDetails("java.exe", "Apache Tomcat 6.0.");
        apacheProcessDetails = new ProcessDetails("notepad.exe", "notepad");
        allProcesses.add(apacheProcessDetails);
        setUpTray();
        checkTimer = new Timer(checkInterval, processTimerAction);
        checkIfProcEnabled();
    } // main

    private static void setUpTray() {
        popup = new PopupMenu();

        trayImgRun = null;
        trayImgStop = null;
        trayImgCheck = null;

        try {
            trayImgRun = (new ImageIcon(TrayProcessTracker.class.getResource("/images/ProcessRunning.png"), "ProcessRunning")).getImage();
            trayImgStop = (new ImageIcon(TrayProcessTracker.class.getResource("/images/ProcessStopped.png"), "ProcessStopped")).getImage();
            trayImgCheck = (new ImageIcon(TrayProcessTracker.class.getResource("/images/ProcessChecking.png"), "ProcessChecking")).getImage();
        } catch (Exception e) {
            e.printStackTrace();
        }

        MouseMotionListener trayMouseMotionListener = new MouseMotionListener() {
            private long lastCheck = System.currentTimeMillis();

            public void mouseDragged(MouseEvent e) {
            }

            public void mouseMoved(MouseEvent e) {
                long currCheck = System.currentTimeMillis();
                if ((currCheck - lastCheck) >= 1000) {
                    checkTimer.stop();
                    checkIfProcEnabled();
                    checkTimer.start();
                    lastCheck = currCheck;
                }
            }
        };

        trayIcon = new TrayIcon(trayImgStop);
        tray = SystemTray.getSystemTray();
        trayIcon.addActionListener(trayDoubleClick);
        trayIcon.addMouseMotionListener(trayMouseMotionListener);

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
