package org.esa.beam.metimage.ui;

import com.bc.ceres.binding.Property;
import com.bc.ceres.binding.PropertyDescriptor;
import org.esa.beam.framework.gpf.ui.DefaultSingleTargetProductDialog;
import org.esa.beam.framework.ui.AppContext;

/**
 * MetImage Processor dialog in Visat
 *
 * @author olafd
 */
public class MetImageProcessingDialog extends DefaultSingleTargetProductDialog {
    public MetImageProcessingDialog(String operatorName, AppContext appContext, String title, String helpID) {
        super(operatorName, appContext, title, helpID);
        setDirectoryProperties();
    }

    private void setDirectoryProperties() {
        Property[] properties = getBindingContext().getPropertySet().getProperties();
        for (Property property : properties) {
            PropertyDescriptor descriptor = property.getDescriptor();
            // this is a workaround to obtain a directory chooser rather than a file chooser
            if (descriptor.getName().toLowerCase().endsWith("directory")) {
                descriptor.setAttribute("directory", Boolean.TRUE);
            }
        }
    }
}
