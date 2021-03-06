/*****************************************************************************
 * Copyright (c) 2011 CEA LIST.
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Camille Letavernier (CEA LIST) camille.letavernier@cea.fr - Initial API and implementation
 *****************************************************************************/
package org.eclipse.papyrus.properties.modelelement;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.papyrus.properties.contexts.Property;
import org.eclipse.papyrus.properties.creation.PropertyEditorFactory;
import org.eclipse.papyrus.properties.runtime.ConfigurationManager;
import org.eclipse.papyrus.widgets.creation.ReferenceValueFactory;
import org.eclipse.papyrus.widgets.providers.EmptyContentProvider;
import org.eclipse.papyrus.widgets.providers.IStaticContentProvider;

/**
 * Provides a default implementation for ModelElement methods applied on the
 * modelElement's properties.
 * 
 * @author Camille Letavernier
 */
public abstract class AbstractModelElement implements ModelElement {

	protected DataSource dataSource;

	public IStaticContentProvider getContentProvider(String propertyPath) {
		return EmptyContentProvider.instance;
	}

	public ILabelProvider getLabelProvider(String propertyPath) {
		return null;
	}

	public boolean isOrdered(String propertyPath) {
		return true;
	}

	public boolean isUnique(String propertyPath) {
		return false;
	}

	public boolean isMandatory(String propertyPath) {
		return false;
	}

	public boolean isEditable(String propertyPath) {
		return true;
	}

	public boolean forceRefresh(String propertyPath) {
		return false;
	}

	public void setDataSource(DataSource source) {
		this.dataSource = source;
	}

	protected Property getProperty(String propertyPath) {
		return ConfigurationManager.instance.getProperty(propertyPath, dataSource.getView().getContext());
	}

	/**
	 * @see org.eclipse.papyrus.properties.modelelement.ModelElement#getValueFactory(java.lang.String)
	 * 
	 * @param propertyPath
	 * @return a default factory based on the property view configuration to
	 *         edit objects, as if they were selected in an editor
	 */
	public ReferenceValueFactory getValueFactory(String propertyPath) {
		return new PropertyEditorFactory();
	}

	public Object getDefaultValue(String propertyPath) {
		return null;
	}

	public boolean getDirectCreation(String propertyPath) {
		return false;
	}

}
