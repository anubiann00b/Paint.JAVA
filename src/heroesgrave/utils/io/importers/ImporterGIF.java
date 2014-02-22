/*
 *	Copyright 2013 HeroesGrave and other Paint.JAVA developers.
 *
 *	This file is part of Paint.JAVA
 *
 *	Paint.JAVA is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	This program is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with this program.  If not, see <http://www.gnu.org/licenses/>
*/

package heroesgrave.utils.io.importers;

import heroesgrave.paint.gui.SimpleModalProgressDialog;
import heroesgrave.paint.image.Canvas;
import heroesgrave.utils.io.ImageImporter;
import heroesgrave.utils.io.importers.gif.DataBlock;
import heroesgrave.utils.io.importers.gif.ImageDescriptorHeader;
import heroesgrave.utils.io.importers.gif.RGBColor;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author anubiann00b
 **/
public class ImporterGIF extends ImageImporter
{
	/*
	 * GIF syntax:
	 * See http://www.mcu.so/download/GIFDecoder.pdf
	 * and http://commandlinefanatic.com/cgi-bin/showarticle.cgi?article=art011
	 */
	
	@Override
	public Canvas read(File file) throws IOException
	{
		DataInputStream in = new DataInputStream(new FileInputStream(file));
				
		int width = 1;
		int height = 1;
		int surfaceArea = 1;
		
		int colorTableSize = 0; // This is part of the field byte.
		
		int bgColorIndex;
		int pixelAspectRatio;
		
		int[] raw;
		
		// Header.
		
		// Check if it's a proper GIF.
		if (in.read() != 47 || in.read() != 49 || in.read() != 46)
		{
			System.out.println("Error: Not proper GIF file.");
		}
		
		// Version is either "38 39 61" (89a) or "38 37 61" (87a).
		if (in.read() != 38 || in.read() != 37 || in.read() != 61)
		{
			System.out.println("Error: Not proper GIF version.");
		}
		
		width = in.readUnsignedShort();
		height = in.readUnsignedShort();
		
		colorTableSize = 1 << (((in.readByte() & 0x07)+1));
		
		bgColorIndex = in.readByte();
		pixelAspectRatio = in.readByte();
		
		RGBColor[] colorArray = new RGBColor[colorTableSize];
		
		for (int i = 0; i < colorTableSize; i++)
		{
			colorArray[i].red = in.readByte();
			colorArray[i].green = in.readByte();
			colorArray[i].blue = in.readByte();
		}
		
		byte block = 0;
		
		ArrayList<DataBlock> data = new ArrayList<DataBlock>();
		
		while(block != 0x3B) // End of file marker.
		{
			block = in.readByte();
			
			switch(block)
			{
				case 0x2C: // Starts data block.
					// Parse header.
					ImageDescriptorHeader header = new ImageDescriptorHeader();
					header.imageLeft = in.readShort();
					header.imageTop = in.readShort();
					header.imageWidth = in.readShort();
					header.imageHeight = in.readShort();
					header.localColorTableSize = 1 << (((in.readByte() & 0x07)+1));
					break;
				default:
					System.out.println("wat");
			}
		}
		
		SimpleModalProgressDialog DIALOG = new SimpleModalProgressDialog("Working!", "Loading Image...", surfaceArea);
		
		/*
		for(int I = 0; I < surfaceArea; I++)
		{
			// get
			int pixel = in.readInt();
			
			// convert
			int R = (pixel >> 24) & 0xff;
			int G = (pixel >> 16) & 0xff;
			int B = (pixel >> 8) & 0xff;
			int A = (pixel) & 0xff;
			
			pixel = 0;
			pixel |= B;
			pixel |= G << 8;
			pixel |= R << 16;
			pixel |= A << 24;
			
			// put
			raw[I] = pixel;
			
			// Don't update the progress-bar for every value, since that can cause some serious slowdown!
			if(I % 128 == 0)
			{
				DIALOG.setValue(I);
			}
		}
		*/
		
		// set progress to 100
		DIALOG.setValue(surfaceArea - 1);
		
		// close progress dialog
		DIALOG.close();
		
		// Read 'EOID'
		in.readInt();
		
		in.close();
		
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		//image.setRGB(0, 0, width, height, raw, 0, width);
		
		return new Canvas("Background", image);
	}
	
	@Override
	public String getFormat()
	{
		return "gif";
	}
	
	@Override
	public String getDescription()
	{
		return "GIF - Graphics Interchange Data Format";
	}
	
}