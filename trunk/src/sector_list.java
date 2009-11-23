//==================================================================
/// Created by Davide Pasca - About 2001
/// See the files "readme.txt" and "license.txt" for general and
/// copyright info.
//==================================================================

//==================================================================
public class sector_list
{
final int	SECTOR_SIZE = 8;
final int	BASE_POS = 0;
final int	LEN = 1;
final int	STEP = 2;

Object	_sector_objs[];
int		_sectors[];
int		_n_sectors;
int		_total_len;

int		_from_sect;
int		_pass_params[];

int		_read_pos;
int		_read_cnt;

//==================================================================
public sector_list( int max_sectors )
{
	_sector_objs = new Object[max_sectors];
	_sectors = new int[max_sectors*SECTOR_SIZE];
	_pass_params = new int[SECTOR_SIZE-2];
	_n_sectors = 0;
	_total_len = 0;
	
	_from_sect = 0;
}

//==================================================================
public void add( int len, int step, int p1, int p2, int p3, Object obj )
{
int	i, prev;

	i = _n_sectors * SECTOR_SIZE;

	_sectors[i+LEN] = len;
	_sectors[i+STEP] = step;
	_sectors[i+3] = p1;
	_sectors[i+4] = p2;
	_sectors[i+5] = p3;
	_sector_objs[_n_sectors] = obj;
	
	if ( _n_sectors > 0 )
	{
		prev = (_n_sectors-1) * SECTOR_SIZE;
		_sectors[i+BASE_POS] = _sectors[prev+BASE_POS] + _sectors[prev+LEN];
	}
	else
		_sectors[i+BASE_POS] = 0;
	
	++_n_sectors;
	_total_len += len;	
}

//==================================================================
public int get_len( int sector )
{
	return _sectors[sector*SECTOR_SIZE+LEN];
}

//==================================================================
public int get_base( int sector )
{
	return _sectors[sector*SECTOR_SIZE+BASE_POS];
}

//==================================================================
public int []get_params( int sector )
{
int	i;

	i = sector * SECTOR_SIZE;
	_pass_params[0] = _sectors[i+3];
	_pass_params[1] = _sectors[i+4];
	_pass_params[2] = _sectors[i+5];
	_pass_params[3] = _sectors[i+6];
	_pass_params[4] = _sectors[i+7];
	
	return _pass_params;
}

//==================================================================
public int get_param( int sector, int param )
{
	return _sectors[sector * SECTOR_SIZE + param + 3];
}

//==================================================================
public Object get_object( int sector )
{
	return _sector_objs[ sector ];
}

//==================================================================
public float get_coe( int sector, int pos )
{
float	coe;
int		base, len;

	base = _sectors[sector*SECTOR_SIZE+BASE_POS];
	len  = _sectors[sector*SECTOR_SIZE+LEN];
	return (pos/100.0f - base) / (len != 0 ? len : 1);
}

//==================================================================
public boolean is_drawable( int sector, int pos )
{
int	step;

	pos /= 100;
	
	step = _sectors[sector * SECTOR_SIZE + STEP];
	if ( step == 0 )
		return false;
		
	--step;
	if ( (pos & step) == step )
		return true;
		
	return false;
}

//==================================================================
public boolean is_drawable2( int sector, int pos )
{
int	step;

	pos /= 100;
	
	step = _sectors[sector * SECTOR_SIZE + STEP];
	if ( step == 0 )
		return false;

	if ( (pos & step) == step )
		return true;
		
	return false;
}

//==================================================================
public void find_reset()
{
	_from_sect = 0;
}

//==================================================================
public int find_prev( int pos )
{
int	i;
int	pos1, pos2;

	pos /= 100;
	for (i=_from_sect; i >= 0; --i)
	{
		pos1 = _sectors[i*SECTOR_SIZE+BASE_POS];
		pos2 = pos1 + _sectors[i*SECTOR_SIZE+LEN];
		if ( pos >= pos1 && pos < pos2 )
			return _from_sect = i;
	}
	
	return -1;
}

//==================================================================
public int find_next( int pos )
{
int	i;
int	pos1, pos2;

	pos /= 100;
	for (i=_from_sect; i < _n_sectors-1; ++i)
	{
		pos1 = _sectors[i*SECTOR_SIZE+BASE_POS];
		pos2 = pos1 + _sectors[i*SECTOR_SIZE+LEN];
		if ( pos >= pos1 && pos < pos2 )
			return _from_sect = i;
	}
	
	return -1;
}


}
