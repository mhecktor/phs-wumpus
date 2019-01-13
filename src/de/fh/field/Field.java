package de.fh.field;

import java.util.HashSet;

import de.fh.util.Point;
import de.fh.util.enums.FieldType;

/**
 * @author christophstockhoff
 */
public class Field {

	protected Point p;
	protected FieldType type;
	private HashSet<FieldType> types;

	public Field( Point p, FieldType type ) {
		this.p = p;
		this.type = type;
		this.types = new HashSet<>();
		if( type != FieldType.EMPTY ) types.add( type );
	}

	public Field( int x, int y, FieldType type ) {
		this( new Point( x, y ), type );
	}

	public Point getPoint() {
		return p;
	}

	public FieldType getType() {
		if( types.contains( FieldType.GOLD ) ) return FieldType.GOLD;

		if( types.contains( FieldType.WALL ) ) return FieldType.WALL;
		if( types.contains( FieldType.HOLE ) ) return FieldType.HOLE;

		if( types.contains( FieldType.PLAYER ) ) return FieldType.PLAYER;
		if( types.contains( FieldType.START ) ) return FieldType.START;

		if( types.contains( FieldType.VISITED ) ) return FieldType.VISITED;
		if( types.contains( FieldType.EMPTY ) ) return FieldType.EMPTY;
		if( types.contains( FieldType.BREEZE ) ) return FieldType.BREEZE;

		return type;
	}

	public boolean isWalkable() {
		return !( types.contains( FieldType.WALL ) || types.contains( FieldType.HOLE ) || types.contains( FieldType.BREEZE ) ) ? true : false;
	}

	public void addType( FieldType type ) {
		// Wenn ein Feld nachtraeglich als sicher empfunden wird, dann entferne
		// das unsichere
		if( type == FieldType.EMPTY && ( types.contains( FieldType.BREEZE ) || types.contains( FieldType.HOLE ) ) ) {
			removeType( FieldType.BREEZE );
			removeType( FieldType.HOLE );
		}
		// Wenn das Feld bereits als sicher empfunden wird, dann ist ein Luftzug
		// egal
		if( type == FieldType.BREEZE && types.contains( FieldType.EMPTY ) ) { return; }
		// Bei zwei Luftzuegen koennte das Feld ein Loch sein
		if( type == FieldType.BREEZE && types.contains( FieldType.BREEZE ) ) {
			types.add( FieldType.HOLE );
		}
		types.add( type );
	}

	public void removeType( FieldType type ) {
		types.remove( type );
	}

	public HashSet<FieldType> getTypes() {
		return types;
	}

	public boolean visited() {
		return types.contains( FieldType.VISITED );
	}

	public boolean equalsOtherFieldCoords( Field f ) {
		return ( p.getX() == f.getPoint().getX() && p.getY() == f.getPoint().getY() );
	}

	public boolean equalsPoint( Point p ) {
		return ( p.getX() == p.getX() && p.getY() == p.getY() );
	}

	@Override
	public String toString() {
		return p.toString() + ":" + getType().ordinal();
	}
}
