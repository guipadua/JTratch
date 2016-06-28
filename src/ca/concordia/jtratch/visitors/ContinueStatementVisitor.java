package ca.concordia.jtratch.visitors;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ContinueStatement;

public class ContinueStatementVisitor extends ASTVisitor{
	private static final Logger logger = LogManager.getLogger(ContinueStatementVisitor.class.getName());
	private CompilationUnit tree;
	
	List<String> continueStatements = new ArrayList<String>();
	
	@Override
	public boolean visit(ContinueStatement node) {
		logger.trace("Visiting a AST node of type "+ node.getNodeType() + " at line " + tree.getLineNumber(node.getStartPosition()));
		
		continueStatements.add(node.toString());

		return super.visit(node);
		
	}
	public void setTree(CompilationUnit cu) {
		tree = cu;
	}
	public List<String> getContinueStatements() {
		return continueStatements;
	}
	
	
}
