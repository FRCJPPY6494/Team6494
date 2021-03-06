package org.usfirst.frc.team6494.robot.subsystems;

import org.usfirst.frc.team6494.robot.RobotMap;
import org.usfirst.frc.team6494.robot.controller.ElevatorController;
import org.usfirst.frc.team6494.robot.subsystems.Elevator;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDOutput;
import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.PIDSourceType;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Elevator implements PIDSource,PIDOutput{
	/**Parameters*/
	private static final double
			
			FEED=0.05,
			MAX_ACCELERATION_PER_MS=0.0015,	
			MAX_ACCELERATION_DISCRETE=0.1,		
			MAX_POWER=0.8,
			MAX_POWER_MANUAL_UP=0.7,
			MAX_POWER_MANUAL_DOWN=0.4,
			HEIGHT_PER_ROTATION=0.06,
			GEAR=3,	
			SIGNALS_PER_ROTATION=4096,
			REST_BOUND=0.02,
			SLOW_BOUND=0.2,
			SLOW_BOUND_VELOCITY=0.13;
			
	private static final double MOTOR_DIRECTION=-1;
	private static final boolean INVERTED=true;
	
	private static final int		
			PEAK_CURRENT_LIMIT=80,
			PEAK_CURRENT_DURATION=200,
			CONTINUOUS_CURRENT_LIMIT=30;
	
	private WPI_TalonSRX mTalon;
	private PIDController mPIDController;
	
	private boolean mPIDEnabled;
	
	private volatile double mLastSpeed;
	private volatile long mLastTime;
		
	private static Elevator sInstance;
	
	public static Elevator get() {
		if(sInstance==null) sInstance=new Elevator(RobotMap.ELEVATOR_TALON);
		return sInstance;
	}
	
	public Elevator(int deviceID) {
		mTalon=new WPI_TalonSRX(deviceID);
		mTalon.setInverted(INVERTED);
		mTalon.setSelectedSensorPosition(0,0,0);
		mPIDController=new PIDController(7.0108, 0.008, 2.705,this,this); // P = 7.0108, I = 0.008, D = 2.705 
		mPIDEnabled=true;	
		mPIDController.enable();
		mPIDController.setSetpoint(ElevatorController.HEIGHT_ZERO);
		mLastSpeed=0;
		mLastTime=System.currentTimeMillis();
	}
	
	public void reset() {
		mTalon.setSelectedSensorPosition(0, 0, 0);
	}
	
	public void gotoPos(double pos) {
		if(isSensorPluggedIn()
				&& (mPIDController.getSetpoint()!=pos || !mPIDController.isEnabled())) {
			mPIDController.reset();
			mPIDController.setSetpoint(pos);
			mPIDEnabled=true;
			mPIDController.enable();
		}
	}
	
	public void gotoRelativePos(double deltaPos) {
		gotoPos(pidGet()+deltaPos);
	}
	
	public void manualControl(double input) {
		input*=(input>0)?MAX_POWER_MANUAL_UP:MAX_POWER_MANUAL_DOWN;
		setSpeed(boundSpeed(boundAcceleration(input)));
	}
	
	public void setStatic() {
		if(isSensorPluggedIn()) {
			gotoPos(pidGet());
		}else {
			setSpeed(FEED);
		}
	}	
	
	public void feedStop() {
		mPIDController.reset();
		mTalon.set(ControlMode.PercentOutput,0);
		mLastSpeed=0;
		mLastTime=System.currentTimeMillis();
	}
	
	public boolean getPIDEnabled() {
		return mPIDEnabled;
	}
	
	public void startPID() {
		if(!mPIDEnabled) {	
			mPIDEnabled=true;
			mPIDController.enable();
		}
	}
	
	public void stopPID() {
		if(mPIDEnabled) {	
			mPIDEnabled=false;
			mPIDController.disable();
		}
	}
	
	@Override
	public void setPIDSourceType(PIDSourceType pidSource) {}
	
	@Override
	public PIDSourceType getPIDSourceType() {
		return PIDSourceType.kDisplacement;
	}
	
	@Override
	public double pidGet() {
		return signalToMeter(mTalon.getSelectedSensorPosition(0));
	}
	
	@Override
	public void pidWrite(double output) {
		if(isSensorPluggedIn()) {
			output*=MAX_POWER;
			setSpeed(boundSpeed(boundAcceleration(output)));
		}else {
			setStatic();
		}
	}
	
	private double boundSpeed(double speed) {
		if(speed<0) {
			if(pidGet()<=REST_BOUND) {
				return 0;
			}else if(pidGet()<=SLOW_BOUND) {
				return Math.max(speed, -SLOW_BOUND_VELOCITY);
			}
		}else {
			
		}
		return speed;
	}
	
	private double boundAcceleration(double speed) {
		double bound=(System.currentTimeMillis()-mLastTime)*MAX_ACCELERATION_PER_MS;
		bound=Math.min(bound,MAX_ACCELERATION_DISCRETE);
		if(Math.abs(speed-mLastSpeed)>bound) {
			speed=mLastSpeed+Math.signum(speed-mLastSpeed)*bound;
		}
		return speed;
	}
	
	private void setSpeed(double speed) {
		mLastSpeed=speed;
		mLastTime=System.currentTimeMillis();
		if(speed < 0) mTalon.set(ControlMode.PercentOutput,speed*MOTOR_DIRECTION*0.35);
		else mTalon.set(ControlMode.PercentOutput,speed*MOTOR_DIRECTION);
	}

	private static double signalToMeter(int count) {
		return count*HEIGHT_PER_ROTATION/(GEAR*SIGNALS_PER_ROTATION);
	}
	
	private boolean isSensorPluggedIn() {
		return mTalon.getSensorCollection().getPulseWidthRiseToRiseUs()!=0;	
	}
	
	public void log() {
		SmartDashboard.putNumber("Elevator.Height", pidGet());
	}

}
