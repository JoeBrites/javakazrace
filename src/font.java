//==================================================================
/// Created by Davide Pasca - About 2001
/// See the files "readme.txt" and "license.txt" for general and
/// copyright info.
//==================================================================

import java.awt.*;
import java.applet.*;

//==================================================================
//==================================================================
public class font
{
int		_fw, _fh;
int		_src_coords[][];
image	_img;
image	_img_out;


//==================================================================
public font( String fname, int fw, int fh, Applet app )
{
Image	jimg;
byte	pixels_out[];
int		i;
int		w, h;

	_fw = fw;
	_fh = fh;

	try
	{
	MediaTracker mt;

		jimg = app.getImage( app.getCodeBase(), fname );
		mt = new MediaTracker( app );
		mt.addImage( jimg, 1 );
		mt.waitForAll();
	}
	catch ( Exception e )
	{
		return;
	}

	_img = new image( jimg, 8, app );
	w = _img._w;
	h = _img._h;
	
	pixels_out = new byte [ w * h ];

int		x, y, xx, yy;
byte	col;

	i = 1+_img._w;
	for (y=1; y < h-1; ++y, ++i)
		for (x=1; x < w-1; ++x, ++i)
		{
			col = _img._pixels_8[i];
			if ( col != 0 )
				for (yy=-w; yy <= w; yy += w)
					for (xx=-1; xx <= 1; ++xx)
						pixels_out[i+xx+yy] = col;
		}
		
	_img_out = new image( pixels_out, w, h );
	
	_src_coords = new int [2][256];
	
int	sx, sy;

	sx = 0;
	sy = 0;
	for (i=32; i < 32+32*3; ++i)
	{
		if ( sx + fw+1 > _img._w )
		{
			sx = 0;
			sy += fh+1;
			if ( sy + fh > _img._h )
				break;
		}

		_src_coords[0][i] = sx;
		_src_coords[1][i] = sy;

		sx += fw+1;
	}
}

//==================================================================
void text_draw( graphics g, int x, int y, int align_x, int align_y,
				int col1, int col2, int bgcol, String str )
{
int		i, len;
int		ch;
int		sx, sy;
int		str_wd;

	bgcol |= 0xff000000;

	len = str.length();

	str_wd = len * (_fw-4);

	if ( align_x == 0 )	x -= str_wd/2;	else
	if ( align_x > 0 )	x -= str_wd;
	
	if ( align_y == 0 )	y -= _fh/2;	else
	if ( align_y > 0 )	y -= _fh;
	
	for (i=0; i < len; ++i)
	{
		ch = str.charAt(i);
		if ( ch >  32 && ch <= 32+32*3 )
		{
			sx = _src_coords[0][ch];
			sy = _src_coords[1][ch];
			
			g.image_draw( _img_out, sx, sy, _fw, _fh, x, y, bgcol );
			g.image_draw( _img, sx, sy, _fw, _fh, x, y, col1, col2 );
		}
		x += _fw-4;
	}
}

}
