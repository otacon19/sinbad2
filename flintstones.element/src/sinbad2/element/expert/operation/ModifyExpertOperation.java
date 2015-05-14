package sinbad2.element.expert.operation;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import sinbad2.element.ProblemElementsSet;
import sinbad2.element.expert.Expert;
import sinbad2.element.expert.listener.EExpertsChange;
import sinbad2.element.expert.listener.ExpertsChangeEvent;

public class ModifyExpertOperation extends AbstractOperation {
	
	private ProblemElementsSet _elementSet;
	private Expert _modifyExpert;
	private List<Expert> _brothers;
	private List<Expert> _childrens;
	private String _newIdExpert;
	private String _oldIdExpert;

	public ModifyExpertOperation(String label, Expert expert, String newId, ProblemElementsSet elementSet) {
		super(label);
		
		_elementSet = elementSet;
		_modifyExpert = expert;
		_newIdExpert = newId;
		_oldIdExpert = expert.getId();
		
		_childrens = new LinkedList<Expert>();
		_brothers = new LinkedList<Expert>();
		
		Expert parent = _modifyExpert.getParent();
		if(parent != null) {
			_childrens = parent.getChildrens();
		} else {
			_brothers = elementSet.getExperts();
		}
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		
		return redo(monitor, info);
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		
		Expert oldExpert = (Expert) _modifyExpert.clone();
		
		_modifyExpert.setId(_newIdExpert);
		
		if(_childrens.size() == 0) {
			Collections.sort(_brothers);
		} else {
			Collections.sort(_childrens);
		}
		
		notify(EExpertsChange.MODIFY_EXPERT, oldExpert, _modifyExpert);
		
		return Status.OK_STATUS;

	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		
		Expert oldExpert = (Expert) _modifyExpert.clone();
		
		_modifyExpert.setId(_oldIdExpert);
		
		if(_childrens.size() == 0) {
			Collections.sort(_brothers);
		} else {
			Collections.sort(_childrens);
		}
		
		notify(EExpertsChange.MODIFY_EXPERT, oldExpert, _modifyExpert);
		
		return Status.OK_STATUS;
		
	}
	
	public void notify(EExpertsChange change, Object oldValue, Object newValue) {
		_elementSet.notifyExpertsChanges(new ExpertsChangeEvent(change, oldValue, newValue));
	}

}