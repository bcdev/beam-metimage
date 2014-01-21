package org.esa.beam.metimage.ui;

import org.esa.beam.framework.gpf.ui.DefaultSingleTargetProductDialog;
import org.esa.beam.framework.ui.command.CommandEvent;
import org.esa.beam.visat.actions.AbstractVisatAction;

import java.awt.*;

/**
 * SCAPE-M Action class for VISAT
 *
 * @author Tonio Fincke, Olaf Danne
 */
public class MetImageAction extends AbstractVisatAction {

    private DefaultSingleTargetProductDialog dialog;

    @Override
    public void actionPerformed(CommandEvent event) {

        final String helpId = event.getCommand().getHelpId();
        if (dialog == null) {
            dialog = new DefaultSingleTargetProductDialog(
                    "beam.metimage",
                    getAppContext(),
                    "MetImage Cloud Processing - v" + org.esa.beam.metimage.operator.MetImageOp.VERSION,
                    helpId);
            dialog.setTargetProductNameSuffix("_METIMAGE");
            dialog.getJDialog().getContentPane().setPreferredSize(new Dimension(500, 400));
        }
        dialog.show();
    }

}
