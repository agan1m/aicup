import model.*;
import model.Item;
import model.Item.Weapon;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.stream.IntStream;

public class MyStrategy {

static double distanceSqr(Vec2Double a, Vec2Double b) {
    return (a.getX() - b.getX()) * (a.getX() - b.getX()) + (a.getY() - b.getY()) * (a.getY() - b.getY());
  }

  public UnitAction getAction(Unit unit, Game game, Debug debug) {
	
    Unit nearestEnemy = null;
    for (Unit other : game.getUnits()) {
      if (other.getPlayerId() != unit.getPlayerId()) {
        if (nearestEnemy == null || distanceSqr(unit.getPosition(),
            other.getPosition()) < distanceSqr(unit.getPosition(), nearestEnemy.getPosition())) {
          nearestEnemy = other;
        }
      }
    }
    LootBox nearestWeapon = null;
    for (LootBox lootBox : game.getLootBoxes()) {
      if (lootBox.getItem() instanceof Item.Weapon) {
        if (nearestWeapon == null || distanceSqr(unit.getPosition(),
            lootBox.getPosition()) < distanceSqr(unit.getPosition(), nearestWeapon.getPosition())) {
          nearestWeapon = lootBox;
        }
      }
    }
    
    LootBox nearestHealth = null;
    for (LootBox lootBox : game.getLootBoxes()) {
    	if (lootBox.getItem() instanceof Item.HealthPack) {
    		if(nearestHealth == null || distanceSqr(unit.getPosition(),
            lootBox.getPosition()) < distanceSqr(unit.getPosition(), nearestHealth.getPosition())) {
    			nearestHealth = lootBox;	
    		}
    	}
    }
    boolean isBestWeapon = false;
    LootBox nearestBestWeapon = null;
    for (LootBox lootBox : game.getLootBoxes()) {
    	model.WeaponType myWeaponType = null;
    	if (unit.getWeapon() != null) {
    		myWeaponType = unit.getWeapon().getTyp();
    	}
    	if(lootBox.getItem() instanceof Item.Weapon) {
    		model.WeaponType itemWeaponType = ((Weapon) lootBox.getItem()).getWeaponType();
    		if(myWeaponType != null && itemWeaponType.ordinal() != myWeaponType.ordinal() && itemWeaponType == WeaponType.ASSAULT_RIFLE) {
    			if(nearestBestWeapon == null || distanceSqr(unit.getPosition(),
        	            lootBox.getPosition()) < distanceSqr(unit.getPosition(), nearestBestWeapon.getPosition())) {
        			nearestBestWeapon = lootBox;
        			isBestWeapon = true;
        		}
    		}
    	}
    }
    
    
    
    Vec2Double targetPos = unit.getPosition();
    if (unit.getWeapon() == null && nearestWeapon != null) {
      targetPos = nearestWeapon.getPosition();
    } else if (nearestHealth != null && unit.getHealth() < (game.getProperties().getUnitMaxHealth() / 2)) {
      targetPos = nearestHealth.getPosition();
    } else if (nearestBestWeapon != null) {
    	targetPos = nearestBestWeapon.getPosition();
    } else if (nearestEnemy != null) {
        if(unit.getWeapon() != null && unit.getWeapon().getTyp() == WeaponType.ROCKET_LAUNCHER) {
            double radius = unit.getWeapon().getParams().getExplosion().getRadius();
            double enemyX = nearestEnemy.getPosition().getX();
            if(unit.getPosition().getX() > nearestEnemy.getPosition().getX()) {

                nearestEnemy.getPosition().setX(enemyX + radius + 0.01);
                targetPos = nearestEnemy.getPosition();
            } else {
                nearestEnemy.getPosition().setX(enemyX - radius - 0.01);
                targetPos = nearestEnemy.getPosition();
            }
        } else {
            targetPos = nearestEnemy.getPosition();
        }
    }
    debug.draw(new CustomData.Log("Target pos: " + targetPos));
    Vec2Double aim = new Vec2Double(0, 0);
    if (nearestEnemy != null) {
        Point2D myPosition = new Point2D.Double(unit.getPosition().getX(), unit.getPosition().getY());
        Point2D enemyPosition = new Point2D.Double(nearestEnemy.getPosition().getX(), nearestEnemy.getPosition().getY());
        ParametricLine line = new ParametricLine(myPosition, enemyPosition);
        ArrayList<Point2D> points = new ArrayList();
        IntStream.range(0, (int) (nearestEnemy.getPosition().getX()) + 1).forEach((p) -> points.add(line.getFraction((double) p / 50)));
        aim = new Vec2Double(nearestEnemy.getPosition().getX() - unit.getPosition().getX(),
            nearestEnemy.getPosition().getY() - unit.getPosition().getY());
    }
    
    boolean allowShooting = true;

    if (unit.getWeapon() != null && unit.getWeapon().getTyp() == WeaponType.ROCKET_LAUNCHER && game.getLevel()
            .getTiles()[(int) (unit.getPosition().getX() + 1)][(int) (unit.getPosition().getY() + 1)] == Tile.WALL) {
    		allowShooting = false;
    }
    if (unit.getWeapon() != null && unit.getWeapon().getTyp() == WeaponType.ROCKET_LAUNCHER && game.getLevel()
            .getTiles()[(int) (unit.getPosition().getX() - 1)][(int) (unit.getPosition().getY() - 1)] == Tile.WALL) {
        	allowShooting = false;
     }
    
    boolean jump = targetPos.getY() > unit.getPosition().getY();
    if (targetPos.getX() > unit.getPosition().getX() && game.getLevel()
        .getTiles()[(int) (unit.getPosition().getX() + 1)][(int) (unit.getPosition().getY())] == Tile.WALL) {
      jump = true;
    }
    if (targetPos.getX() < unit.getPosition().getX() && game.getLevel()
        .getTiles()[(int) (unit.getPosition().getX() - 1)][(int) (unit.getPosition().getY())] == Tile.WALL) {
      jump = true;
    }
    UnitAction action = new UnitAction();

    action.setVelocity(targetPos.getX() - unit.getPosition().getX());

    action.setJump(jump);
    // action.setJumpDown(!jump);
    action.setAim(aim);
    action.setShoot(allowShooting);
    action.setSwapWeapon(isBestWeapon);
    action.setPlantMine(false);
    return action;
  }
}