package de.fh.player;

import java.util.ArrayList;

import de.fh.field.Field;
import de.fh.util.Point;
import de.fh.util.enums.Direction;
import de.fh.wumpus.enums.HunterAction;

/**
 * @author christophstockhoff
 */
public class Player {

	private Direction d = null;
	private Point p = null;
	private HunterAction action = null;
	private int arrows;
	private ArrayList<Field> fieldHistory;

	public Player( Point p, Direction d, int arrows ) {
		this.p = p;
		this.d = d;
		this.arrows = arrows;
		fieldHistory = new ArrayList<>();
	}

	public void setAction( HunterAction action ) {
		this.action = action;
	}

	public void setDirection( Direction d ) {
		this.d = d;
	}

	public HunterAction getAction() {
		return action;
	}

	public Direction getDirection() {
		return d;
	}

	public Point getLoc() {
		return p;
	}

	public void reduceArrows() {
		arrows--;
	}

	public void setArrows( int arrows ) {
		this.arrows = arrows;
	}

	public int getArrows() {
		return arrows;
	}

	public void addFieldToHistory( Field f ) {
		fieldHistory.add( f );
	}

	public ArrayList<Field> getFieldHistory() {
		return fieldHistory;
	}

}
