package sinbad2.phasemethod.multigranular.analysis.ui.view.provider;

import org.eclipse.jface.viewers.LabelProvider;

import sinbad2.element.ProblemElement;

public class FilterLabelProvider extends LabelProvider {

	@Override
	public String getText(Object obj) {
		String result = ((ProblemElement) obj).getId();
		String[] tokens = result.split(":"); //$NON-NLS-1$
		return tokens[tokens.length - 1];
	}
}