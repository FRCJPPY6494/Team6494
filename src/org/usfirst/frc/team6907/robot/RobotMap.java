/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team6907.robot;

/**
 * The RobotMap is a mapping from the ports sensors and actuators are wired into
 * to a variable name. This provides flexibility changing wiring, makes checking
 * the wiring easier and significantly reduces the number of magic numbers
 * floating around.
 * @author Team 6907 (The G.O.A.T.) Ctrl Div.
 */
public class RobotMap {

	public static final int 
			OI_DRIVE=0,
			OI_OPERATE=1;
	
	
	public static final int
			DRIVE_MOTOR_LEFT=0,	
			DRIVE_MOTOR_RIGHT=1,
			INTAKER_SPARK=2,		
			CLIMBER_SPARK=3,	
			CLIMBER_VICTOR=4;	

	public static final int	
			INTAKER_PHOTOGATE=0;
	
	
	public static final int
			ARDUINO_LED_CHANNEL=0;	
	
	public static final int
			ELEVATOR_TALON=1,
			INTAKER_TALON=0;		
}