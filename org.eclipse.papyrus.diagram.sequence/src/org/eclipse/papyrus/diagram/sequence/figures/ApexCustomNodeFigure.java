package org.eclipse.papyrus.diagram.sequence.figures;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure;
import org.eclipse.papyrus.diagram.sequence.draw2d.anchors.ApexHorizontalAnchor;

public class ApexCustomNodeFigure extends NodeFigure {

	@Override
	protected ConnectionAnchor createDefaultAnchor() {
		return new ApexHorizontalAnchor(this);
	}

	@Override
	protected ConnectionAnchor createAnchor(PrecisionPoint p) {
		if (p==null)
			return createDefaultAnchor();
		return new ApexHorizontalAnchor(this, p);
	}

}
