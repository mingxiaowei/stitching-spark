package org.janelia.flatfield;

// import org.janelia.flatfield.FlatfieldCorrection;
// import org.janelia.flatfield.FlatfieldCorrectedRandomAccessible;
import org.janelia.stitching.StitchingArguments;
import org.janelia.stitching.TileInfo;
import org.janelia.stitching.TileLoader;
import org.janelia.stitching.Utils;
import org.janelia.dataaccess.DataProvider;
import org.janelia.dataaccess.DataProviderFactory;
import org.janelia.dataaccess.PathResolver;

import ij.ImagePlus;
import net.imglib2.view.RandomAccessiblePairNullable;
// import net.imglib2.img.imageplus.ImagePlusImg;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
// import net.imglib2.type.numeric.real.DoubleType;
// import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import net.imglib2.converter.Converters;
import net.imglib2.converter.RealConverter;
import net.imglib2.util.Util;

import java.util.ArrayList;
import java.util.List;

public class v2FlatfieldCorrectionTest 
{
    public static void main(String[] args) throws Exception
    {
        test();
    }

    private static <
    T extends NativeType< T > & RealType< T >,
    U extends NativeType< U > & RealType< U > > 
    void test() throws Exception
    {
        
        String[] argsList = getArgsList();
        StitchingArguments sargs = new StitchingArguments(argsList);
        String inputTileConfiguration = sargs.inputTileConfigurations().get(0);
        DataProvider dataProvider = DataProviderFactory.create(DataProviderFactory.detectType(inputTileConfiguration));
        int dimensionality = 3;
        // String SPath_old = "/Volumes/data/sternsonlab/Zhenggang/2acq/outputs/M28C_LHA_S1/stitching_beforeflatfield/c2-flatfield/S.tif";
        // String TPath_old = "/Volumes/data/sternsonlab/Zhenggang/2acq/outputs/M28C_LHA_S1/stitching_beforeflatfield/c2-flatfield/T.tif";
        String SPath = "/Volumes/data/sternsonlab/Mingxiao/S_test.tiff";
        String TPath = "/Volumes/data/sternsonlab/Mingxiao/T_test.tiff";

        final TileInfo[] tiles = dataProvider.loadTiles( inputTileConfiguration );
        RandomAccessiblePairNullable< U, U >  flatfield = FlatfieldCorrection.loadCorrectionImages(
            dataProvider, inputTileConfiguration, dimensionality, SPath, TPath);
        
        if ( flatfield == null )
			throw new NullPointerException( "flatfield images were not found" );
        
        String outputDirectory = "/Users/mingxiaowei/Desktop/smslab/code/stitching-spark/test_results/test1";

        int processed = 0;
		for ( final TileInfo tile : tiles )
		{
			final RandomAccessibleInterval< T > tileImg = TileLoader.loadTile( tile, dataProvider );
			final FlatfieldCorrectedRandomAccessible< T, U > flatfieldCorrectedTileImg = new FlatfieldCorrectedRandomAccessible<>( tileImg, flatfield.toRandomAccessiblePair() );
			final RandomAccessibleInterval< U > correctedImg = Views.interval( flatfieldCorrectedTileImg, tileImg );
			final RandomAccessibleInterval< T > convertedImg = Converters.convert( correctedImg, new RealConverter<>(), Util.getTypeFromInterval( tileImg ) );
			final ImagePlus correctedImp = Utils.copyToImagePlus( convertedImg );
			dataProvider.saveImage( correctedImp, PathResolver.get( outputDirectory, PathResolver.getFileName( tile.getFilePath() ) ) );

			System.out.println( "  processed " + (++processed) + " tiles out of " + tiles.length );
		}

		System.out.println( System.lineSeparator() + "Done" );
    }

    private static String[] getArgsList() {
        String inputPath = "/Volumes/data/sternsonlab/Zhenggang/2acq/outputs/M28C_LHA_S1/stitching/";
        List<String> configArgList = new ArrayList<>();

        for (int channel = 0; channel < 4; channel++) {
            String channelConfig = inputPath + "c" + channel + "-n5.json";
            configArgList.add("-i");
            configArgList.add(channelConfig);
        }

        configArgList.add("--fuse");

        System.out.println(configArgList);

        return configArgList.toArray(new String[0]);
    }
}