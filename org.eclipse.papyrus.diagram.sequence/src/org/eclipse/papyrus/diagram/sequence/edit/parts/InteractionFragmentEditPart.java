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
package org.eclipse.papyrus.diagram.sequence.edit.parts;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ShapeNodeEditPart;
import org.eclipse.gmf.runtime.notation.Bounds;
import org.eclipse.gmf.runtime.notation.Shape;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.diagram.sequence.util.ApexSequenceUtil;
import org.eclipse.papyrus.diagram.sequence.util.CommandHelper;
import org.eclipse.papyrus.diagram.sequence.util.SequenceUtil;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.InteractionOperand;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.UMLPackage;

public abstract class InteractionFragmentEditPart extends ShapeNodeEditPart {

	public InteractionFragmentEditPart(View view) {
		super(view);
	}

	/**
	 * Resize the InteractionFragmentFigure when the covered lifelines are selected in the
	 * properties view.
	 */
	public void resizeInteractionFragmentFigure() {
		Object obj = getModel();
		if(obj instanceof Shape) {
			// we get the element linked to this editpart
			EObject element = ((Shape)obj).getElement();

			if(element instanceof InteractionFragment) {
				// we get the list of the covered lifelinnes by the InteractionUse
				List<Lifeline> lifelineCoveredList = ((InteractionFragment)element).getCovereds();

				if(lifelineCoveredList != null && getParent() != null) {
					// we get the interactionCompartimentEditPart to have access to all lifelines
					// EditParts
					List<EditPart> childrenEditPart = getParent().getChildren();
					if(childrenEditPart != null) {
						// The max value guarantee that the first figure will set the minX value
						int minX = Integer.MAX_VALUE;
						int maxX = -Integer.MAX_VALUE;
						int maxR = -Integer.MAX_VALUE;

						for(EditPart childEditPart : childrenEditPart) {
							// we check all the EditParts to select only the lifelineEditParts
							if(childEditPart instanceof LifelineEditPart) {
								Object childModel = childEditPart.getModel();
								if(childModel instanceof Shape) {
									// we get the object Lifeline linked the selected
									// lifelineEditPart
									EObject childElement = ((Shape)childModel).getElement();
									if(childElement instanceof Lifeline) {
										Lifeline lifeline = (Lifeline)childElement;
										for(Lifeline lfn : lifelineCoveredList) {
											// we check if the lifeLine in the intreactionUse's
											// parent Interaction is a covered Lifeline
											if(lifeline.equals(lfn)) {
												LifelineEditPart liflelineEditPart = (LifelineEditPart)childEditPart;
												if(liflelineEditPart.getFigure().getBounds().x > maxX) {
													maxX = liflelineEditPart.getFigure().getBounds().x;
													// the maxR will represent the futur value of
													// the rectangle right value.
													maxR = liflelineEditPart.getFigure().getBounds().right();

												}
												if(liflelineEditPart.getFigure().getBounds().x < minX) {
													// the min value will represent the rectangle x
													// value.
													minX = liflelineEditPart.getFigure().getBounds().x;
												}
											}
										}
									}
								}
							}
						}

						if(minX != Integer.MAX_VALUE || maxR != -Integer.MAX_VALUE) {
							// after this loop we have the coordinate of two lifeline figure ,
							// even if we have more than two covered Lifelne we choose the
							// extremities.
							getNewSize(minX, maxR);
						}
					}
				}
			}
		}
	}

	/**
	 * resize the interactinUse figure
	 * 
	 * @param min
	 *        the min x position of a covered lifline
	 * @param max
	 *        the max x position of a coverd lifeline
	 * 
	 */
	private void getNewSize(int min, int max) {
		int h = getFigure().getBounds().height;
		int y = getFigure().getBounds().y;

		Dimension size = new Dimension(max - min, h);

		Point loc = new Point(min, y);

		((GraphicalEditPart)getParent()).setLayoutConstraint(this, getFigure(), new Rectangle(loc, size));
	}

