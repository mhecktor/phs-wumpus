package de.fh.field;

import java.util.Stack;

import de.fh.util.enums.Direction;
import de.fh.wumpus.enums.HunterAction;

/**
 * @author christophstockhoff
 */
public class TreeField extends Field {

	private Stack<HunterAction> actionStack;
	private Field f;
	private TreeField vorgaenger;
	private Direction d;
	private int costs = 0;

	public TreeField( Field f, TreeField vorgaenger, Direction d ) {
		super( f.getPoint(), f.getType() );

		this.f = f;
		this.vorgaenger = vorgaenger;
		this.d = d;
		this.actionStack = new Stack<>();

		if( vorgaenger != null ) findActions();
	}

	private void findActions() {
		if( p.x < vorgaenger.getPoint().x ) {
			// left
			switch( d ) {
				case EAST:
					actionStack.push( HunterAction.TURN_RIGHT );
					actionStack.push( HunterAction.TURN_RIGHT );
					actionStack.push( HunterAction.GO_FORWARD );
					break;
				case NORTH:
					actionStack.push( HunterAction.TURN_LEFT );
					actionStack.push( HunterAction.GO_FORWARD );
					break;
				case SOUTH:
					actionStack.push( HunterAction.TURN_RIGHT );
					actionStack.push( HunterAction.GO_FORWARD );
					break;
				case WEST:
					actionStack.push( HunterAction.GO_FORWARD );
					break;
				default:
					break;
			}
			d = Direction.WEST;

		} else if( p.x > vorgaenger.getPoint().x ) {
			// right
			switch( d ) {
				case EAST:
					actionStack.push( HunterAction.GO_FORWARD );
					break;
				case NORTH:
					actionStack.push( HunterAction.TURN_RIGHT );
					actionStack.push( HunterAction.GO_FORWARD );
					break;
				case SOUTH:
					actionStack.push( HunterAction.TURN_LEFT );
					actionStack.push( HunterAction.GO_FORWARD );
					break;
				case WEST:
					actionStack.push( HunterAction.TURN_RIGHT );
					actionStack.push( HunterAction.TURN_RIGHT );
					actionStack.push( HunterAction.GO_FORWARD );
					break;
				default:
					break;
			}
			d = Direction.EAST;
		} else if( p.y < vorgaenger.getPoint().y ) {
			// top
			switch( d ) {
				case EAST:
					actionStack.push( HunterAction.TURN_LEFT );
					actionStack.push( HunterAction.GO_FORWARD );
					break;
				case NORTH:
					actionStack.push( HunterAction.GO_FORWARD );
					break;
				case SOUTH:
					actionStack.push( HunterAction.TURN_RIGHT );
					actionStack.push( HunterAction.TURN_RIGHT );
					actionStack.push( HunterAction.GO_FORWARD );
					break;
				case WEST:
					actionStack.push( HunterAction.TURN_RIGHT );
					actionStack.push( HunterAction.GO_FORWARD );
					break;
				default:
					break;
			}
			d = Direction.NORTH;
		} else if( p.y > vorgaenger.getPoint().y ) {
			// bottom
			switch( d ) {
				case EAST:
					actionStack.push( HunterAction.TURN_RIGHT );
					actionStack.push( HunterAction.GO_FORWARD );
					break;
				case NORTH:
					actionStack.push( HunterAction.TURN_RIGHT );
					actionStack.push( HunterAction.TURN_RIGHT );
					actionStack.push( HunterAction.GO_FORWARD );
					break;
				case SOUTH:
					actionStack.push( HunterAction.GO_FORWARD );
					break;
				case WEST:

					actionStack.push( HunterAction.TURN_LEFT );
					actionStack.push( HunterAction.GO_FORWARD );
					break;
				default:
					break;
			}
			d = Direction.SOUTH;
		}
		costs = vorgaenger.getCosts() + actionStack.size();
	}

	public Field getField() {
		return f;
	}

	public TreeField getVorgaenger() {
		return vorgaenger;
	}

	public Integer getCosts() {
		return costs;
	}

	public Direction getDirection() {
		return d;
	}

	public Stack<HunterAction> getActions() {
		return actionStack;
	}

}
