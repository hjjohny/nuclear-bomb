package org.eclipse.papyrus.diagram.sequence.edit.policies;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.XYLayoutEditPolicy;
import org.eclipse.papyrus.diagram.sequence.edit.parts.LifelineEditPart;

public class ApexPackageXYLayoutEditPolicy extends XYLayoutEditPolicy {

	@Override
	protected Object getConstraintFor(ChangeBoundsRequest request,
			GraphicalEditPart child) {
		Rectangle rect = (Rectangle)super.getConstraintFor(request, child);
		if (child instanceof LifelineEditPart) {
			child.getFigure().translateToRelative(rect);
		}
		return rect;
	}

}
