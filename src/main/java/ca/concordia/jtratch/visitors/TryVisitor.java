package ca.concordia.jtratch.visitors;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TryStatement;


public class TryVisitor extends ASTVisitor {

	private static final Logger logger = LogManager.getLogger(TryVisitor.class.getName());
	private CompilationUnit tree;
	
	List<String> tryStatements = new ArrayList<String>();
	
	
	@Override
	public boolean visit(TryStatement node) {
		logger.trace("Visiting a AST node of type "+ node.getNodeType() + " at line " + tree.getLineNumber(node.getStartPosition()));
		
		tryStatements.add(node.toString());
		
		//If a Try node is found, remove it.
		node.delete();
		
		return super.visit(node);
		
	}
	
	public void setTree(CompilationUnit cu) {
		tree = cu;
	}
	public List<String> getTryStatements() {
		return tryStatements;
	}
}
