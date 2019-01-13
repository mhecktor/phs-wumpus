package de.fh.wumpus;

import java.util.ArrayList;
import java.util.List;

import de.fh.util.Point;
import de.fh.field.Field;

/**
 * @author christophstockhoff
 */
public class WumpusField {

	private Field f;
	private List<Integer> wumpusIDs;

	public WumpusField( Field f ) {
		wumpusIDs = new ArrayList<>();
		this.f = f;
	}

	public Field getField() {
		return f;
	}

	public void addWumpusID( Integer id ) {
		wumpusIDs.add( id );
	}

	public List<Integer> getWumpusIDs() {
		return wumpusIDs;
	}

	public Point getPoint() {
		return f.getPoint();
	}
}
