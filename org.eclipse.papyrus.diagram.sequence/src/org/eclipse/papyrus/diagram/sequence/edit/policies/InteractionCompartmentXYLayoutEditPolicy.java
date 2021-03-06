/*****************************************************************************
 * Copyright (c) 2009 Atos Origin.
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Atos Origin - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.diagram.sequence.edit.policies;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.commands.UnexecutableCommand;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.commands.SetBoundsCommand;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ShapeEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ShapeNodeEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewRequest.ViewDescriptor;
import org.eclipse.gmf.runtime.diagram.ui.requests.RequestConstants;
import org.eclipse.gmf.runtime.draw2d.ui.figures.BaseSlidableAnchor;
import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.emf.type.core.commands.SetValueCommand;
import org.eclipse.gmf.runtime.emf.type.core.requests.SetRequest;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.IdentityAnchor;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.osgi.util.NLS;
import org.eclipse.papyrus.diagram.common.commands.PreserveAnchorsPositionCommand;
import org.eclipse.papyrus.diagram.sequence.edit.parts.ActionExecutionSpecificationEditPart;
import org.eclipse.papyrus.diagram.sequence.edit.parts.BehaviorExecutionSpecificationEditPart;
import org.eclipse.papyrus.diagram.sequence.edit.parts.CombinedFragmentCombinedFragmentCompartmentEditPart;
import org.eclipse.papyrus.diagram.sequence.edit.parts.CombinedFragmentEditPart;
import org.eclipse.papyrus.diagram.sequence.edit.parts.InteractionInteractionCompartmentEditPart;
import org.eclipse.papyrus.diagram.sequence.edit.parts.InteractionOperandEditPart;
import org.eclipse.papyrus.diagram.sequence.edit.parts.LifelineEditPart;
import org.eclipse.papyrus.diagram.sequence.part.Messages;
import org.eclipse.papyrus.diagram.sequence.providers.UMLElementTypes;
import org.eclipse.papyrus.diagram.sequence.util.ApexSequenceUtil;
import org.eclipse.papyrus.diagram.sequence.util.SequenceUtil;
import org.eclipse.papyrus.ui.toolbox.notification.builders.NotificationBuilder;
import org.eclipse.swt.SWT;
import org.eclipse.uml2.uml.CombinedFragment;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.InteractionOperand;

/**
 * The customn XYLayoutEditPolicy for InteractionCompartmentEditPart.
 */
public class InteractionCompartmentXYLayoutEditPolicy extends XYLayoutEditPolicy {

	/* apex added start */
	private static final int VERTICAL_MARGIN = 10;
	/* apex added end */
	
	/**
	 * Handle lifeline and combined fragment resize
	 */
	@Override
	protected Command getResizeChildrenCommand(ChangeBoundsRequest request) {
		CompoundCommand compoundCmd = new CompoundCommand();
		compoundCmd.setLabel("Move or Resize");

		for(Object o : request.getEditParts()) {
			GraphicalEditPart child = (GraphicalEditPart)o;
			Object constraintFor = getConstraintFor(request, child);
			if(constraintFor != null) {
				if(child instanceof LifelineEditPart) {
					addLifelineResizeChildrenCommand(compoundCmd, request, (LifelineEditPart)child, 1);
				} else if(child instanceof CombinedFragmentEditPart) {
					Command resizeChildrenCommand = getCombinedFragmentResizeChildrenCommand(request, (CombinedFragmentEditPart)child);
					if(resizeChildrenCommand != null && resizeChildrenCommand.canExecute()) {
						compoundCmd.add(resizeChildrenCommand);
					} else if(resizeChildrenCommand != null) {
						return UnexecutableCommand.INSTANCE;
					}
				}

				Command changeConstraintCommand = createChangeConstraintCommand(request, child, translateToModelConstraint(constraintFor));
				compoundCmd.add(changeConstraintCommand);
			}
		}
		return compoundCmd.unwrap();

	}

	/**
	 * Resize children of LifelineEditPart (Execution specification and lifeline)
	 * 
	 * @param compoundCmd
	 *        The command
	 * @param request
	 *        The request
	 * @param lifelineEditPart
	 *        The lifelineEditPart to resize
	 * @param number
	 *        The number of brother of the LifelineEditPart
	 */
	private static void addLifelineResizeChildrenCommand(CompoundCommand compoundCmd, ChangeBoundsRequest request, LifelineEditPart lifelineEditPart, int number) {
		// If the width increases or decreases, ExecutionSpecification elements need to
		// be moved
		int widthDelta;
		for(ShapeNodeEditPart executionSpecificationEP : lifelineEditPart.getChildShapeNodeEditPart()) {
			if(executionSpecificationEP.resolveSemanticElement() instanceof ExecutionSpecification) {
				// Lifeline's figure where the child is drawn
				Rectangle rDotLine = lifelineEditPart.getContentPane().getBounds();

				// The new bounds will be calculated from the current bounds
				Rectangle newBounds = executionSpecificationEP.getFigure().getBounds().getCopy();

				widthDelta = request.getSizeDelta().width;

				if(widthDelta != 0) {

					if(rDotLine.getSize().width + widthDelta < newBounds.width * 2) {
						compoundCmd.add(UnexecutableCommand.INSTANCE);
					}

					// Apply SizeDelta to the children
					widthDelta = Math.round(widthDelta / ((float)2 * number));

					newBounds.x += widthDelta;

					// Convert to relative
					newBounds.x -= rDotLine.x;
					newBounds.y -= rDotLine.y;

					SetBoundsCommand setBoundsCmd = new SetBoundsCommand(executionSpecificationEP.getEditingDomain(), "Re-location of a ExecutionSpecification due to a Lifeline movement", executionSpecificationEP, newBounds);
					compoundCmd.add(new ICommandProxy(setBoundsCmd));
				}

				// update the enclosing interaction of a moved execution specification
				compoundCmd.add(SequenceUtil.createUpdateEnclosingInteractionCommand(executionSpecificationEP, request.getMoveDelta(), new Dimension(widthDelta, 0)));
			}
		}

		List<LifelineEditPart> innerConnectableElementList = lifelineEditPart.getInnerConnectableElementList();
		for(LifelineEditPart lifelineEP : innerConnectableElementList) {
			addLifelineResizeChildrenCommand(compoundCmd, request, lifelineEP, number * innerConnectableElementList.size());
		}
	}

