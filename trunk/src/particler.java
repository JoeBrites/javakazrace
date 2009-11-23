//==================================================================
/// Created by Davide Pasca - About 2001
/// See the files "readme.txt" and "license.txt" for general and
/// copyright info.
//==================================================================

//==================================================================
//==================================================================
public class particler
{
static final int    MAX_PARTS=256;

static final int    X=0;
static final int    Y=1;
static final int    Z=2;
static final int    VX=3;
static final int    VY=4;
static final int    VZ=5;
static final int    COORDS_SIZE=6;

static final int    LIFE=0;
static final int    FLAGS=1;
static final int    DATA_SIZE=2;

float				_parts_coords[];
int					_parts_data[];
int					_n_active;

float				_crea_speed, _crea_cnt;
int					_lifespan;
float				_t0_speed_x, _t0_speed_y, _t0_speed_z;
float				_atten_x, _atten_y, _atten_z;
float				_rand_size_x1, _rand_size_y1, _rand_size_z1;
float				_rand_size_x2, _rand_size_y2, _rand_size_z2;
float				_t0_rand_size_x, _t0_rand_size_y, _t0_rand_size_z;
transformer			_xformer;
sprite				_sprt;

boolean				_active=false;

//==================================================================
public particler( transformer xformer )
{
    _parts_coords = new float[ MAX_PARTS * COORDS_SIZE ];
    _parts_data =     new int[ MAX_PARTS * DATA_SIZE ];
    _n_active = 0;
    _xformer = xformer;
}

//==================================================================
public void begin( float crea_speed, int lifespan,
				   float t0_speed_x, float t0_speed_y, float t0_speed_z,
				   float t0_rand_size_x, float t0_rand_size_y, float t0_rand_size_z,
				   float rand_size_x1, float rand_size_y1, float rand_size_z1,
				   float rand_size_x2, float rand_size_y2, float rand_size_z2,
				   float atten_x, float atten_y, float atten_z, sprite sprt )
{
	_crea_speed = crea_speed;
	_lifespan = lifespan;

	_active = true;

	_n_active = 0;
	int	i, ii;
	for (i=0; i < MAX_PARTS; ++i)
	{
		ii = i * DATA_SIZE;
		_parts_data[ii+FLAGS] = 0;
	}

	_t0_speed_x = t0_speed_x;
	_t0_speed_y = t0_speed_y;
	_t0_speed_z = t0_speed_z;
	_rand_size_x1 = rand_size_x1;
	_rand_size_y1 = rand_size_y1;
	_rand_size_z1 = rand_size_z1;
	_rand_size_x2 = rand_size_x2;
	_rand_size_y2 = rand_size_y2;
	_rand_size_z2 = rand_size_z2;
	_t0_rand_size_x = t0_rand_size_x;
	_t0_rand_size_y = t0_rand_size_y;
	_t0_rand_size_z = t0_rand_size_z;
	_atten_x = atten_x;
	_atten_y = atten_y;
	_atten_z = atten_z;

	_sprt = sprt;
}

//==================================================================
float rand_range( float x1, float x2 )
{
float	tf;

	tf = (float)Math.random();
	return tf * x1 + (1.0f - tf) * x2;
}

//==================================================================
public void idle( float px, float py, float pz, float vx, float vy, float vz )
{
int	i, ii;
int	to_create;
float	t0_hsiz_x, t0_hsiz_y, t0_hsiz_z, tf;
int		cnt;

	if ( _active == false )
		to_create = 0;
	else
	{
		_crea_cnt += _crea_speed; 
		to_create = (int)_crea_cnt;	
		_crea_cnt -= to_create;
	}
	
	if ( to_create <= 0 && _n_active <= 0 )
		return;
	
	t0_hsiz_x = _t0_rand_size_x * 0.5f;
	t0_hsiz_y = _t0_rand_size_y * 0.5f;
	t0_hsiz_z = _t0_rand_size_z * 0.5f;
	for (i=0; i < MAX_PARTS; ++i)
	{
		ii = i * DATA_SIZE;
		if ( (_parts_data[ii+FLAGS] & 1) == 0 )
		{
			if ( to_create > 0 )
			{
				_parts_data[ii+FLAGS] = 1;
				_parts_data[ii+LIFE] = _lifespan;
				
				ii = i * COORDS_SIZE;
				_parts_coords[ii+X] = px + t0_hsiz_x - (float)Math.random() * _t0_rand_size_x;
				_parts_coords[ii+Y] = py + t0_hsiz_y - (float)Math.random() * _t0_rand_size_y;
				_parts_coords[ii+Z] = pz + t0_hsiz_z - (float)Math.random() * _t0_rand_size_z;

				_parts_coords[ii+VX] = _t0_speed_x + rand_range( _rand_size_x1, _rand_size_x2 );
				_parts_coords[ii+VY] = _t0_speed_y + rand_range( _rand_size_y1, _rand_size_y2 );
				_parts_coords[ii+VZ] = _t0_speed_z + rand_range( _rand_size_z1, _rand_size_z2 );
				
				++_n_active;
				--to_create;
			}
		}
		else
		{
			ii = i * DATA_SIZE;
			if ( _parts_data[ii+LIFE] > 0 )
			{
				if ( --_parts_data[ii+LIFE] <= 0 )
				{
					_parts_data[ii+FLAGS] = 0;
					--_n_active;
				}
				else
				{
					ii = i * COORDS_SIZE;
					_parts_coords[ii+X] += _parts_coords[ii+VX] + vx;
					_parts_coords[ii+Y] += _parts_coords[ii+VY] + vy;
					_parts_coords[ii+Z] += _parts_coords[ii+VZ] + vz;

					_parts_coords[ii+VX] *= _atten_x;
					_parts_coords[ii+VY] *= _atten_y;
					_parts_coords[ii+VZ] *= _atten_z;
				}
			}
		}
	}
}

//==================================================================
public void draw( graphics g )
{
int		i, ii;
int		sx, sy;
float	zz;

	for (i=0; i < MAX_PARTS; ++i)
	{
		ii = i * DATA_SIZE;
		if ( (_parts_data[ii+FLAGS] & 1) == 1 )
		{
			ii = i * COORDS_SIZE;
			zz = _xformer.xform_zf( _parts_coords[ii+Z] );
			if ( _xformer._clip_flags == 0 )
			{
				sx = _xformer.xform_xf( _parts_coords[ii+X] );
				sy = _xformer.xform_yf( _parts_coords[ii+Y] );

				//System.out.println( "sx="+sx+" sy="+sy );
				//g.line_h( sx, sx+2, sy, 0xffffff );
				_sprt.draw( g, 0, sx, sy, zz, sprite.TRANS100_100_FLG );
			}
		}
	}
}

//==================================================================
public boolean is_active()
{
	return _active;
}

//==================================================================
public void end()
{
	_active = false;
}

}

