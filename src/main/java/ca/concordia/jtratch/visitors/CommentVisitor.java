package ca.concordia.jtratch.visitors;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LineComment;

import ca.concordia.jtratch.utility.IOFile;

public class CommentVisitor extends ASTVisitor{
	private static final Logger logger = LogManager.getLogger(CommentVisitor.class.getName());
	private CompilationUnit tree;
	private String pcatchString;
	
	List<String> toDoComments = new ArrayList<String>();
	List<String> otherComments = new ArrayList<String>();
	
	@Override
	public boolean visit(LineComment node) {
		logger.trace("Visiting a AST node of type "+ node.getNodeType() + " at line " + tree.getLineNumber(node.getStartPosition()));
		
		String updatedComment = IOFile.DeleteSpace(pcatchString);
        
		updatedComment = updatedComment.replaceAll("<.*>", "");
        updatedComment = updatedComment.replaceAll("{.*}", "");
        updatedComment = updatedComment.replaceAll("\\(.*\\)", "");
        updatedComment = updatedComment.toUpperCase();
		
		if (updatedComment.contains("TODO") || updatedComment.contains("FIXME"))
			toDoComments.add(node.toString());
		else
			otherComments.add(node.toString());

		return super.visit(node);
		
	}
	
	@Override
	public boolean visit(BlockComment node) {
		logger.trace("Visiting a AST node of type "+ node.getNodeType() + " at line " + tree.getLineNumber(node.getStartPosition()));
		
		String updatedComment = IOFile.DeleteSpace(pcatchString);
        
		updatedComment = updatedComment.replaceAll("<.*>", "");
        updatedComment = updatedComment.replaceAll("{.*}", "");
        updatedComment = updatedComment.replaceAll("\\(.*\\)", "");
        updatedComment = updatedComment.toUpperCase();
		
		if (updatedComment.contains("TODO") || updatedComment.contains("FIXME"))
			toDoComments.add(node.toString());
		else
			otherComments.add(node.toString());

		return super.visit(node);
		
	}
	
	@Override
	public boolean visit(Javadoc node) {
		logger.trace("Visiting a AST node of type "+ node.getNodeType() + " at line " + tree.getLineNumber(node.getStartPosition()));
		String updatedComment = IOFile.DeleteSpace(pcatchString);
        
		updatedComment = updatedComment.replaceAll("<.*>", "");
        updatedComment = updatedComment.replaceAll("{.*}", "");
        updatedComment = updatedComment.replaceAll("\\(.*\\)", "");
        updatedComment = updatedComment.toUpperCase();
		
		if (updatedComment.contains("TODO") || updatedComment.contains("FIXME"))
			toDoComments.add(node.toString());
		else
			otherComments.add(node.toString());

		return super.visit(node);
		
	}
	public void setTree(CompilationUnit cu) {
		tree = cu;
	}
	public List<String> getToDoComments() {
		return toDoComments;
	}
	public List<String> getOtherComments() {
		return otherComments;
	}

	public void setCatchString(String catchString) {
		pcatchString = catchString;
		
	}
	
	
}