	/**
	 * abstractGraphicalEditPart보다 아래에 있는 item들 모두의 좌표를 request내의 delta만 이동
	 * 
	 * @param abstractGraphicalEditPart
	 * @param request
	 * @return 
	 */
	public static void apexGetResizeOrMoveBelowItemsCommand(ChangeBoundsRequest request, org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart abstractGraphicalEditPart, CompoundCommand compoundCmd) {
		// Root 아래의 모든 EP 나열
		/*8
		System.out.println("###### Children List Start #####");
		SequenceUtil.omwShowChildrenEditPart(abstractGraphicalEditPart.getRoot(), 0);
		System.out.println("###### Children List End #####");
		*/
/*8
System.out.println("###### Translate Start #####");
Rectangle r1 = new Rectangle(300, 100, 500, 200);
System.out.println("  before translate(30, 10) : " + r1);
r1.translate(30, 10);
System.out.println("   after translate(30, 10) : " + r1);
System.out.println("###### Translate End #####");
*/
		// 넘겨받은 AbstractGraphicalEditPart 보다 아래에 있는 belowList 구성		
		List belowEditPartList = ApexSequenceUtil.apexGetBelowEditPartList(abstractGraphicalEditPart);

//		CompoundCommand compoundCmd = new CompoundCommand();

		if ( belowEditPartList.size() > 0 ) {
			// 이동할 위치
			Point moveDelta = request.getMoveDelta();		

			IFigure thisFigure = abstractGraphicalEditPart.getFigure();
			Rectangle origCFBounds = thisFigure.getBounds().getCopy();

/*8
System.out.println("==========================================");		
System.out.println("before translateToAbsolute Bounds : " + origCFBounds);
*/
			thisFigure.getParent().translateToAbsolute(origCFBounds);

/*8
System.out.println("after translateToAbsolute Bounds  : " + origCFBounds);
*/

			origCFBounds.translate(thisFigure.getParent().getBounds().getLocation());
/*8
System.out.println("after translate Bounds            : " + origCFBounds);
System.out.println("==========================================");
*/

			// 넘겨받은 AbstractGraphicalEditPart 의 이동 후 위치
			int yAfterMove = origCFBounds.getBottom().y+moveDelta.y;

			// 넘겨받은 AbstractGraphicalEditPart 바로 아래의 EditPart 구성
			IGraphicalEditPart beneathEditPart  = ApexSequenceUtil.apexGetBeneathEditPart(abstractGraphicalEditPart, belowEditPartList);

			IFigure beneathFigure = beneathEditPart.getFigure();
			Rectangle beneathBounds = beneathFigure.getBounds().getCopy();

			beneathFigure.getParent().translateToAbsolute(beneathBounds);

			int topOfBeneathEditPart = beneathBounds.getTop().y;

			// beneathEditPart 보다 아래로 내릴 경우			
			if (yAfterMove >= topOfBeneathEditPart) {

				Iterator it = belowEditPartList.iterator();
				while( it.hasNext()) {		

					EditPart ep = (EditPart)it.next();
/*8
System.out.println("### in omw below list start ###");
System.out.println("   " + ep.getClass().getSimpleName());
System.out.println("### in omw below list end ###");
*/

					// handle move of object graphically owned by the lifeline
					if(ep instanceof ShapeEditPart) {
						ShapeEditPart sep = (ShapeEditPart)ep;
						EObject elem = sep.getNotationView().getElement();

						if(elem instanceof InteractionFragment) {
							IFigure figure = sep.getFigure();

							Rectangle figureBounds = figure.getBounds().getCopy();
							figure.getParent().translateToAbsolute(figureBounds);

							EditPart parentEP = sep.getParent();

							if(parentEP instanceof LifelineEditPart) {
								ChangeBoundsRequest esRequest = new ChangeBoundsRequest(RequestConstants.REQ_MOVE);
								esRequest.setEditParts(sep);
								esRequest.setMoveDelta(moveDelta);

								Command moveESCommand = LifelineXYLayoutEditPolicy.getResizeOrMoveChildrenCommand((LifelineEditPart)parentEP, esRequest, true, false, true);

								if(moveESCommand != null && !moveESCommand.canExecute()) {
									// forbid move if the es can't be moved correctly
									compoundCmd.add(UnexecutableCommand.INSTANCE);
								} else if(moveESCommand != null) {
									compoundCmd.add(moveESCommand);
								}
							}
							/* apex added start */
							// below에 다른 CombinedFragment 가 있을 경우 같이 이동
							if(sep instanceof CombinedFragmentEditPart) {
								// The new bounds will be calculated from the current bounds
								Rectangle newBounds = sep.getFigure().getBounds().getCopy();

								// Convert to relative
								newBounds.x += moveDelta.x;
								newBounds.y += moveDelta.y;

								SetBoundsCommand setBoundsCmd = new SetBoundsCommand(sep.getEditingDomain(), "Move of a CombinedFragment due to a Upper CF movement", sep, newBounds);
								compoundCmd.add(new ICommandProxy(setBoundsCmd));						
							}
							/* apex added end */
						}
					}

					// handle move of messages directly attached to a lifeline
					// 보통 메세지가 아니라 Lifeline에 직접붙은 메세지. 즉, async 등의 경우 처리
					// sync의 경우 아래의 로직에 의해 이동되는게 아니라
					// 딸린 ExecSpec이 위의 로직에 의해 이동되기 때문에 ExecSpec따라 이동

					// 위 4행의 주석은 원래 Papyrus에 해당하고
					// 수정된 papyrus는 message 가 별도로 움직임

					if(ep instanceof ConnectionEditPart) {
/*8
System.out.println("??? in omw right after ConnectionEditPart ???");
*/
						ConnectionEditPart cep = (ConnectionEditPart)ep;
						Connection msgFigure = cep.getConnectionFigure();

						ConnectionAnchor sourceAnchor = msgFigure.getSourceAnchor();
						ConnectionAnchor targetAnchor = msgFigure.getTargetAnchor();

						Point sourcePoint = sourceAnchor.getReferencePoint();
						Point targetPoint = targetAnchor.getReferencePoint();

						Edge edge = (Edge)cep.getModel();
/*8
System.out.println("is Async? " + cep.getClass().getSimpleName());
System.out.println("Parent of Async : " + cep.getParent().getClass().getSimpleName());
System.out.println("Source of Async : " + cep.getSource().getClass().getSimpleName());
System.out.println("Target of Async : " + cep.getTarget().getClass().getSimpleName());
System.out.println("SourcePoint of Async : " + sourcePoint);
System.out.println("TargetPoint of Async : " + targetPoint);
System.out.println("Bounds before translate : " + thisFigure.getBounds().getCopy());
System.out.println("Bounds after translate : " + origCFBounds);
System.out.println("is Async Source contained: " + origCFBounds.contains(sourcePoint));
System.out.println("is Async Target contained: " + origCFBounds.contains(targetPoint));
*/

						/* apex improved start */
						if(cep.getSource() instanceof LifelineEditPart) {
/*8
System.out.println("??? in omw right after LifelineEditPart source after ConnectionEditPart ???");
*/
							IdentityAnchor gmfAnchor = (IdentityAnchor)edge.getSourceAnchor();
							Rectangle figureBounds = sourceAnchor.getOwner().getBounds();
							compoundCmd.add(new ICommandProxy(getMoveAnchorCommand(moveDelta.y, figureBounds, gmfAnchor)));
						}
						if(cep.getTarget() instanceof LifelineEditPart) {
/*8
System.out.println("??? in omw right after LifelineEditPart target after ConnectionEditPart ???");
*/
							IdentityAnchor gmfAnchor = (IdentityAnchor)edge.getTargetAnchor();
							Rectangle figureBounds = targetAnchor.getOwner().getBounds();
							compoundCmd.add(new ICommandProxy(getMoveAnchorCommand(moveDelta.y, figureBounds, gmfAnchor)));
						}
						/* apex improved end */
/* apex replaced
						if(origCFBounds.contains(sourcePoint) && cep.getSource() instanceof LifelineEditPart) {
System.out.println("??? in omw right after LifelineEditPart source after ConnectionEditPart ???");
							IdentityAnchor gmfAnchor = (IdentityAnchor)edge.getSourceAnchor();
							Rectangle figureBounds = sourceAnchor.getOwner().getBounds();
							compoundCmd.add(new ICommandProxy(getMoveAnchorCommand(moveDelta.y, figureBounds, gmfAnchor)));
						}
						if(origCFBounds.contains(targetPoint) && cep.getTarget() instanceof LifelineEditPart) {
System.out.println("??? in omw right after LifelineEditPart target after ConnectionEditPart ???");
							IdentityAnchor gmfAnchor = (IdentityAnchor)edge.getTargetAnchor();
							Rectangle figureBounds = targetAnchor.getOwner().getBounds();
							compoundCmd.add(new ICommandProxy(getMoveAnchorCommand(moveDelta.y, figureBounds, gmfAnchor)));
						}
*/
					}
				}
			}
		}
//		// Root 아래의 모든 EP 나열
//		/*8
//		System.out.println("###### Children List Start #####");
//		SequenceUtil.omwShowChildrenEditPart(abstractGraphicalEditPart.getRoot(), 0);
//		System.out.println("###### Children List End #####");
//		*/
///*8
//System.out.println("###### Translate Start #####");
//Rectangle r1 = new Rectangle(300, 100, 500, 200);
//System.out.println("  before translate(30, 10) : " + r1);
//r1.translate(30, 10);
//System.out.println("   after translate(30, 10) : " + r1);
//System.out.println("###### Translate End #####");
//*/
//		// 넘겨받은 AbstractGraphicalEditPart 보다 아래에 있는 belowList 구성		
//		List belowEditPartList = ApexSequenceUtil.apexGetBelowEditPartList(abstractGraphicalEditPart);
//		
//		if ( belowEditPartList.size() > 0 ) {
//			// 이동할 위치
//			Point moveDelta = request.getMoveDelta();		
//
//			// 넘겨받은 AbstractGraphicalEditPart 의 이동 후 위치
//			int yBeforeMove = ApexSequenceUtil.apexGetAbsolutePosition(abstractGraphicalEditPart, SWT.BOTTOM);
//			int yAfterMove = yBeforeMove + moveDelta.y;
//
//			// 넘겨받은 AbstractGraphicalEditPart 바로 아래의 EditPart 구성
//			/* apex improved start */
//			AbstractGraphicalEditPart beneathEditPart = (AbstractGraphicalEditPart)belowEditPartList.get(0);
//			/* apex improved end*/
//			/* apex replaced 
//			AbstractGraphicalEditPart beneathEditPart = ApexSequenceUtil.apexGetBeneathEditPart(abstractGraphicalEditPart, belowEditPartList);
//			*/
//			
//			int topOfBeneathEditPart = ApexSequenceUtil.apexGetAbsolutePosition(beneathEditPart, SWT.TOP);
//			
//			// beneathEditPart 보다 아래로 내릴 경우			
//			if (yAfterMove >= topOfBeneathEditPart) {
//				int pushDeltaY = topOfBeneathEditPart - yBeforeMove - VERTICAL_MARGIN;
//				
////				while( it.hasNext())		
//				{
////					EditPart ep = (EditPart)it.next();
//					
//					AbstractGraphicalEditPart ep = beneathEditPart;
//					moveDelta = moveDelta.getCopy();
//					moveDelta.setY(moveDelta.y() - pushDeltaY);
///*8
//System.out.println("### in omw below list start ###");
//System.out.println("   " + ep.getClass().getSimpleName());
//System.out.println("### in omw below list end ###");
//*/
//					// handle move of object graphically owned by the lifeline
//					if(ep instanceof ShapeEditPart) {
//						ShapeEditPart sep = (ShapeEditPart)ep;
//						EObject elem = sep.getNotationView().getElement();
//
//						if(elem instanceof InteractionFragment) {
//							IFigure figure = sep.getFigure();
//
//							Rectangle figureBounds = figure.getBounds().getCopy();
//							figure.getParent().translateToAbsolute(figureBounds);
//							
//							EditPart parentEP = sep.getParent();
//
//							if(parentEP instanceof LifelineEditPart) {
//								ChangeBoundsRequest esRequest = new ChangeBoundsRequest(RequestConstants.REQ_MOVE);
//								esRequest.setEditParts(sep);
//								esRequest.setMoveDelta(moveDelta);
//
//								Command moveESCommand = LifelineXYLayoutEditPolicy.getResizeOrMoveChildrenCommand((LifelineEditPart)parentEP, esRequest, true, false, true);
//
//								if(moveESCommand != null && !moveESCommand.canExecute()) {
//									// forbid move if the es can't be moved correctly
//									compoundCmd.add(UnexecutableCommand.INSTANCE);
//								} else if(moveESCommand != null) {
//									compoundCmd.add(moveESCommand);
//								}
//							}
//							/* apex added start */
//							// below에 다른 CombinedFragment 가 있을 경우 같이 이동
//							if(sep instanceof CombinedFragmentEditPart) {
//								// The new bounds will be calculated from the current bounds
//								Rectangle newBounds = sep.getFigure().getBounds().getCopy();
//
//								// Convert to relative
//								newBounds.x += moveDelta.x;
//								newBounds.y += moveDelta.y;
//								
//								SetBoundsCommand setBoundsCmd = new SetBoundsCommand(sep.getEditingDomain(), "Move of a CombinedFragment due to a Upper CF movement", sep, newBounds);
//								compoundCmd.add(new ICommandProxy(setBoundsCmd));
//								
//								/* apex added start */
//								// jiho
//								ChangeBoundsRequest esRequest = new ChangeBoundsRequest(RequestConstants.REQ_MOVE);
//								esRequest.setEditParts(sep);
//								esRequest.setMoveDelta(moveDelta);
//								compoundCmd.add(getCombinedFragmentResizeChildrenCommand(esRequest, sep));
//								/* apex added end */
//							}
//							/* apex added end */
//						}
//					}
//
//					// handle move of messages directly attached to a lifeline
//					// 보통 메세지가 아니라 Lifeline에 직접붙은 메세지. 즉, async 등의 경우 처리
//					// sync의 경우 아래의 로직에 의해 이동되는게 아니라
//					// 딸린 ExecSpec이 위의 로직에 의해 이동되기 때문에 ExecSpec따라 이동
//
//					// 위 4행의 주석은 원래 Papyrus에 해당하고
//					// 수정된 papyrus는 message 가 별도로 움직임
//					
//					if(ep instanceof ConnectionEditPart) {
///*8
//System.out.println("??? in omw right after ConnectionEditPart ???");
//*/
//						ConnectionEditPart cep = (ConnectionEditPart)ep;
//						Connection msgFigure = cep.getConnectionFigure();
//
//						ConnectionAnchor sourceAnchor = msgFigure.getSourceAnchor();
//						ConnectionAnchor targetAnchor = msgFigure.getTargetAnchor();
//
//						Point sourcePoint = sourceAnchor.getReferencePoint();
//						Point targetPoint = targetAnchor.getReferencePoint();
//
//						Edge edge = (Edge)cep.getModel();
///*8
//System.out.println("is Async? " + cep.getClass().getSimpleName());
//System.out.println("Parent of Async : " + cep.getParent().getClass().getSimpleName());
//System.out.println("Source of Async : " + cep.getSource().getClass().getSimpleName());
//System.out.println("Target of Async : " + cep.getTarget().getClass().getSimpleName());
//System.out.println("SourcePoint of Async : " + sourcePoint);
//System.out.println("TargetPoint of Async : " + targetPoint);
//System.out.println("Bounds before translate : " + thisFigure.getBounds().getCopy());
//System.out.println("Bounds after translate : " + origCFBounds);
//System.out.println("is Async Source contained: " + origCFBounds.contains(sourcePoint));
//System.out.println("is Async Target contained: " + origCFBounds.contains(targetPoint));
//*/
//
//						/* apex improved start */
//						if(cep.getSource() instanceof LifelineEditPart) {
///*8
//System.out.println("??? in omw right after LifelineEditPart source after ConnectionEditPart ???");
//*/
//							IdentityAnchor gmfAnchor = (IdentityAnchor)edge.getSourceAnchor();
//							Rectangle figureBounds = sourceAnchor.getOwner().getBounds();
//							compoundCmd.add(new ICommandProxy(getMoveAnchorCommand(moveDelta.y, figureBounds, gmfAnchor)));
//						}
//						if(cep.getTarget() instanceof LifelineEditPart) {
///*8
//System.out.println("??? in omw right after LifelineEditPart target after ConnectionEditPart ???");
//*/
//							IdentityAnchor gmfAnchor = (IdentityAnchor)edge.getTargetAnchor();
//							Rectangle figureBounds = targetAnchor.getOwner().getBounds();
//							compoundCmd.add(new ICommandProxy(getMoveAnchorCommand(moveDelta.y, figureBounds, gmfAnchor)));
//						}
//						/* apex improved end */
///* apex replaced
//						if(origCFBounds.contains(sourcePoint) && cep.getSource() instanceof LifelineEditPart) {
//System.out.println("??? in omw right after LifelineEditPart source after ConnectionEditPart ???");
//							IdentityAnchor gmfAnchor = (IdentityAnchor)edge.getSourceAnchor();
//							Rectangle figureBounds = sourceAnchor.getOwner().getBounds();
//							compoundCmd.add(new ICommandProxy(getMoveAnchorCommand(moveDelta.y, figureBounds, gmfAnchor)));
//						}
//						if(origCFBounds.contains(targetPoint) && cep.getTarget() instanceof LifelineEditPart) {
//System.out.println("??? in omw right after LifelineEditPart target after ConnectionEditPart ???");
//							IdentityAnchor gmfAnchor = (IdentityAnchor)edge.getTargetAnchor();
//							Rectangle figureBounds = targetAnchor.getOwner().getBounds();
//							compoundCmd.add(new ICommandProxy(getMoveAnchorCommand(moveDelta.y, figureBounds, gmfAnchor)));
//						}
//*/
//						
//						/* apex added start */
//						//jiho
//						ChangeBoundsRequest cbRequest = new ChangeBoundsRequest(RequestConstants.REQ_MOVE);
//						cbRequest.setMoveDelta(moveDelta);
////						compoundCmd.add(MessageConnectionLineSegEditPolicy.apexGetResizeOrMoveTargetsCommand(cbRequest, ep));
//						ep = ApexSequenceUtil.apexGetBottomEditPartInLinked(ep);
//						/* apex added end */
//					}
//					
//					/* apex added start */
//					ChangeBoundsRequest newRequest = new ChangeBoundsRequest(RequestConstants.REQ_MOVE);
//					newRequest.setMoveDelta(moveDelta);
//					apexGetResizeOrMoveBelowItemsCommand(newRequest, ep, compoundCmd);
//					/* apex added end */
//				}
//			}
//		}
	}

	
	/**
	 * apex updated
	 * 
	 * Handle the owning of interaction fragments when moving or resizing a CF.
	 * 
	 * @param compoundCmd
	 *        The command
	 * @param moveDelta
	 *        The move delta (given by the request).
	 * @param sizeDelta
	 *        The size delta (given by the request).
	 * @param combinedFragmentEditPart
	 *        The CF edit part.
	 */
	@SuppressWarnings("unchecked")
	public static Command getCombinedFragmentResizeChildrenCommand(ChangeBoundsRequest request, org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart combinedFragmentEditPart) {
		return getCombinedFragmentResizeChildrenCommand(request, combinedFragmentEditPart, 0);
	}
	
	
	/**
	 * apex updated
	 * 
	 * Handle the owning of interaction fragments when moving or resizing a CF.
	 * 
	 * @param compoundCmd
	 *        The command
	 * @param moveDelta
	 *        The move delta (given by the request).
	 * @param sizeDelta
	 *        The size delta (given by the request).
	 * @param combinedFragmentEditPart
	 *        The CF edit part.
	 */
	@SuppressWarnings("unchecked")
	public static Command getCombinedFragmentResizeChildrenCommand(ChangeBoundsRequest request, org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart combinedFragmentEditPart, int depth) {
		
		Point moveDelta = request.getMoveDelta();

		Dimension sizeDelta = request.getSizeDelta();

/*8
System.out.println("InteractionCompartmentXYLayoutEditPolicy.getCombinedFragmentResizeChildrenCommand(), line : "+Thread.currentThread().getStackTrace()[1].getLineNumber());
System.out.println("moveDelta : " + request.getMoveDelta());
System.out.println("sizeDelta : " + request.getSizeDelta());
//*/
		
		IFigure cfFigure = combinedFragmentEditPart.getFigure();
		Rectangle origCFBounds = cfFigure.getBounds().getCopy();
		
		// origCFBounds 를 화면 좌상단을 원점으로 하는 절대좌표값으로 변경
		cfFigure.getParent().translateToAbsolute(origCFBounds);

		// origCFBounds 를 cfFigure.getParent()의 좌상단 절대좌표값만큼 더하여 변경, 즉 parent의 변경만큼 origCFBounds도 변경 
		origCFBounds.translate(cfFigure.getParent().getBounds().getLocation());		


		CompoundCommand compoundCmd = new CompoundCommand();
		// specific case for move :
		// we want the execution specifications graphically owned by the lifeline to move with the combined fragment, and the contained messages too
		if(sizeDelta.equals(0, 0)) {
			
			/* apex added start */
			//this CF의 bound Resize
			Command cmd = apexResizeCombinedFragmentBoundsCommand(request, (CombinedFragmentEditPart)combinedFragmentEditPart, true);
			if ( !cmd.canExecute() ) {
				return UnexecutableCommand.INSTANCE;
			} else {
				compoundCmd.add(cmd);
			}
			/* apex added end */

			// retrieve all the edit parts in the registry
			Set<Entry<Object, EditPart>> allEditPartEntries = combinedFragmentEditPart.getViewer().getEditPartRegistry().entrySet();

			for(Entry<Object, EditPart> epEntry : allEditPartEntries) {
				EditPart ep = epEntry.getValue();
				
				// handle move of object graphically owned by the lifeline
				// ExecSpec은 아래 로직을 따라 이동
				if(ep instanceof ShapeEditPart) {
					ShapeEditPart sep = (ShapeEditPart)ep;
					EObject elem = sep.getNotationView().getElement();

					if(elem instanceof InteractionFragment) {
						IFigure figure = sep.getFigure();

						Rectangle figureBounds = figure.getBounds().getCopy();
						figure.getParent().translateToAbsolute(figureBounds);

						/* apex improved start */
						// sep 가 CFBounds에 포함되거나
						// sep 가 CFBounds에 포함되지 않고 잘리더라도, ExecSpec이면 이동시킴
						if(origCFBounds.contains(figureBounds) 
						   || (origCFBounds.intersects(figureBounds) 
							   && (sep instanceof ActionExecutionSpecificationEditPart || sep instanceof BehaviorExecutionSpecificationEditPart))) {
							EditPart parentEP = sep.getParent();

							if(parentEP instanceof LifelineEditPart) {
								ChangeBoundsRequest esRequest = new ChangeBoundsRequest(RequestConstants.REQ_MOVE);
								esRequest.setEditParts(sep);
								esRequest.setMoveDelta(moveDelta);

								Command moveESCommand = LifelineXYLayoutEditPolicy.getResizeOrMoveChildrenCommand((LifelineEditPart)parentEP, esRequest, true, false, true);

								if(moveESCommand != null && !moveESCommand.canExecute()) {
									// forbid move if the es can't be moved correctly
									return UnexecutableCommand.INSTANCE;
								} else if(moveESCommand != null) {
									compoundCmd.add(moveESCommand);
								}
							}
						}
						/* apex improved end */
						
						/* apex replaced
						if(origCFBounds.contains(figureBounds)) {
							EditPart parentEP = sep.getParent();

							if(parentEP instanceof LifelineEditPart) {
								ChangeBoundsRequest esRequest = new ChangeBoundsRequest(RequestConstants.REQ_MOVE);
								esRequest.setEditParts(sep);
								esRequest.setMoveDelta(moveDelta);

								Command moveESCommand = LifelineXYLayoutEditPolicy.getResizeOrMoveChildrenCommand((LifelineEditPart)parentEP, esRequest, true, false, true);

								if(moveESCommand != null && !moveESCommand.canExecute()) {
									// forbid move if the es can't be moved correctly
									return UnexecutableCommand.INSTANCE;
								} else if(moveESCommand != null) {
									compoundCmd.add(moveESCommand);
								}
							}
						}
						*/
					}
				}

				// handle move of messages directly attached to a lifeline
				// message는 아래 로직을 따라 이동
				// 위 2행은 원래 Papyrus에 대한 주석이고,
				// 수정에 의해 ExecSpec이 없는 Message는 없고,
				// CF의 이동 시 Message는 ExecSpec의 이동에 따라서 움직이므로 아래의 로직 필요없게 됨
/*
				if(ep instanceof ConnectionEditPart) {
					ConnectionEditPart cep = (ConnectionEditPart)ep;

					Connection msgFigure = cep.getConnectionFigure();

					ConnectionAnchor sourceAnchor = msgFigure.getSourceAnchor();
					ConnectionAnchor targetAnchor = msgFigure.getTargetAnchor();

					Point sourcePoint = sourceAnchor.getReferencePoint();
					Point targetPoint = targetAnchor.getReferencePoint();

					Edge edge = (Edge)cep.getModel();
/*8
System.out.println("cep.getSource() : " + cep.getSource());
System.out.println("sourceAnchor.getOwner() : " + sourceAnchor.getOwner());
System.out.println("origCFBounds : " + origCFBounds);
System.out.println("sourcePoint  : " + sourcePoint);
System.out.println("origCFBounds.contains(sourcePoint)  : " + origCFBounds.contains(sourcePoint));
System.out.println("cep.getSource() instanceof LifelineEditPart  : " + (cep.getSource() instanceof LifelineEditPart));
//
					
					if(origCFBounds.contains(sourcePoint) && cep.getSource() instanceof LifelineEditPart) {
						IdentityAnchor gmfAnchor = (IdentityAnchor)edge.getSourceAnchor();
						Rectangle figureBounds = sourceAnchor.getOwner().getBounds();
//						compoundCmd.add(new ICommandProxy(getMoveAnchorCommand(moveDelta.y, figureBounds, gmfAnchor)));
					}
/*8
System.out.println("cep.getTarget() : " + cep.getTarget());
System.out.println("targetAnchor.getOwner() : " + targetAnchor.getOwner());
System.out.println("origCFBounds : " + origCFBounds);					
System.out.println("targetPoint  : " + targetPoint);
System.out.println("origCFBounds.contains(targetPoint)  : " + origCFBounds.contains(targetPoint));
System.out.println("cep.getTarget() instanceof LifelineEditPart  : " + (cep.getTarget() instanceof LifelineEditPart));
//

					if(origCFBounds.contains(targetPoint) && cep.getTarget() instanceof LifelineEditPart) {
						IdentityAnchor gmfAnchor = (IdentityAnchor)edge.getTargetAnchor();
						Rectangle figureBounds = targetAnchor.getOwner().getBounds();
//						compoundCmd.add(new ICommandProxy(getMoveAnchorCommand(moveDelta.y, figureBounds, gmfAnchor)));
					}

					/* apex added start 
					// target ExecSpec이 잘려있는 경우 위의 438 line 이하 로직에 의해 이동하지 않으므로
					// Target 이 ExecSpec 이고
					// targetPoint가 CF에 포함되기만 하면(즉, ExecSpec 전체의 포함/잘림 여부와 관계없이) target Anchor 이동하도록 처리
					if ( cep.getTarget() instanceof BehaviorExecutionSpecificationEditPart || 
						 cep.getTarget() instanceof ActionExecutionSpecificationEditPart ) {
						ShapeNodeEditPart snep = (ShapeNodeEditPart)cep.getTarget();
						IFigure execSpecFigure = snep.getFigure();
						Rectangle execSpecBounds = snep.getFigure().getBounds().getCopy();
						execSpecFigure.translateToAbsolute(execSpecBounds);

						// ExecSpec이 CF에 포함되지 않고 잘려있는 경우에만 anchor 이동
						if(!origCFBounds.contains(execSpecBounds) &&
							origCFBounds.intersects(execSpecBounds)) {
							IdentityAnchor gmfAnchor = (IdentityAnchor)edge.getTargetAnchor();
							Rectangle figureBounds = targetAnchor.getOwner().getBounds();
//							compoundCmd.add(new ICommandProxy(getMoveAnchorCommand(moveDelta.y, figureBounds, gmfAnchor)));
						}
					}
					/* apex added end 

				}
*/
			}
			
			/* apex added start */			
			if ( moveDelta.y > 0 ) { // 아래로 이동 시
					
				// belowEditPart, beneathEditPart를 구성하여 beneathEditPart보다 아래로 이동하는 경우 belowEditPart 모두 이동
				apexGetResizeOrMoveBelowItemsCommand(request, combinedFragmentEditPart, compoundCmd);
//				CompoundCommand belowProcessedCmd = (CompoundCommand)apexGetResizeOrMoveBelowItemsCommand(request, combinedFragmentEditPart, compoundCmd);
/*
				// 반환 받은 CompoundCmd를 분해하여 compoundCmd에 추가
				List belowProcessedCmdList = belowProcessedCmd.getCommands();
				Iterator it = belowProcessedCmdList.iterator();
				while(it.hasNext()) {
					Command moveCommand = (Command)it.next();
					if(moveCommand != null && !moveCommand.canExecute()) {
						// forbid move if the es can't be moved correctly
						return UnexecutableCommand.INSTANCE;
					} else if(moveCommand != null) {
						compoundCmd.add(moveCommand);
					}
				}
*/
				// 중첩된 child CF의 이동 결과 Parent CF의 경계를 넘는 경우 Parent Resize 처리
				apexResizeParentCombinedFragments(request, combinedFragmentEditPart, compoundCmd, depth);

			} else if ( moveDelta.y < 0 ) { // 위로 이동할 경우
				
				List higherEditPartList = ApexSequenceUtil.apexGetHigherEditPartList(combinedFragmentEditPart);
				
				if ( higherEditPartList.size() > 0 ) {
				
					
					int yAfterMove = ApexSequenceUtil.apexGetAbsolutePosition(combinedFragmentEditPart, SWT.TOP)+moveDelta.y;
					IGraphicalEditPart aboveEditPart  = ApexSequenceUtil.apexGetAboveEditPart(combinedFragmentEditPart, higherEditPartList);
					int yAbove = ApexSequenceUtil.apexGetAbsolutePosition(aboveEditPart, SWT.BOTTOM);
					// aboveEditPart보다 위로 올릴 경우
					
					if ( yAfterMove < yAbove ) {
						return UnexecutableCommand.INSTANCE;
					}
					
				}

			}
			/* apex added end */
			
// 이동 끝
		} else {
// Resize 시작
			// calculate the new CF bounds
			Rectangle newBoundsCF = origCFBounds.getCopy();
			
			newBoundsCF.translate(moveDelta);

			//this CF의 bound Resize
			Command cmd = apexResizeCombinedFragmentBoundsCommand(request, (CombinedFragmentEditPart)combinedFragmentEditPart, false);
			if ( !cmd.canExecute() ) {
				return UnexecutableCommand.INSTANCE;
			} else {
				compoundCmd.add(cmd);
			}
				
/*8
System.out.println("in getCombinedFragmentResizeChildrenCommand, before Resize       : "+newBoundsCF);
combinedFragmentEditPart.getFigure().getParent().translateToAbsolute(newBoundsCF);
System.out.println("in getCombinedFragmentResizeChildrenCommand, before Resize in abs: "+newBoundsCF);
//*/
			
//			newBoundsCF.resize(sizeDelta);
			
/*8
TransactionalEditingDomain editingDomain = combinedFragmentEditPart.getEditingDomain();
ICommand resizeCommand = new SetBoundsCommand(editingDomain, 
		"Apex_CF_Resize",
		new EObjectAdapter((View) combinedFragmentEditPart.getModel()), new Dimension(1500, 300));
compoundCmd.add(new ICommandProxy(resizeCommand));
*/
/*8
System.out.println("in getCombinedFragmentResizeChildrenCommand, after  Resize       : "+newBoundsCF);
//*/
			
			CombinedFragment cf = (CombinedFragment)((CombinedFragmentEditPart)combinedFragmentEditPart).resolveSemanticElement();
			
			// 아래 기존 로직은 CF의 child인 InteractionOperand resize 처리
			if(combinedFragmentEditPart.getChildren().size() > 0 && combinedFragmentEditPart.getChildren().get(0) instanceof CombinedFragmentCombinedFragmentCompartmentEditPart) {

				CombinedFragmentCombinedFragmentCompartmentEditPart compartment = (CombinedFragmentCombinedFragmentCompartmentEditPart)combinedFragmentEditPart.getChildren().get(0);
				List<EditPart> combinedFragmentChildrenEditParts = compartment.getChildren();

				List<InteractionOperandEditPart> interactionOperandEditParts = new ArrayList<InteractionOperandEditPart>();

				InteractionOperand firstOperand = cf.getOperands().get(0);

				// interaction fragments which will not be covered by the operands
				Set<InteractionFragment> notCoveredAnymoreInteractionFragments = new HashSet<InteractionFragment>();
				int headerHeight = 0;

				// InteractionOperands 에 대해
				for(EditPart ep : combinedFragmentChildrenEditParts) {
					if(ep instanceof InteractionOperandEditPart) {
						InteractionOperandEditPart ioEP = (InteractionOperandEditPart)ep;
						InteractionOperand io = (InteractionOperand)ioEP.resolveSemanticElement();

						// 이 InteractionOperand가 넘겨받은 CF의 Operands에 포함되는 경우
						if(cf.getOperands().contains(io)) {
							// interactionOperandEditParts에 이 InteractonOperandEditPart를 add하고
							interactionOperandEditParts.add(ioEP);
							
							// 이 Operand의 모든 Fragments를 notCovered List에 추가
							// fill with all current fragments (filter later)
							notCoveredAnymoreInteractionFragments.addAll(io.getFragments());

							// 이 Operand가 첫번째 Operand이면
							if(firstOperand.equals(io)) {
								
								Rectangle boundsIO = ioEP.getFigure().getBounds().getCopy();
								
								// 이 Operand의 좌표를 절대좌표로 변환
								ioEP.getFigure().getParent().translateToAbsolute(boundsIO);
								
								// 넘겨받은 CF와 이 Operand의 y차이만큼이 header Height
								headerHeight = boundsIO.y - origCFBounds.y;
							}
						}
					}
				}

				double heightRatio = (double)(newBoundsCF.height - headerHeight) / (double)(origCFBounds.height - headerHeight);
				double widthRatio = (double)newBoundsCF.width / (double)origCFBounds.width;

				for(InteractionOperandEditPart ioEP : interactionOperandEditParts) {
					InteractionOperand io = (InteractionOperand)ioEP.resolveSemanticElement();

					// 이 IO의 절대좌표
					Rectangle newBoundsIO = SequenceUtil.getAbsoluteBounds(ioEP);

					// moveDelta만큼 이동
					// apply the move delta which will impact all operands
					newBoundsIO.translate(moveDelta);

					// 실제 경계 변경 처리
					// calculate the new bounds of the interaction operand
					// scale according to the ratio
/*8
System.out.println("This CFEP : " + combinedFragmentEditPart);
System.out.println("This IOEP : " + ioEP);
System.out.println("  UnResized Bounds : " + newBoundsIO);
*/
					newBoundsIO.height = (int)(newBoundsIO.height * heightRatio);
					newBoundsIO.width = (int)(newBoundsIO.width * widthRatio);
/*8
System.out.println("  Resized Bounds   : " + newBoundsIO);
*/
					// 첫번째 Operand의 경우 Header영역도 Operand에 포함(io의 y값은 감소시키고, 그만큼 height는 확장)
					if(firstOperand.equals(io)) {
						// used to compensate the height of the "header" where the OperandKind is stored
						newBoundsIO.y -= headerHeight;
						newBoundsIO.height += headerHeight;
					}

					// 넘겨받은 CF와 그 Operands를 ignoreSet에 추가(새 경계에 새로 포함되는 fragment만 나중에 추출하기 위해 기존 Fragment는 ignoreSet에 추가)
					// ignore current CF and enclosed IO
					Set<InteractionFragment> ignoreSet = new HashSet<InteractionFragment>();
					ignoreSet.add(cf);
					ignoreSet.addAll(cf.getOperands());

					// 새 경계에 포함되는 Fragments 추출
					Set<InteractionFragment> coveredInteractionFragments = SequenceUtil.getCoveredInteractionFragments(newBoundsIO, combinedFragmentEditPart, ignoreSet);
/*8
System.out.println("coveredInteractionFragments : " + coveredInteractionFragments);
//*/
					// 새 경계에 잘리는 Fragments가 있을 경우 null
					if(coveredInteractionFragments == null) {
						return UnexecutableCommand.INSTANCE;
					}

					// notCovered에서 새 경계에 포함되는 Fragments는 제외, 즉 기존 경계에 포함되었던 Fragment를 notCovered에서 제외, 즉 covered로 처리
					// remove fragments that are covered by this operand from the notCovered set
					notCoveredAnymoreInteractionFragments.removeAll(coveredInteractionFragments);

					// 새 경계에 포함되는 Fragments의 EnclosingInteraction으로 io를 setting
					// set the enclosing operand to the moved/resized one if the current enclosing interaction is the enclosing interaction
					// of the moved/resized operand or of another.
					// => the interaction fragment that are inside an other container (like an enclosed CF) are not modified
					for(InteractionFragment ift : coveredInteractionFragments) {
						if(!cf.equals(ift)) {
							Interaction interactionOwner = ift.getEnclosingInteraction();
							InteractionOperand ioOwner = ift.getEnclosingOperand();

							// 포함하는 op가 null이 아니고 (포함하는 op가 cf를 포함하는 op와 같거나-이럴경우가 있나? cf가 포함하는 op의 owner인 경우)
							// 또는
							// 포함하는 interaction이 null이 아니고 (포함하는 interaction이 cf를 포함하는 interaction과 같거나 cf가 포함하는 interaction의 owner인 경우-이럴경우가 있나?)
							if((ioOwner != null && (ioOwner.equals(cf.getEnclosingOperand()) || cf.equals(ioOwner.getOwner())))
									|| (interactionOwner != null && (interactionOwner.equals(cf.getEnclosingInteraction()) || cf.equals(interactionOwner.getOwner())))
							  ) {
								// io를 ift를 포함하는 interaction으로 set해주는 command를 compoundCmd에 추가
								compoundCmd.add(new ICommandProxy(SequenceUtil.getSetEnclosingInteractionCommand(ioEP.getEditingDomain(), ift, io)));
							}
						}
					}
				}

				// notCovered에 포함된 ift는 원래 포함하는 operand나 interaction을 EnclosingInteraction으로 setting
				for(InteractionFragment ift : notCoveredAnymoreInteractionFragments) {
					if(cf.getEnclosingOperand() != null) {
						compoundCmd.add(new ICommandProxy(SequenceUtil.getSetEnclosingInteractionCommand(combinedFragmentEditPart.getEditingDomain(), ift, cf.getEnclosingOperand())));
					} else {
						compoundCmd.add(new ICommandProxy(SequenceUtil.getSetEnclosingInteractionCommand(combinedFragmentEditPart.getEditingDomain(), ift, cf.getEnclosingInteraction())));
					}
				}
			}
			
			/* apex added start */
			// 여기에 중첩된 경우 Resize 처리
			apexResizeParentCombinedFragments(request, combinedFragmentEditPart, compoundCmd, depth);			
			/* apex added end */
// Resize 끝
		}

		// Print a user notification when we are not sure the command is appropriated
		EObject combinedFragment = combinedFragmentEditPart.resolveSemanticElement();
		if(combinedFragment instanceof CombinedFragment && !sizeDelta.equals(0, 0)) {
			if(((CombinedFragment)combinedFragment).getOperands().size() > 1) {
				// append a command which notifies
				Command notifyCmd = new Command() {

					@Override
					public void execute() {
						NotificationBuilder warning = NotificationBuilder.createAsyncPopup(Messages.Warning_ResizeInteractionOperandTitle, NLS.bind(Messages.Warning_ResizeInteractionOperandTxt, System.getProperty("line.separator")));
						warning.run();
					}

					@Override
					public void undo() {
						execute();
					}
				};
				if(notifyCmd.canExecute()) {
					compoundCmd.add(notifyCmd);
				}
			}
		}
		// return null instead of unexecutable empty compound command
		if(compoundCmd.isEmpty()) {
			return null;
		}
		return compoundCmd;
	}
	
