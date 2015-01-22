package org.usfirst.frc.team4213.robot;

import com.kauailabs.nav6.frc.IMU;

import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import java.lang.Math;

public class SlideDrive {
	SpeedController leftWheels, rightWheels, strafingWheels;
	
	  SerialPort serial_port;
	  IMU imu;
	  double desiredHeading = 0;
	  boolean wasRegulatingHeading=true;
	
	public SlideDrive(SpeedController left, SpeedController right, SpeedController strafing) {
		leftWheels=left;
		rightWheels=right;
		strafingWheels=strafing;
		
		try {
	    	serial_port = new SerialPort(57600,SerialPort.Port.kOnboard);
			
			// You can add a second parameter to modify the 
			// update rate (in hz) from 4 to 100.  The default is 100.
			// If you need to minimize CPU load, you can set it to a
			// lower value, as shown here, depending upon your needs.
			
			// You can also use the IMUAdvanced class for advanced
			// features.
			//imu = new IMU(serial_port,(byte)50);
			imu = new IMU(serial_port, (byte)50);
    	} catch( Exception ex ) {
    		ex.printStackTrace();
    	}
        if ( imu != null ) {
            LiveWindow.addSensor("IMU", "Gyro", imu);
        }
        //SmartDashboard.putNumber("Gyro KP", 50);
	}

	
	public void gyroInit() {
		boolean is_calibrating = imu.isCalibrating();
        if (!is_calibrating ) {
            Timer.delay( 0.3 );
            imu.zeroYaw();
        }
	}
	
	public void zeroGyro() {
		imu.zeroYaw();
		desiredHeading = 0;
	}
	    
	public void rawDrive(double y, double x, double w) {
		leftWheels.set((y+w));
		rightWheels.set(-(y-w));
		strafingWheels.set(x);
	}
	
	public void regulatedDrive(double y, double x, double w) {		
		double currentHeading = imu.getYaw();
		
		if (w < 0.1 && w > -0.1) {
			SmartDashboard.putNumber(   "Robot Heading",       currentHeading       );
			
			if (!wasRegulatingHeading)
				desiredHeading=currentHeading;
			
			double error = desiredHeading-currentHeading;
			
			if (error > 180)
				error -= 360;
			else if (error < -180)
				error += 360;
			
			SmartDashboard.putNumber("Robot Heading Error", error);
			
			rawDrive(y, 0, error/SmartDashboard.getNumber("Gyro KP"));
			
			wasRegulatingHeading = true;
		} else {
			rawDrive(y, 0, w);
			
			wasRegulatingHeading = false;
		}
	}
	
	public void setHeading(double heading) {
		desiredHeading = heading;
	}
}
