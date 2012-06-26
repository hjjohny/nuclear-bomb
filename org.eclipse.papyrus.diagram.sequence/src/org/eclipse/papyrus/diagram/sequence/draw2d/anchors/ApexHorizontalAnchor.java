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

	public Point getLocation(Point reference) {
		Point ownReference = normalizeToStraightlineTolerance(reference, getReferencePoint(), 0);
		
		//*8
//		for (int i = 5; i > 1; i--) {
//			StackTraceElement st = Thread.currentThread().getStackTrace()[i];
//			System.out.println(st.getClassName() + "." + st.getMethodName() + "(" + st.getLineNumber() + ")");
//		}
		System.out.println(reference + " // " + getReferencePoint() + " // " + ownReference);
		//*/
		
		Point location = getLocation(ownReference, reference);
		if (location == null) {
			location = getLocation(new PrecisionPoint(getBox().getCenter()), reference);
			if (location == null) {
				location = getBox().getCenter();
			}
		}
		return location;
	}

	@Override
	public Point getReferencePoint() {
		Point referencePoint = super.getReferencePoint();
		//*8
		for (int i = 5; i > 1; i--) {
			StackTraceElement st = Thread.currentThread().getStackTrace()[i];
			System.out.println(st.getClassName() + "." + st.getMethodName() + "(" + st.getLineNumber() + ")");
		}
		System.out.println("ReferencePoint: " + referencePoint);
		//*/
		return referencePoint;
	}

	protected Rectangle getBox() {
		Rectangle rBox = getOwner() instanceof HandleBounds ? new PrecisionRectangle(
				((HandleBounds) getOwner()).getHandleBounds())
				: new PrecisionRectangle(getOwner().getBounds());
		getOwner().translateToAbsolute(rBox);
		return rBox;
	}

}
