package org.janelia.stitching;

import java.util.ArrayList;

import scala.Tuple2;

public class TileHelper {

	public static ArrayList< Tuple2< TileInfo, TileInfo > > findOverlappingTiles( final TileInfo[] tiles ) {
		
		final ArrayList< Tuple2< TileInfo, TileInfo > > overlappingTiles = new ArrayList<>();
		for ( int i = 0; i < tiles.length; i++ )
			for ( int j = i + 1; j < tiles.length; j++ )
				if ( overlap( tiles[ i ], tiles[ j ] ) )
					overlappingTiles.add( new Tuple2< TileInfo, TileInfo >( tiles[ i ], tiles[ j ] ) );
		return overlappingTiles;
	}
	
	public static boolean overlap( final TileInfo t1, final TileInfo t2 ) {
		assert t1.getDimensionality() == t2.getDimensionality();
		
		for ( int d = 0; d < t1.getDimensionality(); d++ ) {
			
			final double p1 = t1.getPosition()[ d ], p2 = t2.getPosition()[ d ];
			final long s1 = t1.getSize()[ d ], s2 = t2.getSize()[ d ];
			
			if ( !( ( p2 >= p1 && p2 <= p1 + s1 ) || 
					( p1 >= p2 && p1 <= p2 + s2 ) ) )
				return false;
		}
		return true;
	}
	
	
	public static Boundaries findBoundaries( final TileInfo[] tiles ) {
		
		if ( tiles.length == 0 )
			return null;
		
		final int dim = tiles[ 0 ].getDimensionality();
		
		final Boundaries boundaries = new Boundaries( dim );
		for ( int d = 0; d < dim; d++ ) {
			boundaries.setMin( d, Integer.MAX_VALUE );
			boundaries.setMax( d, Integer.MIN_VALUE );
		}
			
		for ( final TileInfo tile : tiles ) {
			assert dim == tile.getDimensionality();
			
			for ( int d = 0; d < dim; d++ ) {
				boundaries.setMin( d, Math.min( boundaries.getMin(d), (int)Math.floor( tile.getPosition(d) ) ) );
				boundaries.setMax( d, Math.max( boundaries.getMax(d), (int)Math.ceil( tile.getPosition(d) ) + tile.getSize(d) ) );
			}
		}
		
		return boundaries;
	}
	
	public static ArrayList< TileInfo > divideSpace( final Boundaries space, final int subregionSize ) {
		assert space.validate() && subregionSize > 0;
		if ( !space.validate() || subregionSize <= 0 )
			return null;

		final ArrayList< TileInfo > subregions = new ArrayList<>();
		divideSpaceRecursive( space, subregions, subregionSize, new TileInfo( space.getDimensionality() ), 0 );
		
		for ( int i = 0; i < subregions.size(); i++ )
			subregions.get( i ).setIndex( i );
		return subregions;
	}
	private static void divideSpaceRecursive( final Boundaries space, final ArrayList< TileInfo > subregions, final int subregionSize, final TileInfo currSubregion, final int currDim ) {
		
		if ( currDim == space.getDimensionality() ) {
			subregions.add( currSubregion );
			return;
		}
		
		for ( long coord = space.getMin( currDim ); coord < space.getMax( currDim ); coord += subregionSize ) {
			
			final TileInfo newSubregion = currSubregion.clone();
			newSubregion.setPosition( currDim, coord );
			newSubregion.setSize( currDim, Math.min( subregionSize, space.getMax(currDim) - coord ) );
			
			divideSpaceRecursive( space, subregions, subregionSize, newSubregion, currDim + 1 );
		}
	}
}
