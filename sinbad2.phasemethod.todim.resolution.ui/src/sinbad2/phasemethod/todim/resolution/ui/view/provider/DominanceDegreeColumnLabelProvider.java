package sinbad2.phasemethod.todim.resolution.ui.view.provider;

import org.eclipse.jface.viewers.ColumnLabelProvider;

public class DominanceDegreeColumnLabelProvider extends ColumnLabelProvider {

	@Override
	public String getText(Object element) {
		return ((String[]) element)[3];
	}
	
}
