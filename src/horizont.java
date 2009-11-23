//==================================================================
/// Created by Davide Pasca - About 2001
/// See the files "readme.txt" and "license.txt" for general and
/// copyright info.
//==================================================================

import java.applet.*;

//==================================================================
//==================================================================
public class horizont
{
int		_bot_r, _bot_g, _bot_b;
int		_top1_col, _bot_col;
image	_top_img;
float	_x;
int		_y, _w, _h;
Applet	_app;
sprite	_sky_sptr;

//==================================================================
public horizont( int w, int h, Applet app )
{
	_app = app;
	_y = h/2;
	_w = w;
	_h = h;
	colors_set( 10, 20, 220,
				200, 200, 220,
				200, 100, 10 );
}

//==================================================================
void to15bits( image img )
{
int	w, h, x, y;
int	i, col, r, g, b, a;
int	er, eg, eb;
float	t;

	w = img._w;
	h = img._h;

	i = 0;
	for (y=0; y < h; ++y)
		for (x=0; x < w; ++x, ++i)
		{
			col = img._pixels[i];
			a = (col & 0xff000000) >> 24;
			r = (col & 0x00ff0000) >> 16;
			g = (col & 0x0000ff00) >>  8;
			b = (col & 0x000000ff) >>  0;
			
			er = r & 7; r &= ~7;
			eg = g & 7; g &= ~7;
			eb = b & 7; b &= ~7;
			
			t = (float)Math.random()*7.0f;
			if ( er <= t )  er = 0; else er = 8; 
			
			//t = (float)Math.random()*3.5f;
			if ( eg <= t )  eg = 0; else eg = 8; 
			
			//t = (float)Math.random()*3.5f;
			if ( eb <= t )  eb = 0; else eb = 8; 
			
			//t = er * (float)Math.random() / 8;
			r = r + er;
			g = g + eg;
			b = b + eb;
			
			//r &= ~7;
			//g &= ~7;
			//b &= ~7;
			img._pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
		}
}

//==================================================================
public void paint( graphics g )
{
int	x, y, w, h;

	if ( true )//_sky_sptr != null )
	{
		x = (int)_x;
		w = _sky_sptr.width_get( 0 );

		if ( _y >= _sky_sptr._images[0]._h )
			y = _sky_sptr._images[0]._h-1;
		else
			y = _y;

		x %= w;
		if ( x + w < _w )
			_sky_sptr.draw( g, 0, x+w, y, 1, 1 );
		else
		if ( x > 0 )
			_sky_sptr.draw( g, 0, x-w, y, 1, 1 );
			
		_sky_sptr.draw( g, 0, x, y, 1, 1 );
		
		//h = _y - _sky_sptr._images[0]._h;
		//if ( h > 0 )
		//	g.rect_fill( 0, 0, _w, h, _top1_col );
		
		//g.drawImage( _sky_sptr., 0, _y, _app );
	}
	else
	{
		
		//g.setColor( _top1_col );
		//g.fillRect( 0, 0, _w, _y );

		for (x=0; x < _w; x += 64)
			_sky_sptr.draw( g, 0, x, _y, 1.0f, sprite.NT_FLG );
			//g.image_draw( _sky_sptr, x, _y - _h/2 );
		
		h = _y - _sky_sptr._images[0]._h;
		if ( h > 0 )
			g.rect_fill( 0, 0, _w, h, _top1_col );
	}
	
	//g.rect_fill( 0, _y, _w, _h, _bot_col );
}

//==================================================================
public void x_set( float x )
{
	_x = x;
}

//==================================================================
public void y_set( int y )
{
	_y = y;
}

//==================================================================
void create_sky( int r1, int g1, int b1, int r2, int g2, int b2 )
{
graphics	gfx;
int			y, h;
float		coe, coe1, max;
int			r, g, b;

	h = _h*2/3;
	gfx = new graphics( 64, h, _app );
	_top_img = gfx._img;//  _app.createImage( 64, h );
	//gfx = _top_img.getGraphics();
	
	//r1 = b1 = g1;
	//r2 = b2 = g2;
	
	max = h-1;
	max = max/4 + max*max;
	for (y=h-1; y >= 0; --y)
	{
		//coe = (float)y / h;
		coe = (y/4 + y * y) / max;
		coe1 = 1.0f - coe;
		
		r = (int)(r1 * coe1 + r2 * coe);
		g = (int)(g1 * coe1 + g2 * coe);
		b = (int)(b1 * coe1 + b2 * coe);
		
		//gfx.setColor( new Color( r, g, b ) );
		//gfx.drawLine( 0, y, 63, y );
		gfx.line_h2(0, 64, y, color.make( r, g, b ) );
	}
	
	to15bits( _top_img );

	_sky_sptr = new sprite( _top_img, sprite.LEFT, sprite.BOTTOM, _app );
	_sky_sptr.shadow_off();
}

//==================================================================
public void colors_set( int top1_r, int top1_g, int top1_b,
						int top2_r, int top2_g, int top2_b,
				        int bot_r, int bot_g, int bot_b )
{
	_bot_r = bot_r; _bot_g = bot_g; _bot_b = bot_b;
	
	_top1_col = color.make( top1_r, top1_g, top1_b );
	_bot_col = color.make( _bot_r, _bot_g, _bot_b );
	
	create_sky( top1_r, top1_g, top1_b, top2_r, top2_g, top2_b );
}

//==================================================================
public void sky_picture_set( String picname )
{
	_sky_sptr = new sprite( picname, sprite.LEFT, sprite.BOTTOM, _app );
	_sky_sptr.shadow_off();
}

}
