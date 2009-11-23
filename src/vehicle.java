//==================================================================
/// Created by Davide Pasca - About 2001
/// See the files "readme.txt" and "license.txt" for general and
/// copyright info.
//==================================================================

//==================================================================
public class vehicle
{
sprite  _sprt;
int     _start_pos, _pos, _speed;
vehicle	_next;
int		_x;
boolean	_check_collision;

//==================================================================
public vehicle( sprite sprt, int start_x, int start_pos, int speed, boolean check_collision )
{
    _sprt = sprt;
    _start_pos = start_pos;
    _pos = _start_pos;
    _x = start_x;
    _speed = speed;
    _check_collision = check_collision;
}

//==================================================================
public vehicle( sprite sprt, int start_x, int start_pos, int speed )
{
    this( sprt, start_x, start_pos, speed, true );
}

//==================================================================
public void animate( int far_pos )
{
	if ( far_pos >= _start_pos )
		_pos += _speed;
	//else
	//	_pos = pos;
}

//==================================================================
public boolean is_drawable()
{
	if ( _pos >= _start_pos )
		return true;
		
	return false;
}

//==================================================================
public void draw( graphics g, int frame, int x, int y, float scale )
{
	_sprt.draw( g, frame * 10, x, y, scale );
}

}
