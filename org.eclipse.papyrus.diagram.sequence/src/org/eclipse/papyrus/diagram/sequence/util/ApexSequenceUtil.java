/*****************************************************************************
 * Copyright (c) 2012 ApexSoft
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   ApexSoft - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.diagram.sequence.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionEditPart;
import org.eclipse.papyrus.diagram.sequence.edit.parts.ActionExecutionSpecificationEditPart;
import org.eclipse.papyrus.diagram.sequence.edit.parts.BehaviorExecutionSpecificationEditPart;
import org.eclipse.papyrus.diagram.sequence.edit.parts.CombinedFragmentEditPart;
import org.eclipse.papyrus.diagram.sequence.edit.parts.InteractionOperandEditPart;
import org.eclipse.papyrus.diagram.sequence.edit.parts.LifelineEditPart;
import org.eclipse.uml2.uml.InteractionOperand;

public class ApexSequenceUtil {

	/**
	 * 주어진 EditPartEntry Set에서 해당 AbstractGraphicalEditPart 보다
	 * y 좌표가 아래에 있는 EditPartList 반환 
	 * 
	 * @param agep   기준이 되는 AbstractGraphicalEditPart
	 * @return aep보다 아래에 위치한 EditPart의 List
	 */
	public static List apexGetBelowEditPartList(AbstractGraphicalEditPart agep) {
					
		Set<Entry<Object, EditPart>> wholeEditPartEntries = agep.getViewer().getEditPartRegistry().entrySet();
				
		List<AbstractGraphicalEditPart> belowEditPartList = new ArrayList<AbstractGraphicalEditPart>();		
		
		int yBottomOfAgep = agep.getFigure().getBounds().getBottom().y;
		
		for (Entry<Object, EditPart> aEPEntry : wholeEditPartEntries ) {
			
			EditPart editPart = aEPEntry.getValue();
			if ( editPart instanceof AbstractGraphicalEditPart ) {
				
				AbstractGraphicalEditPart agep1 = (AbstractGraphicalEditPart)editPart;
				IFigure figure = agep1.getFigure();
				
				int yTopThisEP = figure.getBounds().getTop().y;

				if ( yTopThisEP >= yBottomOfAgep) {					
					belowEditPartList.add(agep1);
				}
			}	
		}
/*8
System.out.println("+++++ below List Start +++++");
for ( int i = 0 ; i < belowEditPartList.size() ; i++ ) {
	System.out.println("   ["+i+"] : " + belowEditPartList.get(i));				
}		
System.out.println("+++++ below List End+++++");
*/

		return belowEditPartList;
	}

	/**
	 * 주어진 EditPartList를 검색하여
	 * y좌표 기준 주어진 AbstractGraphicalEditPart의 바로 아래에 위치한 AbstractGraphicalEditPart 반환
	 * 
	 * @param agep    기준이 되는 AbstractGraphicalEditPart
	 * @param belowEditPartList    검색할 EditPart의 List
	 * @return    y좌표 기준 agep의 바로 아래에 위치한 AbstractGraphicalEditPart
	 */
	public static AbstractGraphicalEditPart apexGetBeneathEditPart(AbstractGraphicalEditPart agep, List belowEditPartList) {

		int gap = 1000000;
		AbstractGraphicalEditPart beneathEditPart = null;
		
		int yCF = agep.getFigure().getBounds().getBottom().y;
		
		Iterator it = belowEditPartList.iterator();
		
		while( it.hasNext()) {
			
			AbstractGraphicalEditPart sep = (AbstractGraphicalEditPart)it.next();
			
			int yEP = sep.getFigure().getBounds().getTop().y;
			
			int thisGap = yEP - yCF;
			
			if ( thisGap < gap ) {
				gap = thisGap;
				beneathEditPart = sep;
			}
		}
		return beneathEditPart;
	}

	/**
     * 주어진 EditPart의 모든 children을 Indent하여 출력하는 Util
	 * 코드 내에서 호출 시 전달 된 depth값을 0으로 하여 본 메서드 호출
	 * 
	 * @param ep
	 */
	public static void apexShowChildrenEditPart(EditPart ep) {
		apexShowChildrenEditPart(ep, 0);
	}
	
	/**
	 * 주어진 EditPart의 모든 children을 Indent하여 출력하는 Util
	 * 코드 내에서 호출 시 전달 된 depth값을 기준으로 Indent 처리
	 * (depth>=0인 어떤 값도 무방하나 호출 시 depth=0 권장) 
	 * 
	 * @param ep     출력될 children을 가진 EditPart
	 * @param depth  Indent 수준
	 */
	public static void apexShowChildrenEditPart(EditPart ep, int depth) {
		List childrenList = ep.getChildren();
		Iterator it = childrenList.iterator();
		
		while ( it.hasNext() ) {
			StringBuffer sb = new StringBuffer();
			for ( int i = 0 ; i < depth ; i++ ) {
				sb.append(" ");	
			}
			
			EditPart o = (EditPart)it.next();
			
			System.out.println(sb.toString() + o/*.getClass().getSimpleName()*/);
			// interactionOperand의 경우 포함되는 fragment를 출력
			if ( o instanceof InteractionOperandEditPart ) {
				InteractionOperand io = (InteractionOperand)((InteractionOperandEditPart)o).resolveSemanticElement();
				System.out.println(sb.toString() + "  " + "lifelines : " + io.getCovereds());
				System.out.println(sb.toString() + "  " + "fragments : " + io.getFragments());	
			}
			
			// children 있으면 depth 증가 후 재귀호출
			if ( o.getChildren().size() > 0 ) {
				apexShowChildrenEditPart(o, depth+2);	
			}

		}	
	}
	
	/**
	 * 해당 EditPart의 모든 child EditPart를 leveling 없이 반환
	 * 빈 List를 생성하여 본 메서드 apexGetChildEditPartList(EditPart, List) 호출
	 * 
	 * @param ep
	 * @return
	 */
	public static List apexGetChildEditPartList(EditPart ep) {		
		return apexGetChildEditPartList(ep, new ArrayList());
	}
	
	/**
	 * 해당 EditPart의 모든 child EditPart를 leveling 없이 반환
	 * 
	 * @param ep
	 * @param childEPList
	 * @return
	 */
	public static List apexGetChildEditPartList(EditPart ep, List childEPList) {
		
		List<EditPart> childrenList = ep.getChildren();
		Iterator it = childrenList.iterator();
		
		while ( it.hasNext() ) {
			EditPart o = (EditPart)it.next();

			childEPList.add(o);

			if ( o.getChildren().size() > 0 ) {
				apexGetChildEditPartList(o, childEPList);	
			}
		}
		return childEPList;
	}
	
	/**
	 * ArrayList를 파라미터로 추가하여 apexGetParentCombinedFragmentEditPartList(CombinedFragmentEditPart, List) 호출
	 * 
	 * @param cfep
	 * @return
	 */
	public static List<CombinedFragmentEditPart> apexGetParentCombinedFragmentEditPartList(CombinedFragmentEditPart cfep) {
		return apexGetParentCombinedFragmentEditPartList(cfep, new ArrayList<CombinedFragmentEditPart>());
	}

	/**
	 * 
	 * @param cfep
	 * @param parentCombinedFragmentEditParts
	 * @return
	 */
	public static List<CombinedFragmentEditPart> apexGetParentCombinedFragmentEditPartList(CombinedFragmentEditPart cfep, List<CombinedFragmentEditPart> parentCombinedFragmentEditParts) {
				
		EditPart opParent = cfep.getParent();

		if ( opParent instanceof InteractionOperandEditPart ) {
			
			CombinedFragmentEditPart parentCombinedFragmentEditPart = (CombinedFragmentEditPart)opParent.getParent().getParent();
			
			parentCombinedFragmentEditParts.add(parentCombinedFragmentEditPart);
			
			apexGetParentCombinedFragmentEditPartList(parentCombinedFragmentEditPart, parentCombinedFragmentEditParts);			
		}
		return parentCombinedFragmentEditParts;
	}
	
	/**
	 * 주어진 EditPartEntry Set에서 해당 AbstractGraphicalEditPart 보다
	 * y 좌표가 위에 있는 EditPartList 반환 
	 * 
	 * @param agep   기준이 되는 AbstractGraphicalEditPart
	 * @return aep보다 위에 위치한 EditPart의 List
	 */
	public static List apexGetHigherEditPartList(AbstractGraphicalEditPart agep) {
					
		Set<Entry<Object, EditPart>> wholeEditPartEntries = agep.getViewer().getEditPartRegistry().entrySet();
				
		List<AbstractGraphicalEditPart> higherEditPartList = new ArrayList<AbstractGraphicalEditPart>();		
		
		int yTopOfAgep = agep.getFigure().getBounds().getTop().y;
		
		for (Entry<Object, EditPart> aEPEntry : wholeEditPartEntries ) {
			
			EditPart editPart = aEPEntry.getValue();
			
			if ( editPart instanceof AbstractGraphicalEditPart ) {
				
				AbstractGraphicalEditPart agep1 = (AbstractGraphicalEditPart)editPart;
				IFigure figure = agep1.getFigure();
				
				int yBottomThisEP = figure.getBounds().getBottom().y;

				if ( yBottomThisEP <= yTopOfAgep) {					
					higherEditPartList.add(agep1);
				}
			}	
		}

		return higherEditPartList;
	}

	/**
	 * 주어진 EditPartList를 검색하여
	 * y좌표 기준 주어진 AbstractGraphicalEditPart의 바로 위에 위치한 AbstractGraphicalEditPart 반환
	 * 
	 * @param agep    기준이 되는 AbstractGraphicalEditPart
	 * @param higherEditPartList    검색할 EditPart의 List
	 * @return    y좌표 기준 agep의 바로 아래에 위치한 AbstractGraphicalEditPart
	 */
	public static AbstractGraphicalEditPart apexGetAboveEditPart(AbstractGraphicalEditPart agep, List higherEditPartList) {

		int gap = 1000000;
		AbstractGraphicalEditPart aboveEditPart = null;
		
		int yCF = agep.getFigure().getBounds().getTop().y;
		
		Iterator it = higherEditPartList.iterator();
		
		while( it.hasNext()) {
			AbstractGraphicalEditPart sep = (AbstractGraphicalEditPart)it.next();
			int yEP = sep.getFigure().getBounds().getBottom().y;
			int thisGap = yCF - yEP;
			if ( thisGap < gap ) {
				gap = thisGap;
				aboveEditPart = sep;
			}
		}
		return aboveEditPart;
	}

	/**
	 * Message에 링크된 ExecutionSpec과 Message들을 리스트로 반환
	 * 
	 * @param agep
	 * @param findConnection	Whether to find Message or not 
	 * @param findExecSpec		Whether to find ExecutionSpec or not 
	 * @param findFromStart
	 * @return
	 */
	public static List apexGetLinkedEditPartList(AbstractGraphicalEditPart agep, boolean findConnection, boolean findExecSpec, boolean findFromStart) {
		return apexGetLinkedEditPartList(agep, findConnection, findExecSpec, findFromStart, new ArrayList());
	}
	
	/**
	 * Message에 링크된 ExecutionSpec과 Message들을 리스트로 반환
	 * 
	 * @param agep
	 * @param findConnection
	 * @param findExecSpec
	 * @param findFromStart
	 * @param list
	 * @return
	 */
	public static List apexGetLinkedEditPartList(AbstractGraphicalEditPart agep, boolean findConnection, boolean findExecSpec, boolean findFromStart, List list) {
		if (findFromStart) {
			if (agep instanceof ConnectionEditPart) {
				ConnectionEditPart cep = (ConnectionEditPart)agep;
				AbstractGraphicalEditPart srcEditPart = (AbstractGraphicalEditPart)cep.getSource();
				if ( !(srcEditPart instanceof LifelineEditPart) ) {
					return apexGetLinkedEditPartList(srcEditPart, findConnection, findExecSpec, findFromStart, list);
				}
			}
			if (agep instanceof ActionExecutionSpecificationEditPart || agep instanceof BehaviorExecutionSpecificationEditPart) {
				List tgtConnections = agep.getTargetConnections();
				Iterator iter = tgtConnections.iterator();
				while (iter.hasNext()) {
					ConnectionEditPart tgtConnection = (ConnectionEditPart)iter.next();
					apexGetLinkedEditPartList((ConnectionEditPart)tgtConnection, findConnection, findExecSpec, findFromStart, list);
					if ( !list.isEmpty() ) {
						return list;
					}
				}
			}
		}
		
		if (agep instanceof ConnectionEditPart) {
			ConnectionEditPart cep = (ConnectionEditPart)agep;
			if (findConnection)
				list.add(0, cep);
			AbstractGraphicalEditPart tgtEditPart = (AbstractGraphicalEditPart)cep.getTarget();
			apexGetLinkedEditPartList(tgtEditPart, findConnection, findExecSpec, false, list);
		}
		else if (agep instanceof ActionExecutionSpecificationEditPart || agep instanceof BehaviorExecutionSpecificationEditPart) {
			if (findExecSpec)
				list.add(0, agep);
			List srcConnections = agep.getSourceConnections();
			Iterator iter = srcConnections.iterator();
			while (iter.hasNext()) {
				ConnectionEditPart srcConnection = (ConnectionEditPart)iter.next();
				apexGetLinkedEditPartList((ConnectionEditPart)srcConnection, findConnection, findExecSpec, false, list);
			}
		}
		else if (agep instanceof LifelineEditPart) {
			LifelineEditPart lep = (LifelineEditPart)agep;
			IFigure lLFigure = lep.getFigure();
			Rectangle origLLBounds = lLFigure.getBounds().getCopy();
			lLFigure.getParent().translateToAbsolute(origLLBounds);
			origLLBounds.translate(lLFigure.getParent().getBounds().getLocation());
			
			List editParts = apexGetBelowEditPartList(lep);
			Iterator iter = editParts.iterator();
			while (iter.hasNext()) {
				AbstractGraphicalEditPart editPart = (AbstractGraphicalEditPart)iter.next();
				IFigure figure = editPart.getFigure();
				Rectangle bounds = figure.getBounds().getCopy();
				figure.getParent().translateToAbsolute(bounds);
				if (origLLBounds.contains(bounds)) {
					apexGetLinkedEditPartList(editPart, findConnection, findExecSpec, false, list);
				}
			}
		}
		
		return list;
	}
}
