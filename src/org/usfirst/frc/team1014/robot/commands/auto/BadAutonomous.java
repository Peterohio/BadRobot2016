package org.usfirst.frc.team1014.robot.commands.auto;

import org.usfirst.frc.team1014.robot.commands.auto.defenses.ChevalDeFrise;
import org.usfirst.frc.team1014.robot.commands.auto.defenses.Drawbridge;
import org.usfirst.frc.team1014.robot.commands.auto.defenses.GenericCrossDefense;
import org.usfirst.frc.team1014.robot.commands.auto.defenses.LowBar;
import org.usfirst.frc.team1014.robot.commands.auto.defenses.Portcullis;
import org.usfirst.frc.team1014.robot.commands.auto.defenses.SallyPort;
import org.usfirst.frc.team1014.robot.subsystems.ShooterAndGrabber;

import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.CommandGroup;

/**
 * This {@code CommandGroup} is how we will run our autonomous programs. The constructor takes
 * various variables as inputs and creates its own autonomous program to carry it out.
 * 
 * @author Manu S.
 *
 */
public class BadAutonomous extends CommandGroup
{
	public boolean isShooting;
	public boolean goingForLow;
	public int defenseToCross;

	public Command crossDefense;
	public Command moveToTurnSpot;
	public Command moveShooter;
	public Command turnToGoal;
	public Command shootBall;

	/**
	 * Creates a simple autonomous that goes through the low bar and doesn't shoot.
	 */
	public BadAutonomous()
	{
		this(false, false, 1, "ROUGH TERRAIN", 0);
	}

	/**
	 * Creates a specialized autonomous based on the parameters fed into it.
	 * 
	 * @param willShoot
	 *            - whether or not the robot will shoot the ball at the end, true for yes, false for
	 *            no
	 * @param lowScore
	 *            - whether or not the robot will score in the low goal, true for yes, false for no
	 * @param crossingDefense
	 *            - the placement of the defense the robot will cross (1, 2, 3, 4 or 5)
	 * @param defense
	 *            - the name of the defense it's crossing (spelled correctly) (e.g. Portcullis,
	 *            Drawbridge, Rough Terrain etc)
	 */
	public BadAutonomous(boolean willShoot, boolean lowScore, int crossingDefense, String defense)
	{
		this(willShoot, lowScore, crossingDefense, defense, 0);
	}

	/**
	 * Creates a specialized autonomous based on the parameters fed into it.
	 * 
	 * @param willShoot
	 *            - whether or not the robot will shoot the ball at the end, true for yes, false for
	 *            no
	 * @param lowScore
	 *            - whether or not the robot will score in the low goal, true for yes, false for no
	 * @param crossingDefense
	 *            - the placement of the defense the robot will cross (1, 2, 3, 4 or 5)
	 * @param defense
	 *            - the name of the defense it's crossing (spelled correctly) (e.g. Portcullis,
	 *            Drawbridge, Rough Terrain etc)
	 * @param waitTime
	 *            - the time to wait before carrying out the autonomous
	 */
	public BadAutonomous(boolean willShoot, boolean lowScore, int crossingDefense, String defense, double waitTime)
	{
		isShooting = willShoot;
		goingForLow = lowScore;
		defenseToCross = crossingDefense;

		/*
		 * Picks the defense that the robot will be crossing
		 */
		switch(defense.toUpperCase())
		{
			case "P":
				crossDefense = new Portcullis();
			case "S":
				crossDefense = new SallyPort();
			case "D":
				crossDefense = new Drawbridge();
			case "C":
				crossDefense = new ChevalDeFrise();
			case "L":
				crossDefense = new LowBar();
			default:
				crossDefense = new GenericCrossDefense();
		}

		/*
		 * Make sure people aren't stupid since low bar is always in the first position
		 */
		if(crossingDefense == 1)
			crossDefense = new LowBar();

		/*
		 * Makes the robot move the turn spot if it isn't already there
		 */
		switch(defenseToCross)
		{
			case 2:
				moveToTurnSpot = new AutoDriveDistanceEncoder(.5, 3.046);
			case 5:
				moveToTurnSpot = new AutoDriveDistanceEncoder(.5, 3.690);
			default:
				moveToTurnSpot = new AutoDriveDistanceEncoder(.5, 0);
		}

		/*
		 * Sets the turn amount based on if the robot is shooting high or low
		 */
		if(goingForLow)
		{
			switch(defenseToCross)
			{
				case 1:
					turnToGoal = new AutoTurn(60);
				case 2:
					turnToGoal = new AutoTurn(60);
				case 3:
					turnToGoal = new AutoTurn(-30);
				case 4:
					turnToGoal = new AutoTurn(30);
				case 5:
					turnToGoal = new AutoTurn(-60);
				default:
					turnToGoal = new AutoTurn(0);
			}
		}
		else
		{
			switch(defenseToCross)
			{
				case 1:
					turnToGoal = new AutoTurn(60);
				case 2:
					turnToGoal = new AutoTurn(60);
				case 3:
					turnToGoal = new AutoTurn(22);
				case 4:
					turnToGoal = new AutoTurn(-8);
				case 5:
					turnToGoal = new AutoTurn(-60);
				default:
					turnToGoal = new AutoTurn(0);
			}
		}

		/*
		 * Sets the shooter to shooting position (or not) and creates the command to shoot
		 */
		if(isShooting && !goingForLow)
		{
			moveShooter = new AutoRotate(ShooterAndGrabber.SHOOTER_DEFAULT_SHOOTING_POS);
			shootBall = new FindTarget();
		}
		else if(isShooting && goingForLow)
		{
			moveShooter = new AutoRotate(ShooterAndGrabber.SHOOTER_LOWEST_POS);
			shootBall = new AutoShoot(1);
		}
		else
		{
			moveShooter = new AutoRotate(ShooterAndGrabber.SHOOTER_HIGHEST_POS);
			shootBall = new AutoShoot(0);
		}

		// adds some of the commands to the Scheduler
		this.addSequential(crossDefense, waitTime);
		this.addParallel(moveToTurnSpot);
		this.addParallel(moveShooter);
		this.addSequential(turnToGoal);

		/*
		 * If scoring low, add some more commands to get robot to the right spot
		 */
		if(goingForLow)
		{
			if(defenseToCross == 3 && goingForLow)
			{
				this.addSequential(new AutoDriveDistanceEncoder(.5, 3.638));
				this.addSequential(new AutoTurn(90));
			}
			else if(defenseToCross == 4 && goingForLow)
			{
				this.addSequential(new AutoDriveDistanceEncoder(.5, 4.192));
				this.addSequential(new AutoTurn(-90));
			}
			else
			{
			}
		}

		// add the final part
		this.addSequential(shootBall);
	}

}
