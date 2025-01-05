package org.janelia.flatfield;

// import java.util.RandomAccess;

import java.util.ArrayList;
import java.util.List;

// import org.scijava.ui.SystemClipboard;

// import org.janelia.dataaccess.DataProvider;
// import org.janelia.dataaccess.DataProviderFactory;
// import org.janelia.dataaccess.PathResolver;
// import org.janelia.stitching.StitchingArguments;
// import org.janelia.stitching.TileInfo;
// import org.janelia.stitching.TileLoader;
// import org.janelia.stitching.Utils;

// import ij.ImagePlus;
import net.imglib2.RandomAccessibleInterval;

import net.imglib2.RandomAccess;
import net.imglib2.converter.Converters;
import net.imglib2.Cursor;
// import net.imglib2.converter.Converters;
// import net.imglib2.converter.RealConverter;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.Img;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
// import net.imglib2.util.Util;
// import net.imglib2.view.RandomAccessiblePairNullable;
import net.imglib2.view.Views;
import net.imglib2.type.numeric.real.DoubleType;

public class StackTo3D {

    public interface TypeFactory<T extends NativeType<T> & RealType<T>> {
        T create();
    }

    public static <T extends NativeType<T> & RealType<T>> RandomAccessibleInterval<T> convertToType(
            RandomAccessibleInterval<DoubleType> input, TypeFactory<T> factory) {

        return Converters.convert(
                input,
                (inputValue, outputValue) -> outputValue.setReal(inputValue.getRealDouble()),
                factory.create()
        );
    }
        // public static
    //     <T extends NativeType< T > & RealType< T >,
    // U extends NativeType< U > & RealType< U > > 
    public static 
    <T extends NativeType< T > & RealType< T >,
    U extends NativeType< U > & RealType< U > >
    void main(String[] args) {
        // Example input shapes
        double[] values = {1.1, 2.2, 3.3, 4.4, 5.5, 6.6};
        double[] z_values = {1.0, 1.0, 1.0, 1.0};
        int x = 3, y = 2, z = 4;

        // double[] values = {1.1, 2.2, 3.3, 4.4, 5.5, 6.6};
        // int x = 3, y = 2;

        RandomAccessibleInterval<DoubleType> T_2d = ArrayImgs.doubles(values, y, x);
        RandomAccessibleInterval<DoubleType> B_1d = ArrayImgs.doubles(z_values, z);
        
        printImage(T_2d, x, y);
        printArray(B_1d);

        // Stack T_2d in the z-dimension
        RandomAccessibleInterval<DoubleType> stacked = stackInZDimension(T_2d, z);

        // Print the result (z, x, y)
        print3DArray(stacked, z, x, y);
        System.out.println(stacked.toString());


        // // Use a factory for FloatType
        // TypeFactory<FloatType> floatFactory = FloatType::new;
        // RandomAccessibleInterval<FloatType> floatImg = convertToType(T_2d, floatFactory);

    }

    private static <T extends NativeType<T> & RealType<T>> void printImage(RandomAccessibleInterval<T> img, int x, int y) {
        // Create a RandomAccess object for the image
        RandomAccess<T> randomAccess = img.randomAccess();
    
        for (int row = 0; row < y; row++) {
            for (int col = 0; col < x; col++) {
                // Move the RandomAccess to the specified position
                randomAccess.setPosition(new long[]{row, col});
                
                // Get and print the value at the current position
                System.out.print(randomAccess.get().getRealDouble() + " ");
            }
            System.out.println(); // Newline after each row
        }
    }

    public static void printArray(RandomAccessibleInterval<DoubleType> array) {
        for (DoubleType value : Views.flatIterable(array)) {
            System.out.print(value.get() + " ");
        }
        System.out.println(); // Newline after all elements
    }

    public static <T extends NativeType<T> & RealType<T>> RandomAccessibleInterval<T> convertToType(
            RandomAccessibleInterval<DoubleType> input, T typeInstance) {

        // Use Converters to map DoubleType to T
        return Converters.convert(
                input,
                (inputValue, outputValue) -> outputValue.setReal(inputValue.getRealDouble()),
                typeInstance
        );
    }

    public static RandomAccessibleInterval<DoubleType> stackInZDimension(
            RandomAccessibleInterval<DoubleType> T_2d, int z) {
        // Create a list to hold z copies of T_2d
        List<RandomAccessibleInterval<DoubleType>> slices = new ArrayList<>();

        for (int i = 0; i < z; i++) {
            // Create a new image for each slice
            Img<DoubleType> sliceCopy = ArrayImgs.doubles(
                    new double[(int) T_2d.dimension(0) * (int) T_2d.dimension(1)],
                    (int) T_2d.dimension(0),
                    (int) T_2d.dimension(1)
            );

            // Copy data from T_2d into the new slice
            Cursor<DoubleType> tCursor = Views.iterable(T_2d).cursor();
            Cursor<DoubleType> sliceCursor = Views.iterable(sliceCopy).cursor();
            while (tCursor.hasNext()) {
                sliceCursor.next().set(tCursor.next());
            }

            slices.add(sliceCopy);
        }

        // Stack the slices along the z-dimension
        return Views.stack(slices);
    }

