package de.fh.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import de.fh.field.Field;
import de.fh.field.TreeField;
import de.fh.util.enums.Direction;
import de.fh.util.enums.FieldType;
import de.fh.wumpus.enums.HunterAction;

/**
 * @author christophstockhoff
 */
public class FieldUtil {
	// Relevant fuer das Aussuchen der naechsten Aktion
	private static List<TreeField> manhattenList;
	private static List<Field> closedList;
	private static Stack<HunterAction> actionStack;
	private static ArrayList<Field> map;

	public static int distance( Point p1, Point p2 ) {
		return Math.abs( p1.getX() - p2.getX() ) + Math.abs( p1.getY() - p2.getY() );
	}

	public static Field entryExisting( Point p, ArrayList<Field> map ) {
		Field f = FieldUtil.getFieldByPoint( p, map );
		if( f == null ) {
			f = new Field( p, FieldType.EMPTY );
			map.add( f );
		}
		return f;
	}

	public static HunterAction actionsToFieldType( FieldType typeToFind, ArrayList<Field> map, Point p, Direction d ) {
		FieldUtil.map = map;
		actionStack = new Stack<>();
		manhattenList = new ArrayList<>();
		closedList = new ArrayList<>();
		locateFieldType( new TreeField( getFieldByPoint( p, map ), null, d ), typeToFind );
		if( actionStack.isEmpty() )
			return null;
		else
			return actionStack.pop();
	}

	public static Stack<HunterAction> actionsToField( Field fieldToFind, ArrayList<Field> map, Point p, Direction d ) {
		actionStack = new Stack<>();

		// TODO Muss besser gemacht werden!!! Wenn das zu suchende Feld aktuell
		// noch als unbegehbar angesehen
		// wird, dann Error!
		Field tField = getFieldByPoint( fieldToFind.getPoint(), map );
		if( tField != null && !tField.isWalkable() ) return actionStack;

		manhattenList = new ArrayList<>();
		closedList = new ArrayList<>();
		ArrayList<Field> tMap = new ArrayList<>( map );
		FieldUtil.map = tMap;
		locateField( new TreeField( getFieldByPoint( p, tMap ), null, d ), fieldToFind );
		return actionStack;
	}

	private static void locateField( TreeField tf, Field f ) {
		closedList.add( tf.getField() );

		if( tf.equalsOtherFieldCoords( f ) ) {
			TreeField tTreeField = tf;
			while( tTreeField.getVorgaenger() != null ) {
				Stack<HunterAction> tActions = tTreeField.getActions();
				while( !tActions.isEmpty() )
					actionStack.push( tActions.pop() );
				tTreeField = tTreeField.getVorgaenger();
			}
			return;
		}

		Point tPos = tf.getPoint();
		Field top, left, right, bottom;

		top = entryExisting( new Point( tPos.getX(), tPos.getY() - 1 ), map );
		left = entryExisting( new Point( tPos.getX() - 1, tPos.getY() ), map );
		right = entryExisting( new Point( tPos.getX() + 1, tPos.getY() ), map );
		bottom = entryExisting( new Point( tPos.getX(), tPos.getY() + 1 ), map );

		if( top != null && top.isWalkable() && !closedList.contains( top ) ) {
			manhattenList.add( new TreeField( top, tf, tf.getDirection() ) );
		}
		if( left != null && left.isWalkable() && !closedList.contains( left ) ) {
			manhattenList.add( new TreeField( left, tf, tf.getDirection() ) );
		}
		if( right != null && right.isWalkable() && !closedList.contains( right ) ) {
			manhattenList.add( new TreeField( right, tf, tf.getDirection() ) );
		}
		if( bottom != null && bottom.isWalkable() && !closedList.contains( bottom ) ) {
			manhattenList.add( new TreeField( bottom, tf, tf.getDirection() ) );
		}

		if( manhattenList.size() > 0 ) {
			// Es tritt ein Fehler auf, wenn das eigentlich gesuchte Feld nicht
			// "begehbar" ist bzw. kein Weg dort hinfuehrt
			Collections.sort( manhattenList, ( a, b ) -> a.getCosts().compareTo( b.getCosts() ) );
			locateField( manhattenList.remove( 0 ), f );
		}
	}

	private static void locateFieldType( TreeField tf, FieldType ft ) {
		closedList.add( tf.getField() );

		if( tf.getType() == ft ) {
			TreeField tTreeField = tf;
			while( tTreeField.getVorgaenger() != null ) {
				Stack<HunterAction> tActions = tTreeField.getActions();
				while( !tActions.isEmpty() )
					actionStack.push( tActions.pop() );
				tTreeField = tTreeField.getVorgaenger();
			}
			return;
		}

		Point tPos = tf.getPoint();
		Field top, left, right, bottom;

		top = getFieldByPoint( new Point( tPos.getX(), tPos.getY() - 1 ), map );
		left = getFieldByPoint( new Point( tPos.getX() - 1, tPos.getY() ), map );
		right = getFieldByPoint( new Point( tPos.getX() + 1, tPos.getY() ), map );
		bottom = getFieldByPoint( new Point( tPos.getX(), tPos.getY() + 1 ), map );

		if( top != null && top.isWalkable() && !closedList.contains( top ) ) {
			manhattenList.add( new TreeField( top, tf, tf.getDirection() ) );
		}
		if( left != null && left.isWalkable() && !closedList.contains( left ) ) {
			manhattenList.add( new TreeField( left, tf, tf.getDirection() ) );
		}
		if( right != null && right.isWalkable() && !closedList.contains( right ) ) {
			manhattenList.add( new TreeField( right, tf, tf.getDirection() ) );
		}
		if( bottom != null && bottom.isWalkable() && !closedList.contains( bottom ) ) {
			manhattenList.add( new TreeField( bottom, tf, tf.getDirection() ) );
		}

		if( manhattenList.size() > 0 ) {
			Collections.sort( manhattenList, ( a, b ) -> a.getCosts().compareTo( b.getCosts() ) );
			locateFieldType( manhattenList.remove( 0 ), ft );
		}
	}

	public static Field getFieldByPoint( Point p, ArrayList<Field> map ) {
		for( Field f : map )
			if( f.getPoint().getX() == p.getX() && f.getPoint().getY() == p.getY() ) return f;

		return null;
	}
}
