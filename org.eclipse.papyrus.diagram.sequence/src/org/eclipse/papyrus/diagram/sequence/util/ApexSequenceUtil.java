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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionNodeEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.INodeEditPart;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.diagram.common.util.DiagramEditPartsUtil;
import org.eclipse.papyrus.diagram.sequence.edit.parts.ActionExecutionSpecificationEditPart;
import org.eclipse.papyrus.diagram.sequence.edit.parts.BehaviorExecutionSpecificationEditPart;
import org.eclipse.papyrus.diagram.sequence.edit.parts.CombinedFragment2EditPart;
import org.eclipse.papyrus.diagram.sequence.edit.parts.CombinedFragmentEditPart;
import org.eclipse.papyrus.diagram.sequence.edit.parts.ContinuationEditPart;
import org.eclipse.papyrus.diagram.sequence.edit.parts.InteractionEditPart;
import org.eclipse.papyrus.diagram.sequence.edit.parts.InteractionOperandEditPart;
import org.eclipse.papyrus.diagram.sequence.edit.parts.InteractionUseEditPart;
import org.eclipse.papyrus.diagram.sequence.edit.parts.LifelineEditPart;
import org.eclipse.swt.SWT;
import org.eclipse.uml2.uml.CombinedFragment;
import org.eclipse.uml2.uml.ExecutionOccurrenceSpecification;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.InteractionOperand;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ApexSequenceUtil {

	/**
	 * 주어진 EditPartEntry Set에서 해당 AbstractGraphicalEditPart 보다
	 * y 좌표가 아래에 있는 EditPartList 반환 
	 * 
	 * @param agep   기준이 되는 AbstractGraphicalEditPart
	 * @return aep보다 아래에 위치한 EditPart의 List
	 */
	public static List apexGetBelowEditPartList(IGraphicalEditPart agep) {
					
		Set<Entry<Object, EditPart>> wholeEditPartEntries = agep.getViewer().getEditPartRegistry().entrySet();
		
		Map<IGraphicalEditPart, Integer> belowEditPartMap = new HashMap<IGraphicalEditPart, Integer>();

		int yBottomOfAgep = apexGetAbsolutePosition(agep, SWT.BOTTOM);
		
		for (Entry<Object, EditPart> aEPEntry : wholeEditPartEntries ) {
			
			EditPart editPart = aEPEntry.getValue();
			if (editPart.equals(agep))
				continue;
			if (!(editPart instanceof INodeEditPart))
				continue;
			if ( editPart instanceof IGraphicalEditPart ) {
				
				IGraphicalEditPart agep1 = (IGraphicalEditPart)editPart;
				
				int yTopThisEP = apexGetAbsolutePosition(agep1, SWT.TOP);

				if ( yTopThisEP >= yBottomOfAgep
						&& !belowEditPartMap.containsKey(agep1)) {
					belowEditPartMap.put(agep1, yTopThisEP);
				}
			}	
		}
		
		Collection<Entry<IGraphicalEditPart, Integer>> entrySet = belowEditPartMap.entrySet();
		List<Entry<IGraphicalEditPart, Integer>> entryList = new ArrayList<Entry<IGraphicalEditPart, Integer>>(entrySet);
		Collections.sort(entryList, new Comparator<Entry<IGraphicalEditPart, Integer>>() {
			public int compare(Entry<IGraphicalEditPart, Integer> o1,
					Entry<IGraphicalEditPart, Integer> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}
		});
		
		List<IGraphicalEditPart> belowEditPartList = new ArrayList<IGraphicalEditPart>(entryList.size());
		for (Entry<IGraphicalEditPart, Integer> entry : entryList) {
			belowEditPartList.add(entry.getKey());
		}

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
	public static IGraphicalEditPart apexGetBeneathEditPart(IGraphicalEditPart agep, List belowEditPartList) {

		int gap = Integer.MAX_VALUE;
		IGraphicalEditPart beneathEditPart = null;

		int yCF = apexGetAbsolutePosition(agep, SWT.BOTTOM);
		
		Iterator it = belowEditPartList.iterator();
		
		while( it.hasNext()) {
			
			IGraphicalEditPart sep = (IGraphicalEditPart)it.next();
			
			int yEP = apexGetAbsolutePosition(sep, SWT.TOP);
			
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
	public static List apexGetHigherEditPartList(IGraphicalEditPart agep) {
					
		Set<Entry<Object, EditPart>> wholeEditPartEntries = agep.getViewer().getEditPartRegistry().entrySet();

		Map<IGraphicalEditPart, Integer> higherEditPartMap = new HashMap<IGraphicalEditPart, Integer>();
		
		int yTopOfAgep = apexGetAbsolutePosition(agep, SWT.TOP);
		
		for (Entry<Object, EditPart> aEPEntry : wholeEditPartEntries ) {
			
			EditPart editPart = aEPEntry.getValue();
			
			if (editPart.equals(agep))
				continue;
			if (!(editPart instanceof INodeEditPart))
				continue;
			if ( editPart instanceof IGraphicalEditPart ) {
				
				IGraphicalEditPart agep1 = (IGraphicalEditPart)editPart;
				
				int yBottomThisEP = apexGetAbsolutePosition(agep1, SWT.BOTTOM);

				if ( yBottomThisEP <= yTopOfAgep
						&& !higherEditPartMap.containsKey(agep1)) {					
					higherEditPartMap.put(agep1, yBottomThisEP);
				}
			}	
		}

		Collection<Entry<IGraphicalEditPart, Integer>> entrySet = higherEditPartMap.entrySet();
		List<Entry<IGraphicalEditPart, Integer>> entryList = new ArrayList<Entry<IGraphicalEditPart, Integer>>(entrySet);
		Collections.sort(entryList, new Comparator<Entry<IGraphicalEditPart, Integer>>() {
			public int compare(Entry<IGraphicalEditPart, Integer> o1,
					Entry<IGraphicalEditPart, Integer> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}
		});
		
		List<IGraphicalEditPart> higherEditPartList = new ArrayList<IGraphicalEditPart>(entryList.size());
		for (Entry<IGraphicalEditPart, Integer> entry : entryList) {
			higherEditPartList.add(entry.getKey());
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
	public static IGraphicalEditPart apexGetAboveEditPart(IGraphicalEditPart agep, List higherEditPartList) {

		int gap = Integer.MAX_VALUE;
		IGraphicalEditPart aboveEditPart = null;
		
		int yCF = apexGetAbsolutePosition(agep, SWT.TOP);
		
		Iterator it = higherEditPartList.iterator();
		
		while( it.hasNext()) {
			IGraphicalEditPart sep = (IGraphicalEditPart)it.next();
			int yEP = apexGetAbsolutePosition(sep, SWT.BOTTOM);
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
	public static List apexGetLinkedEditPartList(IGraphicalEditPart agep, boolean findConnection, boolean findExecSpec, boolean findFromStart) {
		return apexGetLinkedEditPartList(agep, findConnection, findExecSpec, findFromStart, new ArrayList());
	}
	
	/**
	 * Message에 링크된 ExecutionSpec과 Message들을 리스트로 반환
	 * 
	 * @param agep Message or ExecutionSpecification
	 * @param findConnection
	 * @param findExecSpec
	 * @param findFromStart
	 * @param list
	 * @return
	 */
	public static List apexGetLinkedEditPartList(IGraphicalEditPart agep, boolean findConnection, boolean findExecSpec, boolean findFromStart, List list) {
		if (findFromStart) {
			if (agep instanceof ConnectionNodeEditPart) {
				ConnectionNodeEditPart cep = (ConnectionNodeEditPart)agep;
				IGraphicalEditPart srcEditPart = (IGraphicalEditPart)cep.getSource();
				if ( !(srcEditPart instanceof LifelineEditPart) ) {
					return apexGetLinkedEditPartList(srcEditPart, findConnection, findExecSpec, findFromStart, list);
				}
			}
			if (agep instanceof ActionExecutionSpecificationEditPart || agep instanceof BehaviorExecutionSpecificationEditPart) {
				List tgtConnections = agep.getTargetConnections();
				Iterator iter = tgtConnections.iterator();
				while (iter.hasNext()) {
					ConnectionNodeEditPart tgtConnection = (ConnectionNodeEditPart)iter.next();
					apexGetLinkedEditPartList((ConnectionNodeEditPart)tgtConnection, findConnection, findExecSpec, findFromStart, list);
					if ( !list.isEmpty() ) {
						return list;
					}
				}
			}
		}
		
		if (agep instanceof ConnectionNodeEditPart) {
			ConnectionNodeEditPart cep = (ConnectionNodeEditPart)agep;
			if (findConnection)
				list.add(0, cep);
			IGraphicalEditPart tgtEditPart = (IGraphicalEditPart)cep.getTarget();
			apexGetLinkedEditPartList(tgtEditPart, findConnection, findExecSpec, false, list);
		}
		else if (agep instanceof ActionExecutionSpecificationEditPart || agep instanceof BehaviorExecutionSpecificationEditPart) {
			if (findExecSpec)
				list.add(0, agep);
			List srcConnections = agep.getSourceConnections();
			Iterator iter = srcConnections.iterator();
			while (iter.hasNext()) {
				ConnectionNodeEditPart srcConnection = (ConnectionNodeEditPart)iter.next();
				apexGetLinkedEditPartList((ConnectionNodeEditPart)srcConnection, findConnection, findExecSpec, false, list);
			}
		}
		else if (agep instanceof LifelineEditPart) {
			LifelineEditPart lep = (LifelineEditPart)agep;
			IFigure lLFigure = lep.getFigure();
			Rectangle origLLBounds = lLFigure.getBounds().getCopy();
			lLFigure.getParent().translateToAbsolute(origLLBounds);
			
			List editParts = apexGetBelowEditPartList(lep);
			Iterator iter = editParts.iterator();
			while (iter.hasNext()) {
				IGraphicalEditPart editPart = (IGraphicalEditPart)iter.next();
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

	/**
	 * Message에 링크된 EditPart들 중 가장 하위에 있는 EditPart를 반환 (bottom값이 가장 하위)
	 * 
	 * @param agep
	 * @return
	 */
	public static IGraphicalEditPart apexGetBottomEditPartInLinked(IGraphicalEditPart agep) {
		List editPartList = apexGetLinkedEditPartList(agep, true, true, false, new ArrayList());
		Iterator iter = editPartList.iterator();
		int bottom = Integer.MIN_VALUE;
		IGraphicalEditPart bottomEditPart = null;
		while (iter.hasNext()) {
			Object next = iter.next();
			if (next instanceof IGraphicalEditPart) {
				int b = apexGetAbsolutePosition((IGraphicalEditPart)next, SWT.BOTTOM);
				if (b > bottom) {
					bottom = b;
					bottomEditPart = (IGraphicalEditPart)next;
				}
			}
		}
		return bottomEditPart;
	}
	
	public static Rectangle apexGetAbsoluteRectangle(IGraphicalEditPart gep) {
		Rectangle bounds = null;
		if (gep instanceof ConnectionNodeEditPart) {
			Connection conn = ((ConnectionNodeEditPart)gep).getConnectionFigure();
			Point p2 = conn.getTargetAnchor().getReferencePoint();
			Point p1 = conn.getSourceAnchor().getLocation(p2);
			bounds = new Rectangle(p1.x(), p1.y, p2.x() - p1.x(), p2.y() - p1.y());
		} else {
			IFigure figure = gep.getFigure();
			bounds = figure.getBounds().getCopy();
			figure.getParent().translateToAbsolute(bounds);
		}
		
		return bounds;
	}
	
	/**
	 * EditPart의 Bottom값을 반환. Message인 경우 Bendpoint들 중 가장 하위의 값을 반환
	 * 
	 * @param gep
	 * @param type	one of TOP, BOTTOM
	 * @return
	 */
	public static int apexGetAbsolutePosition(IGraphicalEditPart gep, int type) {
		Rectangle bounds = apexGetAbsoluteRectangle(gep);
		switch (type) {
		case SWT.TOP:
			return bounds.getTop().y();
		case SWT.BOTTOM:
			return bounds.getBottom().y();
		}
		return -1;
	}
	
	/**
	 * SequenceUtil에 있던 메서드지만 아무도 호출하지 않아
	 * ApexSequenceUtil 로 가져와서 개조 사용
	 * Property의 covered 설정과 관계없이
	 * 해당 Rectangle에 intersect되는 모든 Lifeline 반환
	 * 절대좌표로 비교
	 * 
	 * @param selectionRect
	 * @param hostEditPart
	 * @return
	 */
	public static List apexGetPositionallyCoveredLifelineEditParts(Rectangle selectionRect, AbstractGraphicalEditPart hostEditPart) {
		
		hostEditPart.getFigure().translateToAbsolute(selectionRect);
		
		List positionallyCoveredLifelineEditParts = new ArrayList();

		// retrieve all the edit parts in the registry
		Set<Entry<Object, EditPart>> allEditPartEntries = hostEditPart.getViewer().getEditPartRegistry().entrySet();
		for(Entry<Object, EditPart> epEntry : allEditPartEntries) {
			EditPart ep = epEntry.getValue();

			if(ep instanceof LifelineEditPart) {
				Rectangle figureBounds = SequenceUtil.getAbsoluteBounds((LifelineEditPart)ep);

				if(selectionRect.intersects(figureBounds)) {
					positionallyCoveredLifelineEditParts.add(ep);
				}
			}

		}
		return positionallyCoveredLifelineEditParts;
	}
	
	
	public static ConnectionNodeEditPart apexGetPrevConnectionEditPart(ConnectionNodeEditPart connectionPart) {
		EditPart sourcePart = connectionPart.getSource();
		if (sourcePart instanceof ActionExecutionSpecificationEditPart ||
				sourcePart instanceof BehaviorExecutionSpecificationEditPart) {
			List<ConnectionNodeEditPart> srcConns = apexGetSortedConnections((IGraphicalEditPart)sourcePart, true, true);
			if (srcConns.size() > 0 && srcConns.indexOf(connectionPart) > 0) {
				int index = srcConns.indexOf(connectionPart);
				return srcConns.get(index - 1);
//				return apexGetLastConnectionEditPart(srcConns.get(index - 1));
			}
			List<ConnectionNodeEditPart> tgtConns = apexGetSortedConnections((IGraphicalEditPart)sourcePart, false, true);
			if (tgtConns.size() > 0) {
				return tgtConns.get(tgtConns.size() - 1);
			}
		}
		return null;
	}
	
	public static ConnectionNodeEditPart apexGetNextConnectionEditPart(ConnectionNodeEditPart connectionPart) {
		EditPart targetPart = connectionPart.getTarget();
		if (targetPart instanceof ActionExecutionSpecificationEditPart ||
				targetPart instanceof BehaviorExecutionSpecificationEditPart) {
			List<ConnectionNodeEditPart> srcConns = apexGetSortedConnections((IGraphicalEditPart)targetPart, true, true);
			if (srcConns.size() > 0) {
				return srcConns.get(0);
			}
		}
		EditPart sourcePart = connectionPart.getSource();
		if (sourcePart instanceof ActionExecutionSpecificationEditPart ||
				sourcePart instanceof BehaviorExecutionSpecificationEditPart) {
			List<ConnectionNodeEditPart> srcConns = apexGetSortedConnections((IGraphicalEditPart)sourcePart, true, true);
			if (srcConns.size() > 0) {
				int index = srcConns.indexOf(connectionPart);
				if (index > -1 && index < srcConns.size() - 1) {
					return srcConns.get(index + 1);
				}
			}
		}
		return null;
	}
	
	public static ConnectionNodeEditPart apexGetLastConnectionEditPart(ConnectionNodeEditPart connectionPart) {
		EditPart targetPart = connectionPart.getTarget();
		if (targetPart instanceof LifelineEditPart) {
			return connectionPart;
		}
		else if (targetPart instanceof ActionExecutionSpecificationEditPart ||
				targetPart instanceof BehaviorExecutionSpecificationEditPart) {
			List<ConnectionNodeEditPart> sourceConns = apexGetSortedConnections((IGraphicalEditPart)targetPart, true, true);
			if (sourceConns.size() > 0) {
				return apexGetLastConnectionEditPart(sourceConns.get(sourceConns.size() - 1));
			}
			else {
				return connectionPart;
			}
		}
		return null;
	}
	
	public static List<ConnectionNodeEditPart> apexGetSortedConnections(IGraphicalEditPart editPart, boolean isSource, final boolean forward) {
		List<ConnectionNodeEditPart> srcConns = new ArrayList<ConnectionNodeEditPart>();
		List<?> conns = isSource ? editPart.getSourceConnections() : editPart.getTargetConnections();
		for (Object conn : conns) {
			if (conn instanceof ConnectionNodeEditPart) {
				srcConns.add((ConnectionNodeEditPart)conn);
			}
		}
		Collections.sort(srcConns, new Comparator<ConnectionNodeEditPart>() {
			public int compare(ConnectionNodeEditPart o1, ConnectionNodeEditPart o2) {
				Point p1 = SequenceUtil.getAbsoluteEdgeExtremity((ConnectionNodeEditPart)o1, true);
				Point p2 = SequenceUtil.getAbsoluteEdgeExtremity((ConnectionNodeEditPart)o2, true);
				
				if (p1 != null && p2 != null) {
					int compare = forward ? p1.y() - p2.y() : p2.y() - p1.y();
					return compare >= 0 ? (compare != 0 ? 1 : 0) : -1;
				}
				return 0;
			}
		});
		
		return srcConns;
	}
	
	public static List<IGraphicalEditPart> apexGetSiblingEditParts(IGraphicalEditPart gep) {
		List<InteractionFragment> fragmentList = new ArrayList<InteractionFragment>();
		
		InteractionFragment fragment = null;
		if (gep instanceof ConnectionNodeEditPart) {
			ConnectionNodeEditPart connection = (ConnectionNodeEditPart)gep;
			Point edge = SequenceUtil.getAbsoluteEdgeExtremity(connection, true);
			fragment = SequenceUtil.findInteractionFragmentContainerAt(edge, gep);
		}
		else {
			Rectangle bounds = SequenceUtil.getAbsoluteBounds(gep);
			fragment = SequenceUtil.findInteractionFragmentContainerAt(bounds, gep);
		}

		if (fragment instanceof Interaction) {
			fragmentList.addAll( ((Interaction) fragment).getFragments() );
		}
		else if (fragment instanceof InteractionOperand) {
			fragmentList.addAll( ((InteractionOperand) fragment).getFragments() );
		}
		else if (fragment instanceof CombinedFragment) {
			fragmentList.addAll( ((CombinedFragment) fragment).getOperands() );
		}
		
		List<IGraphicalEditPart> result = new ArrayList<IGraphicalEditPart>(fragmentList.size());
		for (InteractionFragment itf : fragmentList) {
			EObject parseElement = itf;
			if (itf instanceof MessageOccurrenceSpecification) {
				parseElement = ((MessageOccurrenceSpecification)itf).getMessage();
			}
			else if (itf instanceof ExecutionOccurrenceSpecification) {
				parseElement = ((ExecutionOccurrenceSpecification)itf).getExecution();
			}
			
			List<View> views = DiagramEditPartsUtil.findViews(parseElement, gep.getViewer());
			for (View view : views) {
				EditPart part = DiagramEditPartsUtil.getEditPartFromView(view, gep);
				boolean isCombinedFragment = part instanceof CombinedFragmentEditPart || part instanceof CombinedFragment2EditPart;
				boolean isContinuation = part instanceof ContinuationEditPart;
				boolean isInteractionOperand = part instanceof InteractionOperandEditPart;
				boolean isInteractionUse = part instanceof InteractionUseEditPart;
				boolean isInteraction = part instanceof InteractionEditPart;
				boolean isMessage = part instanceof ConnectionNodeEditPart;
				boolean isActivation = part instanceof ActionExecutionSpecificationEditPart || part instanceof BehaviorExecutionSpecificationEditPart;
				boolean isSameEditPart = gep.equals(part);
				if(isCombinedFragment || isContinuation || isInteractionOperand || isInteractionUse || isInteraction || isMessage /*|| isActivation*/) {
					if (!result.contains(part) && !isSameEditPart) {
						result.add((IGraphicalEditPart) part);
					}
				}
			}
		}
		
		return result;
	}
	
	public static List<IGraphicalEditPart> apexGetNextSiblingEditParts(IGraphicalEditPart gep) {
		List<IGraphicalEditPart> removeList = new ArrayList<IGraphicalEditPart>();
		List<IGraphicalEditPart> result = apexGetSiblingEditParts(gep);
		int y = ApexSequenceUtil.apexGetAbsolutePosition(gep, SWT.BOTTOM);
		for (IGraphicalEditPart part : result) {
			int top = ApexSequenceUtil.apexGetAbsolutePosition(part, SWT.TOP);
			if (y > top) {
				removeList.add(part);
			}
		}
		
		result.removeAll(removeList);
		return result;
	}
	
	public static List<IGraphicalEditPart> apexGetPrevSiblingEditParts(IGraphicalEditPart gep) {
		List<IGraphicalEditPart> removeList = new ArrayList<IGraphicalEditPart>();
		List<IGraphicalEditPart> result = apexGetSiblingEditParts(gep);
		int y = ApexSequenceUtil.apexGetAbsolutePosition(gep, SWT.TOP);
		for (IGraphicalEditPart part : result) {
			int top = ApexSequenceUtil.apexGetAbsolutePosition(part, SWT.BOTTOM);
			if (y < top) {
				removeList.add(part);
			}
		}
		
		result.removeAll(removeList);
		return result;
	}
}
