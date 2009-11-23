//==================================================================
/// Created by Davide Pasca - About 2001
/// See the files "readme.txt" and "license.txt" for general and
/// copyright info.
//==================================================================

//==================================================================
public class color
{
static int make( int r, int g, int b, int a )
{
    if ( r < 0 ) r = 0; else if ( r > 255 ) r = 255;
    if ( g < 0 ) g = 0; else if ( g > 255 ) g = 255;
    if ( b < 0 ) b = 0; else if ( b > 255 ) b = 255;
    if ( a < 0 ) a = 0; else if ( a > 255 ) a = 255;
    
    return (a << 24) | (r << 16) | (g << 8) | b;
}

//==================================================================
static int make( int r, int g, int b )
{
    if ( r < 0 ) r = 0; else if ( r > 255 ) r = 255;
    if ( g < 0 ) g = 0; else if ( g > 255 ) g = 255;
    if ( b < 0 ) b = 0; else if ( b > 255 ) b = 255;

    return (0xff << 24) | (r << 16) | (g << 8) | b;
}

//==================================================================
static int make_inpt( int r1, int g1, int b1,
					  int r2, int g2, int b2, float t )
{
float	tt = 1 - t;
int		r, g, b;

	r = (int)(r1 * tt + r2 * t);
	g = (int)(g1 * tt + g2 * t);
	b = (int)(b1 * tt + b2 * t);
	
    return (0xff << 24) | (r << 16) | (g << 8) | b;
}


//==================================================================
static int make_inpt( int col1, int col2, float t )
{
float	tt = 1 - t;
int		r, g, b;

	r = (int)(((col1 >> 16) & 0xff) * tt + ((col2 >> 16) & 0xff) * t);
	g = (int)(((col1 >>  8) & 0xff) * tt + ((col2 >>  8) & 0xff) * t);
	b = (int)(((col1 >>  0) & 0xff) * tt + ((col2 >>  0) & 0xff) * t);
	
    return (0xff << 24) | (r << 16) | (g << 8) | b;
}
}
