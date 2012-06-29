package org.eclipse.papyrus.diagram.sequence.edit.policies;

import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.diagram.core.internal.commands.IPropertyValueDeferred;
import org.eclipse.gmf.runtime.diagram.core.util.ViewUtil;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.commands.SetBoundsCommand;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.ResizableShapeEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.internal.requests.ChangeBoundsDeferredRequest;
import org.eclipse.gmf.runtime.diagram.ui.l10n.DiagramUIMessages;
import org.eclipse.gmf.runtime.diagram.ui.requests.RequestConstants;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.diagram.sequence.edit.parts.CombinedFragmentEditPart;
import org.eclipse.papyrus.diagram.sequence.edit.parts.InteractionOperandEditPart;
import org.eclipse.papyrus.diagram.sequence.util.ApexSequenceUtil;

/**
 * 이동 방향에 제한을 둘 수 있는 ResizableShapedEditPolicy
 * @author Jiho
 */
public class ApexResizableShapeEditPolicy extends ResizableShapeEditPolicy {

	private int moveDirection;
	
	private boolean autoSizeEnabled = true;
	
	private boolean moveDeferredEnabled = true;
	
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
	
	public ApexResizableShapeEditPolicy(int moveDirection, boolean autoSizeEnabled) {
		super();
		setMoveDirection(moveDirection);
		setAutoSizeEnabled(autoSizeEnabled);
	}
	