    // private static void print3DArray(RandomAccessibleInterval<DoubleType> img, int z, int x, int y) {
    //     RandomAccess<DoubleType> randomAccess = img.randomAccess(); // Create a RandomAccess object for navigation
    
    //     for (int slice = 0; slice < z; slice++) {
    //         System.out.println("Slice " + slice + ":");
    //         for (int row = 0; row < y; row++) {
    //             for (int col = 0; col < x; col++) {
    //                 // Set position in the correct order: (slice, col, row) -> (z, y, x)
    //                 System.out.println();
    //                 randomAccess.setPosition(new long[]{slice, col, row});
    //                 System.out.print(randomAccess.get().getRealDouble() + " ");
    //             }
    //             System.out.println();
    //         }
    //         System.out.println();
    //     }
    // }
    
    private static void print3DArray(RandomAccessibleInterval<DoubleType> img, int z, int x, int y) {
        RandomAccess<DoubleType> randomAccess = img.randomAccess(); // Create a RandomAccess object for navigation
    
        for (int slice = 0; slice < z; slice++) { // Loop over z (slices)
            // System.out.println("Slice " + slice + ":");
            for (int row = 0; row < y; row++) { // Loop over y (rows)
                for (int col = 0; col < x; col++) { // Loop over x (columns)
                    // Set position in the correct order (slice, row, col corresponds to z, y, x)
                    System.out.println("slice: " + slice + ", row: " + row + ", col: " + col);
                    randomAccess.setPosition(slice, 0); // Set position in z-dimension
                    randomAccess.setPosition(row, 2);  // Set position in y-dimension
                    randomAccess.setPosition(col, 1);  // Set position in x-dimension
                    System.out.print(randomAccess.get().getRealDouble() + " ");
                }
                System.out.println();
            }
            System.out.println();
        }
    }
    
    // void main() throws Exception
    // {
        
    //     String[] argsList = getArgsList();
    //     StitchingArguments args = new StitchingArguments(argsList);
    //     String inputTileConfiguration = args.inputTileConfigurations().get(0);
    //     DataProvider dataProvider = DataProviderFactory.create(DataProviderFactory.detectType(inputTileConfiguration));
    //     int dimensionality = 3;
    //     // String SPath_old = "/Volumes/data/sternsonlab/Zhenggang/2acq/outputs/M28C_LHA_S1/stitching_beforeflatfield/c2-flatfield/S.tif";
    //     // String TPath_old = "/Volumes/data/sternsonlab/Zhenggang/2acq/outputs/M28C_LHA_S1/stitching_beforeflatfield/c2-flatfield/T.tif";
    //     String SPath = "/Volumes/data/sternsonlab/Mingxiao/df_s0.tiff";
    //     String TPath = "/Volumes/data/sternsonlab/Mingxiao/ff_s0.tiff";

    //     final TileInfo[] tiles = dataProvider.loadTiles( inputTileConfiguration );
    //     RandomAccessiblePairNullable< U, U >  flatfield = FlatfieldCorrection.loadCorrectionImages(
    //         dataProvider, args.flatfieldFile(), dimensionality, SPath, TPath);
        
    //     if ( flatfield == null )
	// 		throw new NullPointerException( "flatfield images were not found" );
        
    //     String outputDirectory = "/Users/mingxiaowei/Desktop/smslab/code/stitching-spark/test_results";

    //     int processed = 0;
	// 	for ( final TileInfo tile : tiles )
	// 	{
	// 		final RandomAccessibleInterval< T > tileImg = TileLoader.loadTile( tile, dataProvider );
	// 		final FlatfieldCorrectedRandomAccessible< T, U > flatfieldCorrectedTileImg = new FlatfieldCorrectedRandomAccessible<>( tileImg, flatfield.toRandomAccessiblePair() );
	// 		final RandomAccessibleInterval< U > correctedImg = Views.interval( flatfieldCorrectedTileImg, tileImg );
	// 		final RandomAccessibleInterval< T > convertedImg = Converters.convert( correctedImg, new RealConverter<>(), Util.getTypeFromInterval( tileImg ) );
	// 		final ImagePlus correctedImp = Utils.copyToImagePlus( convertedImg );
	// 		dataProvider.saveImage( correctedImp, PathResolver.get( outputDirectory, PathResolver.getFileName( tile.getFilePath() ) ) );

	// 		System.out.println( "  processed " + (++processed) + " tiles out of " + tiles.length );
	// 	}

	// 	System.out.println( System.lineSeparator() + "Done" );
    // }

    // private static String[] getArgsList() {
    //     String inputPath = "/Volumes/data/sternsonlab/Zhenggang/2acq/outputs/M28C_LHA_S1/stitching/";
    //     List<String> configArgList = new ArrayList<>();

    //     for (int channel = 0; channel < 4; channel++) {
    //         String channelConfig = inputPath + "c" + channel + "-n5.json";
    //         configArgList.add("-i");
    //         configArgList.add(channelConfig);
    //     }

    //     System.out.println(configArgList);

    //     return configArgList.toArray(new String[0]);
    // }

}
