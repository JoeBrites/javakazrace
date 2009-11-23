//==================================================================
/// Created by Davide Pasca - About 2001
/// See the files "readme.txt" and "license.txt" for general and
/// copyright info.
//==================================================================

import java.awt.*;
import java.applet.*;
import java.awt.image.*;

//==================================================================
//==================================================================
public class image
{
public int			_w, _h;
int					_depth;
public int			_pixels[];
public byte			_pixels_8[];
//MemoryImageSource	_mis;
//Image				_mis_img;

//==================================================================
public image( int pixels[], int w, int h )
{
	_w = w;
	_h = h;
	_depth = 24;
	_pixels = pixels;
	_pixels_8 = null;
}

//==================================================================
public image( byte pixels_8[], int w, int h )
{
	_w = w;
	_h = h;
	_depth = 8;
	_pixels = null;
	_pixels_8 = pixels_8;
}

//==================================================================
public image( Image jimg, int force_depth, Applet a )
{
int	x, y, i;

	_w = jimg.getWidth(a);
	_h = jimg.getHeight(a);
	_depth = force_depth;

	_pixels = new int [ _w * _h ];

	PixelGrabber pg = new PixelGrabber( jimg, 0, 0, _w, _h, _pixels, 0, _w );
	try { pg.grabPixels(); } catch (InterruptedException e) {return;}

	if ( force_depth == 8 )
	{
		_pixels_8 = new byte [ _w * _h ];

		i = 0;
		for (y=0; y < _h; ++y)
			for (x=0; x < _w; ++x, ++i)
				_pixels_8[i] = (byte)(_pixels[i] & 0x000000ff);
			
		_pixels = null;	// flush this
	}	

/*	DirectColorModel dm;
	dm = new DirectColorModel( 24, 0x00ff0000, 0x0000ff00, 0x000000ff );
	_mis = new MemoryImageSource( _w, _h, dm, _pixels, 0, _w );
	_mis.setAnimated( true );
	_mis_img = a.createImage( _mis );
	_mis.newPixels();
*/
}

//==================================================================
public image( Image jimg, Applet a )
{
	this( jimg, 24, a );
}

}