package org.eclipse.papyrus.sysml.modelexplorer.handler;

import org.eclipse.core.commands.IHandler;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.common.core.command.UnexecutableCommand;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateElementRequest;
import org.eclipse.papyrus.service.edit.commands.IConfigureCommandFactory;
import org.eclipse.papyrus.service.edit.service.ElementEditServiceUtils;
import org.eclipse.papyrus.service.edit.service.IElementEditService;
import org.eclipse.papyrus.sysml.diagram.common.commands.CreateValueWithTypeConfigureCommandFactory;
import org.eclipse.papyrus.sysml.service.types.element.SysMLElementTypes;
import org.eclipse.uml2.uml.Element;

/**
 * <pre>
 * Command handler for Value (Property) creation
 *
 * </pre>
 * @generated
 */
public class ValuePropertyHandler extends CreateCommandHandler implements IHandler {

	/**
	 * <pre>
	 * @see org.eclipse.papyrus.uml.service.creation.handler.CreateHandler#getElementTypeToCreate()
	 *
	 * @return the IElementType this handler is supposed to create
	 *
	 * </pre>
	 * @generated
	 */
	protected IElementType getElementTypeToCreate() {
    return SysMLElementTypes.VALUE_PROPERTY;
  }
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ICommand buildCommand() {

		if(getSelectedElements().size() != 1) {
			return UnexecutableCommand.INSTANCE;
		}

		EObject container = getSelectedElements().get(0);
		if ((container == null) || !(container instanceof Element)) {
			return UnexecutableCommand.INSTANCE;
		}
		
		IElementEditService provider = ElementEditServiceUtils.getCommandProvider(container);
		if(provider == null) {
			return UnexecutableCommand.INSTANCE;
		}

		// Retrieve create command from the Element Edit service
		CreateElementRequest createRequest = new CreateElementRequest(container, getElementTypeToCreate());
		createRequest.setParameter(IConfigureCommandFactory.CONFIGURE_COMMAND_FACTORY_ID, new CreateValueWithTypeConfigureCommandFactory());
		ICommand createGMFCommand = provider.getEditCommand(createRequest);

		return createGMFCommand;
	}
}

