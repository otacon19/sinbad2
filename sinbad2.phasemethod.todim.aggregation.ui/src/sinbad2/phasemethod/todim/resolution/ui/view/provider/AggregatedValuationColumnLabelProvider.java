package sinbad2.phasemethod.todim.resolution.ui.view.provider;

import org.eclipse.jface.viewers.ColumnLabelProvider;

public class AggregatedValuationColumnLabelProvider extends ColumnLabelProvider {

	@Override
	public String getText(Object element) {
		if (element instanceof Object[]) {
			return (String) ((Object[]) element)[4];
		} else {
			return null;
		}
	}
	
}