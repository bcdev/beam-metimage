package org.esa.beam.metimage;

/**
 * MetImage MetImage  Constants
 *
 * @author Marco Zuehlke, Olaf Danne
 */
public class MetImageConstants {

    public static final int NUM_BINS = 20;
    public static final int NUM_QUANTILES = 100000;

    public static final int ALPHA = 100;
    public static final int MIN_SAMPLES_PER_HISTOGRAM = 50;

    public static final String TSKIN_DEFAULT_FILE_NAME = "2007_tskin_730.nc";
    public static final String TSKIN_DEFAULT_BAND_NAME = "skt_time730";

    public static final String MODIS_CSV_PRODUCT_SURFACETYPE_BAND_NAME = "PIXEL_SURFACE_TYPE_ID";
    public static final String MODIS_CSV_PRODUCT_GLINT_BAND_NAME = "GLINT_ID";
    public static final String MODIS_CSV_PRODUCT_SNOW_BAND_NAME = "SNOW_ID";
    public static final String MODIS_CSV_PRODUCT_DAYTIME_BAND_NAME = "DAY_TIME_ID";
    public static final String MODIS_CSV_PRODUCT_CLOUDHEIGHT_BAND_NAME = "CLOUD_HEIGHT_ID";
    public static final String MODIS_CSV_PRODUCT_NDSI_BAND_NAME = "NDSI";
    public static final String MODIS_CSV_PRODUCT_LATITUDE_BAND_NAME = "Latitude";
    public static final String MODIS_CSV_PRODUCT_LONGITUDE_BAND_NAME = "Longitude";

//    public static final String MODIS_CSV_PRODUCT_RHO600_BAND_NAME = "EV_1KM_RefSB_13lo";
    public static final String MODIS_CSV_PRODUCT_RHO600_BAND_NAME = "EV_250_Aggr1km_RefSB_1";   // RP, 20140226
//    public static final String MODIS_CSV_PRODUCT_RHO860_BAND_NAME = "EV_1KM_RefSB_16";
    public static final String MODIS_CSV_PRODUCT_RHO860_BAND_NAME = "EV_250_Aggr1km_RefSB_2";   // RP, 20140226
    public static final String MODIS_CSV_PRODUCT_RHO1380_BAND_NAME = "EV_1KM_RefSB_26";
    public static final String MODIS_CSV_PRODUCT_BT3700_BAND_NAME = "EV_1KM_Emissive_20";
    public static final String MODIS_CSV_PRODUCT_BT4050_BAND_NAME = "EV_1KM_Emissive_23";
    public static final String MODIS_CSV_PRODUCT_BT4515_BAND_NAME = "EV_1KM_Emissive_25";
    public static final String MODIS_CSV_PRODUCT_BT7300_BAND_NAME = "EV_1KM_Emissive_28";
    public static final String MODIS_CSV_PRODUCT_BT8600_BAND_NAME = "EV_1KM_Emissive_29";
    public static final String MODIS_CSV_PRODUCT_BT11000_BAND_NAME = "EV_1KM_Emissive_31";
    public static final String MODIS_CSV_PRODUCT_BT12000_BAND_NAME = "EV_1KM_Emissive_32";
    public static final String MODIS_CSV_PRODUCT_BT13000_BAND_NAME = "EV_1KM_Emissive_33";

    public static final String MODIS_CSV_PRODUCT_RHO645_BAND_NAME = "EV_250_Aggr1km_RefSB_1";
    public static final String MODIS_CSV_PRODUCT_RHO469_BAND_NAME = "EV_500_Aggr1km_RefSB_3";
    public static final String MODIS_CSV_PRODUCT_RHO555_BAND_NAME = "EV_500_Aggr1km_RefSB_4";
    public static final String MODIS_CSV_PRODUCT_RHO1240_BAND_NAME = "EV_500_Aggr1km_RefSB_5";
    public static final String MODIS_CSV_PRODUCT_RHO1640_BAND_NAME = "EV_500_Aggr1km_RefSB_6";
    public static final String MODIS_CSV_PRODUCT_RHO2130_BAND_NAME = "EV_500_Aggr1km_RefSB_7";

    public static final int NUM_TESTS = 7;

    public static final int MEASURE_HERITAGE_1 = 1;
    public static final int MEASURE_HERITAGE_2 = 2;
    public static final int MEASURE_HERITAGE_3 = 3;
    public static final int MEASURE_HERITAGE_4 = 4;
    public static final int MEASURE_HERITAGE_5 = 5;
    public static final int MEASURE_HERITAGE_6 = 6;
    public static final int MEASURE_HERITAGE_7 = 7;

    public static final int MEASURE_NEW_1 = 8;
    public static final int MEASURE_NEW_2 = 9;
    public static final int MEASURE_NEW_3 = 10;
    public static final int MEASURE_NEW_4 = 11;
    public static final int MEASURE_NEW_5 = 12;
    public static final int MEASURE_NEW_6 = 13;
    public static final int MEASURE_NEW_7 = 14;

