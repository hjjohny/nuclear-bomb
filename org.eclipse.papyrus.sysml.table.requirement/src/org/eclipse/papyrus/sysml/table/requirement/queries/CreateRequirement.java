/*****************************************************************************
 * Copyright (c) 2011 CEA LIST.
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Vincent Lorenzo (CEA LIST) vincent.lorenzo@cea.fr - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.sysml.table.requirement.queries;

import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.facet.infra.query.core.exception.ModelQueryExecutionException;
import org.eclipse.emf.facet.infra.query.core.java.IJavaModelQuery;
import org.eclipse.emf.facet.infra.query.core.java.ParameterValueList;
import org.eclipse.emf.facet.infra.query.runtime.ModelQueryParameterValue;
import org.eclipse.emf.facet.widgets.nattable.tableconfiguration.InstantiationMethodParameters;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateElementRequest;
import org.eclipse.papyrus.diagram.common.command.wrappers.GMFtoEMFCommandWrapper;
import org.eclipse.papyrus.service.edit.service.ElementEditServiceUtils;
import org.eclipse.papyrus.service.edit.service.IElementEditService;
import org.eclipse.papyrus.sysml.service.types.element.SysMLElementTypes;
import org.eclipse.uml2.uml.Package;

/** a query which creates a new requirement and associates it with its parent (contained by the scope) */
public class CreateRequirement implements IJavaModelQuery<Package, org.eclipse.uml2.uml.Class> {

	/**
	 * 
	 * @see org.eclipse.emf.facet.infra.query.core.java.IJavaModelQuery#evaluate(org.eclipse.emf.ecore.EObject,
	 *      org.eclipse.emf.facet.infra.query.core.java.ParameterValueList)
	 * 
	 * @param context
	 * @param parameterValues
	 * @return
	 * @throws ModelQueryExecutionException
	 */
	public org.eclipse.uml2.uml.Class evaluate(final Package context, final ParameterValueList parameterValues) throws ModelQueryExecutionException {
		EditingDomain editingDomain = null;
		ModelQueryParameterValue model = parameterValues.getParameterValueByName(InstantiationMethodParameters.getEditingDomainParameter().getName());
		if(model != null) {
			if(model.getValue() instanceof EditingDomain) {
				editingDomain = (EditingDomain)model.getValue();
			}
		}
		if(editingDomain != null) {
			if(context != null) {

				IElementEditService provider = ElementEditServiceUtils.getCommandProvider(context);

				CreateElementRequest createRequest = new CreateElementRequest(context, SysMLElementTypes.REQUIREMENT);
				ICommand createGMFCommand = provider.getEditCommand(createRequest);

				editingDomain.getCommandStack().execute(new GMFtoEMFCommandWrapper(createGMFCommand));
				if(createGMFCommand.getCommandResult() != null) {//it's null, when the model is not a SysML model
					Object returnedValue = createGMFCommand.getCommandResult().getReturnValue();
					return (org.eclipse.uml2.uml.Class)returnedValue;
				}

			}
		}
		return null;
	}
}
