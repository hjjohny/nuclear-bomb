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
package org.eclipse.papyrus.properties.uml.expression;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.papyrus.widgets.editors.AbstractEditor;
import org.eclipse.papyrus.widgets.editors.ICommitListener;


public class ExpressionList extends WritableList implements IChangeListener, ICommitListener {

	private List<Expression> expressions;

	private IObservableList languages;

	private IObservableList bodies;

	private boolean ignoreChanges = false;

	public ExpressionList(IObservableList languages, IObservableList bodies) {
		super(new LinkedList<Expression>(), Expression.class);
		expressions = wrappedList;

		this.languages = languages;
		this.bodies = bodies;

		languages.addChangeListener(this);
		bodies.addChangeListener(this);

		UMLToExpression();
	}

	private void UMLToExpression() {
		expressions.clear();
		int maxSize = Math.max(languages.size(), bodies.size());

		Iterator<?> languageIterator = languages.iterator();
		Iterator<?> bodyIterator = bodies.iterator();

		for(int i = 0; i < maxSize; i++) {
			Expression expression = new Expression(this);

			if(languageIterator.hasNext()) {
				expression.language = (String)languageIterator.next();
			}

			if(bodyIterator.hasNext()) {
				expression.body = (String)bodyIterator.next();
			}

			expressions.add(expression);
		}
	}

	public void handleChange(ChangeEvent event) {
		//A change occurs on languages or bodies
		if(!ignoreChanges)
			UMLToExpression();
	}

	public void commit(AbstractEditor editor) {
		ignoreChanges = true;
		expressionToUML();
		if(languages instanceof ICommitListener) {
			((ICommitListener)languages).commit(editor);
		}

		if(bodies instanceof ICommitListener) {
			((ICommitListener)bodies).commit(editor);
		}
		ignoreChanges = false;
	}

	void expressionToUML() { //Executes two commands, one on languages, and another one on bodies
		int fillLanguage = 0, fillBody = 0;

		languages.clear();
		bodies.clear();

		Iterator<Expression> iterator = expressions.iterator();
		while(iterator.hasNext()) {
			Expression expression = iterator.next();
			if(expression == null) {
				iterator.remove();
				continue;
			}

			String language = expression.getLanguage();
			String body = expression.getBody();
			if(language == null && body == null) {
				iterator.remove();
				continue;
			}

			if(language != null && body != null) {
				addLanguage(language, fillLanguage);
				addBody(body, fillBody);

				fillBody = fillLanguage = 0;
			}

			if(language == null) {
				fillLanguage++;
				addBody(body, fillBody);
				fillBody = 0;
			} else if(body == null) {
				fillBody++;
				addLanguage(language, fillLanguage);
				fillLanguage = 0;
			}
		}
	}

	private void addBody(String body, int fillBody) {
		for(int i = 0; i < fillBody; i++) {
			bodies.add(""); //$NON-NLS-1$
		}

		bodies.add(body);
	}

	private void addLanguage(String language, int fillLanguage) {
		for(int i = 0; i < fillLanguage; i++) {
			languages.add(""); //$NON-NLS-1$
		}

		languages.add(language);
	}

	public static class Expression {

		String language;

		String body;

		private ExpressionList owner;

		public Expression() {

		}

		public Expression(ExpressionList owner) {
			this.owner = owner;
		}

		public void setOwner(ExpressionList owner) {
			this.owner = owner;
		}

		public String getLanguage() {
			return language;
		}

		public String getBody() {
			return body;
		}

		public void setLanguage(String language) {
			this.language = language;
		}

		public void setBody(String body) {
			this.body = body;
		}

		@Override
		public String toString() {
			return language;
		}

		@Override
		public int hashCode() {
			return language == null ? 0 : language.hashCode();
		}

		@Override
		public boolean equals(Object other) {
			if(!(other instanceof Expression)) {
				return false;
			}

			Expression otherExpression = (Expression)other;
			if(otherExpression.getLanguage() == null) {
				return getLanguage() == null;
			}

			return otherExpression.getLanguage().equals(getLanguage());
		}
	}
}
