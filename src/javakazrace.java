//==================================================================
/// Created by Davide Pasca - About 2001
/// See the files "readme.txt" and "license.txt" for general and
/// copyright info.
//==================================================================

import java.awt.*;
//import java.net.*;
import java.util.*;
//import java.awt.event.*;
import java.applet.*;
//import sun.audio.*; //import the sun.audio package
//import sun.audio.AudioDevice;
//import java.io.*;

//==================================================================
//==================================================================
public class javakazrace extends Applet implements Runnable
{
Thread		anim_th;
printout	_po;
horizont	_hor;
transformer	_xformer;
font		_font;
road		_road;
sprite		_mycar;
sprite		_f500;
sprite		_moo;
sprite		_smoke_sprt;

sprite		_rqueen1, _rqueen2, _rqueen3, _rqueen4;
sprite		_start_frame;
particler	_smoke_l, _smoke_r;

Dimension   _off_d;
graphics	_off_g;

int			_screen_d;
int			_speed, _accel;

boolean		_left_isdown;
boolean		_right_isdown;
boolean		_up_isdown;
boolean		_down_isdown;

final int	WHEEL_MAX=32, WHEEL_MIN=-32;
int			_wheel_pos = 0;

int			_drifting = 0;
boolean		_nosediving;

Vector		_vehicles;

long		_laptimes[];
int			_n_laps;

//==================================================================
void idle_controls()
{
	if ( _left_isdown || _right_isdown )
	{
		if ( _left_isdown )
			if ( _wheel_pos > -32 )
				_wheel_pos -= 8;

		if ( _right_isdown )
			if ( _wheel_pos < 32 )
				_wheel_pos += 8;
	}
	else
	{
		if ( _wheel_pos > 0 )
			_wheel_pos -= 8;
		else
		if ( _wheel_pos < 0 )
			_wheel_pos += 8;
	}
	
}

//==================================================================
void anim_cars()
{
Enumeration	enum;
vehicle		v;

	enum = _vehicles.elements();
	while ( enum.hasMoreElements() )
	{
		v = (vehicle)enum.nextElement();
		v.animate( _road.far_pos_get() );
	}
}

//==================================================================
public void animate()
{
boolean	colliding;
int		step_wd;
int		off_coll;
int		max_x_accel;

int		car_x_accel;
int		road_x_accel;
int		t;

	_road.pos_set( _road.pos_get()+ (_speed>>10) );	// 46 = ~50km/h

	// kinematics
	_speed += _accel;
	
	//_mycar._dx = 0;
	step_wd = 25;
	
	car_x_accel = (-(step_wd * _speed) / 100) * _wheel_pos >> 5;
	
	t = _speed / 1024;
	t = (t * t);
	road_x_accel = -_road._cur_xacc * t >> 8;
	
	if ( _up_isdown )
		road_x_accel /= 4;
	else
		road_x_accel /= 9;
	
	_po.add( "Speed =" + _speed );
	_po.add( "Accel =" + _accel );
	_po.add( "_wheel_pos =" + _wheel_pos );
	_po.add( "road_x_accel =" + road_x_accel );
	_po.add( "car_x_accel =" + car_x_accel );
	
	if ( (car_x_accel >  25000 && road_x_accel < -30000 && _wheel_pos <= -WHEEL_MAX) ||
		 (car_x_accel < -25000 && road_x_accel >  30000 && _wheel_pos >=  WHEEL_MAX) )
	{
		car_x_accel = -road_x_accel;
		++_drifting;
		_po.add( "drifting = YES" );
	}
	else
	{
		_po.add( "drifting = NO" );
		_drifting = 0;
	}

	_hor.x_set( _hor._x - road_x_accel/10000.0f );

	_road.x_off( road_x_accel + car_x_accel );

	colliding = false;

	// handle controls
	off_coll = _road.edge_collision( _mycar._x, _mycar._y, _mycar._z ); // -102400
	if ( off_coll < 0 )
	{
		_road.x_off( off_coll );	// -128*100
		_accel -= 400;
		colliding = true;
	}

	off_coll = _road.edge_collision( _mycar._x, _mycar._y, _mycar._z ); // +102400
	if ( off_coll > 0 )
	{
		_road.x_off( off_coll );	// +128*100
		_accel -= 400;
		colliding = true;
	}
		
	//_road.x_off( -_mycar._dx );

	if ( _up_isdown )
	{
		//if ( !colliding )
			_accel += 100;
	}
	else
	if ( _down_isdown )
		_accel -= 200;
	else
	{
		_accel -= 20;
		if ( _accel < -400 )
			_accel = -400;
	}

	// clamp accel
	if ( _accel > 1000 )
		_accel = 1000;

	if ( _accel < -2000 )
		_accel = -2000;

	if ( _road.collided_vehicle() != null )
	{
		_accel -= (int)(100 * _road.collided_vehicle_strength());
		//_accel = -201;//(int)(_accel * 0.8f);
		_speed = (int)(_road.collided_vehicle()._speed * 1024 * 0.9f);		
	}

	// clamp speed
	if ( _speed <= 0 && _accel < 0 )
	{
		_speed = 0;
		_accel = 0;
	}

	if ( _speed > (140<<10) && _accel > 0 )
	{
		_speed = 140<<10;
		_accel = 0;
	}
	
	_smoke_l.idle( _mycar._x/1024.0f-45, _mycar._y/1024.0f+15, _mycar._z/1024.0f+_screen_d,
				   0, 0, -_speed/1024.0f/8 );

	_smoke_r.idle( _mycar._x/1024.0f+45, _mycar._y/1024.0f+15, _mycar._z/1024.0f+_screen_d,
				   0, 0, -_speed/1024.0f/8 );
	
	anim_cars();
}

//==================================================================
public void update( Graphics g )
{
	//paint( g );
}
//==================================================================
public void paint( Graphics g )
{
}

//==================================================================
String zero_pad( long val )
{
	if ( val < 10 )
		return "0"+val;
	
	return ""+val;
}

//==================================================================
void lap_add_new()
{
	++_n_laps;
		
	for (int i=_laptimes.length-1; i >= 1; --i)
		_laptimes[i] = _laptimes[i-1];

	_laptimes[0] = 0;
}

//==================================================================
void lap_draw( graphics g )
{
long	laptime, min, sec, cent;
int		i, mini_idx;
long	mini;
int		col1, col2;
int		y;

	_font.text_draw( g, g._w-8, 4, 1, -1, 0xff5050, 0xa00000, 0x000000, "LAP:         " );
	
	
	mini = 2000000000;
	mini_idx = -1;
	for (i=1; i < _laptimes.length; ++i)
	{
		laptime = _laptimes[i];
		if ( laptime != 0 )
			if ( laptime < mini )
			{
				mini = laptime;
				mini_idx = i;
			}
    }	
	
	y = 4;
	for (i=0; i < _laptimes.length; ++i)
	{
		if ( i == mini_idx )
		{
			col1 = 0xff9090;
			col2 = 0x909090;
		}
		else
		{
			col1 = 0xffffff;
			col2 = 0x909090;
		}
		
		laptime = _laptimes[i];
		if ( laptime != 0 )
		{
			min = laptime / 1000 / 60;
			sec = (laptime - (min * 60 * 1000)) / 1000;
			cent = (laptime - ((min * 60 * 1000) + sec * 1000)) / 10;
			_font.text_draw( g, g._w-8, y, 1, -1, col1, col2, 0x000000, "      " +
							min + "'" + zero_pad(sec) + "\"." + zero_pad(cent) );
							
			y += _font._fh-2;
		}
	}
}

//==================================================================
public void mypaint( Graphics g )
{
	_hor.y_set( _road.y_get() );
	_hor.paint( _off_g );
	if ( _road.paint( _off_g ) )
	{
		lap_add_new();
		reset_pos();
	}

int	target_frame, frame, last_x_frame;

	_nosediving = false;

	// if was noseup or nosedown then the frame_x is 2 (neutral)
	last_x_frame = _mycar._last_frame;
	if ( last_x_frame > 40 )
		last_x_frame = 20;
	
	
	frame = target_frame = (_wheel_pos * 20 / WHEEL_MAX) + 20;
	
/*	target_frame = 10*_mycar._dx >> 10;
	if ( target_frame < -100 )	target_frame = -100;
	if ( target_frame >  100 )	target_frame =  100;
	target_frame = (target_frame / 5) + 20;
*/
	if ( target_frame < last_x_frame )
		frame = last_x_frame - 2;
	else
	if ( target_frame > last_x_frame )
		frame = last_x_frame + 2;
	else
	{
		frame = target_frame;
		if ( !_left_isdown && !_right_isdown )
		{
			// nose down
			if ( _road.collided_vehicle() != null ||
				 (_down_isdown && _speed > (5<<10) && _accel < 0) )
			{
				frame = 60;
				_nosediving = true;
			}
			else
			// nose up
			if ( _speed <= (50<<10) && _accel > 200 )
			{
				frame = 50;
				_nosediving = true;
			}
		}
	}

	if ( _nosediving || _drifting > 0 )
	{
		if ( !_smoke_l.is_active() )
			_smoke_l.begin( 0.5f, 60,
							0, 2, 0,
							14, 1, 0,
							-3f, 2,  -2,
							 3f, 0,  0,
							.92f, .92f, .92f,
							_smoke_sprt );

		if ( !_smoke_r.is_active() )
			_smoke_r.begin( 0.5f, 60,
							0, 2, 0,
							14, 1, 0,
							-3f, 2,  -2,
							 3f, 0,  0,
							.92f, .92f, .92f,
							_smoke_sprt );
	}
	else
	{
		_smoke_l.end();
		_smoke_r.end();
	}

	//System.out.println( "last_x_frame="+last_x_frame + " frame="+frame + " last="+_mycar._last_frame );
	//if ( frame < 0 )
	//	_mycar.draw( _off_g, frame, pos[0]>>10, pos[1]>>10, 1.0f, sprite.FLIP_X_FLG );
	//else

int	px, py;

	_xformer.xform_zi( _mycar._z );
	px = _xformer.xform_xi( _mycar._x ) >> 10;
	py = _xformer.xform_yi( _mycar._y ) >> 10;

	_mycar.draw( _off_g, frame, px, py, 1.0f );

	_smoke_l.draw( _off_g );
	_smoke_r.draw( _off_g );

	//----------- TIME -------------
	lap_draw( _off_g );

	//----------- STAGE -------------
	_font.text_draw( _off_g, _off_g._w-8, _off_g._h-6, 1, 1, 0xffff50, 0xa0a000, 0x000000, "STAGE  " );
	_font.text_draw( _off_g, _off_g._w-8, _off_g._h-6, 1, 1, 0xffffff, 0x909090, 0x000000, "      1" );

	
	_off_g.update_area();
	_off_g.redraw();
	//_po.paint( g );
}

//==================================================================
void load_cars()
{
	_rqueen1 = new sprite( "rq1.gif", sprite.MIDDLE, sprite.BOTTOM, this );
	_rqueen2 = new sprite( "rq2.gif", sprite.MIDDLE, sprite.BOTTOM, this );
	_rqueen3 = new sprite( "rq3.gif", sprite.MIDDLE, sprite.BOTTOM, this );
	_rqueen4 = new sprite( "rq4.gif", sprite.MIDDLE, sprite.BOTTOM, this );
	_start_frame = new sprite( "start_frame.gif", sprite.MIDDLE, sprite.BOTTOM, 2.0f, this );

	_f500 = new sprite( "f500_-1.gif", sprite.MIDDLE, sprite.BOTTOM, this );
	_f500.add_frame( "f500_0.gif" );
	_f500.add_frame( "f500_1.gif" );
	_f500._last_frame = 10;

	_moo = new sprite( "moo_0_-2.gif", sprite.MIDDLE, sprite.BOTTOM, 2.0f, this );
	_moo.add_frame( "moo_0_-1.gif" );
	_moo.add_frame( "moo_0_0.gif" );
	_moo.add_frame( "moo_0_1.gif" );
	_moo.add_frame( "moo_0_1.gif" );


	_mycar = new sprite( "f355_0_-2.gif", sprite.MIDDLE, sprite.MIDDLE, this );
	_mycar.add_frame( "f355_0_-1.gif" );
	_mycar.add_frame( "f355_0_0.gif" );
	_mycar.add_frame( "f355_0_1.gif" );
	_mycar.add_frame( "f355_0_2.gif" );
	_mycar.add_frame( "f355_-1_0.gif" );
	_mycar.add_frame( "f355_1_0.gif" );
	
	_mycar._last_frame = 20;
	_mycar._x = 0;
	_mycar._y = 0;
	_mycar._z = _screen_d * 160 / 100;
}

//==================================================================
void reset_pos()
{
	_road.pos_set( 0 );

	_vehicles.removeAllElements();

	_vehicles.addElement( new vehicle( _start_frame, (150+-500)*1024, 150, 0, false ) );

	_vehicles.addElement( new vehicle( _rqueen1, (150+-610)*1024, 100, 0, false ) );
	_vehicles.addElement( new vehicle( _rqueen2, (150+-390)*1024, 100, 0, false ) );
	_vehicles.addElement( new vehicle( _rqueen3, (150+-630)*1024, 50, 0, false ) );
	_vehicles.addElement( new vehicle( _rqueen4, (150+-380)*1024, 50, 0, false ) );

	_vehicles.addElement( new vehicle( _f500, 80*1024, 0, 80 ) );

int	stage_len = 2400;
int	i;
int	speed, x_off, road_pos;
sprite	sprt;

	for (i=0; i < 30; ++i)
	{
		if ( (int)(Math.random() * 100) <= 80 )
			sprt = _f500;
		else
			sprt = _moo;

		speed = (int)(Math.random() * 30) + 90;
		x_off = (int)(Math.random() * 1100*1024) + -550*1024;
		road_pos = (int)(Math.random() * stage_len) * 100;

		_vehicles.addElement( new vehicle( sprt, x_off, road_pos, speed ) );
	}
}

//==================================================================
public void load_road()
{
Dimension	d = this.getSize();

	// setup road
	_road = new road( d.width, d.height,
					  250, 100, _xformer );  // 360
	
	_road.colors_set( 130, 125, 120,
					  250, 250, 250,
					  230, 230, 0,//20, 20, 20,
					  230, 230, 0,//220, 220, 5,
					  200, 190, 170,
					  160, 150, 155 );

	// load road sprites
	sprite sprt = new sprite( "beach_l.gif", sprite.RIGHT, sprite.BOTTOM, this);
	sprt.shadow_off();
	//_road.sprite_fill( sprt );

	sprite	rock = new sprite( "rock.gif", sprite.MIDDLE, sprite.BOTTOM, 2.0f, this);
			rock.shadow_off();
			
	sprite	r_palm = new sprite( "palm2_r.gif", sprite.MIDDLE, sprite.BOTTOM, this);
	sprite	bush1  = new sprite(  "bush2.gif", sprite.MIDDLE, sprite.BOTTOM, this);
	sprite	bb_rq1  = new sprite(  "bboard_rq1.gif", sprite.MIDDLE, sprite.BOTTOM, this);
	sprite	pole_r  = new sprite(  "pole_r.gif", sprite.MIDDLE, sprite.BOTTOM, this);
	sprite	pole2_r  = new sprite(  "pole2_r.gif", sprite.MIDDLE, sprite.BOTTOM, this);
	sprite	grass_side_r  = new sprite(  "grass_side_r.jpg", sprite.MIDDLE, sprite.BOTTOM, this);
	sprite	white_house  = new sprite(  "white_house.gif", sprite.MIDDLE, sprite.BOTTOM, 3.0f, this);
	sprite	hotel_l  = new sprite(  "hotel_l.gif", sprite.RIGHT, sprite.BOTTOM, 6.0f, this);
	
	grass_side_r.shadow_off();

	int lanes = 5;
	
/*
int rdef[] =
{
	LANES, 5,
	
	SEC, 0, 0, 100,
	R1, 11, 100, 8, 0, 
};
*/
final int L1_XOFF = -350;
final int L2_XOFF = -300;
final int R1_XOFF = 350;
final int R2_XOFF = 350;

final int L3_XOFF = -60;
final int R3_XOFF =  60;


	_road.sector_sprites_add( road.LSIDE, grass_side_r, 3300, 0, 0 );
	_road.sector_sprites_add( road.RSIDE, grass_side_r, 3300, 0, 0 );

	_road.sector_sprites_add( road.L3, pole_r, 300, 16, L3_XOFF+70 );
	_road.sector_sprites_add( road.R3, pole_r, 300, 16, R3_XOFF-70 );

	_road.sector_sprites_add( road.L3, pole2_r, 300, 16, L3_XOFF+70 );
	_road.sector_sprites_add( road.R3, pole2_r, 300, 16, R3_XOFF-70 );

	_road.sector_sprites_add( road.L3, pole_r, 300, 16, L3_XOFF+70 );
	_road.sector_sprites_add( road.R3, pole_r, 300, 16, R3_XOFF-70 );

	_road.sector_sprites_add( road.L3, pole2_r, 300, 16, L3_XOFF+70 );
	_road.sector_sprites_add( road.R3, pole2_r, 300, 16, R3_XOFF-70 );

	_road.sector_sprites_add( road.L3, pole2_r, 1000, 16, L3_XOFF+70 );
	_road.sector_sprites_add( road.R3, pole2_r, 1000, 16, R3_XOFF-70 );

	_road.sector_sprites_add( road.L3, pole_r, 300, 16, L3_XOFF+70 );
	_road.sector_sprites_add( road.R3, pole_r, 300, 16, R3_XOFF-70 );

	_road.sector_sprites_add( road.L3, pole2_r, 1000, 16, L3_XOFF+70 );
	_road.sector_sprites_add( road.R3, pole2_r, 1000, 16, R3_XOFF-70 );

	_road.sector_sprites_add( road.L3, pole2_r, 10, 16, L3_XOFF+70 );
	_road.sector_sprites_add( road.R3, pole2_r, 10, 16, R3_XOFF-70 );

	_road.sector_add( lanes, 0, 0, 100 );
		
		_road.sector_sprites_add( road.L1, r_palm, 100, 16, L1_XOFF );	// grass_side_r
		_road.sector_sprites_add( road.L2, bush1, 100, 8, L2_XOFF );
		_road.sector_sprites_add( road.R1, bush1, 100, 8, R1_XOFF );
		_road.sector_sprites_add( road.R2, white_house, 100, 16, R2_XOFF+500 );

	
	_road.sector_add( lanes, 0, 20, 20 );
	_road.sector_add( lanes, 0, -15, 10 );
	_road.sector_add( lanes, 0, 0, 30 );
		
		_road.sector_sprites_add( road.L1, bush1, 60, 8, L1_XOFF );
		_road.sector_sprites_add( road.L2, hotel_l, 60, 16, L1_XOFF );
		_road.sector_sprites_add( road.R1, white_house, 60, 16, R1_XOFF+500 );
		_road.sector_sprites_add( road.R2, bush1, 60, 8, R2_XOFF );
	
	_road.sector_add( lanes,  50, 0, 40 );
		
		_road.sector_sprites_add( road.L1, bush1, 40, 8, L1_XOFF );
		_road.sector_sprites_add( road.L2, hotel_l, 40, 16, L2_XOFF );
		_road.sector_sprites_add( road.R1, r_palm,  40, 8, R1_XOFF );
		_road.sector_sprites_add( road.R2, r_palm,  40, 8, R2_XOFF+200 );

	
	_road.sector_add( lanes, -70, -40, 30 );
	
		_road.sector_sprites_add( road.L1, r_palm, 30, 16, L1_XOFF );	// grass_side_r
		_road.sector_sprites_add( road.L2, bush1, 30, 8, L2_XOFF );
		_road.sector_sprites_add( road.R1, bb_rq1,  30, 8, R1_XOFF );
		_road.sector_sprites_add( road.R2, 30 );


	_road.sector_add( lanes, -70, 0, 60 );
	
		_road.sector_sprites_add( road.L1, white_house, 40, 16, L1_XOFF-500 );
		_road.sector_sprites_add( road.L2, 60 );
		_road.sector_sprites_add( road.R1, bush1, 60, 4, R1_XOFF );
		_road.sector_sprites_add( road.R2, 60 );
											
	_road.sector_add( lanes,  0,  40, 100 );
		
		_road.sector_sprites_add( road.L1, bush1, 100, 4, L1_XOFF );
		_road.sector_sprites_add( road.L2, 100 );
		_road.sector_sprites_add( road.R1, bush1, 100, 4, R1_XOFF );
		_road.sector_sprites_add( road.R2, white_house, 100, 16, R2_XOFF+1500 );

	_road.sector_add( lanes, 0, 60, 150 );
		
		_road.sector_sprites_add( road.L1, bush1, 150, 4, L1_XOFF );
		_road.sector_sprites_add( road.L2, 150 );
		_road.sector_sprites_add( road.R1, rock, 100, 4, R1_XOFF );
		_road.sector_sprites_add( road.R1, rock, 20, 4, R1_XOFF );
		_road.sector_sprites_add( road.R1, rock, 30, 4, R1_XOFF );
		_road.sector_sprites_add( road.R2, 150 );

	_road.sector_add( lanes, 0, -60, 100 );
		
		_road.sector_sprites_add( road.L1, bush1, 100, 4, L1_XOFF );
		_road.sector_sprites_add( road.L2, r_palm, 100, 16, L2_XOFF );
		_road.sector_sprites_add( road.R1, bb_rq1, 100, 8, R1_XOFF );
		_road.sector_sprites_add( road.R2, r_palm, 100, 16, R2_XOFF+400 );
								
	_road.sector_add( lanes, -40, 0, 150 );
	
		_road.sector_sprites_add( road.L1, bush1, 150, 8, L1_XOFF );
		_road.sector_sprites_add( road.L2, hotel_l, 150, 16, L2_XOFF );
		_road.sector_sprites_add( road.R1, rock,100, 4, R1_XOFF );
		_road.sector_sprites_add( road.R1, rock, 50, 4, R1_XOFF );
		_road.sector_sprites_add( road.R2, 150 );
								 
	_road.sector_add( lanes, 0, -80, 200 );
	
		_road.sector_sprites_add( road.L1, bush1, 200, 8, L1_XOFF );
		_road.sector_sprites_add( road.L2, hotel_l, 200, 16, L2_XOFF );

		_road.sector_sprites_add( road.R1, r_palm, 200, 16, R1_XOFF );
		_road.sector_sprites_add( road.R2, bush1, 200, 8, R2_XOFF );
								  
	_road.sector_add( lanes, 40, 60, 100 );
	
		_road.sector_sprites_add( road.L1, bush1, 100, 8, L1_XOFF );
		_road.sector_sprites_add( road.L2, 100 );
		_road.sector_sprites_add( road.R1, r_palm, 100, 8, R1_XOFF );
		_road.sector_sprites_add( road.R2, bush1, 100, 8, R2_XOFF );

	_road.sector_add( lanes, 0, -150, 400 );
	
		_road.sector_sprites_add( road.L1, bush1, 400, 8, L1_XOFF );
		_road.sector_sprites_add( road.L2, r_palm, 400, 16, L2_XOFF );

		_road.sector_sprites_add( road.R1, 200 );
		_road.sector_sprites_add( road.R1, bush1, 200, 8, R1_XOFF );
		_road.sector_sprites_add( road.R2, r_palm, 400, 16, R2_XOFF );
	
	_road.sector_add( lanes, 0, 80, 50 );
	_road.sector_add( lanes, 0, -70, 50 );
	
		_road.sector_sprites_add( road.L1, bush1, 100, 8, L1_XOFF );
		_road.sector_sprites_add( road.L2, bush1, 100, 16, L2_XOFF );

		_road.sector_sprites_add( road.R1, bush1, 100, 8, R1_XOFF );
		_road.sector_sprites_add( road.R2, bush1, 100, 16, R2_XOFF );

	_road.sector_add( lanes, 0, 0, 300 );
	_road.sector_add( lanes, 60, 0, 15 );
	_road.sector_add( lanes, -60, 0, 15 );
	_road.sector_add( lanes, 0, 0, 370 );
	
		_road.sector_sprites_add( road.L1, 700 );//bush1, 700, 8, L1_XOFF );
		_road.sector_sprites_add( road.L2, r_palm, 700, 16, L2_XOFF );

		_road.sector_sprites_add( road.R1, r_palm, 400, 8, R1_XOFF );
		_road.sector_sprites_add( road.R1, bush1, 200, 8, R1_XOFF );
		_road.sector_sprites_add( road.R1, bb_rq1, 100, 8, R1_XOFF );
		
		_road.sector_sprites_add( road.R2, r_palm, 400, 16, R1_XOFF+400 );
		_road.sector_sprites_add( road.R2, r_palm, 200, 16, R1_XOFF+400 );		
		_road.sector_sprites_add( road.R2, r_palm, 100, 16, R1_XOFF+400 );

	_road.sector_add( lanes, -20, 0, 100 );
		
		_road.sector_sprites_add( road.R1, bush1, 100, 8, R1_XOFF );
		_road.sector_sprites_add_empty( road.R2, 100 );

	_road.sector_add( lanes, -10, 60, 100 );
	
	_road.sector_add( lanes, 0, 0, 200 );
	
		_road.sector_sprites_add( road.R1, r_palm, 200, 16, R1_XOFF );
		_road.sector_sprites_add_empty( road.R2, 200 );
	
	_road.sector_add( lanes, 30, -40, 150 );
	
		_road.sector_sprites_add( road.R1, rock, 150, 4, R1_XOFF );
		_road.sector_sprites_add_empty( road.R2, 150 );

	_road.sector_add( lanes, 30, 40, 50 );
	
		_road.sector_sprites_add( road.R1, rock, 50, 4, R1_XOFF );
		_road.sector_sprites_add_empty( road.R2, 50 );

	_road.sector_add( lanes, 30, -40, 20 );
	
		_road.sector_sprites_add( road.R1, rock, 20, 4, R1_XOFF );
		_road.sector_sprites_add_empty( road.R2, 20 );

	_road.sector_add( lanes, 0, 0, 300 );
	
		_road.sector_sprites_add( road.R1, r_palm, 300, 8, R1_XOFF );
		_road.sector_sprites_add( road.R2, rock, 300, 8, R1_XOFF+200 );

	_road.sector_add( lanes, 0, 0, 100 );
	
		_road.sector_sprites_add( road.R1, r_palm, 100, 8, R1_XOFF );
		_road.sector_sprites_add( road.R2, rock, 100, 8, R1_XOFF+200 );    


	_road.x_off( 350*1024 );
	_road.vehicles_set( _vehicles );
}
//==================================================================
public void load_stage()
{
Graphics	g = this.getGraphics();
Dimension	d = getSize();

    _po.set_offset( 150, 340/2 );

    _po.add( "Loading sky..." );
    g.setColor( Color.BLACK );
    g.fillRect( 0, 0, d.width, d.height );    
    _po.paint( g );
    // load sky
    _hor.sky_picture_set( "bg_cloudy3.jpg" );

    g.setColor( Color.BLACK );
    g.fillRect( 0, 0, d.width, d.height );    
    _po.add( "Loading Road..." );
    _po.paint( g );

    load_road();

    g.setColor( Color.BLACK );
    g.fillRect( 0, 0, d.width, d.height );    
    _po.add( "Loading Cars..." );
    _po.paint( g );

    load_cars();

	_smoke_sprt = new sprite( "tsmoke1.jpg", sprite.MIDDLE, sprite.MIDDLE, this );
	_smoke_sprt.shadow_off();
	_smoke_l = new particler( _xformer );
	_smoke_r = new particler( _xformer );

    g.setColor( Color.BLACK );
    g.fillRect( 0, 0, d.width, d.height );    
    _po.add( "Done !!!" );
    _po.paint( g );

    reset_pos();
	
	_off_g.update_area();
	_off_g.redraw();
	//_po.paint( g );
}

//==================================================================
public void init()
{
Dimension	d;

	d = this.getSize();

	_po = new printout();

	// setup offscreen
	_off_d = d;
	_off_g = new graphics( d.width, d.height, this );

	_font = new font( "font1.gif", 12, 16, this );

	// setup global stuff
	//_screen_d = d.width * 60 / 100;
	_screen_d = d.width * 70 / 100;

	_xformer = new transformer( d.width, d.height, _screen_d );

	// setup horizont
	_hor = new horizont( d.width, d.height, this );
	_hor.colors_set( 80, 160, 230,
					210, 220, 240,
					220, 200, 180 );
	
	//_hor.y_set( 120 );
	
	
	_vehicles = new Vector();	
	_laptimes = new long[5];

	//this.enableEvents( AWTEvent.KEY_EVENT_MASK );

	//{{INIT_CONTROLS
//	setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
//	setBackground(java.awt.Color.black);
//	setSize(512,320);
	//}}
	
//	this.requestFocus();
}

//==================================================================
public boolean keyDown( Event e, int key )
{
	switch ( key )
	{
	case Event.LEFT:	_left_isdown = true; break;
	case Event.RIGHT:	_right_isdown = true; break;
	case 'a':
	case Event.UP:		_up_isdown = true; break;	
	case ' ':
	case Event.DOWN:	_down_isdown = true; break;
	}
	
	return true;
}

//==================================================================
public boolean keyUp( Event e, int key )
{
	switch ( key )
	{
	case Event.LEFT:	_left_isdown = false; break;
	case Event.RIGHT:	_right_isdown = false; break;
	case 'a':
	case Event.UP:		_up_isdown = false; break;
	case ' ':
	case Event.DOWN:	_down_isdown = false; break;
	}
	
	return true;
}

//==================================================================
public void start()
{
	anim_th = new Thread(this);
	anim_th.start();
}

//==================================================================
public void run()
{
final int	ms_sub_interval = 1000 / 30;
long		cur_time, last_time, rendering_ticks;
Graphics	g;

int			cnt=0;
long		last_ms=0, cur_ms, delta;
float		fps;
String		fps_str="";

int			stage_loaded = 0;

	last_time = System.currentTimeMillis();
	while ( Thread.currentThread() == anim_th )
	{
		g = this.getGraphics();
		
		if ( stage_loaded == 0 )
		{
		    stage_loaded = 1;
		    load_stage();
		}

		cur_ms = cur_time = System.currentTimeMillis();
		if ( cnt == 20 )
		{
			delta = cur_ms - last_ms;
			last_ms = cur_ms;
			fps = 20000.0f / delta;
			fps_str = "FPS = " + (((int)(fps * 10)) / 10.0f);
			//System.out.println( delta );
			cnt = 0;
		}
		++cnt;

		idle_controls();
		animate();
		mypaint( g );
		
		_laptimes[0] += 33;

		//if ( _show_fps != 0 )
		{
			//g.setColor( Color.black );
			//g.fillRect( 8, 2, 84, 24 );
			g.setColor( Color.green );
			g.setPaintMode();
			g.drawString( fps_str, 16, 16 );
		}

		rendering_ticks = cur_time - last_time;
		if ( rendering_ticks < ms_sub_interval )
		{
			try
			{
				Thread.sleep( ms_sub_interval - rendering_ticks );
			}
			catch( Exception exc ) {};
		}
		last_time = cur_time;

		//long ct;
		//do {
		//	ct = System.currentTimeMillis();
		//} while ( (ct - last_time) < ms_sub_interval );

	}
}

//==================================================================
public void stop()
{
	anim_th = null;
}

	//{{DECLARE_CONTROLS
	//}}
}