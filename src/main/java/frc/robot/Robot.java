// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.SparkMaxLimitSwitch;
import com.revrobotics.SparkMaxLimitSwitch.Type;

import edu.wpi.first.math.controller.ArmFeedforward;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the
 * name of this class or
 * the package after creating this project, you must also update the
 * build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();

  CANSparkMax mainSM, followerSM;
  int mainSMID = 16;
  int followerSMID = 17;

  SparkMaxLimitSwitch forwardLimitSwitch, reverseLimitSwitch;

  ArmFeedforward feedforward;

  RelativeEncoder encoder;

  PIDController pid;
  double kp, ki, kd;
  double[] kpid = {kp, ki, kd};
  double currentDistance;

  double setPoint;

  double currentVoltage, currentVelocity;
  double exportedValue;

  double kA = 0.0;
  double kG = 0.65;
  double kS = 0.12;
  double kV = 49.52; 

  /**
   * This function is run when the robot is first started up and should be used
   * for any
   * initialization code.
   */
  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);

    mainSM = new CANSparkMax(mainSMID, MotorType.kBrushless);
    followerSM = new CANSparkMax(followerSMID, MotorType.kBrushless);

    // mainSM.setInverted(true);
    followerSM.follow(mainSM, true);

    forwardLimitSwitch = mainSM.getForwardLimitSwitch(Type.kNormallyOpen);
    reverseLimitSwitch = mainSM.getReverseLimitSwitch(Type.kNormallyOpen);

    SmartDashboard.putNumber("voltage", 0);
    SmartDashboard.putNumber("armAngle", 0);
    SmartDashboard.putNumber("currentVelocity", 0);
    SmartDashboard.putNumber("exportedValue", 0);

    SmartDashboard.putNumber("kP - pid", kp);
    SmartDashboard.putNumber("kD - pid", kd);

    forwardLimitSwitch.enableLimitSwitch(true);
    reverseLimitSwitch.enableLimitSwitch(true);

    encoder = mainSM.getEncoder();
    encoder.setPositionConversionFactor(360.0 / 36.6);
    encoder.setVelocityConversionFactor(0.1047); // Xrev/m -> rad/s (=(X*2PI) / 60s) 

    feedforward = new ArmFeedforward(kS, kV, kG, kA);

    pid = new PIDController(kp, ki, kd);

    
  }

  /**
   * This function is called every 20 ms, no matter the mode. Use this for items
   * like diagnostics
   * that you want ran during disabled, autonomous, teleoperated and test.
   *
   * <p>
   * This runs after the mode specific periodic functions, but before LiveWindow
   * and
   * SmartDashboard integrated updating.
   */

  @Override
  public void robotPeriodic() {
    currentVoltage = SmartDashboard.getNumber("voltage", 0);
    currentVelocity = encoder.getVelocity();
    SmartDashboard.putNumber("currentVelocity", currentVelocity);
    SmartDashboard.putNumber("voltage", currentVoltage);
    exportedValue = currentVoltage / currentVelocity;

    currentVoltage = (forwardLimitSwitch.isPressed() || reverseLimitSwitch.isPressed()) ? 0 : currentVoltage;

    SmartDashboard.putNumber("armAngle", encoder.getPosition()); // check: position to angle?
    SmartDashboard.putBoolean("forward ls pressed", forwardLimitSwitch.isPressed());
    SmartDashboard.putBoolean("reverse ls pressed", reverseLimitSwitch.isPressed());

  }

  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different
   * autonomous modes using the dashboard. The sendable chooser code works with
   * the Java
   * SmartDashboard. If you prefer the LabVIEW Dashboard, remove all of the
   * chooser code and
   * uncomment the getString line to get the auto name from the text box below the
   * Gyro
   *
   * <p>
   * You can add additional auto modes by adding additional comparisons to the
   * switch structure
   * below with additional strings. If using the SendableChooser make sure to add
   * them to the
   * chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {
    switch (m_autoSelected) {
      case kCustomAuto:
        // Put custom auto code here
        break;
      case kDefaultAuto:
      default:
        // Put default auto code here
        break;
    }
  }

  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopInit() {
  }

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {
    mainSM.setVoltage(currentVoltage + feedforward.calculate(Math.toRadians(
      encoder.getPosition()),
      encoder.getVelocity()
    ));


    pid.calculate(Math.abs(encoder.getPosition()-setPoint), setPoint);
  }

  /** This function is called once when the robot is disabled. */
  @Override
  public void disabledInit() {
  }

  /** This function is called periodically when disabled. */
  @Override
  public void disabledPeriodic() {
  }

  /** This function is called once when test mode is enabled. */
  @Override
  public void testInit() {
  }

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {
  }

  /** This function is called once when the robot is first started up. */
  @Override
  public void simulationInit() {
  }

  /** This function is called periodically whilst in simulation. */
  @Override
  public void simulationPeriodic() {
  }
} //
