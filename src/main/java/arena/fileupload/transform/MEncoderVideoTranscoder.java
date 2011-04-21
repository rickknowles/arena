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
import java.util.ArrayList;
import java.util.List;

import arena.utils.StringUtils;
/**
 * Uses an mencoder external process to transcode video between formats
 *  
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id$
 */
public class MEncoderVideoTranscoder extends ExternalProcessFileTransform {
    
    private String convertCommand = "mencoder";
    private Integer resizeWidth;
    private Integer resizeHeight;
    private String videoArgs[] = {"-ovc", "lavc", "-lavcopts", "vcodec=mpeg4:vhq:vbitrate=###bitrate###"};
    private int videoBitrate = 1000;
    private String audioArgs[] = {"-oac", "mp3lame", "-lameopts", "cbr:br=###bitrate###"};
    private int audioBitrate = 128;
    private String fourCC = "DIVX";

    protected String[] buildParsedCommandLine(File in, File out) {
        List<String> args = new ArrayList<String>();
        args.add(this.convertCommand);
        args.add(in.getAbsolutePath());
        args.add("-o");
        args.add(out.getAbsolutePath());
        if ((this.resizeHeight != null) || (this.resizeHeight != null)) {
            args.add("-sws");
            args.add("2"); // bicubic transform
            args.add("-vf");
            args.add("scale=" + this.resizeWidth + ":" + this.resizeHeight);
        }
        for (String arg : this.videoArgs) {
            args.add(StringUtils.stringReplace(arg, "###bitrate###", 
                    Integer.toString(this.videoBitrate)));
        }
        for (String arg : this.audioArgs) {
            args.add(StringUtils.stringReplace(arg, "###bitrate###", 
                    Integer.toString(this.audioBitrate)));
        }
        args.add("-ffourcc");
        args.add(this.fourCC);
        for (String arg : getExtraArgs()) {
            args.add(arg);
        }
        return args.toArray(new String[args.size()]);
    }

    public void setConvertCommand(String convertCommand) {
        this.convertCommand = convertCommand;
    }

    public void setResizeWidth(int resizeWidth) {
        this.resizeWidth = resizeWidth;
    }

    public void setResizeHeight(int resizeHeight) {
        this.resizeHeight = resizeHeight;
    }

    public String[] getVideoArgs() {
        return videoArgs;
    }

    public void setVideoArgs(String[] videoArgs) {
        this.videoArgs = videoArgs;
    }

    public int getVideoBitrate() {
        return videoBitrate;
    }

    public void setVideoBitrate(int videoBitrate) {
        this.videoBitrate = videoBitrate;
    }

    public String[] getAudioArgs() {
        return audioArgs;
    }

    public void setAudioArgs(String[] audioArgs) {
        this.audioArgs = audioArgs;
    }

    public int getAudioBitrate() {
        return audioBitrate;
    }

    public void setAudioBitrate(int audioBitrate) {
        this.audioBitrate = audioBitrate;
    }

    public String getFourCC() {
        return fourCC;
    }

    public void setFourCC(String fourCC) {
        this.fourCC = fourCC;
    }

    public String getConvertCommand() {
        return convertCommand;
    }
    
    public String[] getExtraArgs() {
        return new String[0];
    }
}
