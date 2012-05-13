/*
* generated by Xtext
*/
grammar InternalUmlCollaborationUse;

options {
	superClass=AbstractInternalAntlrParser;
	
}

@lexer::header {
package org.eclipse.papyrus.collaborationuse.editor.xtext.parser.antlr.internal;

// Hack: Use our own Lexer superclass by means of import. 
// Currently there is no other way to specify the superclass for the lexer.
import org.eclipse.xtext.parser.antlr.Lexer;
}

@parser::header {
package org.eclipse.papyrus.collaborationuse.editor.xtext.parser.antlr.internal; 

import org.eclipse.xtext.*;
import org.eclipse.xtext.parser.*;
import org.eclipse.xtext.parser.impl.*;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.xtext.parser.antlr.AbstractInternalAntlrParser;
import org.eclipse.xtext.parser.antlr.XtextTokenStream;
import org.eclipse.xtext.parser.antlr.XtextTokenStream.HiddenTokens;
import org.eclipse.xtext.parser.antlr.AntlrDatatypeRuleToken;
import org.eclipse.papyrus.collaborationuse.editor.xtext.services.UmlCollaborationUseGrammarAccess;

}

@parser::members {

 	private UmlCollaborationUseGrammarAccess grammarAccess;
 	
    public InternalUmlCollaborationUseParser(TokenStream input, UmlCollaborationUseGrammarAccess grammarAccess) {
        this(input);
        this.grammarAccess = grammarAccess;
        registerRules(grammarAccess.getGrammar());
    }
    
    @Override
    protected String getFirstRuleName() {
    	return "CollaborationUseRule";	
   	}
   	
   	@Override
   	protected UmlCollaborationUseGrammarAccess getGrammarAccess() {
   		return grammarAccess;
   	}
}

@rulecatch { 
    catch (RecognitionException re) { 
        recover(input,re); 
        appendSkippedTokens();
    } 
}




// Entry rule entryRuleCollaborationUseRule
entryRuleCollaborationUseRule returns [EObject current=null] 
	:
	{ newCompositeNode(grammarAccess.getCollaborationUseRuleRule()); }
	 iv_ruleCollaborationUseRule=ruleCollaborationUseRule 
	 { $current=$iv_ruleCollaborationUseRule.current; } 
	 EOF 
;

// Rule CollaborationUseRule
ruleCollaborationUseRule returns [EObject current=null] 
    @init { enterRule(); 
    }
    @after { leaveRule(); }:
((
(
		{ 
	        newCompositeNode(grammarAccess.getCollaborationUseRuleAccess().getVisibilityVisibilityKindEnumRuleCall_0_0()); 
	    }
		lv_visibility_0_0=ruleVisibilityKind		{
	        if ($current==null) {
	            $current = createModelElementForParent(grammarAccess.getCollaborationUseRuleRule());
	        }
       		set(
       			$current, 
       			"visibility",
        		lv_visibility_0_0, 
        		"VisibilityKind");
	        afterParserOrEnumRuleCall();
	    }

)
)(
(
		lv_name_1_0=RULE_ID
		{
			newLeafNode(lv_name_1_0, grammarAccess.getCollaborationUseRuleAccess().getNameIDTerminalRuleCall_1_0()); 
		}
		{
	        if ($current==null) {
	            $current = createModelElement(grammarAccess.getCollaborationUseRuleRule());
	        }
       		setWithLastConsumed(
       			$current, 
       			"name",
        		lv_name_1_0, 
        		"ID");
	    }

)
)	otherlv_2=':' 
    {
    	newLeafNode(otherlv_2, grammarAccess.getCollaborationUseRuleAccess().getColonKeyword_2());
    }
((
(
		{ 
	        newCompositeNode(grammarAccess.getCollaborationUseRuleAccess().getTypeTypeRuleParserRuleCall_3_0_0()); 
	    }
		lv_type_3_0=ruleTypeRule		{
	        if ($current==null) {
	            $current = createModelElementForParent(grammarAccess.getCollaborationUseRuleRule());
	        }
       		set(
       			$current, 
       			"type",
        		lv_type_3_0, 
        		"TypeRule");
	        afterParserOrEnumRuleCall();
	    }

)
)
    |	otherlv_4='<Undefined>' 
    {
    	newLeafNode(otherlv_4, grammarAccess.getCollaborationUseRuleAccess().getUndefinedKeyword_3_1());
    }
))
;





// Entry rule entryRuleTypeRule
entryRuleTypeRule returns [EObject current=null] 
	:
	{ newCompositeNode(grammarAccess.getTypeRuleRule()); }
	 iv_ruleTypeRule=ruleTypeRule 
	 { $current=$iv_ruleTypeRule.current; } 
	 EOF 
;

// Rule TypeRule
ruleTypeRule returns [EObject current=null] 
    @init { enterRule(); 
    }
    @after { leaveRule(); }:
((
(
		{ 
	        newCompositeNode(grammarAccess.getTypeRuleAccess().getPathQualifiedNameParserRuleCall_0_0()); 
	    }
		lv_path_0_0=ruleQualifiedName		{
	        if ($current==null) {
	            $current = createModelElementForParent(grammarAccess.getTypeRuleRule());
	        }
       		set(
       			$current, 
       			"path",
        		lv_path_0_0, 
        		"QualifiedName");
	        afterParserOrEnumRuleCall();
	    }

)
)?(
(
		{
			if ($current==null) {
	            $current = createModelElement(grammarAccess.getTypeRuleRule());
	        }
        }
	otherlv_1=RULE_ID
	{
		newLeafNode(otherlv_1, grammarAccess.getTypeRuleAccess().getTypeCollaborationCrossReference_1_0()); 
	}

)
))
;





