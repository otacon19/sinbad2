package sinbad2.method.linguistic.heterogeneous.fusion;

import sinbad2.element.ProblemElementsManager;
import sinbad2.element.ProblemElementsSet;
import sinbad2.element.alternative.Alternative;
import sinbad2.element.criterion.Criterion;
import sinbad2.element.expert.Expert;
import sinbad2.method.MethodImplementation;
import sinbad2.method.state.MethodStateChangeEvent;
import sinbad2.valuation.valuationset.ValuationSet;
import sinbad2.valuation.valuationset.ValuationSetManager;

public class Fusion extends MethodImplementation {
	
	public static final String ID = "flintstones.method.linguistic.heterogeneous.fusion";
	
	private static final String NOT_SET_ALL_ASSIGNMENTS = "Not set all assignments";
	
	private ProblemElementsSet _elementsSet;
	private ValuationSet _valuationSet;
	
	public Fusion() {
		ProblemElementsManager elementsManager = ProblemElementsManager.getInstance();
		_elementsSet = elementsManager.getActiveElementSet();
		
		ValuationSetManager valuationSetManager = ValuationSetManager.getInstance();
		_valuationSet = valuationSetManager.getActiveValuationSet();
	}
	
	@Override
	public MethodImplementation newInstance() {
		return new Fusion();
	}

	@Override
	public void notifyMethodStateChange(MethodStateChangeEvent event) {}

	@Override
	public String isAvailable() {
		for(Expert expert : _elementsSet.getExperts()) {
			for(Criterion criterion : _elementsSet.getCriteria()) {
				for(Alternative alternative : _elementsSet.getAlternatives()) {
					if(_valuationSet.getValuation(expert, alternative, criterion) == null) {
						return NOT_SET_ALL_ASSIGNMENTS;
					}
				}
			}
		}
		return "";
	}
}