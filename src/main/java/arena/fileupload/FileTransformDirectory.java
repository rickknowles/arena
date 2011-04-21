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
package arena.fileupload;

public class FileTransformDirectory {    
    private String name;
    private String outputDirectory;
    private FileTransform transform;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getOutputDirectory() {
        return outputDirectory;
    }
    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }
    public FileTransform getTransform() {
        return transform;
    }
    public void setTransform(FileTransform transform) {
        this.transform = transform;
    }
}