	/**
	 * CF bound의 resize 처리
	 * Child CF의 right, bottom 보다 작게 Resize 방지
	 * 
	 * @param combinedFragmentEditPart
	 * @param isResizeByChildMove       Child CF의 Move에 의한 Resize인지 구별
	 * @return
	 */
	public static Command apexResizeCombinedFragmentBoundsCommand(ChangeBoundsRequest request, CombinedFragmentEditPart combinedFragmentEditPart, boolean isResizeByChildMove) {
				
		// request 에서 size 뽑아서 처리
		IFigure cfFigure = combinedFragmentEditPart.getFigure();
		Rectangle origCFBounds = cfFigure.getBounds().getCopy();
		combinedFragmentEditPart.getFigure().translateToAbsolute(origCFBounds);
		
		Point moveDelta = request.getMoveDelta();
		
		Dimension sizeDelta = isResizeByChildMove ? new Dimension(0, moveDelta.y) : request.getSizeDelta();
/*8		
System.out.println("InteractionCompartmentXYLayoutEditPolicy.apexResizeCombinedFragmentBoundCommand(), line : "+Thread.currentThread().getStackTrace()[1].getLineNumber());
System.out.println("before translate, parent-origCFBounds : " + origCFBounds);
System.out.println("before resize,    parent-origCFBounds : " + origCFBounds);
//*/
		origCFBounds.translate(moveDelta);
		origCFBounds.resize(sizeDelta);
/*8
System.out.println("after  translate, parent-origCFBounds : " + origCFBounds);
System.out.println("after  resize,    parent-origCFBounds : " + origCFBounds);
//*/
		
		// childCombinedFragment가 있고, child의 right 보다 작게 resize 안되게
		List children = ApexSequenceUtil.apexGetChildEditPartList(combinedFragmentEditPart);

		Iterator it1 = children.iterator();
		
		while ( it1.hasNext()) {
			EditPart childEp = (EditPart)it1.next();
			if ( childEp instanceof CombinedFragmentEditPart ) {
				CombinedFragmentEditPart cfep = (CombinedFragmentEditPart)childEp;

				Rectangle childRect = cfep.getFigure().getBounds().getCopy();
				cfep.getFigure().translateToAbsolute(childRect);
				
/*8
System.out.println("parent-origCFBounds.right() : " + origCFBounds.right());
System.out.println("childRect.right()           : " + childRect.right());
//*/
				// child.right보다 작으면 X
				if ( origCFBounds.right() <= childRect.right() ) {					
					return UnexecutableCommand.INSTANCE;
				}
				// child.bottom보다 작으면 X
				if ( origCFBounds.bottom() <= childRect.bottom() ) {					
					return UnexecutableCommand.INSTANCE;
				}
			}			
		}
		
		// CF 경계 변경 실제 처리 부분
		TransactionalEditingDomain editingDomain = combinedFragmentEditPart.getEditingDomain();
		ICommand resizeCommand = new SetBoundsCommand(editingDomain, 
			                                          "Apex_CF_Resize",
			                                          new EObjectAdapter((View) combinedFragmentEditPart.getModel()),
			                                          new Dimension(origCFBounds.width, origCFBounds.height));		
		return new ICommandProxy(resizeCommand);
	}
	
