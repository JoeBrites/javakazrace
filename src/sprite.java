//==================================================================
/// Created by Davide Pasca - About 2001
/// See the files "readme.txt" and "license.txt" for general and
/// copyright info.
//==================================================================

import java.awt.*;
import java.applet.*;

//==================================================================
//==================================================================
public class sprite
{
static int	LEFT=0, RIGHT=1, TOP=2, BOTTOM=3, MIDDLE=4;
image		_images[];
image		_images_flx[];
int			_n_frames;
int			_last_frame;
int			_sx, _sy, _x, _y, _z;
int			_dx, _dy;
int			_align_x, _align_y;
Applet		_app;
float		_base_scale;
boolean		_has_shadow;

final static int	NT_FLG=1, FLIP_X_FLG=2, TRANS100_100_FLG=4;

//==================================================================
public sprite( String fname, int align_x, int align_y, float base_scale, Applet app )
{
	_align_x = align_x;
	_align_y = align_y;
	_base_scale = base_scale;
	_app = app;
	_has_shadow = true;
	_images = new image[8];
	
	add_frame( fname );
}

//==================================================================
public sprite( image img, int align_x, int align_y, float base_scale, Applet app )
{
	_align_x = align_x;
	_align_y = align_y;
	_base_scale = base_scale;
	_app = app;
	_has_shadow = true;
	_images = new image[8];
	
	add_frame( img );
}

//==================================================================
public sprite( String fname, int align_x, int align_y, Applet app )
{
	this( fname, align_x, align_y, 1.0f, app );
}

//==================================================================
public sprite( image img, int align_x, int align_y, Applet app )
{
	this( img, align_x, align_y, 1.0f, app );
}

//==================================================================
void create_flipped_x()
{
int		x, y, i, yi;
image	src, des;
int		npixels[];

	if ( _images_flx == null )
		_images_flx = new image[8];
	
	for (i=0; i < _n_frames; ++i)
	{
		src = _images[i];
		npixels = new int [src._w * src._h];
		
		for (y=0; y < src._h; ++y)
		{
			yi = y * src._w;
			for (x=0; x < src._w; ++x)
				npixels[ yi + x ] = src._pixels[ yi + src._w-1 - x ];
		}
		_images_flx[i] = new image( npixels, src._w, src._h );
	}
}

//==================================================================
public void shadow_off()
{
	_has_shadow = false;
}

//==================================================================
public int add_frame( image img )
{
	_images[_n_frames++] = img;

	return 0;
}

//==================================================================
public int add_frame( String fname )
{
Image	jimg;

	try
	{
	MediaTracker mt;

		jimg = _app.getImage( _app.getCodeBase(), fname );
		mt = new MediaTracker( _app );
		mt.addImage( jimg, 1 );
		mt.waitForAll();
	}
	catch ( Exception e )
	{
		return -1;
	}

	add_frame( new image( jimg, _app ) );

	return 0;
}

//==================================================================
public int width_get( int frame )
{
	return _images[frame]._w;
}

//==================================================================
public void draw( graphics g, int frame, int x, int y, float scale )
{
	draw( g, frame, x, y, scale, 0 );
}

//==================================================================
void draw_shadow( graphics g, image img, int x, int y, int w, int h, float scale )
{
int	yb;
int	i, ii;
int	off;
int	sh_he;

	sh_he = (int)(14 * scale);
	
	yb = y + h;
	for (i=0; i < sh_he; ++i)
	{
		ii = i - sh_he/2;
		y = yb + ii;

		if ( ii > 0 )
			off = ii / 2 + 150 * (ii * ii) / sh_he / 100;
		else
			off = -ii / 2 + 100 * (ii * ii) / sh_he / 100;
		
		
		g.line_h_dk50( x+off, x+w-off, y );
	}
/*
int	xs, ws;

	sh_he = (int)(12 * scale);
	ws = w * 18 / 20;
	xs = x + (w-ws) / 2;
	if ( sh_he > 1 )
	{
	for (i=0; i < sh_he; ++i)
	{
		ii = i - (sh_he+1)/2;
		y = yb + ii - (sh_he+3)/4;

		if ( ii > 0 )
			off = ii / 2 + 150 * (ii * ii) / sh_he / 100;
		else
			off = -ii / 2 + 100 * (ii * ii) / sh_he / 100;

		g.line_h_dk25( xs+off, xs+ws-off, y );
	}
	}
*/
}


//==================================================================
public void draw( graphics g, int frame,
					int x1a, int x2a, int ya,
					int x1b, int x2b, int yb, int flags )
{
image	img;

	_last_frame = frame;
	if ( frame < 0 )
		frame = -frame;

	frame /= 10;
	if ( (flags & FLIP_X_FLG) != 0 )
	{
		if ( _images_flx == null )
			create_flipped_x();

		img = _images_flx[frame];
	}
	else
		img = _images[frame];

	g.image_draw_trap( img, x1a, x2a, ya, x1b, x2b, yb );
}

//==================================================================
public void draw( graphics g, int frame, int x, int y, float scale, int flags )
{
image	img;
int		w, h;
boolean	scaling;

	if ( _base_scale != 1.0f )
		scale *= _base_scale;

	if ( scale != 1.0f )
		scaling = true;
	else
		scaling = false;

	_last_frame = frame;
	if ( frame < 0 )
		frame = -frame;

	frame /= 10;
	if ( (flags & FLIP_X_FLG) != 0 )
	{
		if ( _images_flx == null )
			create_flipped_x();

		img = _images_flx[frame];
	}
	else
		img = _images[frame];

	w = (int)(img._w * scale);
	h = (int)(img._h * scale);

	if ( _align_x == RIGHT )
		x -= w;
	else
	if ( _align_x == MIDDLE )
		x -= w/2;
		
	if ( _align_y == BOTTOM )
		y -= h;
	else
	if ( _align_y == MIDDLE )
		y -= h/2;


	if ( _has_shadow )
		draw_shadow( g, img, x, y, w, h, scale );

	if ( (flags & 1) != 0 )
		g.image_draw_nt( img, x, y );
	else
	{
		if ( (flags & TRANS100_100_FLG) != 0 )
			g.image_draw_100_100( img, x, y, w, h );
		else
		{
			if ( scaling )
				g.image_draw( img, x, y, w, h );
			else
				g.image_draw( img, x, y );
		}
	}
}
}
