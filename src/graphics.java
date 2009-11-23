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
public class graphics
{
public int	_w, _h;
Applet		_app;
Image		_off_img;
Graphics	_off_gfx;

MemoryImageSource	_mis;
Image				_mis_img;
Graphics			_mis_gfx;
int					_pixels[];
image				_img;

//==================================================================
public graphics( int w, int h, Applet app )
{
	_w = w;
	_h = h;

	_app = app;
	_pixels = new int [ w * h ];
	_img = new image( _pixels, w, h );
}

//==================================================================
void create_mis()
{
	if ( _mis == null || _mis_img == null )
	{
		_off_img = _app.createImage( _w, _h );
		_off_gfx = _off_img.getGraphics();

		_mis = new MemoryImageSource( _w, _h,
						new DirectColorModel( 24, 0x00ff0000, 0x0000ff00, 0x000000ff ),
						_pixels, 0, _w );
		//_mis = new MemoryImageSource( w, h, _pixels, 0, w );
		_mis.setAnimated( true );
		_mis_img = _app.createImage( _mis );
	}
}

//==================================================================
public void fill( int color )
{
int	i,n;

	n = _w * _h;
	for (i=0; i < n; ++i)
		_pixels[i] = color;
}
//==================================================================
public int[] get_pixels()
{
	return _pixels;
}
//==================================================================
public void update_area( int x, int y, int w, int h )
{
	create_mis();
	_mis.newPixels( x, y, w, h );
}
//==================================================================
public void update_area()
{
	create_mis();
	_mis.newPixels( 0, 0, _w, _h );
}
//==================================================================
public void redraw()
{
	create_mis();
	_off_gfx.drawImage( _mis_img, 0, 0, _app );
	_app.getGraphics().drawImage( _off_img, 0, 0, _app );
}

//==================================================================
public void rect_fill( int x, int y, int w, int h, int col )
{
int	t;
int	i, ii;
int	x2, y2;

	x2 = x + w-1;
	y2 = y + h-1;

	if ( y >= _h || y2 < 0 || x >= _w || x2 < 0 )
		return;
		
	if ( x < 0 ) x = 0;
	if ( y < 0 ) y = 0;
	if ( x2 >= _w ) x2 = _w-1;
	if ( y2 >= _h ) y2 = _h-1;

	i = y * _w + x;

	h = y2 - y + 1;

int	i0;

	i0 = i;
	ii = i;
	w = x2 - x;
	while (w-- >= 0)
		_pixels[ii++] = col;
				
	w = x2 - x;
	for (--h; h > 0; --h)
	{
		i += _w;
		System.arraycopy( _pixels, i0, _pixels, i, w );
	}
	
	/*
	for (; h > 0; --h)
	{
		ii = i;
		w = x2 - x;
		while (w-- >= 0)
			_pixels[ii++] = col;
			
		i += _w;
	}*/
}

//==================================================================
public void line_h_dk50( int x1, int x2, int y )
{
int	t;
int	i;

	if ( y < 0 || y >= _h )
		return;

	if ( x2 < x1 )
	{
		t = x1;
		x1 = x2;
		x2 = t;
	}

	if ( x1 < 0 )	x1 = 0;
	if ( x2 >= _w )	x2 = _w-1;

	i = y * _w + x1;
	x2 -= x1;
	while (x2-- >= 0)
	{
		_pixels[i] = (_pixels[i] & 0xfefefefe) >> 1;
		++i;
	}
}

//==================================================================
public void line_h_dk25( int x1, int x2, int y )
{
int	t;
int	i;

	if ( y < 0 || y >= _h )
		return;

	if ( x2 < x1 )
	{
		t = x1;
		x1 = x2;
		x2 = t;
	}

	if ( x1 < 0 )	x1 = 0;
	if ( x2 >= _w )	x2 = _w-1;

	i = y * _w + x1;
	x2 -= x1;
	while (x2-- >= 0)
	{
		_pixels[i] = ((_pixels[i] & 0xfcfcfcfc) >> 2) + ((_pixels[i] & 0xfefefefe) >> 1);
		++i;
	}
}

//==================================================================
public void line_h( int x1, int x2, int y, int col )
{
int	t;
int	i;

	if ( y < 0 || y >= _h )
		return;

	if ( x2 < x1 )
	{
		t = x1;
		x1 = x2;
		x2 = t;
	}

	if ( x1 < 0 )	x1 = 0;
	if ( x2 >= _w )	x2 = _w-1;

	i = y * _w + x1;
	x2 -= x1;
	while (x2-- >= 0)
		_pixels[i++] = col;
}

//==================================================================
public void line_h2( int x1, int w, int y, int col )
{
int	t;
int	i;
int	x2;

	if ( y < 0 || y >= _h )
		return;

	x2 = x1 + w-1;
	if ( x2 < x1 )
	{
		t = x1;
		x1 = x2;
		x2 = t;
	}

	if ( x1 < 0 )	x1 = 0;
	if ( x2 >= _w )	x2 = _w-1;

	i = y * _w + x1;
	x2 -= x1;
	while (x2-- >= 0)
		_pixels[i++] = col;
}

//==================================================================
public void image_draw( image img, int sx, int sy, int sw, int sh, int x, int y, int col1, int col2 )
{
int	iox, ioy, ix, iy;
int	iw, ih, icw, ich;
int	des[], src[];
int	sstride;

	sstride = img._w;

	iox = sx;
	ioy = sy;
	iw = sw;
	ih = sh;

	if ( x >= _w || y >= _h )
		return;
	if ( x+iw <= 0 || y+ih <= 0 )
		return;

	if ( x+iw > _w )	icw = _w-x; else icw = iw;
	if ( y+ih > _h )	ich = _h-y; else ich = ih;

	if ( x < 0 )	{ iox -= x; icw += x; x = 0; }
	if ( y < 0 )	{ ioy -= y; ich += y; y = 0; }

	des = _pixels;
	src = img._pixels;

	int	d_i, d_ii;
	int	s_i, s_ii;
	s_i = ioy * sstride;
	d_i = y * _w;

	if ( img._depth == 8 )
	{
	byte	src8[];
	
		src8 = img._pixels_8;
		
		if ( col1 != 0 )
		{
			if ( col2 != 0 )
			{
			int	r1, g1, b1;
			int	r2, g2, b2;
			int	dr, dg, db;
			int	ooh;

				ooh = 65536 / sh;
				r1 = (col1 & 0x00ff0000) >> 16;
				g1 = (col1 & 0x0000ff00) >>  8;
				b1 = (col1 & 0x000000ff) >>  0;
				r2 = (col2 & 0x00ff0000) >> 16;
				g2 = (col2 & 0x0000ff00) >>  8;
				b2 = (col2 & 0x000000ff) >>  0;
				
				dr = (r2 - r1) * ooh;
				dg = (g2 - g1) * ooh;
				db = (b2 - b1) * ooh;
				
				r1 <<= 16;
				g1 <<= 16;
				b1 <<= 16;
			
				for (iy=ich; iy > 0; --iy)
				{
				int	row_col;
				
					s_ii = s_i + iox;
					d_ii = d_i + x;
					s_i += sstride;
					d_i += _w;
					
					row_col = ( r1      & 0x00ff0000) | 
							  ((g1>> 8) & 0x0000ff00) |
							  ((b1>>16) & 0x000000ff);
							  
					for (ix=icw; ix > 0; --ix)
					{
						if ( src8[s_ii++] != 0 )
							des[d_ii] = row_col;

						++d_ii;
					}
					r1 += dr;
					g1 += dg;
					b1 += db;
				}
			}
			else
			{
				for (iy=ich; iy > 0; --iy)
				{
					s_ii = s_i + iox;
					d_ii = d_i + x;
					s_i += sstride;
					d_i += _w;
					for (ix=icw; ix > 0; --ix)
					{
					int	c;

						c = src8[s_ii++] & 0x000000ff;
						if ( c != 0 )
							des[d_ii] = col1;

						++d_ii;
					}
				}
			}
		}
		else
		{
			for (iy=ich; iy > 0; --iy)
			{
				s_ii = s_i + iox;
				d_ii = d_i + x;
				s_i += sstride;
				d_i += _w;
				for (ix=icw; ix > 0; --ix)
				{
				int	c;

					c = src8[s_ii++] & 0x000000ff;
					if ( c != 0 )
						des[d_ii] = c | (c << 8) | (c << 16);

					++d_ii;
				}
			}
		}
	}
	else
		for (iy=ich; iy > 0; --iy)
		{
			s_ii = s_i + iox;
			d_ii = d_i + x;
			s_i += sstride;
			d_i += _w;
			for (ix=icw; ix > 0; --ix)
			{
			int	c;

				c = src[s_ii++];
				if ( (c & 0xff000000) == 0xff000000 )
					des[d_ii] = c;

				++d_ii;
			}
		}
}

//==================================================================
public void image_draw( image img, int x, int y )
{
	image_draw( img, 0,0, img._w,img._h, x, y, 0 );
}

//==================================================================
public void image_draw( image img, int sx, int sy, int sw, int sh, int x, int y )
{
	image_draw( img, sx, sy, sw, sh, x, y, 0 );
}

//==================================================================
public void image_draw( image img, int sx, int sy, int sw, int sh, int x, int y, int col )
{
	image_draw( img, sx, sy, sw, sh, x, y, col, 0 );
}

//==================================================================
public void image_draw( image img, int x, int y, int nw, int nh )
{
int	w, h;
int	des[], src[], scol;
int	sdx, sdy;
int	si, di, dii, j;
int	x2, y2;
int	sx, sy;

	x2 = x + nw-1;
	y2 = y + nh-1;
	if ( x >= _w || y >= _h || x2 < 0 || y2 < 0 || nw == 0 || nh == 0 )
		return;

	w = img._w;
	h = img._h;

	sdx = (w << 10) / nw;
	sdy = (h << 10) / nh;

int	sx_off, sy_off;

	if ( x < 0 )
	{
		sx_off = -x * sdx;
		x = 0;
	}
	else
		sx_off = 0;
	
	if ( y < 0 )
	{
		sy_off = -y * sdy;
		y = 0;
	}
	else
		sy_off = 0;
	
	if ( x2 >= _w ) x2 = _w-1;
	if ( y2 >= _h ) y2 = _h-1;

	src = img._pixels;
	des = _pixels;

	di = x + _w * y;
	sy = sy_off;
	for (; y <= y2; ++y)
	{
		si = (sy >> 10) * w;
		sx = sx_off;
		dii = di;
		
		for (j = x2-x; j >= 0; --j, ++dii)
		{
			scol = src[ (sx >> 10) + si ]; 
			if ( (scol & 0xff000000) != 0 )
				des[dii] = scol;
					
			sx += sdx;
		}
		di += _w;
		sy += sdy;
	}
}

//==================================================================
public void image_draw_trap( image img, int x1a, int x2a, int ya,
										int x1b, int x2b, int yb )
{
int	w, h;
int	des[], src[], scol;
int	sdx, sdy;
int	si, di, dii, j;
int	sx, sy;

int	min_x, max_x;
int	delta_l, delta_r;
int	dy, t;
int	nw, nh;

	ya >>= 10;
	yb >>= 10;

	if ( ya >= _h || yb < 0 )
		return;

	dy = yb - ya + 1;
	if ( dy <= 0 )
		return;
	nh = dy;

	if ( x1a > x2a ){ t = x1a; x1a = x2a; x2a = t; }
	if ( x1b > x2b ){ t = x1b; x1b = x2b; x2b = t; }

	if ( x1b < x1a ) min_x = x1b; else min_x = x1a;
	if ( x2b > x2a ) max_x = x2b; else max_x = x2a;
	
	if ( (min_x>>10) >= _w || max_x < 0 )
		return;

	// speedup
	if ( dy > 1 )
	{
		delta_l = (x1b - x1a) / dy;
		delta_r = (x2b - x2a) / dy;
	}
	else
		delta_l = delta_r = 0;

	w = img._w;
	h = img._h;

	sdy = (h << 10) / nh;

int	sx_off, sy_off;
	
	if ( ya < 0 )
	{
		sy_off = -ya * sdy;
		ya = 0;
	}
	else
		sy_off = 0;
	
	if ( yb >= _h ) yb = _h-1;

	src = img._pixels;
	des = _pixels;

	di = _w * ya;
	sy = sy_off;
	for (; ya < yb; ++ya)
	{
		si = (sy >> 10) * w;
		
		nw = x2a - x1a >> 10;
		if ( nw > 0 )
		{
		int	x, x2;

			x = x1a >> 10;
			x2 = x2a >> 10;

			sdx = ((w-1) << 10) / nw;
			if ( x < 0 )
			{
				sx_off = -x * sdx;
				x = 0;
			}
			else
				sx_off = 0;
			if ( x2 >= _w ) x2 = _w-1;
			
			sx = sx_off;
			dii = di + x;
			
			for (j = x2-x; j > 0; --j, ++dii)
			{
				//scol = src[ (sx >> 10) + si ];
				des[dii] = src[ (sx >> 10) + si ]; 
				//if ( (scol & 0xff000000) != 0 )
				//	des[dii] = scol;
						
				sx += sdx;
			}
		}

		di += _w;
		sy += sdy;

		x1a += delta_l;
		x2a += delta_r;
	}
}


//==================================================================
public void image_draw_100_100( image img, int x, int y, int nw, int nh )
{
int	w, h;
int	des[], src[], scol;
int	sdx, sdy;
int	si, di, dii, j;
int	x2, y2;
int	sx, sy;
boolean	flip_x;

	if ( nw < 0 )
	{
		nw = -nw;
		flip_x = true;
	}
	else
		flip_x = false;

	x2 = x + nw-1;
	y2 = y + nh-1;
	if ( x >= _w || y >= _h || x2 < 0 || y2 < 0 || nw == 0 || nh == 0 )
		return;

	w = img._w;
	h = img._h;

	sdx = (w << 10) / nw;
	sdy = (h << 10) / nh;

int	sx_off, sy_off;

	if ( x < 0 )
	{
		sx_off = -x * sdx;
		x = 0;
	}
	else
		sx_off = 0;
	
	if ( y < 0 )
	{
		sy_off = -y * sdy;
		y = 0;
	}
	else
		sy_off = 0;
	
	if ( x2 >= _w ) x2 = _w-1;
	if ( y2 >= _h ) y2 = _h-1;

	src = img._pixels;
	des = _pixels;

	if ( flip_x )
		sdx = -sdx;

	di = x + _w * y;
	sy = sy_off;
	for (; y <= y2; ++y)
	{
	int	dcol, ncol, t;
	
		si = (sy >> 10) * w;
		sx = sx_off;
		dii = di;
		
		if ( flip_x )
		{
			sx = -sx;
			si += w-1;
		}
		for (j = x2-x; j >= 0; --j, ++dii)
		{
			scol = src[ (sx >> 10) + si ]; 
			//if ( (scol & 0xff000000) != 0 )
			{
				dcol = des[dii];
				
				t = (dcol & 0x00ff0000) + (scol & 0x00ff0000);
				ncol = t > 0x00ff0000 ? 0x00ff0000 : t;

				t = (dcol & 0x0000ff00) + (scol & 0x0000ff00);
				ncol |= t > 0x0000ff00 ? 0x0000ff00 : t;

				t = (dcol & 0x000000ff) + (scol & 0x000000ff);
				ncol |= t > 0x000000ff ? 0x000000ff : t;
				
				des[dii] = ncol;
			}
					
			sx += sdx;
		}
		di += _w;
		sy += sdy;
	}
}

//==================================================================
public void image_draw_nt( image img, int x, int y )
{
int	iox, ioy, ix, iy;
int	iw, ih, icw, ich;
int	des[], src[];

	iw = img._w;
	ih = img._h;

	if ( x >= _w || y >= _h )
		return;
	if ( x+iw <= 0 || y+ih <= 0 )
		return;

	if ( x+iw > _w )	icw = _w-x; else icw = iw;
	if ( y+ih > _h )	ich = _h-y; else ich = ih;

	if ( x < 0 )	{ iox = -x; icw += x; x = 0; }
	else        	iox = 0;

	if ( y < 0 )	{ ioy = -y; ich += y; y = 0; }
	else        	ioy = 0;

	des = _pixels;
	src = img._pixels;

	int	d_i, d_ii;
	int	s_i, s_ii;
	s_i = ioy * iw;
	d_i = y * _w;
	for (iy=ich; iy > 0; --iy)
	{
		s_ii = s_i + iox;
		d_ii = d_i + x;
		s_i += iw;
		d_i += _w;
		System.arraycopy(src, s_ii, des, d_ii, icw);
		//for (ix=icw; ix > 0; --ix)
		//	des[d_ii++] = src[s_ii++];
	}
}
//==================================================================
public void image_draw_50_50( image img, int x, int y )
{
int	iox, ioy, ix, iy;
int	iw, ih, icw, ich;
int	des[], src[];

	iw = img._w;
	ih = img._h;

	if ( x >= _w || y >= _h )
		return;
	if ( x+iw <= 0 || y+ih <= 0 )
		return;

	if ( x+iw > _w )	icw = _w-x; else icw = iw;
	if ( y+ih > _h )	ich = _h-y; else ich = ih;

	if ( x < 0 )	{ iox = -x; icw += x; x = 0; }
	else        	iox = 0;

	if ( y < 0 )	{ ioy = -y; ich += y; y = 0; }
	else        	ioy = 0;

	des = _pixels;
	src = img._pixels;

	int	d_i, d_ii;
	int	s_i, s_ii;
	s_i = ioy * iw;
	d_i = y * _w;
	for (iy=ich; iy > 0; --iy)
	{
		s_ii = s_i + iox;
		d_ii = d_i + x;
		s_i += iw;
		d_i += _w;
		for (ix=icw; ix > 0; --ix)
		{
		int	sc, dc;

			sc = src[s_ii++];
			if ( (sc & 0xff000000) != 0 )
			{
				dc = des[d_ii];
				sc &= 0x00fefefe;
				dc &= 0x00fefefe;

				des[d_ii] = sc + dc >> 1;
			}

			++d_ii;
		}
	}
}
}
