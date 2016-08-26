package org.janelia.util;

public final class ComparablePair< A extends Comparable< ? super A >, B extends Comparable< ? super B > > implements Comparable< ComparablePair< A, B > >
{
	public A first;
	public B second;

	public ComparablePair( final A first, final B second )
	{
		this.first = first;
		this.second = second;
	}

	@Override
	public int compareTo( final ComparablePair< A, B > other )
	{
		final int compareFirst = first.compareTo( other.first );
		if ( compareFirst != 0)
			return compareFirst;

		return second.compareTo( other.second );
	}

	@Override
	public String toString()
	{
		return "(" + first.toString() + "," + second.toString() + ")";
	}
}
