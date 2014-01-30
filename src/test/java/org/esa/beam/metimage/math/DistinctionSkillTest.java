package org.esa.beam.metimage.math;

import com.bc.ceres.core.ProgressMonitor;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.esa.beam.metimage.MetImageConstants;
import org.esa.beam.util.math.IndexValidator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DistinctionSkillTest {

    private int numCloud;
    private int numNoCloud;

    @Before
    public void setUp() throws Exception {
        numCloud = 112269;
        numNoCloud = 176308;
    }

    @Test
    public void testCalcDistinctionSkillHistogramsUnequalLength() {
        final double[] histoNoCloud = new double[]{1.0, 2.0, 3.0};
        final double[] histoCloud = new double[]{1.0, 2.0, 3.0, 4.0};
        final double[] histoBins = new double[]{0.1, 0.2, 0.3, 0.4};

        try {
            final double skill =
                    DistinctionSkill.computeDistinctionSkillFromCramerMisesAndersonMetric(histoNoCloud, histoCloud, histoBins,
                                                                                          numNoCloud, numCloud);
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
            assertEquals("Histograms have unequal length - cannot proceed.", e.getMessage());
        }
    }

    @Test
    public void testCalcDistinctionSkillBinHistogramWrongLength() {
        final double[] histoNoCloud = new double[]{2.0, 3.0, 4.0, 5.0};
        final double[] histoCloud = new double[]{1.0, 2.0, 3.0, 4.0};
        final double[] histoBins = new double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6};

        try {
            final double skill =
                    DistinctionSkill.computeDistinctionSkillFromCramerMisesAndersonMetric(histoNoCloud, histoCloud, histoBins,
                                                                                          numNoCloud, numCloud);
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
            assertEquals("Number of histogram bins does not match - cannot proceed.", e.getMessage());
        }
    }

    @Test
    public void testCalcDistinctionSkillReflectance() {
        // test data taken from RP's 'meris_cloud_naive_bayes_all_data_ndvi.npz'

        // first distribution of reflectance samples
        final double[] histoNoCloud = new double[]{
                0.00061817, 0.01890794, 0.06560501, 0.11566512,
                0.13827639, 0.13763554, 0.11794497, 0.09684225,
                0.0752518, 0.05984302, 0.07637471, 0.04034527,
                0.01524999, 0.00590377, 0.00237625, 0.00109455,
                0.00139513, 0.00224014, 0.00651059, 0.02191938};

        // second distribution of reflectance samples
        final double[] histoCloud = new double[]{
                8.90559182e-06, 8.90559182e-06, 8.90559182e-06, 2.22639796e-04,
                3.56223673e-04, 4.71996367e-04, 5.25429917e-04, 7.03541754e-04,
                1.13101016e-03, 1.55847857e-03, 2.88630231e-02, 6.41291667e-02,
                8.59834890e-02, 1.03741239e-01, 1.28151466e-01, 1.21570234e-01,
                1.32737846e-01, 1.17874413e-01, 1.26495026e-01, 8.54580591e-02};

        // the borders of the bins:
        final double[] histoBins = new double[]{
                0.02550527, 0.03044264, 0.03538002, 0.0403174,
                0.04525477, 0.05019215, 0.05512953, 0.0600669,
                0.06500428, 0.06994166, 0.07487903, 0.08650545,
                0.10184053, 0.11717561, 0.13436025, 0.15568443,
                0.17700862, 0.20294027, 0.22932291, 0.26741164,
                0.53685361};

        final double skill =
                DistinctionSkill.computeDistinctionSkillFromCramerMisesAndersonMetric(histoNoCloud, histoCloud, histoBins,
                                                                                      numNoCloud, numCloud);

        final double expected = 0.857929;
        assertEquals(expected, skill, 1.E-5);
    }

    @Test
    public void testCalcDistinctionSkillNdvi() {
        // test data taken from RP's 'meris_cloud_naive_bayes_all_data_ndvi.npz'

        // first distribution of reflectance samples
        final double[] histoNoCloud = new double[]{
                0.07779252, 0.07779819, 0.07432739, 0.01907808,
                0.02194773, 0.02719931, 0.03528084, 0.04260242,
                0.04648156, 0.04072524, 0.02836192, 0.01796652,
                0.02825983, 0.08911234, 0.04522821, 0.03078921,
                0.05577673, 0.07770745, 0.08190418, 0.08166031};

        // second distribution of reflectance samples
        final double[] histoCloud = new double[]{
                8.90559182e-06, 8.90559182e-06, 1.13991575e-03, 1.42489469e-03,
                2.51137689e-03, 4.70215248e-03, 8.18423888e-03, 2.51493913e-02,
                4.06540267e-02, 1.14490288e-01, 2.69527736e-01, 2.56596817e-01,
                1.56275325e-01, 5.51434246e-02, 3.72431850e-02, 2.15248154e-02,
                5.38788305e-03, 8.90559182e-06, 8.90559182e-06, 8.90559182e-06};

        // the borders of the bins:
        final double[] histoBins = new double[]{
                -0.56842989, -0.22005687, -0.13986412, -0.08003084,
                -0.06748618, -0.05494151, -0.04239684, -0.02985217,
                -0.01730751, -0.00476284, 0.00778183, 0.0203265,
                0.03287116, 0.05477141, 0.08389139, 0.11301138,
                0.14213136, 0.19640211, 0.28978371, 0.39315372,
                0.64510043};

        final double skill =
                DistinctionSkill.computeDistinctionSkillFromCramerMisesAndersonMetric(histoNoCloud, histoCloud, histoBins,
                                                                                      numNoCloud, numCloud);

        final double expected = 0.160655;
        assertEquals(expected, skill, 1.E-5);
    }

    @Test
    public void testDistinctionSkillFromCloudNoCloudMeasureHistograms() {

        // start with a simple measure: solar brightness (rho860 over water)

        // these are MERIS rho_toa_13 samples (100 cloudy and 200 non-cloudy pixels)
        // the distributions are totally separated and have no overlap --> should result in distinction skill close to 1
        final double[] merisRho860CloudSamples = new double[]{
                0.15360162, 0.28005034, 0.08141995, 0.08155428, 0.3497656,
                0.15285456, 0.26146826, 0.08141445, 0.08185381, 0.2614545,
                0.15284404, 0.26145038, 0.077594206, 0.08184832, 0.26143712,
                0.27272546, 0.28363952, 0.12186693, 0.084408626, 0.294752,
                0.3749981, 0.15212235, 0.1218587, 0.100298904, 0.21204297,
                0.29783747, 0.1673388, 0.10046418, 0.10029218, 0.2120289,
                0.29781696, 0.16732743, 0.088888146, 0.09949642, 0.17038408,
                0.28517437, 0.12961687, 0.08888215, 0.10917015, 0.17543696,
                0.36402845, 0.11767024, 0.08239077, 0.10916284, 0.17542537,
                0.3640035, 0.11766223, 0.09358338, 0.12514709, 0.18644553,
                0.5143665, 0.106522866, 0.09357707, 0.19000845, 0.4796427,
                0.61498994, 0.09129105, 0.0818894, 0.18999574, 0.479611,
                0.61494786, 0.09128484, 0.0831379, 0.37268704, 0.5957112,
                0.33719718, 0.08427356, 0.0831323, 0.4347586, 0.5956719,
                0.5005299, 0.0798073, 0.081300735, 0.4347297, 0.40480998,
                0.51972485, 0.07980187, 0.08638749, 0.5376044, 0.25051448,
                0.51968926, 0.11733171, 0.086381696, 0.5676209, 0.25049797,
                0.3606243, 0.078940265, 0.07786687, 0.56758314, 0.4431559,
                0.19807456, 0.07893491, 0.07816676, 0.47205153, 0.56579715,
                0.19806102, 0.098700255, 0.07816152, 0.47202015, 0.56575984};

        final double[] merisRho860NoCloudSamples = new double[]{
                0.03484056, 0.032981742, 0.029965084, 0.03255001, 0.053690944,
                0.034838047, 0.03297938, 0.029655565, 0.032547727, 0.053687233,
                0.033895757, 0.03279194, 0.029744955, 0.030186351, 0.03520837,
                0.03420038, 0.03246625, 0.029742852, 0.03719751, 0.027419366,
                0.034197915, 0.032463938, 0.029878242, 0.037194904, 0.027417473,
                0.034133125, 0.031231962, 0.029645532, 0.028831596, 0.031017698,
                0.034314558, 0.031305905, 0.029643435, 0.031140948, 0.031015567,
                0.03394205, 0.031303674, 0.03054551, 0.031138765, 0.025808597,
                0.033939615, 0.031346895, 0.028564882, 0.031472854, 0.027332623,
                0.033690292, 0.031205818, 0.028562862, 0.047036853, 0.027330745,
                0.033471785, 0.031203594, 0.029204208, 0.047033556, 0.027328867,
                0.033469383, 0.03107792, 0.02946222, 0.044114903, 0.027326988,
                0.03278955, 0.030338136, 0.02946014, 0.047232434, 0.03445995,
                0.032494303, 0.030488903, 0.029472869, 0.047229137, 0.03445746,
                0.03249197, 0.03048673, 0.029393649, 0.042543925, 0.033992335,
                0.03319623, 0.03011559, 0.029391576, 0.036599524, 0.033650454,
                0.033285387, 0.029591035, 0.029419646, 0.038712967, 0.03364803,
                0.034097064, 0.029588932, 0.03153143, 0.038710274, 0.03402962,
                0.03409462, 0.030154046, 0.031529218, 0.040659457, 0.03411875,
                0.033922385, 0.029967204, 0.031174133, 0.040656645, 0.033500005,
                0.033248287, 0.03113289, 0.029532408, 0.033129524, 0.025026118,
                0.032922108, 0.031130672, 0.029069832, 0.033127215, 0.03387904,
                0.032919746, 0.031404257, 0.030983618, 0.029850215, 0.033876594,
                0.033085868, 0.030648997, 0.030981429, 0.035156313, 0.033857983,
                0.032621376, 0.030200955, 0.029384542, 0.035153862, 0.03351607,
                0.032619037, 0.030198805, 0.029918404, 0.03902086, 0.033513654,
                0.032815907, 0.030103967, 0.029916292, 0.03573035, 0.033079483,
                0.03298199, 0.030300794, 0.034786902, 0.03572788, 0.0328301,
                0.03331718, 0.030298641, 0.030523993, 0.031982914, 0.033411823,
                0.03331479, 0.03107863, 0.030521847, 0.029307628, 0.033409424,
                0.03438776, 0.030185757, 0.03195935, 0.029305601, 0.033252414,
                0.033416215, 0.03018362, 0.033856194, 0.034861498, 0.033003125,
                0.033413824, 0.030088855, 0.033853814, 0.030659731, 0.033000756,
                0.031658746, 0.03128286, 0.046610054, 0.030657612, 0.032751538,
                0.031809498, 0.031280648, 0.031887524, 0.02749496, 0.03276386,
                0.03180723, 0.029529123, 0.031885285, 0.027493069, 0.03276151,
                0.031220466, 0.02895897, 0.044444058, 0.030772645, 0.031927966,
                0.030848896, 0.028956922, 0.028928334, 0.02503128, 0.031817347,
                0.030846698, 0.028862333, 0.028926315, 0.025029559, 0.032337204,
                0.03084388, 0.029534498, 0.03222943, 0.02502784, 0.032334883};

        int numCloud = merisRho860CloudSamples.length;
        int numNoCloud = merisRho860NoCloudSamples.length;

        final double cloudSampleMax = (new Max()).evaluate(merisRho860CloudSamples);
        final double cloudSampleMin = (new Min()).evaluate(merisRho860CloudSamples);

        final double noCloudSampleMax = (new Max()).evaluate(merisRho860NoCloudSamples);
        final double noCloudSampleMin = (new Min()).evaluate(merisRho860NoCloudSamples);

        final double min = Math.min(cloudSampleMin, noCloudSampleMin);
        final double max = Math.max(cloudSampleMax, noCloudSampleMax);

        final MetImageHistogram cloudHisto = new MetImageHistogram(new int[MetImageConstants.NUM_BINS],
                                                                   min, max,
                                                                   MetImageConstants.ALPHA);
        cloudHisto.aggregate(merisRho860CloudSamples, false, IndexValidator.TRUE, ProgressMonitor.NULL);
        cloudHisto.computeDensityFunctions();

        final MetImageHistogram noCloudHisto = new MetImageHistogram(new int[MetImageConstants.NUM_BINS],
                                                                     min, max,
                                                                     MetImageConstants.ALPHA);
        noCloudHisto.aggregate(merisRho860NoCloudSamples, false, IndexValidator.TRUE, ProgressMonitor.NULL);
        noCloudHisto.computeDensityFunctions();

        final double distSkillRho860 =
                DistinctionSkill.computeDistinctionSkillFromCramerMisesAndersonMetric(noCloudHisto, cloudHisto, numNoCloud, numCloud);

        assertEquals(1.0, distSkillRho860, 5.E-2);
        System.out.println("distSkillRho860 = " + distSkillRho860);
    }

    @Test
    public void testDistinctionSkillFromRandomMeasureHistograms() {
        // the distributions are totally overlapping in range --> should result in distinction skill near zero

        double[] randomSamples1 = new double[100];
        double[] randomSamples2 = new double[200];

        for (int i = 0; i < randomSamples1.length; i++) {
            randomSamples1[i] = Math.random();
        }
        for (int i = 0; i < randomSamples2.length; i++) {
            randomSamples2[i] = Math.random();
        }

        final MetImageHistogram histo1 = new MetImageHistogram(new int[MetImageConstants.NUM_BINS],
                                                                   0.0, 1.0,
                                                                   MetImageConstants.ALPHA);
        histo1.aggregate(randomSamples1, false, IndexValidator.TRUE, ProgressMonitor.NULL);
        histo1.computeDensityFunctions();

        final MetImageHistogram histo2 = new MetImageHistogram(new int[MetImageConstants.NUM_BINS],
                                                               0.0, 1.0,
                                                               MetImageConstants.ALPHA);
        histo2.aggregate(randomSamples2, false, IndexValidator.TRUE, ProgressMonitor.NULL);
        histo2.computeDensityFunctions();

        final double distSkillRandom =
                DistinctionSkill.computeDistinctionSkillFromCramerMisesAndersonMetric(histo1, histo2, 100, 200);

        System.out.println("distSkillRandom = " + distSkillRandom);
        assertEquals(0.0, distSkillRandom, 5.E-2);   // we get a skill of about 0.015, which should be ok
    }
}