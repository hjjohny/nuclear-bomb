/*
 * generated by Xtext
 */
package org.eclipse.papyrus.message.editor.xtext.ui;

import org.eclipse.xtext.ui.guice.AbstractGuiceAwareExecutableExtensionFactory;
import org.osgi.framework.Bundle;

import com.google.inject.Injector;

/**
 * This class was generated. Customizations should only happen in a newly
 * introduced subclass. 
 */
public class UmlMessageExecutableExtensionFactory extends AbstractGuiceAwareExecutableExtensionFactory {

	@Override
	protected Bundle getBundle() {
		return org.eclipse.papyrus.message.editor.xtext.ui.internal.UmlMessageActivator.getInstance().getBundle();
	}
	
	@Override
	protected Injector getInjector() {
		return org.eclipse.papyrus.message.editor.xtext.ui.internal.UmlMessageActivator.getInstance().getInjector("org.eclipse.papyrus.message.editor.xtext.UmlMessage");
	}
	
}
