package arena.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ExternalScriptSupport {
    private final Log log = LogFactory.getLog(ExternalScriptSupport.class);
    
    private String logEncoding = "8859_1";

    public void runScript(String[] cmdArgs) throws IOException {
        runScript(cmdArgs, getClass().getName(), logEncoding, log);
    }

    public static void runScript(String[] cmdArgs, String className, String logEncoding, Log log) throws IOException {
        if (System.getProperty("os.name", "").toUpperCase().startsWith("WINDOWS")) {
            String winPrefix[] = new String[cmdArgs.length + 2];
            winPrefix[0] = "cmd.exe";
            winPrefix[1] = "/C";
            System.arraycopy(cmdArgs, 0, winPrefix, 2, cmdArgs.length);
            cmdArgs = winPrefix;
        }
        log.debug("Launching external process: " + Arrays.asList(cmdArgs)); //comment out
        Process p = Runtime.getRuntime().exec(cmdArgs);
        Thread thStdOut = new Thread(new LoggingStreamConsumer(p.getInputStream(), logEncoding));
        Thread thStdErr = new Thread(new LoggingStreamConsumer(p.getErrorStream(), logEncoding));
        thStdOut.setDaemon(true);
        thStdOut.start();
        thStdErr.setDaemon(true);
        thStdErr.start();
        try {
            int result = p.waitFor();
            log.info(className + " process completed with exit code " + result);
        } catch (InterruptedException err) {
            log.error("Timeout waiting for " + className + " process", err);
        }
    }

    public void setLogEncoding(String logEncoding) {
        this.logEncoding = logEncoding;
    }
    
    static class LoggingStreamConsumer implements Runnable {
        private final Log log = LogFactory.getLog(LoggingStreamConsumer.class);
        private InputStream in;
        private String encoding;
        
        LoggingStreamConsumer(InputStream in, String encoding) {
            this.in = in;
            this.encoding = encoding;
        }
        
        public void run() {
            byte buffer[] = new byte[4096];
            int read = 0;
            try {
                while ((read = in.read(buffer)) != -1) {
                    log.debug(new String(buffer, 0, read, encoding));
                }
            } catch (IOException err) {
                log.error("Error in stream consumer", err);
            }
            log.debug("Stream consumer finished");
            try {
                in.close();
            } catch (IOException err) {
                log.error("Error closing stream", err);
            }
        }
    }
}