	/**
	 * 넘겨받은 GraphicalEditPart가 중첩되어 있는 child 인 경우 
	 * @param request
	 * @param combinedFragmentEditPart
	 * @param ccmd
	 * @param depth
	 * @return
	 */
	public static Command apexResizeParentCombinedFragments(ChangeBoundsRequest request, org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart combinedFragmentEditPart, CompoundCommand ccmd, int depth) {

		// parent Operand(또는 InteractionInteractionCompartmentEditPart)가 있고, 즉 중첩되어 있고
		EditPart ep = combinedFragmentEditPart.getParent();
		if ( ep instanceof InteractionOperandEditPart || ep instanceof InteractionInteractionCompartmentEditPart ) {
			
			Point moveDelta = request.getMoveDelta();
			Dimension sizeDelta = request.getSizeDelta();
			
			IFigure cfFigure = combinedFragmentEditPart.getFigure();
			Rectangle origCFBounds = cfFigure.getBounds().getCopy();

			// origCFBounds 를 화면 좌상단을 원점으로 하는 절대좌표값으로 변경
			cfFigure.getParent().translateToAbsolute(origCFBounds);

			// origCFBounds 를 cfFigure.getParent()의 좌상단 절대좌표값만큼 더하여 변경, 즉 parent의 변경만큼 origCFBounds도 변경 
			origCFBounds.translate(cfFigure.getParent().getBounds().getLocation());	

			// Resize된 CF의 새 Bounds
			Rectangle newBoundsCF = origCFBounds.getCopy();
			newBoundsCF.translate(moveDelta);
			newBoundsCF.resize(sizeDelta);
			
			AbstractGraphicalEditPart parentEditPart = (AbstractGraphicalEditPart)ep.getParent().getParent();
			
			// Resize결과 parentOperand보다 크면 parentCF도 Resize 처리
			if ( ep instanceof InteractionOperandEditPart ) {
				InteractionOperandEditPart ioep = (InteractionOperandEditPart)ep;
				Rectangle parentOperandBounds = ioep.getFigure().getBounds().getCopy();
				parentEditPart.getFigure().translateToAbsolute(parentOperandBounds);
/*8				
				System.out.println("---------------------------------");		
				System.out.println("depth                        : " + depth);
				System.out.println("this EP                      : " + combinedFragmentEditPart);
				System.out.println("origCFBounds                 : " + origCFBounds + ", right = " + origCFBounds.right() + ", bottom = " + origCFBounds.bottom());
				System.out.println("sizeDelta.width              : " + sizeDelta.width);
				System.out.println("resizedCFBounds              : " + newBoundsCF + ", right = " + newBoundsCF.right() + ", bottom = " + newBoundsCF.bottom());
				System.out.println("parentOperandBounds          : " + parentOperandBounds + ", right = " + parentOperandBounds.right() + ", bottom = " + parentOperandBounds.bottom());
				System.out.println("parent EP                    : " + parentEditPart);
				System.out.println("parent IO                    : " + (InteractionOperandEditPart)ep);
*/
				if ( newBoundsCF.right() > parentOperandBounds.right() ||
					     newBoundsCF.bottom() > parentOperandBounds.bottom() ) {
/*8					
System.out.println("newBounds is bigger than parentOperand");
//*/
					apexGetCombinedFragmentResizeChildrenCommand(request, (CombinedFragmentEditPart)parentEditPart, ccmd, depth);
				} else {
					return ccmd;
				}
			} else if (ep instanceof InteractionInteractionCompartmentEditPart) { // 최상위 CF의 경우
				//Resize 계통 method가 CF가 아닌 org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart로 동작하도록 개조
				InteractionInteractionCompartmentEditPart iicep = (InteractionInteractionCompartmentEditPart)ep;
				Rectangle parentIicEPBounds = iicep.getFigure().getBounds().getCopy();
				parentEditPart.getFigure().translateToAbsolute(parentIicEPBounds);
/*8
				System.out.println("---------------------------------");		
				System.out.println("depth                        : " + depth);
				System.out.println("this EP                      : " + combinedFragmentEditPart);
				System.out.println("origCFBounds                 : " + origCFBounds + ", right = " + origCFBounds.right() + ", bottom = " + origCFBounds.bottom());
				System.out.println("sizeDelta.width              : " + sizeDelta.width);
				System.out.println("resizedCFBounds              : " + newBoundsCF + ", right = " + newBoundsCF.right() + ", bottom = " + newBoundsCF.bottom());
				System.out.println("parentIICBounds              : " + parentIicEPBounds + ", right = " + parentIicEPBounds.right() + ", bottom = " + parentIicEPBounds.bottom());
				System.out.println("parent EP                    : " + parentEditPart);
				System.out.println("parent IIC                   : " + (InteractionInteractionCompartmentEditPart)ep);
*/
				if ( newBoundsCF.right() > parentIicEPBounds.right() ||
					     newBoundsCF.bottom() > parentIicEPBounds.bottom() ) {
/*8
System.out.println("newBounds is bigger than parentIIC");
*/
//					apexGetCombinedFragmentResizeChildrenCommand(request, (PackageEditPart)parentEditPart, ccmd, depth);
				} else {
					return ccmd;
				}
			}
				


			/*
			for (CombinedFragmentEditPart aParentCfep : parentCfEditParts) {

				InteractionOperandEditPart ioep = (InteractionOperandEditPart)ep;
				Rectangle parentOperandBounds = ioep.getFigure().getBounds().getCopy();
				aParentCfep.getFigure().translateToAbsolute(parentOperandBounds);

System.out.println("---------------------------------");		
System.out.println("depth                        : " + depth);
System.out.println("this CF                      : " + combinedFragmentEditPart);
System.out.println("origCFBounds                 : " + origCFBounds + ", right = " + origCFBounds.right() + ", bottom = " + origCFBounds.bottom());
System.out.println("sizeDelta.width              : " + sizeDelta.width);
System.out.println("resizedCFBounds              : " + newBoundsCF);
System.out.println("parentOperandBounds          : " + parentOperandBounds + ", right = " + parentOperandBounds.right() + ", bottom = " + parentOperandBounds.bottom());
System.out.println("newBounds.right()            : " + newBoundsCF.right());
System.out.println("parentOperandBounds.right()  : " + parentOperandBounds.right());
System.out.println("newBounds.bottom()           : " + newBoundsCF.bottom());
System.out.println("parentOperandBounds.bottom() : " + parentOperandBounds.bottom());
System.out.println("parent CF                    : " + aParentCfep);
				
			
				// Resize결과 parentOperand보다 크면 parentCF도 Resize 처리
				if ( newBoundsCF.right() > parentOperandBounds.right() ||
				     newBoundsCF.bottom() > parentOperandBounds.bottom() ) {
System.out.println("newBounds is bigger than parentOperand");
					apexGetCombinedFragmentResizeChildrenCommand(request, aParentCfep, ccmd, depth);
				} else {
					return ccmd;
				}
			}		
			*/	
		} 
		return ccmd;
	}
	
