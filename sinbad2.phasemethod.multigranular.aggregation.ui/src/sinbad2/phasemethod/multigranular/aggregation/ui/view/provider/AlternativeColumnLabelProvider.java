package sinbad2.phasemethod.multigranular.aggregation.ui.view.provider;

import org.eclipse.jface.viewers.ColumnLabelProvider;

public class AlternativeColumnLabelProvider extends ColumnLabelProvider {

	@Override
	public String getText(Object element) {
		return (String) ((Object[]) element)[1];
	}
}
