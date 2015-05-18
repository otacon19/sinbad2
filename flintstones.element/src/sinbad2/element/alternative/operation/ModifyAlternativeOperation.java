package sinbad2.element.alternative.operation;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import sinbad2.core.undoable.UndoableOperation;
import sinbad2.element.ProblemElementsSet;
import sinbad2.element.alternative.Alternative;

public class ModifyAlternativeOperation extends UndoableOperation {
	
	private ProblemElementsSet _elementSet;
	private Alternative _modifyAlternative;
	private String _newIdAlternative;
	private String _oldIdAlternative;

	public ModifyAlternativeOperation(String label, ProblemElementsSet elementSet, Alternative modifyAlternative, String newIdAlternative) {
		super(label);
		
		_elementSet = elementSet;
		_modifyAlternative = modifyAlternative;
		_newIdAlternative = newIdAlternative;
		_oldIdAlternative = modifyAlternative.getId();
	}

	@Override
	public IStatus executeOperation(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return redo(monitor, info);
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		
		_elementSet.modifyAlternative(_modifyAlternative, _newIdAlternative);
		
		return Status.OK_STATUS;
		
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		
		_elementSet.modifyAlternative(_modifyAlternative, _oldIdAlternative);
		
		return Status.OK_STATUS;
		
	}

}
