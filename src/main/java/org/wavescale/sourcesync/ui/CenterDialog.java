package org.wavescale.sourcesync.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Created by fauri on 02/05/16.
 * <p/>
 * Custom dialog that centers on the parent window.
 */


public class CenterDialog extends JDialog {

    public CenterDialog(Window parent) {
        super(parent);
    }

    /**
     * Centers dialog in the middle of the parent. Make sure {@link Window#pack()} is called first.
     */
    public void centerOnParent() {
        boolean useChildsOwner = this.getOwner() != null ? ((this.getOwner() instanceof JFrame) || (this.getOwner() instanceof JDialog)) : false;
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final Dimension parentSize = useChildsOwner ? this.getOwner().getSize() : screenSize;
        final Point parentLocationOnScreen = useChildsOwner ? this.getOwner().getLocationOnScreen() : new Point(0, 0);
        final Dimension childSize = this.getSize();
        childSize.width = Math.min(childSize.width, screenSize.width);
        childSize.height = Math.min(childSize.height, screenSize.height);
        this.setSize(childSize);
        int x;
        int y;
        if ((this.getOwner() != null) && this.getOwner().isShowing()) {
            x = (parentSize.width - childSize.width) / 2;
            y = (parentSize.height - childSize.height) / 2;
            x += parentLocationOnScreen.x;
            y += parentLocationOnScreen.y;
        } else {
            x = (screenSize.width - childSize.width) / 2;
            y = (screenSize.height - childSize.height) / 2;
        }
        this.setLocation(x, y);
    }
}
