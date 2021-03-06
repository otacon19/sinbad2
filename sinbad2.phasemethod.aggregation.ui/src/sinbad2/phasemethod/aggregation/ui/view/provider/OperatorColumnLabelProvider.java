package sinbad2.phasemethod.aggregation.ui.view.provider;

import org.eclipse.jface.viewers.ColumnLabelProvider;

import sinbad2.aggregationoperator.AggregationOperator;
import sinbad2.element.ProblemElement;
import sinbad2.phasemethod.aggregation.AggregationPhase;
import sinbad2.phasemethod.aggregation.ui.nls.Messages;

public class OperatorColumnLabelProvider extends ColumnLabelProvider {

	private AggregationPhase _aggregationPhase;
	private String _type;
	
	public OperatorColumnLabelProvider(AggregationPhase aggregationPhase, String type) {
		_aggregationPhase = aggregationPhase;
		_type = type;
	}
	
	@Override
	public String getText(Object element) {
		AggregationOperator operator;
		ProblemElement problemElement = null;
		
		if (element instanceof ProblemElement) {
			problemElement = (ProblemElement) element;
		}
		if (AggregationPhase.EXPERTS.equals(_type)) {
			operator = _aggregationPhase.getExpertOperator(problemElement);
		} else {
			operator = _aggregationPhase.getCriterionOperator(problemElement);			
		}
		
		if (operator == null) {
			return Messages.OperatorColumnLabelProvider_Unassigned;
		} else {
			String name = operator.getName();
			
			return name;							
		}
	}
}


