package org.usfirst.frc.team6494.robot.controller;

import org.usfirst.frc.team6494.robot.Robot;
import org.usfirst.frc.team6494.robot.commands.AutoCmd;
import org.usfirst.frc.team6494.robot.subsystems.Elevator;
import org.usfirst.frc.team6494.robot.subsystems.OperateOI;
import org.usfirst.frc.team6494.robot.subsystems.Throttler;
import org.usfirst.frc.team6494.robot.controller.BaseController;
import org.usfirst.frc.team6494.robot.controller.ThrottlerController;
import org.usfirst.frc.team6494.robot.controller.ThrottlerController.GotoPosCmd;

public class ThrottlerController extends BaseController{
	private static final double EPS=0.001;
	
	private final static double _zeroPosition = 0;
	private final static double _delta = 0.045;
	public static final double 
			THROTTLER_ZERO = _zeroPosition + 0.10,       //0.09
			THROTTLER_LAUNCH = _zeroPosition + 0.065,      //0.065
			THROTTLER_HORIZONTAL = _zeroPosition + 0.00;
	
	public static final double
			HEIGHT_MANUAL_ADJUST=0.1;
	
	private static ThrottlerController sInstance;
	
	private OperateOI mOI;
	private Throttler mThrottler;
	
	private boolean mLastManual;
	private boolean mLastManualAdjust;
	
	public static ThrottlerController get() {
		if(sInstance==null) sInstance=new ThrottlerController();
		return sInstance;
	}
	
	public ThrottlerController() {
		super();
		mOI=OperateOI.get();
		mThrottler=Throttler.get();
		mLastManual=false;
		mLastManualAdjust=false;
	}
	
	public void initAuto(int rPos, int sPos) {
		if(rPos==Robot.MIDDLE&&sPos!=Robot.NONE) {
			mCmds.add(new GotoPosCmd(0, THROTTLER_LAUNCH));
			mCmds.add(new GotoPosCmd(100, THROTTLER_ZERO+0.01));
			mCmds.add(new GotoPosCmd(5500, THROTTLER_LAUNCH));
			mCmds.add(new GotoPosCmd(6000, THROTTLER_HORIZONTAL));
			mCmds.add(new GotoPosCmd(11000, THROTTLER_ZERO));
		}else if(rPos==sPos) {
			mCmds.add(new GotoPosCmd(0, THROTTLER_LAUNCH));
			mCmds.add(new GotoPosCmd(100, THROTTLER_ZERO+0.01));
			mCmds.add(new GotoPosCmd(6000, THROTTLER_LAUNCH));
			mCmds.add(new GotoPosCmd(6500, THROTTLER_HORIZONTAL));
			//mCmds.add(new GotoPosCmd(11000, THROTTLER_ZERO));
		}else {
			mCmds.add(new GotoPosCmd(0, THROTTLER_LAUNCH));
			mCmds.add(new GotoPosCmd(100, THROTTLER_ZERO+0.01));
			mCmds.add(new GotoPosCmd(1000, THROTTLER_ZERO));
			if(rPos!=Robot.MIDDLE) {
				mCmds.add(new GotoPosCmd(5000, THROTTLER_HORIZONTAL));
			}
		}
	}
	
	@Override
	public void stopAuto() {
		super.stopAuto();
		mThrottler.feedStop();
	}
	
	public void runTeleOp() {
		System.out.println(mThrottler.pidGet());
		if(!mOI.isThrottlerManualActivated()) {
			if(mLastManual) {
				mThrottler.setStatic();
			}else {
				mThrottler.startPID();
				switch (mOI.getThrottlerSetPoint()) {
					case OperateOI.THROTTLER_ZERO:
						mThrottler.gotoPos(THROTTLER_ZERO);
						break;
					case OperateOI.THROTTLER_LAUNCH:
						mThrottler.gotoPos(THROTTLER_LAUNCH);
						break;			
					case OperateOI.THROTTLER_HORIZONTAL:
						mThrottler.gotoPos(THROTTLER_HORIZONTAL);
						break;
					default:
						break;
				}
				if(Math.abs(mOI.getThrottlerSpeed())>EPS 
						&& !mLastManualAdjust) {
					mThrottler.gotoRelativePos(
							(mOI.getThrottlerSpeed()>0?1:-0.3)*HEIGHT_MANUAL_ADJUST);
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
				Throttler.get().gotoPos(mPosition);
			}
		}
		@Override
		public void stop() {
			super.stop();
			Throttler.get().feedStop();
		}
	}
}
