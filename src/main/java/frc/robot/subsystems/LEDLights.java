package frc.robot.subsystems;

import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import edu.wpi.first.wpilibj.AddressableLEDBufferView;
import edu.wpi.first.wpilibj.LEDPattern;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import static edu.wpi.first.units.Units.*;


public class LEDLights extends SubsystemBase {
    public static final Color allyColor = (DriverStation.getAlliance().equals(Alliance.Red))? Color.kRed: Color.kBlue;
    //public static final Color allyColor = colorRGBToGRB(Color.kBlue); //temporary set, change later when we can test on the field
    //NOTE LED STRIP IS GRB, NOT RGB, SO ALL COLORS NEED TO BE SET AS SUCH (EX: Color.kRed IS ACTUALLY GREEN, Color.kBlue IS ACTUALLY RED, AND Color.kGreen IS ACTUALLY BLUE)
    private static final AddressableLED m_led = new AddressableLED(0); //replace PWM port with constant later

    private static AddressableLEDBuffer m_ledBuffer = new AddressableLEDBuffer(320); //set the length of the strip here
    private static AddressableLEDBufferView m_rightTurretBufferView = m_ledBuffer.createView(0, 79);
    private static AddressableLEDBufferView m_leftTurretBufferView = m_ledBuffer.createView(80, 159);
    private static AddressableLEDBufferView m_underglowBufferView = m_ledBuffer.createView(160, 319);
    
    //set these in the robot container using the subsystems/command's methods
    private BooleanSupplier isClimbing;
    private BooleanSupplier isAutoComplete;
    private BooleanSupplier isFiring;
    private BooleanSupplier cantAim;
    private DoubleSupplier leftShooterTorque;
    private DoubleSupplier rightShooterTorque;

    public enum UnderglowStates {
        PASSIVE(LEDPattern.solid(allyColor)),
        AUTO_COMPLETED(LEDPattern.solid(Color.kGreen)),
        CLIMBING(LEDPattern.rainbow(255, 120).scrollAtRelativeSpeed(Percent.per(Second).of(0.5)));

        private LEDPattern pattern;

        private UnderglowStates(LEDPattern pattern) {
            this.pattern = pattern;
        }

        public void apply(final double brightness) {
            LEDPattern patternToSet = pattern.atBrightness(Percent.of(brightness));
            patternToSet.applyTo(m_underglowBufferView);
            m_led.setData(m_ledBuffer);
        }

        public LEDPattern getPattern() {
            return pattern;
        }
    }
    
    public enum TurretStates {
        PASSIVE(LEDPattern.solid(allyColor).breathe(Second.of(2))),
        FIRING(LEDPattern.solid(allyColor)),
        CANT_AIM(LEDPattern.solid(allyColor).mask(LEDPattern.steps(Map.of(0, allyColor)).scrollAtRelativeSpeed(Percent.per(Second).of(0.5)))),
        AUTO_COMPLETED(LEDPattern.solid(Color.kGreen)),
        CLIMBING(LEDPattern.rainbow(255, 120).scrollAtRelativeSpeed(Percent.per(Second).of(0.5)));

        private LEDPattern pattern;

        private TurretStates(LEDPattern pattern) {
            this.pattern = pattern;
        }

        public void apply(final double leftBrightness, final double rightBrightness) {
            LEDPattern patternToSetLeft = pattern.atBrightness(Percent.of(leftBrightness));
            LEDPattern patternToSetRight = pattern.atBrightness(Percent.of(rightBrightness));
            patternToSetLeft.applyTo(m_leftTurretBufferView);
            patternToSetRight.applyTo(m_rightTurretBufferView);
            m_led.setData(m_ledBuffer);
        }

        public LEDPattern getPattern() {
            return pattern;
        }
    }

    public LEDLights(BooleanSupplier isClimbing, BooleanSupplier isFiring, BooleanSupplier cantAim, DoubleSupplier leftShooterTorque, DoubleSupplier rightShooterTorque) {
        this.isClimbing = isClimbing;
        this.isFiring = isFiring;
        this.cantAim = cantAim;
        this.leftShooterTorque = leftShooterTorque;
        this.rightShooterTorque = rightShooterTorque;
        m_led.setLength(m_ledBuffer.getLength());
        m_led.setData(m_ledBuffer);
        m_led.start();
    }

    private static Color colorRGBToGRB(Color color){
        return new Color(color.green, color.red, color.blue);
    }

    public void autoIsCompleted() {
        UnderglowStates.AUTO_COMPLETED.apply(1);
        TurretStates.AUTO_COMPLETED.apply(1, 1);

        SmartDashboard.putString("LED State UnderGlow", "AUTO COMPLETED");
        SmartDashboard.putString("LED State Turret", "AUTO COMPLETED");
    }

    @Override
    public void periodic(){ //auto will be set later, as it is only a temperary set
        
        if(isClimbing.getAsBoolean()){
            UnderglowStates.CLIMBING.apply(1);
            TurretStates.CLIMBING.apply(1, 1);

            SmartDashboard.putString("LED State UnderGlow", "CLIMBING");
            SmartDashboard.putString("LED State Turret", "CLIMBING");
        } else {
            UnderglowStates.PASSIVE.apply(1);
            SmartDashboard.putString("LED State UnderGlow", "PASSIVE");
        }

        if(isFiring.getAsBoolean()){
            TurretStates.FIRING.apply(leftShooterTorque.getAsDouble(), rightShooterTorque.getAsDouble());
            SmartDashboard.putString("LED State Turret", "FIRING");
        }else if(cantAim.getAsBoolean()){
            TurretStates.CANT_AIM.apply(1, 1);
            SmartDashboard.putString("LED State Turret", "CANT AIM");
        } else{
            TurretStates.PASSIVE.apply(1, 1);
            SmartDashboard.putString("LED State Turret", "PASSIVE");
        }
        
    }
}