package sinbad2.domain.operation;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import sinbad2.core.undoable.UndoableOperation;
import sinbad2.domain.Domain;
import sinbad2.domain.DomainSet;

public class AddDomainOperation extends UndoableOperation {
	
	private DomainSet _domainSet;
	private Domain _addDomain;

	public AddDomainOperation(String label, Domain addDomain, DomainSet domainSet) {
		super(label);
		
		_domainSet = domainSet;
		_addDomain = addDomain;
	}

	@Override
	public IStatus executeOperation(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return redo(monitor, info);
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		
		_domainSet.addDomain(_addDomain, _inUndoRedo);
		
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		
		_domainSet.removeDomain(_addDomain, _inUndoRedo);
		
		return Status.OK_STATUS;
	}


	
}
