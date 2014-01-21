package org.esa.beam.metimage.operator;

import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.OperatorSpi;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.annotations.SourceProduct;
import org.esa.beam.framework.gpf.annotations.TargetProduct;

/**
 * Operator for MERIS atmospheric correction with SCAPE-M algorithm.
 *
 * @author Marco Zuehlke, Olaf Danne
 */
@OperatorMetadata(alias = "beam.metimage", version = "1.0-SNAPSHOT",
                  authors = "Rene Preusker, Olaf Danne, Marco Zuehlke",
                  copyright = "(c) 2013/14 FU Berlin, Brockmann Consult",
                  description = "Operator for MetImage Cloud processing.")
public class MetImageOp extends Operator {
    public static final String VERSION = "1.0-SNAPSHOT";

    @SourceProduct(alias = "MODIS_L1b", description = "MODIS L1B product")
    private Product sourceProduct;

    @TargetProduct
    private Product targetProduct;


    @Override
    public void initialize() throws OperatorException {

    }

    public static class Spi extends OperatorSpi {

        public Spi() {
            super(org.esa.beam.metimage.operator.MetImageOp.class);
        }
    }
}
