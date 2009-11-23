//==================================================================
/// Created by Davide Pasca - About 2001
/// See the files "readme.txt" and "license.txt" for general and
/// copyright info.
//==================================================================

import java.awt.*;

//==================================================================
//==================================================================
public class printout
{
String	_list[];
int		_list_cnt;
Color	_c1;
int		_off_x = 6, _off_y = 0;

//==================================================================
public printout()
{
	_list = new String[64];
	_list_cnt = 0;
	
	_c1 = new Color( 0, 255, 0 );
}

//==================================================================
public void add( String line )
{
	if ( _list_cnt >= 64 )
		_list_cnt = 0;

	_list[_list_cnt++] = line;
}

//==================================================================
public void set_offset( int x, int y )
{
    _off_x = x;
    _off_y = y;
}
//==================================================================
public void paint( Graphics g )
{
int 	i;

	g.setColor( Color.black );
	for (i=0; i < _list_cnt; ++i)
		g.drawString( _list[i], _off_x + 1, _off_y + (i+1) * 14 + 1 );

	g.setColor( _c1 );
	for (i=0; i < _list_cnt; ++i)
		g.drawString( _list[i], _off_x, _off_y + (i+1) * 14 );
		
	_list_cnt = 0;
}

}

