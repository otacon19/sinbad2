package sinbad2.phasemethod.emergency.computinggainslosses.ui.view.provider;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class GainsLossesTableContentProvider implements IStructuredContentProvider {
	
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

	@SuppressWarnings("unchecked")
	@Override
	public Object[] getElements(Object inputElement) {
		return ((List<String[]>) inputElement).toArray(new String[0][0]);
	}

	@Override
	public void dispose() {}
	
}