	public ApexResizableShapeEditPolicy(int moveDirection, boolean autoSizeEnabled, boolean moveDeferredEnabled) {
		super();
		setMoveDirection(moveDirection);
		setAutoSizeEnabled(autoSizeEnabled);
		setMoveDeferredEnabled(moveDeferredEnabled);
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
	 * apex update
	 * 
	 * autoSize, moveDefer 통제 추가
	 */
	@Override
	public Command getCommand(Request request) {
		/* apex improved start */
		if (RequestConstants.REQ_AUTOSIZE.equals(request.getType()) /*&& autoSizeEnabled*/) {
//System.out.println("autosize doing");
			return getAutoSizeCommand(request);
		}
			
		if (RequestConstants.REQ_MOVE_DEFERRED.equals(request.getType()) /*&& moveDeferredEnabled*/) {
//System.out.println("moveDeffered doing");
			return getMoveDeferredCommand((ChangeBoundsDeferredRequest) request);
		}
			
		return super.getCommand(request);
		/* apex improved start */
		
		/* apex replaced
		if (RequestConstants.REQ_AUTOSIZE.equals(request.getType()))
			return getAutoSizeCommand(request);
		if (RequestConstants.REQ_MOVE_DEFERRED.equals(request.getType()))
			return getMoveDeferredCommand((ChangeBoundsDeferredRequest) request);
		return super.getCommand(request);
		*/
	}
	
	/**
	 * Cfreates a new AutoSize comamnd
	 * 
	 * @param request
	 * @return command
	 */
	@Override
	protected Command getAutoSizeCommand(Request request) {
		/* apex improved start */
		int width = -1;
		
		int height = -1;
//*8		
		if ( getHost() instanceof CombinedFragmentEditPart) {
			
			CombinedFragmentEditPart cfep = (CombinedFragmentEditPart)getHost();
			Rectangle thisRect = cfep.getFigure().getBounds().getCopy();
/*8
System.out.println("before toAbsolute : " + thisRect);
//*/
			// 절대좌표로 변환
			cfep.getFigure().getParent().translateToAbsolute(thisRect);
/*8
System.out.println("after  toAbsolute : " + thisRect);
//*/
			int buffer = 30;
			int thisWidth = thisRect.width;
			int thisRight = thisRect.right();
			int thisLeft = thisRect.x;
			int maxChildRight = thisRight;
			List<EditPart> childrenList = ApexSequenceUtil.apexGetChildEditPartList(cfep);

			for ( EditPart ep : childrenList) {
				if ( ep instanceof CombinedFragmentEditPart) {
					AbstractGraphicalEditPart agep = (AbstractGraphicalEditPart)ep;
					Rectangle childRect = agep.getFigure().getBounds().getCopy();
					// 절대좌표로 변환
					agep.getFigure().getParent().translateToAbsolute(childRect);
					int childRight = childRect.right();
					// 절대값으로 비교
					if (childRight > maxChildRight)
						maxChildRight = childRight;						
				}
			}						
			
			if (maxChildRight > thisRight)
				width = thisWidth + (maxChildRight-thisRight) + buffer;
			else
				width = thisWidth;
		
			Rectangle newBounds = cfep.getFigure().getBounds().getCopy();
			
/*8
System.out.println("////////////");
System.out.println("request.getType()      : " + request.getType());
if ( request instanceof ChangeBoundsRequest)
	System.out.println("request.sizeDelta  : " + ((ChangeBoundsRequest)request).getSizeDelta());
System.out.println("thisLeft      : " + thisLeft);
System.out.println("thisWidth     : " + thisWidth);
System.out.println("thisRight     : " + thisRight);
System.out.println("maxChildRight : " + maxChildRight);
System.out.println("result width  : " + width);
//*/
			height = newBounds.height;
		}
//*/

//		width = ((CombinedFragmentEditPart)getHost()).getFigure().getBounds().width+30;
		
		TransactionalEditingDomain editingDomain = ((IGraphicalEditPart) getHost()).getEditingDomain();
		ICommand resizeCommand = new SetBoundsCommand(editingDomain, 
			DiagramUIMessages.SetAutoSizeCommand_Label,
			new EObjectAdapter((View) getHost().getModel()), new Dimension(width, height));
//System.out.println("in ApexRe~EditPolicy, resizeCommand.canExecute() : " + resizeCommand.canExecute());
		return new ICommandProxy(resizeCommand);
		/* apex improved end */
		
		/* apex replaced
		TransactionalEditingDomain editingDomain = ((IGraphicalEditPart) getHost()).getEditingDomain();
		ICommand resizeCommand = new SetBoundsCommand(editingDomain, 
			DiagramUIMessages.SetAutoSizeCommand_Label,
			new EObjectAdapter((View) getHost().getModel()), new Dimension(-1,
				-1));
		return new ICommandProxy(resizeCommand);
    	*/
	}

	/**
	 * Method getMoveDeferredCommand.
	 * 
	 * @param request
	 * @return Command
	 */
	protected Command getMoveDeferredCommand(ChangeBoundsDeferredRequest request) {
System.out.println("moveDeferred");
		final class SetDeferredPropertyCommand
			extends AbstractTransactionalCommand {

			private IAdaptable newValue;

			private IAdaptable viewAdapter;

			/**
			 * constructor
			 * 
             * @param editingDomain
             * the editing domain through which model changes are made
			 * @param label
			 * @param viewAdapter
			 * @param newValue
			 */
			public SetDeferredPropertyCommand(TransactionalEditingDomain editingDomain, String label,
					IAdaptable viewAdapter, IAdaptable newValue) {
				super(editingDomain, label, null);
				this.viewAdapter = viewAdapter;
				this.newValue = newValue;
			}

			protected CommandResult doExecuteWithResult(
                    IProgressMonitor progressMonitor, IAdaptable info)
                throws ExecutionException {
                
				if (null == viewAdapter || null == newValue)
					return CommandResult.newCancelledCommandResult();

				View view = (View) viewAdapter.getAdapter(View.class);
				Point p = (Point) newValue
					.getAdapter(IPropertyValueDeferred.class);
				ViewUtil.setStructuralFeatureValue(view, NotationPackage.eINSTANCE.getLocation_X(),
						Integer.valueOf(p.x));
				ViewUtil.setStructuralFeatureValue(view, NotationPackage.eINSTANCE.getLocation_Y(),
						Integer.valueOf(p.y));

				// clear for garbage collection
				viewAdapter = null;
				newValue = null;
				return CommandResult.newOKCommandResult();
			}
		}
        View view = (View) getHost().getModel();
        
        TransactionalEditingDomain editingDomain = ((IGraphicalEditPart) getHost())
            .getEditingDomain();
        
        SetDeferredPropertyCommand cmd = new SetDeferredPropertyCommand(editingDomain,
			DiagramUIMessages.ResizableShapeEditPolicy_MoveDeferredCommand_label,
			new EObjectAdapter(view), request
				.getLocationAdapter());
		return new ICommandProxy(cmd);
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

	public boolean isAutoSizeEnabled() {
		return autoSizeEnabled;
	}

	public void setAutoSizeEnabled(boolean autoSizeEnabled) {
		this.autoSizeEnabled = autoSizeEnabled;
	}

	public boolean isMoveDeferredEnabled() {
		return moveDeferredEnabled;
	}

	public void setMoveDeferredEnabled(boolean moveDeferredEnabled) {
		this.moveDeferredEnabled = moveDeferredEnabled;
	}
	
}