	/**
	 * apex updated
	 * 
	 * 중첩CF처리 재귀호출을 위한 메서드
	 * 기존 getCombinedFragmentResizeChildrenCommand(ChangeBoundsRequest, CombinedFragmentEditPart)를 호출하고
	 * 그 결과 CompoundCommand를 분해하여
	 * 파라미터로 받은 ccmd에 add 하고
	 * ccmd 를 리턴
	 * 
	 * @param request
	 * @param combinedFragmentEditPart
	 * @param ccmd
	 * @return
	 */
	public static Command apexGetCombinedFragmentResizeChildrenCommand(ChangeBoundsRequest request, CombinedFragmentEditPart combinedFragmentEditPart, CompoundCommand ccmd, int depth) {
/*8
System.out.println("*****************************");
System.out.println("InteractionCompartmentXYLayoutEditPolicy.apexGetCombinedFragmentResizeChildrenCommand(), line : "+Thread.currentThread().getStackTrace()[1].getLineNumber());
System.out.println("재귀호출 - " + depth);
System.out.println("request.getType() " + request.getType());
//*/
		
		Command cpCmd = getCombinedFragmentResizeChildrenCommand(request, combinedFragmentEditPart, ++depth);
		
		// cpCmd를 분해하여 넘겨받은 원래의 ccmd 에 add
		if ( cpCmd.equals(UnexecutableCommand.INSTANCE)) {
			return UnexecutableCommand.INSTANCE;
		} else if ( cpCmd instanceof CompoundCommand ) {			
			List cpCmdList = ((CompoundCommand)cpCmd).getCommands();
			Iterator itCpCmd = cpCmdList.iterator();
			while ( itCpCmd.hasNext() ) {
				Command aResizeCommand = (Command)itCpCmd.next();
				if ( aResizeCommand != null && !aResizeCommand.canExecute()) {
					return UnexecutableCommand.INSTANCE;
				} else if ( aResizeCommand != null ) {
					ccmd.add(aResizeCommand);
				}
			}	
		} else {

System.out.println("return of getCFResizeChildrenCommand is neither UnexecutableCommand nor CompoundCommand : " + cpCmd);
			return UnexecutableCommand.INSTANCE;

		}
		
		return ccmd;
	}
	
	
	
	
	
	
	private static ICommand getMoveAnchorCommand(int yDelta, Rectangle figureBounds, IdentityAnchor gmfAnchor) {
		String oldTerminal = gmfAnchor.getId();
		PrecisionPoint pp = BaseSlidableAnchor.parseTerminalString(oldTerminal);

		int yPos = (int)Math.round(figureBounds.height * pp.preciseY);
		yPos += yDelta;

		pp.preciseY = (double)yPos / figureBounds.height;

		if(pp.preciseY > 1.0) {
			pp.preciseY = 1.0;
		} else if(pp.preciseY < 0.0) {
			pp.preciseY = 0.0;
		}

		String newTerminal = (new BaseSlidableAnchor(null, pp)).getTerminal();

		return new SetValueCommand(new SetRequest(gmfAnchor, NotationPackage.Literals.IDENTITY_ANCHOR__ID, newTerminal));
	}

