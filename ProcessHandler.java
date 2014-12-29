/**
 * Copyright (C) 2014 Cleanthis E. Metaxas
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ProcessHandler {

    public static void killProcess(ProcessDetails procToKill) {
        String cmdToRun;

        try {
            cmdToRun = System.getenv("windir") + "\\system32\\" + "taskkill.exe" + " /FI \"PID eq " + procToKill.getProcessID() + "\" /F";
            Runtime.getRuntime().exec(cmdToRun);
        } catch (Exception err) {
            err.printStackTrace();
        } // catch

        procToKill.setIsRunning(false);
    }

    private static void updateProcess(ProcessDetails procToUpdate, Boolean foundRunning, String result) {
        procToUpdate.setIsRunning(foundRunning);
        if (foundRunning) {
            String[] procInfo = result.split(",");
            String cmdLine = "";

            for (int i = 1; i < procInfo.length - 3; i++) {
                cmdLine += procInfo[i];
            }

            procToUpdate.setCommandLine(cmdLine);
            procToUpdate.setExecPath(procInfo[procInfo.length - 3]);
            procToUpdate.setProcessID(Integer.parseInt(procInfo[procInfo.length - 2]));
            procToUpdate.setMemoryUsage(Long.parseLong(procInfo[procInfo.length - 1]));
        }
    }

    public static boolean checkProcess(ProcessDetails procToCheck) {
        String cmdToRun;
        try {
            // Construct the command
            cmdToRun = System.getenv("windir") + "\\system32\\wbem\\" + "wmic.exe" + " PROCESS where NAME=\"" + procToCheck.getProcessName() + "\" get commandline, executablepath, processid, workingsetsize";
            cmdToRun += " /FORMAT:\"" + System.getenv("windir") + "\\System32\\wbem\\en-US\\csv.xsl\"";

            String line; // current result line
            Process p = Runtime.getRuntime().exec(cmdToRun);
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream())); // get output of process executed

            while ((line = input.readLine()) != null) {
                //System.out.println(line); //<-- Parse data here.
                if (line.contains(procToCheck.getDescription())) {
                    updateProcess(procToCheck, true, line);
                    return true;
                }
            } // while
            input.close(); // close input stream
        } catch (Exception err) {
            err.printStackTrace();
        } // catch
        updateProcess(procToCheck, false, null);
        return false;
    } // checkProcess
} // ProcessChecker
