package org.eclipse.papyrus.diagram.sequence.draw2d.anchors;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.handles.HandleBounds;
import org.eclipse.gmf.runtime.gef.ui.figures.SlidableAnchor;

public class ApexHorizontalAnchor extends SlidableAnchor {

	public ApexHorizontalAnchor(IFigure figure) {
		super(figure);
	}
	
	public ApexHorizontalAnchor(IFigure figure, PrecisionPoint point) {
		super(figure, point);
	}
	
	static private int STRAIGHT_LINE_TOLERANCE = 0;

	public Point getLocation(Point reference) {
		Point ownReference = normalizeToStraightlineTolerance(reference, getReferencePoint(), STRAIGHT_LINE_TOLERANCE);
		
		Point location = getLocation(ownReference, reference);
		if (location == null) {
			location = getLocation(new PrecisionPoint(getBox().getCenter()), reference);
			if (location == null) {
				location = getBox().getCenter();
			}
		}
		
		// SEQ-LL-005
		location.y = reference.y;
		
		return location;
	}

	protected Rectangle getBox() {
		Rectangle rBox = getOwner() instanceof HandleBounds ? new PrecisionRectangle(
				((HandleBounds) getOwner()).getHandleBounds())
				: new PrecisionRectangle(getOwner().getBounds());
		getOwner().translateToAbsolute(rBox);
		return rBox;
	}

}