	/**
	 * Change constraint for comportment by return null if the resize is lower than the minimun
	 * size.
	 */
	@Override
	protected Object getConstraintFor(ChangeBoundsRequest request, GraphicalEditPart child) {
		Rectangle rect = new PrecisionRectangle(child.getFigure().getBounds());
		child.getFigure().translateToAbsolute(rect);
		rect = request.getTransformedRectangle(rect);
		child.getFigure().translateToRelative(rect);
		rect.translate(getLayoutOrigin().getNegated());

		if(request.getSizeDelta().width == 0 && request.getSizeDelta().height == 0) {
			Rectangle cons = getCurrentConstraintFor(child);
			if(cons != null) {
				rect.setSize(cons.width, cons.height);
			}
		} else { // resize
			Dimension minSize = getMinimumSizeFor(child);
			if(rect.width < minSize.width) {
				return null;
			}
			if(rect.height < minSize.height) {
				return null;
			}
		}
		rect = (Rectangle)getConstraintFor(rect);

		Rectangle cons = getCurrentConstraintFor(child);
		if(request.getSizeDelta().width == 0) {
			rect.width = cons.width;
		}
		if(request.getSizeDelta().height == 0) {
			rect.height = cons.height;
		}

		return rect;
	}

	/**
	 * Handle mininum size for lifeline
	 */
	@Override
	protected Dimension getMinimumSizeFor(GraphicalEditPart child) {
		Dimension minimunSize;
		if(child instanceof LifelineEditPart) {
			minimunSize = getMinimumSizeFor((LifelineEditPart)child);
		} else {
			minimunSize = super.getMinimumSizeFor(child);
		}
		return minimunSize;
	}

