// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PWMSparkMax;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.SerialPort;
import edu.wpi.first.wpilibj.TimedRobot;
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
    if (arduino != null) {
      double batteryCharge = (pdp.getVoltage() - 10.5)/2.2;
      int batteryLights = (int) (batteryCharge * 20);
      int battColor = Math.min(85, (int) ((batteryCharge + 0.4) * 85));

      arduino.writeString(String.format("s%d %d", batteryLights, battColor));
    }
  }

  @Override
  public void teleopPeriodic() {
    // Use the joystick X axis for lateral movement, Y axis for forward
    // movement, and Z axis for rotation.
    m_robotDrive.driveCartesian (.5*Math.pow(m_stick.getX(), 3), .5*-Math.pow(m_stick.getY(), 3), .5*Math.pow(m_stick.getZ(), 3), 0.0);
  }
}
