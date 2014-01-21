package org.esa.beam.metimage.math;

/**
 * Class providing the distinction skill computation as proposed by R.Preusker, FUB
 *
 * @author M. Zuehlke, O. Danne
 */
public class DistinctionSkill {


    public static double computeCramerMisesAndersonMetric(double[] pdfa, double[] pdfb, double[] bins, int numCloud, int numNoCloud) {
        final int nPdfa = pdfa.length;
        final int nPdfb = pdfb.length;

        // todo: implement this:

//        #calc cumulative sum and norm it  to cumulative  probability
//          cdfa=np.zeros(len(pdfa)+1)
//          cdfb=np.zeros(len(pdfb)+1)
//          cdfa[1:]=pdfa.cumsum()
//          cdfa/= cdfa[-1]
//          cdfb[1:]=pdfb.cumsum()
//          cdfb/= cdfb[-1]
//          f_cdfa  = interp1d(bins,cdfa)
//          f_cdfb  = interp1d(bins,cdfb)
//          f_cdfab = interp1d(bins,(cdfb*nb+cdfa*na)/(na+nb))
//
//          # define quantiles in physical space
//          nn=100000
//          q=np.linspace(bins[0],bins[-1],nn)
//          # distance
//          ff=(f_cdfa(q)-f_cdfb(q))**2
//          # integral and norm
//          return np.trapz(ff,f_cdfab(q))*3.
//


        return 0;
    }
}
