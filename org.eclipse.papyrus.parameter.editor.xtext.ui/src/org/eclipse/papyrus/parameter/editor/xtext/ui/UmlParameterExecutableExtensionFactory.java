/*
 * generated by Xtext
 */
package org.eclipse.papyrus.parameter.editor.xtext.ui;

import org.eclipse.xtext.ui.guice.AbstractGuiceAwareExecutableExtensionFactory;
import org.osgi.framework.Bundle;

import com.google.inject.Injector;

/**
 * This class was generated. Customizations should only happen in a newly
 * introduced subclass. 
 */
public class UmlParameterExecutableExtensionFactory extends AbstractGuiceAwareExecutableExtensionFactory {

	@Override
	protected Bundle getBundle() {
		return org.eclipse.papyrus.parameter.editor.xtext.ui.internal.UmlParameterActivator.getInstance().getBundle();
	}
	
	@Override
	protected Injector getInjector() {
		return org.eclipse.papyrus.parameter.editor.xtext.ui.internal.UmlParameterActivator.getInstance().getInjector("org.eclipse.papyrus.parameter.editor.xtext.UmlParameter");
	}
	
}
