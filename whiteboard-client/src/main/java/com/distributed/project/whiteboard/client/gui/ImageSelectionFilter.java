package com.distributed.project.whiteboard.client.gui;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * This class is used for adding a filter for the files being selected for
 * loading image onto the draw area.
 * 
 * @implNote It extends {@link FileFilter} which is used in initializing the
 *           {@link JFileChooser}.
 * 
 * @author Abhijeet - 1278218
 *
 */
public class ImageSelectionFilter extends FileFilter {

	@Override
	public boolean accept(File f) {
		// If the file selected is directory then accept
		if (f.isDirectory()) {
			return true;
		}

		// Only display files with .png extension
		return f.getName().matches(".*(.png)");
	}

	@Override
	public String getDescription() {
		return "PNG Image files";
	}

}