	/**
	 * apex updated
	 * Property 창에서 강제로 covereds에서 제외된 Lifeline은
	 * CF 경계 변동 시(CF는 좌우이동 안되므로 Resize에만 해당)에도 covereds에 자동 포함 안되도록 개선 
	 * 중첩된 CF에도 covered 제대로 반영되도록 수정 
	 * 절대좌표로 바꾸면 오작동
	 * 
	 * Update covered lifelines of a Interaction fragment
	 * 
	 * @param newBounds
	 */
	public void updateCoveredLifelines(Bounds newBounds) {
		/* apex added start */
		//Rectangle origRect = SequenceUtil.getAbsoluteBounds(this);
		Rectangle origRect = this.getFigure().getBounds().getCopy();		
		/* apex added end */		
		
		Rectangle newBound = new Rectangle(newBounds.getX(), newBounds.getY(), newBounds.getWidth(), newBounds.getHeight());
		InteractionFragment combinedFragment = (InteractionFragment)resolveSemanticElement();
		EList<Lifeline> coveredLifelines = combinedFragment.getCovereds();

		List<Lifeline> coveredLifelinesToAdd = new ArrayList<Lifeline>();
		List<Lifeline> coveredLifelinesToRemove = new ArrayList<Lifeline>();
		
		/* apex improved start */
		List lifelineEditPartsToCheck = null;
		
		if ( origRect.equals(0, 0, 0, 0)) { // 새 CombinedFragment 생성하는 경우 Interaction 내 모든 Lifeline을 check
			lifelineEditPartsToCheck = new ArrayList();
			Set<Entry<Object, EditPart>> allEditPartEntries = getViewer().getEditPartRegistry().entrySet();
			for(Entry<Object, EditPart> epEntry : allEditPartEntries) {
				EditPart ep = epEntry.getValue();
				if( ep instanceof LifelineEditPart ) {
					lifelineEditPartsToCheck.add((LifelineEditPart)ep);
				}
			}
		} else {
			// width 축소 시 원래 경계 내에 있던 lifeline을 check
			if (origRect.width >= newBound.width ) {
				lifelineEditPartsToCheck = ApexSequenceUtil.apexGetPositionallyCoveredLifelineEditParts(origRect, this);	
			} else { // width 확대 시 새 경계내에 있는 lifeline을 check
				lifelineEditPartsToCheck = ApexSequenceUtil.apexGetPositionallyCoveredLifelineEditParts(newBound, this);
			}				
		}
			
		int i = 0;
/*8
System.out.println("in updateCoveredLifelines, lifelineEditPartsToCheck : " + lifelineEditPartsToCheck);
//*/
		for(Object child : lifelineEditPartsToCheck) {

			LifelineEditPart lifelineEditPart = (LifelineEditPart)child;
			Lifeline lifeline = (Lifeline)lifelineEditPart.resolveSemanticElement();
			Rectangle lifelineRect = lifelineEditPart.getFigure().getBounds().getCopy();
			//lifelineEditPart.getFigure().getParent().translateToAbsolute(lifelineRect);
			
/*8
System.out.println("=================================== "+i++);
System.out.println("InteractionFragmentEditPart.updateCoveredLifelines(), line : "+Thread.currentThread().getStackTrace()[1].getLineNumber());			
System.out.println("in updateCoveredLifelines, orig.right()     : " + origRect.right());
System.out.println("in updateCoveredLifelines, new.right()      : " + newBound.right());
System.out.println("in updateCoveredLifelines, lifeline.x       : " + lifelineRect.x);
//*/
			
			// 새 경계와 lifeline 경계가 교차되고
			if(newBound.intersects(lifelineRect)) {
/*8
System.out.println("in updateCoveredLifelines, intersects");
//*/
				// 원래의 covered에 없던 lifeline이면
				if(!coveredLifelines.contains(lifeline)) {
					// 원래 CF에 포함되어 있으면서 coveredLifelines에 없는 것은
					// Property창에서 수동으로 covereds에서 제외한 것이므로
					// 원래 CF에 포함되지 않았던 경우에만 add
					if (!origRect.intersects(lifelineEditPart.getFigure().getBounds())) {
						coveredLifelinesToAdd.add(lifeline);							
					}						
				}
			// 새 경계와 lifeline 경계가 교차되지 않고
			// 원래 covered에 있던 lifeline은 remove
			} else {
				coveredLifelinesToRemove.add(lifeline);
			}
		}
		
/*8
System.out.println("in updateCoveredLifelines, coveredLifelinesToAdd    : " + coveredLifelinesToAdd);
System.out.println("in updateCoveredLifelines, coveredLifelinesToRemove : " + coveredLifelinesToRemove);
//*
		/* apex improved end */
		
		/* apex replaced
		for(Object child : getParent().getChildren()) {
			if(child instanceof LifelineEditPart) {
				LifelineEditPart lifelineEditPart = (LifelineEditPart)child;
				Lifeline lifeline = (Lifeline)lifelineEditPart.resolveSemanticElement();
				if(newBound.intersects(lifelineEditPart.getFigure().getBounds())) {

					if(!coveredLifelines.contains(lifeline)) {
						coveredLifelinesToAdd.add(lifeline);										
					}
					
				} else if(coveredLifelines.contains(lifeline)) {
					coveredLifelinesToRemove.add(lifeline);
				}
			}
		}
		*/
		if(!coveredLifelinesToAdd.isEmpty()) {
			CommandHelper.executeCommandWithoutHistory(getEditingDomain(), AddCommand.create(getEditingDomain(), combinedFragment, UMLPackage.eINSTANCE.getInteractionFragment_Covered(), coveredLifelinesToAdd));
		}
		if(!coveredLifelinesToRemove.isEmpty()) {
			CommandHelper.executeCommandWithoutHistory(getEditingDomain(), RemoveCommand.create(getEditingDomain(), combinedFragment, UMLPackage.eINSTANCE.getInteractionFragment_Covered(), coveredLifelinesToRemove));
		}

	}

}
