/*****************************************************************************
 * Copyright (c) 2009-2011 CEA LIST.
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Yann Tanguy (CEA LIST) yann.tanguy@cea.fr - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.diagram.composite.edit.commands;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.emf.type.core.commands.EditElementCommand;
import org.eclipse.gmf.runtime.emf.type.core.requests.ReorientRelationshipRequest;
import org.eclipse.papyrus.diagram.composite.edit.policies.UMLBaseItemSemanticEditPolicy;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.ConnectorEnd;
import org.eclipse.uml2.uml.StructuredClassifier;

/**
 * @generated
 */
public class ConnectorReorientCommand extends EditElementCommand {

	/**
	 * @generated
	 */
	private final int reorientDirection;

	/**
	 * @generated
	 */
	private final EObject oldEnd;

	/**
	 * @generated
	 */
	private final EObject newEnd;

	/**
	 * @generated
	 */
	public ConnectorReorientCommand(ReorientRelationshipRequest request) {
		super(request.getLabel(), request.getRelationship(), request);
		reorientDirection = request.getDirection();
		oldEnd = request.getOldRelationshipEnd();
		newEnd = request.getNewRelationshipEnd();
	}

	/**
	 * @generated
	 */
	public boolean canExecute() {
		if(false == getElementToEdit() instanceof Connector) {
			return false;
		}
		if(reorientDirection == ReorientRelationshipRequest.REORIENT_SOURCE) {
			return canReorientSource();
		}
		if(reorientDirection == ReorientRelationshipRequest.REORIENT_TARGET) {
			return canReorientTarget();
		}
		return false;
	}

	/**
	 * @generated
	 */
	protected boolean canReorientSource() {
		if(!(oldEnd instanceof ConnectorEnd && newEnd instanceof ConnectorEnd)) {
			return false;
		}
		if(getLink().getEnds().size() != 1) {
			return false;
		}
		ConnectorEnd target = (ConnectorEnd)getLink().getEnds().get(0);
		if(!(getLink().eContainer() instanceof StructuredClassifier)) {
			return false;
		}
		StructuredClassifier container = (StructuredClassifier)getLink().eContainer();
		return UMLBaseItemSemanticEditPolicy.getLinkConstraints().canExistConnector_4013(container, getLink(), getNewSource(), target);
	}

	/**
	 * @generated
	 */
	protected boolean canReorientTarget() {
		if(!(oldEnd instanceof ConnectorEnd && newEnd instanceof ConnectorEnd)) {
			return false;
		}
		if(getLink().getEnds().size() != 1) {
			return false;
		}
		ConnectorEnd source = (ConnectorEnd)getLink().getEnds().get(0);
		if(!(getLink().eContainer() instanceof StructuredClassifier)) {
			return false;
		}
		StructuredClassifier container = (StructuredClassifier)getLink().eContainer();
		return UMLBaseItemSemanticEditPolicy.getLinkConstraints().canExistConnector_4013(container, getLink(), source, getNewTarget());
	}

	/**
	 * @generated
	 */
	protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		if(!canExecute()) {
			throw new ExecutionException("Invalid arguments in reorient link command"); //$NON-NLS-1$
		}
		if(reorientDirection == ReorientRelationshipRequest.REORIENT_SOURCE) {
			return reorientSource();
		}
		if(reorientDirection == ReorientRelationshipRequest.REORIENT_TARGET) {
			return reorientTarget();
		}
		throw new IllegalStateException();
	}

	/**
	 * @generated
	 */
	protected CommandResult reorientSource() throws ExecutionException {
		getLink().getEnds().remove(getOldSource());
		getLink().getEnds().add(getNewSource());
		return CommandResult.newOKCommandResult(getLink());
	}

	/**
	 * @generated
	 */
	protected CommandResult reorientTarget() throws ExecutionException {
		getLink().getEnds().remove(getOldTarget());
		getLink().getEnds().add(getNewTarget());
		return CommandResult.newOKCommandResult(getLink());
	}

	/**
	 * @generated
	 */
	protected Connector getLink() {
		return (Connector)getElementToEdit();
	}

	/**
	 * @generated
	 */
	protected ConnectorEnd getOldSource() {
		return (ConnectorEnd)oldEnd;
	}

	/**
	 * @generated
	 */
	protected ConnectorEnd getNewSource() {
		return (ConnectorEnd)newEnd;
	}

	/**
	 * @generated
	 */
	protected ConnectorEnd getOldTarget() {
		return (ConnectorEnd)oldEnd;
	}

	/**
	 * @generated
	 */
	protected ConnectorEnd getNewTarget() {
		return (ConnectorEnd)newEnd;
	}
}
