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

public class ImageMagickImageResizer extends ExternalProcessFileTransform {
    
    private String convertCommand = "convert";
    private int resizeWidth = 200;
    private int resizeHeight = 200;

    protected String[] buildParsedCommandLine(File in, File out) {
        return new String[] {
                this.convertCommand,                
                "-resize",
                this.resizeWidth + "x" + this.resizeHeight,
                in.getAbsolutePath(),              
                out.getAbsolutePath()
        };
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
}
