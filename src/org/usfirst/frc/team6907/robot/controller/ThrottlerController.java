package org.usfirst.frc.team6907.robot.controller;

import org.usfirst.frc.team6907.robot.commands.AutoCmd;
import org.usfirst.frc.team6907.robot.subsystems.Elevator;
import org.usfirst.frc.team6907.robot.subsystems.OperateOI;
import org.usfirst.frc.team6907.robot.subsystems.Throttler;

public class ThrottlerController extends BaseController{
	private static final double EPS = 0.001;
	
	public static final double
			HEIGHT_ZERO = 0,	
			HEIGHT_LAUNCH = 0,
			HEIGHT_HORIZONTAL = 0;
	
	public static final double
			HEIGHT_MANUAL_ADJUST=0.1;
	
	public static ThrottlerController mInstance;
	
	private OperateOI mOI;
	
	private Throttler mThrottler;
	
	private boolean mLastManual;
	private boolean mLastManualAdjust;
	
	public static ThrottlerController get(){
		if(mInstance == null) mInstance = new ThrottlerController();
		return mInstance;
	}
	
	public ThrottlerController(){
		super();
		mOI = OperateOI.get();
		mThrottler = Throttler.get();
		mLastManual = false;
		mLastManualAdjust = false;
	}
	
	public void initAuto(int pos){
		mCmds.add(new GotoPosCmd(1000, HEIGHT_HORIZONTAL));
	}
	
	@Override
	public void stopAuto() {
		super.stopAuto();
		mThrottler.feedStop();
	}
	
	public void runTeleOp(){
		if(!mOI.isElevatorManualActivated()) {
			System.out.println(Throttler.get().pidGet());
			if(mLastManual) {
				mThrottler.setStatic();
			}else {
				mThrottler.startPID();
				switch (mOI.getThrottlerSetPoint()) {
					case OperateOI.THROTTLER_ZERO:
						mThrottler.gotoPos(HEIGHT_ZERO);
						break;
					case OperateOI.THROTTLER_LAUNCH:
						mThrottler.gotoPos(HEIGHT_LAUNCH);
						break;	
					case OperateOI.THROTTLER_HORIZONTAL:
						mThrottler.gotoPos(HEIGHT_HORIZONTAL);
						break;
				}
				if(Math.abs(mOI.getThrottlerSpeed())>EPS 
						&& !mLastManualAdjust) {
					mThrottler.gotoRelativePos(
							(mOI.getThrottlerSpeed()>0?1:-0.5)*HEIGHT_MANUAL_ADJUST);
				}
				if(!mThrottler.getPIDEnabled()) mThrottler.setStatic();
			}
			mLastManual=false;
		}else {
			double input=mOI.getThrottlerSpeed();
			if(Math.abs(input)<EPS) {
				if(!mThrottler.getPIDEnabled()) mThrottler.setStatic();
			} else {
				mThrottler.stopPID();
				mThrottler.manualControl(input);
			}
			mLastManual=true;
		}
		mLastManualAdjust=Math.abs(mOI.getThrottlerSpeed())>EPS;
		
	}

	static class GotoPosCmd extends AutoCmd{
		private double mPosition;
		public GotoPosCmd(long startTimestamp,double pos) {
			super();
			mStartTimestamp=startTimestamp;
			mPosition=pos;
		}
		@Override
		public void run(long time) {
			if(mStatus==NOT_STARTED) {
				super.run();
				Elevator.get().gotoPos(mPosition);
			}
		}
		@Override
		public void stop() {
			super.stop();
			Elevator.get().feedStop();
		}
	}
}