	/**
	 * Get minimun for a lifeline
	 * 
	 * @param child
	 *        The lifeline
	 * @return The minimun size
	 */
	private Dimension getMinimumSizeFor(LifelineEditPart child) {
		LifelineEditPart lifelineEditPart = child;
		Dimension minimunSize = lifelineEditPart.getFigure().getMinimumSize();
		for(LifelineEditPart lifelineEP : lifelineEditPart.getInnerConnectableElementList()) {
			minimunSize.union(getMinimumSizeFor(lifelineEP));
		}
		for(ShapeNodeEditPart executionSpecificationEP : lifelineEditPart.getChildShapeNodeEditPart()) {
			int minimunHeight = executionSpecificationEP.getFigure().getBounds().bottom();
			minimunSize.setSize(new Dimension(minimunSize.width, Math.max(minimunSize.height, minimunHeight)));
		}
		return minimunSize;
	}

	/**
	 * Block adding element by movement on Interaction
	 */
	@Override
	public Command getAddCommand(Request request) {
		if(request instanceof ChangeBoundsRequest) {
			return UnexecutableCommand.INSTANCE;
		}

		return super.getAddCommand(request);
	}


	/**
	 * Overrides to change the policy of connection anchors when resizing the lifeline.
	 * When resizing the lifeline, the connection must not move.
	 * 
	 * @see org.eclipse.gmf.runtime.diagram.ui.editpolicies.XYLayoutEditPolicy#getCommand(org.eclipse.gef.Request)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Command getCommand(Request request) {
		if(request instanceof ChangeBoundsRequest) {
			ChangeBoundsRequest cbr = (ChangeBoundsRequest)request;

			int resizeDirection = cbr.getResizeDirection();

			CompoundCommand compoundCmd = new CompoundCommand("Resize of Interaction Compartment Elements");

			for(EditPart ep : (List<EditPart>)cbr.getEditParts()) {
				if(ep instanceof LifelineEditPart) {
					// Lifeline EditPart
					LifelineEditPart lifelineEP = (LifelineEditPart)ep;

					int preserveY = PreserveAnchorsPositionCommand.PRESERVE_Y;
					Dimension newSizeDelta = PreserveAnchorsPositionCommand.getSizeDeltaToFitAnchors(lifelineEP, cbr.getSizeDelta(), preserveY);

					// SetBounds command modifying the sizeDelta
					compoundCmd.add(getSetBoundsCommand(lifelineEP, cbr, newSizeDelta));

					// PreserveAnchors command
					compoundCmd.add(new ICommandProxy(new PreserveAnchorsPositionCommand(lifelineEP, newSizeDelta, preserveY, lifelineEP.getPrimaryShape().getFigureLifelineDotLineFigure(), resizeDirection)));
				}
			}

			if(compoundCmd.size() == 0) {
				return super.getCommand(request);
			} else {
				return compoundCmd;
			}
		}

		return super.getCommand(request);
	}

	/**
	 * It obtains an appropriate SetBoundsCommand for a LifelineEditPart. The
	 * newSizeDelta provided should be equal o less than the one contained in
	 * the request. The goal of this newDelta is to preserve the anchors'
	 * positions after the resize. It is recommended to obtain this newSizeDelta
	 * by means of calling
	 * PreserveAnchorsPositionCommand.getSizeDeltaToFitAnchors() operation
	 * 
	 * @param lifelineEP
	 *        The Lifeline that will be moved or resized
	 * @param cbr
	 *        The ChangeBoundsRequest for moving or resized the lifelineEP
	 * @param newSizeDelta
	 *        The sizeDelta to used instead of the one contained in the
	 *        request
	 * @return The SetBoundsCommand
	 */
	@SuppressWarnings("rawtypes")
	protected Command getSetBoundsCommand(LifelineEditPart lifelineEP, ChangeBoundsRequest cbr, Dimension newSizeDelta) {
		// Modify request
		List epList = cbr.getEditParts();
		Dimension oldSizeDelta = cbr.getSizeDelta();
		cbr.setEditParts(lifelineEP);
		cbr.setSizeDelta(newSizeDelta);

		// Obtain the command with the modified request
		Command cmd = super.getCommand(cbr);

		// Restore the request
		cbr.setEditParts(epList);
		cbr.setSizeDelta(oldSizeDelta);

		// Return the SetBoundsCommand only for the Lifeline and with the
		// sizeDelta modified in order to preserve the links' anchors positions
		return cmd;
	}


	/**
	 * apex updated
	 * 
	 * Lifeline 생성 시 상단 고정 정렬
	 */
	@Override
	protected Object getConstraintFor(CreateRequest request) {
/* apex added start*/
		CreateViewRequest req = (CreateViewRequest)request;
		Rectangle BOUNDS = (Rectangle)super.getConstraintFor(request);
		Iterator<? extends ViewDescriptor> iter = req.getViewDescriptors().iterator();
		while (iter.hasNext()) {
			ViewDescriptor descriptor = iter.next();
			IElementType elementType = (IElementType)descriptor.getElementAdapter().getAdapter(IElementType.class);
			if (UMLElementTypes.Lifeline_3001.equals(elementType)) {
				BOUNDS.y = 0;
			}
		}
		return BOUNDS;
/* apex added end */
	}


}
