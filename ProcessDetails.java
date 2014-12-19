public class ProcessDetails {

    private String description; // This MUST be contained in the command line string
    private String processName; // Process name as it appears in task manager
    private Boolean isRunning;
    private int processID;
    private String commandLine;
    private String execPath;
    private long memoryUsage; // in bytes

    public ProcessDetails(String processName, String description) {
        this.description = description;
        this.processName = processName;
        this.isRunning = false;
        this.processID = 0;
        this.commandLine = "";
        this.execPath = "";
        this.memoryUsage = 0;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public Boolean getIsRunning() {
        return isRunning;
    }

    public void setIsRunning(Boolean isRunning) {
        this.isRunning = isRunning;
    }

    public int getProcessID() {
        return processID;
    }

    public void setProcessID(int processID) {
        this.processID = processID;
    }

    public String getCommandLine() {
        return commandLine;
    }

    public void setCommandLine(String commandLine) {
        this.commandLine = commandLine;
    }

    public String getExecPath() {
        return execPath;
    }

    public void setExecPath(String execPath) {
        this.execPath = execPath;
    }

    public long getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(long memoryUsage) {
        this.memoryUsage = memoryUsage;
    }
} // ProcessDetails
