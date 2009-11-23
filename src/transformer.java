//==================================================================
/// Created by Davide Pasca - About 2001
/// See the files "readme.txt" and "license.txt" for general and
/// copyright info.
//==================================================================

//==================================================================
//==================================================================
public class transformer
{
float   _screen_d;
float   _foff_y;
int		_ioff_y;
float	_hf_wd, _hf_he;
int		_hi_wd, _hi_he;
float	_zz;
int		_clip_flags;

//==================================================================
void h_seti( int he )
{
    _hf_he = he * 0.5f;
    _hi_he = he << 9;
}

//==================================================================
public transformer( int wd, int he, int screen_d )
{
    _screen_d = screen_d;
    
    _hf_wd = wd * 0.5f;
    _hf_he = he * 0.5f;

    _hi_wd = wd << 9;
    _hi_he = he << 9;
}

//==================================================================
void offy_seti( int ioy )
{
	_ioff_y = ioy;
	_foff_y = ioy * (1.0f/1024);
}

//==================================================================
void offy_setf( float foy )
{
	_ioff_y = (int)(foy * 1024);
	_foff_y = foy;
}

//==================================================================
void zz_setf( float zz )
{
	_zz = zz;
}


//==================================================================
float xform_zf( float z )
{
	if ( z <= 0 )
	{
		_zz = 0.01f;
		_clip_flags = 1;
	}
	else
	{
		_zz = (float)_screen_d / z;
		_clip_flags = 0;
	}
		
	return _zz;
}

//==================================================================
float xform_zi( int z )
{
	return xform_zf( (float)z );
}

//==================================================================
int xform_xf( float x )
{
	return (int)(_hf_wd + x * _zz);
}

//==================================================================
int xform_xi( int x )
{
	return _hi_wd + (int)(x * _zz);
}

//==================================================================
int xform_yf( float y )
{
	y += _foff_y;
	return (int)(_hf_he - y * _zz);
}
//==================================================================
int xform_yi( int y )
{
	y += _ioff_y;
	return _hi_he - (int)(y * _zz);
}

}
