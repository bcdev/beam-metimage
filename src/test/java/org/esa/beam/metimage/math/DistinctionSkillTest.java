package org.esa.beam.metimage.math;

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


}