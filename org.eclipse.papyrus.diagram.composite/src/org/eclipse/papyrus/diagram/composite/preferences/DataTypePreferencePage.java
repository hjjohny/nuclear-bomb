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
package org.eclipse.papyrus.diagram.composite.preferences;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.papyrus.diagram.composite.edit.parts.CompositeStructureDiagramEditPart;
import org.eclipse.papyrus.diagram.composite.part.UMLDiagramEditorPlugin;
import org.eclipse.papyrus.preferences.pages.AbstractPapyrusNodePreferencePage;
import org.eclipse.papyrus.preferences.utils.PreferenceConstantHelper;

/**
 * @generated
 */
public class DataTypePreferencePage extends AbstractPapyrusNodePreferencePage {



	/**
	 * @generated
	 */
	public static final String compartments[] = { "properties", "operations" };

	/**
	 * @generated
	 */
	public DataTypePreferencePage() {
		super();
		setPreferenceKey(CompositeStructureDiagramEditPart.MODEL_ID + "_DataType");
	}

	/**
	 * @generated
	 */
	@Override
	protected String getBundleId() {
		return UMLDiagramEditorPlugin.ID;
	}

	/**
	 * @generated
	 */
	public static void initDefaults(IPreferenceStore store) {

		String key = CompositeStructureDiagramEditPart.MODEL_ID + "_DataType";
		store.setDefault(PreferenceConstantHelper.getElementConstant(key, PreferenceConstantHelper.WIDTH), 40);
		store.setDefault(PreferenceConstantHelper.getElementConstant(key, PreferenceConstantHelper.HEIGHT), 40);

		Map<String, Boolean> map = getStaticCompartmentVisibilityPreferences();
		for(String name : map.keySet()) {
			String preferenceName = PreferenceConstantHelper.getLabelElementConstant(key, name, PreferenceConstantHelper.COMPARTMENT_VISIBILITY);
			store.setDefault(preferenceName, map.get(name));
		}

		map = getStaticCompartmentTitleVisibilityPreferences();
		for(String name : map.keySet()) {
			String preferenceName = PreferenceConstantHelper.getLabelElementConstant(key, name, PreferenceConstantHelper.COMPARTMENT_NAME_VISIBILITY);
			store.setDefault(preferenceName, map.get(name));
		}




		//org.eclipse.jface.preference.PreferenceConverter.setDefault(store, org.eclipse.papyrus.preferences.utils.PreferenceConstantHelper.getElementConstant(elementName, org.eclipse.papyrus.preferences.utils.PreferenceConstantHelper.COLOR_FILL), new org.eclipse.swt.graphics.RGB(255, 255, 255));
		//org.eclipse.jface.preference.PreferenceConverter.setDefault(store, org.eclipse.papyrus.preferences.utils.PreferenceConstantHelper.getElementConstant(elementName, org.eclipse.papyrus.preferences.utils.PreferenceConstantHelper.COLOR_LINE), new org.eclipse.swt.graphics.RGB(0, 0, 0));

		// Set the default for the gradient
		//store.setDefault(org.eclipse.papyrus.preferences.utils.PreferenceConstantHelper.getElementConstant(elementName, org.eclipse.papyrus.preferences.utils.PreferenceConstantHelper.GRADIENT_POLICY),false);
		//org.eclipse.papyrus.preferences.utils.GradientPreferenceConverter gradientPreferenceConverter = new  org.eclipse.papyrus.preferences.utils.GradientPreferenceConverter(
		//		new org.eclipse.swt.graphics.RGB(255, 255, 255),
		//		new org.eclipse.swt.graphics.RGB(0, 0, 0), 0, 0);
		//store.setDefault(org.eclipse.papyrus.preferences.utils.PreferenceConstantHelper.getElementConstant(elementName, org.eclipse.papyrus.preferences.utils.PreferenceConstantHelper.COLOR_GRADIENT), gradientPreferenceConverter.getPreferenceValue());

	}

	/**
	 * @generated
	 */
	@Override
	protected void initializeCompartmentsList() {
		for(String name : compartments) {
			this.compartmentsList.add(name);
		}
	}

	/**
	 * @generated
	 */
	private static TreeMap<String, Boolean> getStaticCompartmentVisibilityPreferences() {
		TreeMap<String, Boolean> map = new TreeMap<String, Boolean>();
		map.put("properties", Boolean.TRUE);
		map.put("operations", Boolean.TRUE);
		return map;
	}

	/**
	 * @generated
	 */
	private static TreeMap<String, Boolean> getStaticCompartmentTitleVisibilityPreferences() {
		TreeMap<String, Boolean> map = new TreeMap<String, Boolean>();

		map.put("properties", Boolean.TRUE);

		map.put("operations", Boolean.TRUE);
		return map;
	}

	/**
	 * @generated
	 */
	protected TreeMap<String, Boolean> getCompartmentTitleVisibilityPreferences() {
		return getStaticCompartmentTitleVisibilityPreferences();
	}



}