// Entry rule entryRuleQualifiedName
entryRuleQualifiedName returns [EObject current=null] 
	:
	{ newCompositeNode(grammarAccess.getQualifiedNameRule()); }
	 iv_ruleQualifiedName=ruleQualifiedName 
	 { $current=$iv_ruleQualifiedName.current; } 
	 EOF 
;

// Rule QualifiedName
ruleQualifiedName returns [EObject current=null] 
    @init { enterRule(); 
    }
    @after { leaveRule(); }:
((
(
		{
			if ($current==null) {
	            $current = createModelElement(grammarAccess.getQualifiedNameRule());
	        }
        }
	otherlv_0=RULE_ID
	{
		newLeafNode(otherlv_0, grammarAccess.getQualifiedNameAccess().getPathNamespaceCrossReference_0_0()); 
	}

)
)	otherlv_1='::' 
    {
    	newLeafNode(otherlv_1, grammarAccess.getQualifiedNameAccess().getColonColonKeyword_1());
    }
(
(
		{ 
	        newCompositeNode(grammarAccess.getQualifiedNameAccess().getRemainingQualifiedNameParserRuleCall_2_0()); 
	    }
		lv_remaining_2_0=ruleQualifiedName		{
	        if ($current==null) {
	            $current = createModelElementForParent(grammarAccess.getQualifiedNameRule());
	        }
       		set(
       			$current, 
       			"remaining",
        		lv_remaining_2_0, 
        		"QualifiedName");
	        afterParserOrEnumRuleCall();
	    }

)
)?)
;







// Entry rule entryRuleBoundSpecification
entryRuleBoundSpecification returns [EObject current=null] 
	:
	{ newCompositeNode(grammarAccess.getBoundSpecificationRule()); }
	 iv_ruleBoundSpecification=ruleBoundSpecification 
	 { $current=$iv_ruleBoundSpecification.current; } 
	 EOF 
;

// Rule BoundSpecification
ruleBoundSpecification returns [EObject current=null] 
    @init { enterRule(); 
    }
    @after { leaveRule(); }:
(
(
		lv_value_0_0=RULE_UNLIMITEDLITERAL
		{
			newLeafNode(lv_value_0_0, grammarAccess.getBoundSpecificationAccess().getValueUnlimitedLiteralTerminalRuleCall_0()); 
		}
		{
	        if ($current==null) {
	            $current = createModelElement(grammarAccess.getBoundSpecificationRule());
	        }
       		setWithLastConsumed(
       			$current, 
       			"value",
        		lv_value_0_0, 
        		"UnlimitedLiteral");
	    }

)
)
;





// Rule VisibilityKind
ruleVisibilityKind returns [Enumerator current=null] 
    @init { enterRule(); }
    @after { leaveRule(); }:
((	enumLiteral_0='+' 
	{
        $current = grammarAccess.getVisibilityKindAccess().getPublicEnumLiteralDeclaration_0().getEnumLiteral().getInstance();
        newLeafNode(enumLiteral_0, grammarAccess.getVisibilityKindAccess().getPublicEnumLiteralDeclaration_0()); 
    }
)
    |(	enumLiteral_1='-' 
	{
        $current = grammarAccess.getVisibilityKindAccess().getPrivateEnumLiteralDeclaration_1().getEnumLiteral().getInstance();
        newLeafNode(enumLiteral_1, grammarAccess.getVisibilityKindAccess().getPrivateEnumLiteralDeclaration_1()); 
    }
)
    |(	enumLiteral_2='#' 
	{
        $current = grammarAccess.getVisibilityKindAccess().getProtectedEnumLiteralDeclaration_2().getEnumLiteral().getInstance();
        newLeafNode(enumLiteral_2, grammarAccess.getVisibilityKindAccess().getProtectedEnumLiteralDeclaration_2()); 
    }
)
    |(	enumLiteral_3='~' 
	{
        $current = grammarAccess.getVisibilityKindAccess().getPackageEnumLiteralDeclaration_3().getEnumLiteral().getInstance();
        newLeafNode(enumLiteral_3, grammarAccess.getVisibilityKindAccess().getPackageEnumLiteralDeclaration_3()); 
    }
));





RULE_UNLIMITEDLITERAL : ('0'..'9' ('0'..'9')*|'*');

RULE_ID : '^'? ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'_'|'0'..'9')*;

RULE_INT : ('0'..'9')+;

RULE_STRING : ('"' ('\\' ('b'|'t'|'n'|'f'|'r'|'u'|'"'|'\''|'\\')|~(('\\'|'"')))* '"'|'\'' ('\\' ('b'|'t'|'n'|'f'|'r'|'u'|'"'|'\''|'\\')|~(('\\'|'\'')))* '\'');

RULE_ML_COMMENT : '/*' ( options {greedy=false;} : . )*'*/';

RULE_SL_COMMENT : '//' ~(('\n'|'\r'))* ('\r'? '\n')?;

RULE_WS : (' '|'\t'|'\r'|'\n')+;

RULE_ANY_OTHER : .;


