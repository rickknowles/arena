/*
 * Keystone Development Framework
 * Copyright (C) 2004-2009 Rick Knowles
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * Version 2 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License Version 2 for more details.
 *
 * You should have received a copy of the GNU Library General Public License
 * Version 2 along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package arena.fileupload.transform;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import arena.fileupload.FileTransform;
import arena.utils.StringUtils;

public class ExternalProcessFileTransform implements FileTransform {
    private final Log log = LogFactory.getLog(ExternalProcessFileTransform.class);
    
    private String logEncoding = "8859_1";
    private String commandLineArgs[];

    public void transform(File in, File out) throws IOException {
        String[] resizeCmdArgs = buildParsedCommandLine(in, out);
        if (System.getProperty("os.name", "").toUpperCase().startsWith("WINDOWS")) {
            String winPrefix[] = new String[resizeCmdArgs.length + 2];
            winPrefix[0] = "cmd.exe";
            winPrefix[1] = "/C";
            System.arraycopy(resizeCmdArgs, 0, winPrefix, 2, resizeCmdArgs.length);
            resizeCmdArgs = winPrefix;
        }
        log.debug("Launching external process: " + Arrays.asList(resizeCmdArgs)); //comment out
        Process p = Runtime.getRuntime().exec(resizeCmdArgs);
        Thread thStdOut = new Thread(new LoggingStreamConsumer(p.getInputStream(), logEncoding));
        Thread thStdErr = new Thread(new LoggingStreamConsumer(p.getErrorStream(), logEncoding));
        thStdOut.setDaemon(true);
        thStdOut.start();
        thStdErr.setDaemon(true);
        thStdErr.start();
        try {
            int result = p.waitFor();
            log.info(getClass().getName() + " process completed with exit code " + result);
        } catch (InterruptedException err) {
            log.error("Timeout waiting for " + getClass().getName() + " process", err);
        }
    }

    protected String[] buildParsedCommandLine(File in, File out) {
        if (this.commandLineArgs == null) {
            throw new RuntimeException("No command line defined");
        }
        String copied[] = new String[this.commandLineArgs.length];
        
        for (int n = 0; n < this.commandLineArgs.length; n++) {
            String arg = this.commandLineArgs[n];
            if (arg.equalsIgnoreCase("###in###")) {
                copied[n] = in.getAbsolutePath();
            } else if (arg.equalsIgnoreCase("###out###")) {
                copied[n] = out.getAbsolutePath();
            } else {
                copied[n] = arg;
            }
        }
        return copied;
    }

    public void setCommandLine(String commandLine) {
        setCommandLineArgs(StringUtils.tokenizeToArray(commandLine, " "));
    }

    public void setCommandLineArgs(String[] commandLineArgs) {
        this.commandLineArgs = commandLineArgs;
    }

    public void setLogEncoding(String logEncoding) {
        this.logEncoding = logEncoding;
    }
    
    class LoggingStreamConsumer implements Runnable {
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
            log.trace("Stream consumer finished");
            try {
                in.close();
            } catch (IOException err) {
                log.error("Error closing stream", err);
            }
        }
    }
}