    public static final int[] MEASURE_HERITAGE = {
            MEASURE_HERITAGE_1,
            MEASURE_HERITAGE_2,
            MEASURE_HERITAGE_3,
            MEASURE_HERITAGE_4,
            MEASURE_HERITAGE_5,
            MEASURE_HERITAGE_6,
            MEASURE_HERITAGE_7
    };
    public static final int[] MEASURE_NEW = {
            MEASURE_NEW_1,
            MEASURE_NEW_2,
            MEASURE_NEW_3,
            MEASURE_NEW_4,
            MEASURE_NEW_5,
            MEASURE_NEW_6,
            MEASURE_NEW_7
    };

    public static final String[] DAYTIME_FILTER_ID = {"ALL", "DAY", "NIGHT", "TWILIGHT"};
    public static final String[] SURFACE_FILTER_ID = {"ALL", "LAND", "SEA", "ICE"};
    public static final String[] CLOUDTYPE_FILTER_ID = {"ALL", "LOW", "MIDLEVEL", "HIGH", "SEMITRANSPARENT"};

//    public static final String[] DAYTIME_FILTER_ID = {"ALL", "DAY", "NIGHT"};
//    public static final String[] SURFACE_FILTER_ID = {"ALL"};
//    public static final String[] CLOUDTYPE_FILTER_ID = {"ALL"};


    //    #Planck constant [Js]
    public static final double PLANCK_CONSTANT = 6.62606896e-34;
    //    #c in vacuum [m/s]
    public static final double VACUUM_LIGHT_SPEED = 2.99792458e8;
    //    #Boltzmann constant  [J/K]
    public static final double BOLTZMANN_CONSTANT = 1.3806504e-23;

    //    #temperature correction intercept [K], for band number in [20,36], but not 26
    public static final double[] TCI = {
            4.770532E-01,    // band 20
            9.262664E-02,    // band 21
            9.757996E-02,    // band 22
            8.929242E-02,    // band 23
            7.310901E-02,    // band 24
            7.060415E-02,    // band 25
            0.0,             // band 26
            2.204921E-01,    // band 27
            2.046087E-01,    // band 28
            1.599191E-01,    // band 29
            8.253401E-02,    // band 30
            1.302699E-01,    // band 31
            7.181833E-02,    // band 32
            1.972608E-02,    // band 33
            1.913568E-02,    // band 34
            1.817817E-02,    // band 35
            1.583042E-02};   // band 36

    //    #temperature correction slope [1]
    public static final double[] TCS = {
            9.993411E-01,    // band 20
            9.998646E-01,    // band 21
            9.998584E-01,    // band 22
            9.998682E-01,    // band 23
            9.998819E-01,    // band 24
            9.998845E-01,    // band 25
            0.0,             // band 26
            9.994877E-01,    // band 27
            9.994918E-01,    // band 28
            9.995495E-01,    // band 29
            9.997398E-01,    // band 30
            9.995608E-01,    // band 31
            9.997256E-01,    // band 32
            9.999160E-01,    // band 33
            9.999167E-01,    // band 34
            9.999191E-01,    // band 35
            9.999281E-01};   // band 36

    public static final double[] MODIS_EMISSIVE_WAVELENGTHS = {
            3785.3,   // band 20
            3991.6,   // band 21
            3991.6,   // band 22
            4056.0,   // band 23
            4472.6,   // band 24
            4544.7,   // band 25
            1375.0,   // band 26
            6766.0,   // band 27
            7338.2,   // band 28
            8523.8,   // band 29
            9730.3,   // band 30
            11012.1,  // band 31
            12025.9,  // band 32
            13362.9,  // band 33
            13681.8,  // band 34
            13910.8,  // band 35
            14193.7   // band 36
    };

//    public static final double[] cloudHeights = new double[]{
//            0.0,    //    0	  UNKNOWN
//            3000.0,    //    1    low (<3km)
//            6000.0,    //    2    middle (3-6km)
//            9000.0     //    3    high (>6km)
//    };

//    public static final double UPPER_LIM_RHO600 = 0.125;
    public static final double UPPER_LIM_RHO600 = 2.0;
//    public static final double UPPER_LIM_RHO860 = 0.175;
    public static final double UPPER_LIM_RHO860 = 1.5;
    public static final double UPPER_LIM_RHO1380 = 1.0;

    public static final double UPPER_LIM_BT3700 = 2.0;
    public static final double UPPER_LIM_BT7300 = 5.0;
    public static final double UPPER_LIM_BT8600 = 15.0;
    public static final double UPPER_LIM_BT11000 = 15.0;
    public static final double UPPER_LIM_BT12000 = 15.0;
    public static final double UPPER_LIM_BT13000 = 15.0;

}