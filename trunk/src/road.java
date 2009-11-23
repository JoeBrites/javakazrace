//==================================================================
/// Created by Davide Pasca - About 2001
/// See the files "readme.txt" and "license.txt" for general and
/// copyright info.
//==================================================================

import java.util.*;

//==================================================================
//==================================================================
public class road
{
int		_w, _x, _y, _pos;
transformer _xformer;
int		_sy_min;
int		_road_w, _lane_w, _strip_h;
int		_road_col_lt, _road_col_dk, _line_col, _line_mip_col;
int		_edge_a_col, _edge_b_col;

int		_side_lt_col, _side_dk_col;
int		_fog_r, _fog_g, _fog_b;
int		_fog_col;

int		_sxy_patches[];
int		_xy_patches[];
float	_z_patches[];
int		_cur_xacc;

final int	ANGLE=0;
final int	LANES=1;
final int	VANGLE=2;
final int	VIEW_PATCHES=120;
final float GR_TO_RAD=3.14159265f/180.0f;
final int	MAX_SIDE_SPRT=3;

sector_list	_rd_sec;
sector_list	_sprt_l_sec[], _sprt_r_sec[];
sector_list	_side_l_sec, _side_r_sec;
static final int LSIDE=0, RSIDE=1;
static final int L1=2, R1=3;
static final int L2=4, R2=5;
static final int L3=6, R3=7;

final int	OBJ_OFFX=0;

sprite	_lsprite;

Vector	_vehicles;
vehicle	_v_list[];
vehicle	_collided_vehicle;
float	_collided_vehicle_strength;

//==================================================================
public road( int w, int h, int lane_w, int strip_h, transformer xformer )
{
	_w = w;
	_x = 0;
	_lane_w = lane_w * 1024;
	
	_road_w = _lane_w * 4;
	
	_strip_h = strip_h;
	_xformer = xformer;
	
	
	_xformer.h_seti( h * 120 / 100 );
	_xformer.offy_seti( 1024 * -strip_h * 135 / 100 );

	_y = _xformer._hi_he;

	colors_set( 70, 70, 70,
				250, 250, 250,
				20, 20, 20,
				220, 220, 5,
				200, 100, 10,
				128, 140, 180 );
				
	_rd_sec = new sector_list( 512 );

	_sprt_l_sec = new sector_list[ MAX_SIDE_SPRT ];
	_sprt_r_sec = new sector_list[ MAX_SIDE_SPRT ];
	for (int i=0; i < MAX_SIDE_SPRT; ++i)
	{
		_sprt_l_sec[i] = new sector_list( 512 );
		_sprt_r_sec[i] = new sector_list( 512 );
	}
	
	_side_l_sec = new sector_list( 512 );
	_side_r_sec = new sector_list( 512 );

	_sxy_patches = new int[ (VIEW_PATCHES+1)*5 ];
	_xy_patches = new int[ (VIEW_PATCHES+1)*5 ];
	_z_patches = new float[ (VIEW_PATCHES+1) ];
	
	_v_list = new vehicle[VIEW_PATCHES*4+3];
}

//==================================================================
public void sector_add( int lanes, int vangle, int angle, int len )
{
	_rd_sec.add( len, 2, angle, lanes, vangle, null );
}

//==================================================================
sector_list get_sector( int sec_type )
{
	switch ( sec_type )
	{
	case LSIDE: return _side_l_sec;
	case RSIDE: return _side_r_sec;
	case L1: return _sprt_l_sec[0];
	case R1: return _sprt_r_sec[0];
	case L2: return _sprt_l_sec[1];
	case R2: return _sprt_r_sec[1];
	case L3: return _sprt_l_sec[2];
	case R3: return _sprt_r_sec[2];
	}
	
	return null;
}

//==================================================================
public void sector_sprites_add( int sec_type, sprite sptr, int len, int step, int off )
{
	get_sector( sec_type ).add( len, step, off, 0, 0, sptr );
}

//==================================================================
public void sector_sprites_add( int sec_type, int len )
{
	sector_sprites_add_empty( sec_type, len );
}
	
//==================================================================
public void sector_sprites_add_empty( int sec_type, int len )
{
	get_sector( sec_type ).add( len, 0, 0, 0, 0, null );
}

//==================================================================
public void sprite_fill( sprite spr )
{
	_lsprite = spr;
}

//==================================================================
int col_mod( int r, int g, int b, float coe )
{
	return color.make( (int)(r * coe), (int)(g * coe),  (int)(b * coe) );
}

//==================================================================
public void colors_set( int road_r, int road_g, int road_b,
						int line_r, int line_g, int line_b,
						int edge_a_r, int edge_a_g, int edge_a_b,
						int edge_b_r, int edge_b_g, int edge_b_b,
						int	side_r, int side_g, int side_b,
						int	fog_r, int fog_g, int fog_b )
{
	_road_col_lt = col_mod( road_r, road_g, road_b, 1.015f );
	_road_col_dk = col_mod( road_r, road_g, road_b,  .985f );

	_line_col = color.make( line_r, line_g, line_b );
	_line_mip_col = color.make( road_r + line_r >> 1,
							   road_g + line_g >> 1,
							   road_b + line_b >> 1 );

	_edge_a_col = color.make( edge_a_r, edge_a_g, edge_a_b );
	_edge_b_col = color.make( edge_b_r, edge_b_g, edge_b_b );

	_fog_r = fog_r; _fog_g = fog_g; _fog_b = fog_b;
	_fog_col = color.make( _fog_r, _fog_g, _fog_b );

	_side_lt_col = col_mod( side_r, side_g, side_b, 1.015f );
	_side_dk_col = col_mod( side_r, side_g, side_b,  .985f );
}

//==================================================================
void draw_patch( graphics g,
				 int x1a, int x2a, int ya,
				 int x1b, int x2b, int yb,
				 int road_col,
				 int edge_w_coe, int edge_col,
				 int line_w_coe, int draw_line )
{
int	delta_l, delta_r;
int	dy, y, x, w, edge_w, line_w;
int	mid_x;

	ya >>= 10;
	yb >>= 10;

	dy = yb - ya + 1;
	if ( dy <= 0 )
		return;

	// speedup
	if ( dy > 1 )
	{
		delta_l = (x1b - x1a) / dy;
		delta_r = (x2b - x2a) / dy;
	}
	else
		delta_l = delta_r = 0;
	
	for (y=ya; y <= yb; ++y)
	{
		w = x2a - x1a;

		edge_w = (w * edge_w_coe) >> 8;
		line_w = (w * line_w_coe) >> 8;

		x = x1a;

		g.line_h2( x >> 10, edge_w >> 10, y, edge_col );
		g.line_h2( x+w-edge_w >> 10, edge_w >> 10, y, edge_col );
		g.line_h2( x+edge_w >> 10, w-edge_w*2 >> 10, y, road_col );

		if ( draw_line != 0 && line_w > 64 )
		{
			if ( line_w >= 1024 )
			{
				mid_x = x + w / 5 + edge_w/2 - line_w/2;
				g.line_h2( mid_x >> 10, line_w >> 10, y, _line_col );

				mid_x += w / 5;
				g.line_h2( mid_x >> 10, line_w >> 10, y, _line_col );

				mid_x += w / 5;
				g.line_h2( mid_x >> 10, line_w >> 10, y, _line_col );

				mid_x += w / 5;
				g.line_h2( mid_x >> 10, line_w >> 10, y, _line_col );
			}
			else
			{
				mid_x = x + w / 5 + edge_w/2;
				g.line_h2( mid_x >> 10, 1, y, _line_mip_col );

				mid_x += w / 5;
				g.line_h2( mid_x >> 10, 1, y, _line_mip_col );

				mid_x += w / 5;
				g.line_h2( mid_x >> 10, 1, y, _line_mip_col );

				mid_x += w / 5;
				g.line_h2( mid_x >> 10, 1, y, _line_mip_col );
			}
		}
		
		x1a += delta_l;
		x2a += delta_r;
	}
}

//==================================================================
void prepare_vehicle_list( vehicle v_list[], int pos_base )
{
Enumeration	enum;
vehicle		v;
int			pos_min, pos_max;
int			i;

	pos_base -= 200;
	if ( pos_base < 0 )
		pos_base = 0;

	pos_min = pos_base;
	pos_max = pos_base + _strip_h * VIEW_PATCHES;

	for (i=0; i < v_list.length; ++i)
		v_list[i] = null;

	enum = _vehicles.elements();
	while ( enum.hasMoreElements() )
	{
		v = (vehicle)enum.nextElement();
		if ( v._pos >= pos_min && v._pos < pos_max )
		{
			i = (v._pos - pos_base) / (_strip_h/4);			
			v._next = v_list[i];
			v_list[i] = v;
		}
	}
}

//==================================================================
void draw_vehicles( graphics g, vehicle v_list[], int pos, int base_pos,
					   int r_x, int r_y, boolean check_collision )
{
vehicle	v;
int		x, y, z;
float	zz;
int		vsx, vsy;
int		i, ii;
int		wh;
int		frame;
int		t;

	i = (pos - base_pos) / _strip_h;
	i *= 4;
	//System.out.println( "pos="+pos + " base_pos="+base_pos +" i="+i );

	wh = _xformer._hi_wd;

	for (ii=3; ii >= 0; --ii)
	{
		v = v_list[ i + ii ];
		while ( v != null )
		{
			//System.out.println( "i+ii = "+(i+ii) );
			if ( v.is_drawable() )
			{
				x = r_x + v._x;
				y = r_y + 0;
				z = v._pos - base_pos + (int)_xformer._screen_d;
				if ( z > 0 )
				{
					zz  = _xformer.xform_zi( z );
					vsx = _xformer.xform_xi( x );
					vsy = _xformer.xform_yi( y );

					if ( v._sprt._n_frames >= 1 )
					{
						t = ((vsx - wh) >> 10);
						frame = v._sprt._n_frames / 2;				

						if ( t <= -140 ) frame += 2;	else
						if ( t >=  140 ) frame -= 2;	else
						if ( t <=  -70 ) frame += 1;	else
						if ( t >=   70 ) frame -= 1;
							
						if ( frame < 0 )				  frame = 0;
						if ( frame >= v._sprt._n_frames ) frame = v._sprt._n_frames-1;
					}
					else
						frame = 0;

					if ( check_collision && v._check_collision )
					{
						if ( x >= -1024*60 &&
							 x <=  1024*60 )
						{
						float tf;
						
							tf = (x / (1024*60.0f));
							if ( tf < 0 )	tf = -tf;

							_collided_vehicle = v;
							_collided_vehicle_strength = 1.0f - tf;
						}
					}
					
					v.draw( g, frame, vsx >> 10, vsy >> 10, zz );
					//System.out.println( " "+(vsx>>10)+ " "+t );
				}
			}
			
			v = v._next;
		}
	}
}

//==================================================================
boolean sector_draw( graphics g, vehicle v_list[], int pos )
{
int		cur_sect, r1_cur_sect, r2_cur_sect, side_r_cursec, side_l_cursec;
float	cur_s_coe, next_s_coe;
int		pos_i;

int	i, j;
int	posi;
int	x0, x1, x2, x3, x, y, z, zoff;
float	zz, yy;
float	xacc, yacc;
int	sx0, sx1, sx2, sx3, sy, he;
int oldsx0, oldsx1, oldsx2, oldsx3, oldsy, oldhe;

float	tp, ts, t, ttp, tts;
float	zoff_t;

int		ox1_ang, ox0_ang;
float	ox1_sin, ox0_sin;

int		oy1_ang, oy0_ang;
float	oy1_sin, oy0_sin;

int		last_sect;
int		sec_params[];
boolean	end_reached;

int		fake_road_w;
sector_list	rd = _rd_sec;
sector_list	ls[] = _sprt_l_sec;
sector_list	rs[] = _sprt_r_sec;

	if ( pos >= (_rd_sec.get_base( _rd_sec._n_sectors-1 )-1-VIEW_PATCHES)*100 )
		end_reached = true;
	else
		end_reached = false;
	
	pos = pos % ((_rd_sec.get_base( _rd_sec._n_sectors-1 )-1-VIEW_PATCHES)*100);

	zoff = pos % _strip_h;

	x = 0;
	z = -zoff;
	//if ( zoff != 0 )
	//	System.out.println( "zoff ="+zoff );
	last_sect = 0;
	xacc = 0;
	yacc = 0;
	cur_sect = 0;
	
	sy = 0;
	_sy_min = 1000<<10;

	rd.find_reset();
	_side_l_sec.find_reset();
	_side_r_sec.find_reset();

	for (j=0; j < MAX_SIDE_SPRT; ++j)
	{
		ls[j].find_reset();
		rs[j].find_reset();
	}

	posi = pos;
	//posi = (pos/100) * 100;

	float ox_sin;
	float old_xacc, new_xacc;

	float oy_sin;
	float old_yacc, new_yacc;
	
	old_xacc = xacc;
	old_yacc = yacc;

	zoff_t = (float)zoff / _strip_h;
	for (i=0; i <= VIEW_PATCHES; ++i, posi += 100)
	{
		cur_sect = rd.find_next( posi );
		if ( cur_sect < 0 )
		{
			cur_sect = 0;
			pos = 0;
			//return;
		}
		side_l_cursec = _side_l_sec.find_next( posi );
		side_r_cursec = _side_r_sec.find_next( posi );
		for (j=0; j < MAX_SIDE_SPRT; ++j)
		{
			ls[j].find_next( posi );
			rs[j].find_next( posi );
		}
		

		tp = (float)i / VIEW_PATCHES;
		ts = rd.get_coe( cur_sect, posi ) * 3;
		if ( ts > 1.0f ) ts = 1.0f;

		ox1_ang = rd.get_param( cur_sect, ANGLE );
		if ( cur_sect <= 0 )
			ox0_ang = 0;
		else
			ox0_ang = rd.get_param( cur_sect-1, ANGLE );

		t = tp * GR_TO_RAD;
		ox1_sin = (float)Math.sin( t * ox1_ang );
		ox0_sin = (float)Math.sin( t * ox0_ang );
		
		ox_sin = (1-ts) * ox0_sin + ts * ox1_sin;
		
		new_xacc = old_xacc + 5*1024 * ox_sin;
		xacc += old_xacc * (zoff_t) + new_xacc * (1-zoff_t);
		old_xacc = new_xacc;

		if ( i == 2 )
			_cur_xacc = (int)(-100*1024*ox_sin);

		_road_w = _lane_w * rd.get_param( cur_sect, LANES );
			
		//if ( i >= 60 )
		//	fake_road_w = _road_w - (i - 60)*16 * 1024;
		//else
			fake_road_w = _road_w - i*8 * 1024;
		//fake_road_w = _road_w;

		x1 = x - fake_road_w/2 + _x;
		x2 = x + fake_road_w/2 + _x;
		x0 = x1 - 800 * 1024;
		x3 = x2 + 800 * 1024;
		
		oy1_ang = rd.get_param( cur_sect, VANGLE );
			
		if ( cur_sect <= 0 )
			oy0_ang = 0;
		else
			oy0_ang = rd.get_param( cur_sect-1, VANGLE );

		oy1_sin = (float)Math.sin( t * oy1_ang );
		oy0_sin = (float)Math.sin( t * oy0_ang );

		oy_sin = (1-ts) * oy0_sin + ts * oy1_sin;

		new_yacc = old_yacc + 2*1024 * oy_sin;
		yacc += old_yacc * (zoff_t) + new_yacc * (1-zoff_t);
		old_yacc = new_yacc;


		x = (int)xacc;					
		y = (int)yacc;

		zz  = _xformer.xform_zi( z );
		sx0 = _xformer.xform_xi( x0 );
		sx1 = _xformer.xform_xi( x1 );
		sx2 = _xformer.xform_xi( x2 );
		sx3 = _xformer.xform_xi( x3 );
		sy  = _xformer.xform_yi( y );

		_xy_patches[ i*5 + 0 ] = x0;
		_xy_patches[ i*5 + 1 ] = x1;
		_xy_patches[ i*5 + 2 ] = x2;
		_xy_patches[ i*5 + 3 ] = x3;
		_xy_patches[ i*5 + 4 ] = y;

		_sxy_patches[ i*5 + 0 ] = sx0;
		_sxy_patches[ i*5 + 1 ] = sx1;
		_sxy_patches[ i*5 + 2 ] = sx2;
		_sxy_patches[ i*5 + 3 ] = sx3;
		_sxy_patches[ i*5 + 4 ] = sy;
		_z_patches[ i ] = zz;

		z += _strip_h;

		if ( sy < _sy_min )
			_sy_min = sy;
	}

	//System.out.println( "_sy_min ="+_sy_min );

	_y = sy;

int		edge_col_idx, draw_line;
int		edge_col, road_col;
int		side_col;
int		obj_i, obj_posi;
int		oldx=0;

	edge_col_idx = (pos / _strip_h) & 1;
	draw_line = 3 - ((pos / _strip_h) & 3);


	oldsx0 = _sxy_patches[ VIEW_PATCHES*5 + 0 ];
	oldsx1 = _sxy_patches[ VIEW_PATCHES*5 + 1 ];
	oldsx2 = _sxy_patches[ VIEW_PATCHES*5 + 2 ];
	oldsx3 = _sxy_patches[ VIEW_PATCHES*5 + 3 ];
	oldsy = _sxy_patches[ VIEW_PATCHES*5 + 4 ];
	oldhe = 10;

	_collided_vehicle = null;

	for (i=VIEW_PATCHES; i >= 0; --i)
	{
		tp = (float)i / VIEW_PATCHES;

		posi -= 100;
		cur_sect = rd.find_prev( posi );
		if ( cur_sect < 0 )
		{
			cur_sect = 0;
			pos = 0;
		}
		
		sx0 = _sxy_patches[ i*5 + 0 ];
		sx1 = _sxy_patches[ i*5 + 1 ];
		sx2 = _sxy_patches[ i*5 + 2 ];
		sx3 = _sxy_patches[ i*5 + 3 ];
		sy  = _sxy_patches[ i*5 + 4 ];

		float coe = tp;// * 0.9f;
		if ( (edge_col_idx++ & 1) == 0 )
		{
			edge_col = color.make_inpt(  _edge_b_col, _fog_col, coe );
			road_col = color.make_inpt( _road_col_lt, _fog_col, coe );
			side_col = color.make_inpt( _side_lt_col, _fog_col, coe );
		}
		else
		{
			edge_col = color.make_inpt(  _edge_a_col, _fog_col, coe );
			road_col = color.make_inpt( _road_col_dk, _fog_col, coe );
			side_col = color.make_inpt( _side_dk_col, _fog_col, coe );
		}
		
		// ------------------- road --------------------
		he = (sy>>10) - (oldsy >> 10) + 1;

		if ( (he != oldhe || sy != oldsy) && oldsy <= sy )
		{
			g.rect_fill( 0, oldsy>>10, _w, (sy-oldsy>>10) + 1, side_col );
			
			draw_patch( g,
						oldsx1, oldsx2, oldsy,
						sx1,    sx2,    sy,
						road_col,
						(int)(0.010 * 256), edge_col,
						(int)(0.010 * 256),
						_rd_sec.is_drawable2( cur_sect, posi ) ? 1 : 0 ); // edge_col_idx&1 );
		}
		++draw_line;
		
		// ------------------ sides -------------------
		side_l_cursec = _side_l_sec.find_prev( posi );
		side_r_cursec = _side_r_sec.find_prev( posi );
		{
		sprite	sptr;
			
			if ( side_l_cursec >= 0 )
			{
				sptr = (sprite)_side_l_sec.get_object( side_l_cursec );
				sptr.draw( g, 0, oldsx0, oldsx1, oldsy, sx0, sx1, sy, sprite.FLIP_X_FLG );
			}
			if ( side_r_cursec >= 0 )
			{
				sptr = (sprite)_side_r_sec.get_object( side_r_cursec );
				sptr.draw( g, 0, oldsx2, oldsx3, oldsy, sx2, sx3, sy, 0 );
			}
		}


		// --------------- side objects ----------------
		int		cs, lr_i;
		sector_list	slist;
		sector_list	slist_array[];

		obj_i = i + 2;
		obj_posi = posi;// + 200;
		if ( obj_i <= 110 && obj_i > 0 )
		{
		int		obj_sx, obj_sy;
		int		obj_sx1, obj_sx2;
		int		obj_x1, obj_x2, obj_y;
		int		obj_off_x, org_x;
		float	obj_zz;
		int		flags;
		
			obj_x1 = _xy_patches[ obj_i*5 + 1 ];
			obj_x2 = _xy_patches[ obj_i*5 + 2 ];
			obj_y  = _xy_patches[ obj_i*5 + 4 ];
			obj_zz = _z_patches[obj_i];
			
			_xformer.zz_setf( obj_zz );
			obj_sy = _xformer.xform_yi( obj_y ) >> 10;
			
			slist_array = ls;
			org_x = obj_x1;
			flags = sprite.FLIP_X_FLG;
			
			for (lr_i=0; lr_i < 2; ++lr_i)
			{
				for (j=0; j < MAX_SIDE_SPRT; ++j)
				{
					slist = slist_array[j];
					cs = slist.find_prev( obj_posi-j*200 );
					if ( cs >= 0 )
						if ( slist.is_drawable( cs, obj_posi-j*200 ) )
						{
						sprite	sptr;
						
							obj_off_x = slist.get_param( cs, OBJ_OFFX ) * 1024;
							obj_sx = _xformer.xform_xi( org_x + obj_off_x ) >> 10;
							
							sptr = (sprite)slist.get_object( cs );
							sptr.draw( g, 0, obj_sx, obj_sy, obj_zz * 2, flags );
						}
				}
				slist_array = rs;
				org_x = obj_x2;
				flags &= ~sprite.FLIP_X_FLG;
			}
		}

		// ------------------ vehicles --------------------
		x = _xy_patches[i*5+1] + _xy_patches[i*5+2] >> 1;
		y = _xy_patches[i*5+4];

		if ( i < VIEW_PATCHES-2 )
		{
		int		xx;
		
			xx = (int)(oldx * (zoff_t) + x * (1-zoff_t));

			draw_vehicles( g, v_list, posi, pos, xx, y, (i >= 2 && i <= 4) );
		}
		oldx = x;

		oldsx0 = sx0;
		oldsx1 = sx1;
		oldsx2 = sx2;
		oldsx3 = sx3;
		oldsy  = sy;
		oldhe = he;
	}
	
	return end_reached;
}

//==================================================================
public boolean paint( graphics g )
{
	prepare_vehicle_list( _v_list, _pos );
	return sector_draw( g, _v_list, _pos );
}

//==================================================================
public void pos_set( int pos )
{
	_pos = pos;
}

//==================================================================
public int pos_get()
{
	return _pos;
}

//==================================================================
public int far_pos_get()
{
	return _pos + _strip_h * VIEW_PATCHES;
}

//==================================================================
public int edge_collision( int x, int y, int z )
{
	if ( _x+_road_w/2 < x )
		return x - (_x+_road_w/2);

	if ( (_x-_road_w/2) > x )
		return x - (_x-_road_w/2);
		
	return 0;
}

//==================================================================
public int cur_x_road_get()
{
	return 0;
}

//==================================================================
public int x_get()
{
	return _x;
}

//==================================================================
public void x_set( int x )
{
	_x = x;
	//_x = -500*1024;
}

//==================================================================
public int x_off( int x )
{
	//x = 0;
	//_x = -500*1024;
	return _x += x;
}

//==================================================================
public int y_get()
{
	return _y >> 10;
}

//==================================================================
public int sy_min_get()
{
	return _sy_min >> 10;
}


//==================================================================
public vehicle collided_vehicle()
{
	return _collided_vehicle;
}

//==================================================================
public float collided_vehicle_strength()
{
	return _collided_vehicle_strength;
}

//==================================================================
public void vehicles_set( Vector v )
{
	_vehicles = v;
}

}
