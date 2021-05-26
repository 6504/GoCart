// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PWMSparkMax;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.SerialPort;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.drive.MecanumDrive;

/** This is a demo program showing how to use Mecanum control with the RobotDrive class. */
public class Robot extends TimedRobot {
  private static final int kFrontLeftChannel = 2;
  private static final int kRearLeftChannel = 3;
  private static final int kFrontRightChannel = 1;
  private static final int kRearRightChannel = 0;

  private static final int kJoystickChannel = 0;

  private MecanumDrive m_robotDrive;
  private Joystick m_stick;
  private SerialPort arduino = null;
  private PowerDistributionPanel pdp = new PowerDistributionPanel();

  private double curPowerMultiplier = 0.5;

  private double lastVoltageUpdateTime = Timer.getFPGATimestamp();
  private double lastUnderglowUpdateTime = Timer.getFPGATimestamp();

  @Override
  public void robotInit() {
    PWMSparkMax frontLeft = new PWMSparkMax(kFrontLeftChannel);
    PWMSparkMax rearLeft = new PWMSparkMax(kRearLeftChannel);
    PWMSparkMax frontRight = new PWMSparkMax(kFrontRightChannel);
    PWMSparkMax rearRight = new PWMSparkMax(kRearRightChannel);

    // Invert the left side motors.
    // You may need to change or remove this to match your robot.
    //frontLeft.setInverted(true);
    //rearLeft.setInverted(true);
    //frontRight.setInverted(true);
    //rearRight.setInverted(true);

    m_robotDrive = new MecanumDrive(frontLeft, rearLeft, frontRight, rearRight);

    m_stick = new Joystick(kJoystickChannel);

    try {
      arduino = new SerialPort(9600, SerialPort.Port.kUSB1);
    } catch (Exception ex) {
      System.out.println("Could not connect to Arduino");
    }
  }

  @Override
  public void robotPeriodic() {
    if (arduino != null && (Timer.getFPGATimestamp() - lastVoltageUpdateTime) > 1) {
      double batteryCharge = (pdp.getVoltage() - 10.5)/2.2;
      int batteryLights = (int) (batteryCharge * 20);
      int battColor;

      if(batteryCharge >= 0.5) {
        battColor = Math.min(85, (int) ((batteryCharge+0.4)*85));
      } else {
        battColor = (int)(120*batteryCharge);
      }

      arduino.writeString(String.format("s%d %d", batteryLights, battColor));
      lastVoltageUpdateTime = Timer.getFPGATimestamp();
    }
  }

  @Override
  public void teleopPeriodic() {
    // Use the joystick X axis for lateral movement, Y axis for forward
    // movement, and Z axis for rotation.

    double prevPowerMultiplier = curPowerMultiplier;

    if (m_stick.getTrigger()) {
      if (m_stick.getPOV() >= 0) {
        curPowerMultiplier = 1;
      } else {
        curPowerMultiplier = 0.75;
      }
      /*if ((Timer.getFPGATimestamp() - lastUnderglowUpdateTime) > 1) {
        arduino.writeString("U");
        lastUnderglowUpdateTime = Timer.getFPGATimestamp();
      }*/
    } else {
      curPowerMultiplier = 0.5;
      /*if ((Timer.getFPGATimestamp() - lastUnderglowUpdateTime) > 1) {
        arduino.writeString("u");
        lastUnderglowUpdateTime = Timer.getFPGATimestamp();
      }*/
    }

    if (curPowerMultiplier > prevPowerMultiplier) {

      arduino.writeString("U");
    } else if (curPowerMultiplier < prevPowerMultiplier) {
      arduino.writeString("u");
    }

    m_robotDrive.driveCartesian (curPowerMultiplier*Math.pow(m_stick.getX(), 3), curPowerMultiplier*-Math.pow(m_stick.getY(), 3), curPowerMultiplier*Math.pow(m_stick.getZ(), 3), 0.0);
    


  }
}
