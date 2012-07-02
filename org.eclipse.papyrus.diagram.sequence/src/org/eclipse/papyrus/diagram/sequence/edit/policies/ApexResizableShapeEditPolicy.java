package org.eclipse.papyrus.diagram.sequence.edit.policies;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.ResizableShapeEditPolicy;

/**
 * 이동 방향에 제한을 둘 수 있는 ResizableShapedEditPolicy
 * @author Jiho
 */
public class ApexResizableShapeEditPolicy extends ResizableShapeEditPolicy {

	private int moveDirection;
	
	/**
	 * Constructor
	 * 
	 * @param moveDirection
	 * 				the direction of the move
	 */
	public ApexResizableShapeEditPolicy(int moveDirection) {
		super();
		setMoveDirection(moveDirection);
	}

	@Override
	protected void showChangeBoundsFeedback(ChangeBoundsRequest request) {
		Point moveDelta = request.getMoveDelta();
		if ((moveDirection & PositionConstants.EAST) == 0 && moveDelta.x < 0)
			moveDelta.x = 0;
		if ((moveDirection & PositionConstants.WEST) == 0 && moveDelta.x > 0)
			moveDelta.x = 0;
		if ((moveDirection & PositionConstants.SOUTH) == 0 && moveDelta.y < 0)
			moveDelta.y = 0;
		if ((moveDirection & PositionConstants.NORTH) == 0 && moveDelta.y > 0)
			moveDelta.y = 0;
		request.setMoveDelta(moveDelta);
		super.showChangeBoundsFeedback(request);
	}

	@Override
	protected Command getMoveCommand(ChangeBoundsRequest request) {
		ChangeBoundsRequest req = new ChangeBoundsRequest(REQ_MOVE_CHILDREN);
		req.setEditParts(getHost());
		req.setMoveDelta(request.getMoveDelta());
		req.setSizeDelta(request.getSizeDelta());
		req.setLocation(request.getLocation());
		req.setExtendedData(request.getExtendedData());
		req.setResizeDirection(moveDirection);
		return getHost().getParent().getCommand(req);
	}

	/**
	 * Returns the direction the figure is being moved. Possible values are
	 * <ul>
	 * <li>{@link org.eclipse.draw2d.PositionConstants#EAST}
	 * <li>{@link org.eclipse.draw2d.PositionConstants#WEST}
	 * <li>{@link org.eclipse.draw2d.PositionConstants#NORTH}
	 * <li>{@link org.eclipse.draw2d.PositionConstants#SOUTH}
	 * <li>{@link org.eclipse.draw2d.PositionConstants#NORTH_EAST}
	 * <li>{@link org.eclipse.draw2d.PositionConstants#NORTH_WEST}
	 * <li>{@link org.eclipse.draw2d.PositionConstants#SOUTH_EAST}
	 * <li>{@link org.eclipse.draw2d.PositionConstants#SOUTH_WEST}
	 * </ul>
	 * 
	 * @return the move direction
	 */
	public int getMoveDirection() {
		return moveDirection;
	}

	/**
	 * Sets the direction the figure is being moved.
	 * @param moveDirection
	 */
	public void setMoveDirection(int moveDirection) {
		this.moveDirection = moveDirection;
	}
	
}
