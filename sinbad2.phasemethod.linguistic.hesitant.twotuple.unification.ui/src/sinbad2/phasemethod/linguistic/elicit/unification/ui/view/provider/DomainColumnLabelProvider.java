package sinbad2.phasemethod.linguistic.elicit.unification.ui.view.provider;

import org.eclipse.jface.viewers.ColumnLabelProvider;

public class DomainColumnLabelProvider extends ColumnLabelProvider {
	
	@Override
	public String getText(Object element) {
		if (element instanceof Object[]) {
			return (String) ((Object[]) element)[1];
		} else {
			return null;
		}
	}	
}