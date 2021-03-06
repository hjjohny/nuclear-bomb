/**
 * 
 */
package org.eclipse.papyrus.core.services.internal;

import org.eclipse.papyrus.core.services.BadStateException;
import org.eclipse.papyrus.core.services.ServiceDescriptor;
import org.eclipse.papyrus.core.services.ServiceException;
import org.eclipse.papyrus.core.services.ServiceState;
import org.eclipse.papyrus.core.services.ServicesRegistry;


/**
 * A service entry used for faulty services.
 * In this implementation, methods do nothings or throw an error.
 * 
 * 
 * @author cedric dumoulin
 *
 */
public class ErrorServiceTypeEntry extends ServiceTypeEntry {

	/**
	 * The original service descriptor.
	 */
	private ServiceDescriptor descriptor;
	
	
	/**
	 * 
	 * Constructor.
	 *
	 * @param descriptor
	 */
	public ErrorServiceTypeEntry( ServiceDescriptor descriptor ) {
		super(descriptor);
	}
	
	/**
	 * 
	 * @see org.eclipse.papyrus.core.services.internal.ServiceTypeEntry#getServiceInstance()
	 *
	 * @return
	 * @throws ServiceException
	 */
	@Override
	public Object getServiceInstance() throws ServiceException {
		throw new BadStateException("Service has not started.", ServiceState.error, descriptor );
	}

	/**
	 * 
	 * @see org.eclipse.papyrus.core.services.internal.ServiceTypeEntry#createService()
	 *
	 * @throws ServiceException
	 */
	@Override
	public void createService() throws ServiceException {
		// do nothing
		
	}

	/**
	 * 
	 * @see org.eclipse.papyrus.core.services.internal.ServiceTypeEntry#initService(ServicesRegistry)
	 *
	 * @param servicesRegistry
	 * @throws ServiceException
	 */
	@Override
	public void initService(ServicesRegistry servicesRegistry) throws ServiceException {
		// do nothing
		
	}

	/**
	 * 
	 * @see org.eclipse.papyrus.core.services.internal.ServiceTypeEntry#startService()
	 *
	 * @throws ServiceException
	 */
	@Override
	public void startService() throws ServiceException {
		// do nothing
		
	}

	/**
	 * 
	 * @see org.eclipse.papyrus.core.services.internal.ServiceTypeEntry#disposeService()
	 *
	 * @throws ServiceException
	 */
	@Override
	public void disposeService() throws ServiceException {
		// do nothing
		
	}

}
