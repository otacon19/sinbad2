package sinbad2.method.linguistic.unbalanced.methodology;

import sinbad2.element.ProblemElementsManager;
import sinbad2.element.ProblemElementsSet;
import sinbad2.element.alternative.Alternative;
import sinbad2.element.criterion.Criterion;
import sinbad2.element.expert.Expert;
import sinbad2.method.MethodImplementation;
import sinbad2.method.state.MethodStateChangeEvent;
import sinbad2.valuation.valuationset.ValuationSet;
import sinbad2.valuation.valuationset.ValuationSetManager;

public class Unbalanced extends MethodImplementation {
	
	public static final String ID = "flintstones.method.linguistic.unbalanced.methodology";
	
	private static final String EVALUATIONS_IN_NOT_LINGUISTIC_DOMAINS = "Evaluations iN not linguistic domains";
	private static final String EVALUATIONS_IN_NOT_UNBALANCED_DOMAIN = "Evaluations in not unbalanced domain";
	private static final String EVALUATIONS_IN_DIFFERENT_DOMAINS = "Evaluations in different domains";
	private static final String NOT_SET_ALL_ASSIGNMENTS = "Not set all assignemnts";
	
	private ProblemElementsSet _elementsSet;
	private ValuationSet _valuationSet;
	
	public Unbalanced() {
		ProblemElementsManager elementsManager = ProblemElementsManager.getInstance();
		_elementsSet = elementsManager.getActiveElementSet();
		
		ValuationSetManager valuationSetManager = ValuationSetManager.getInstance();
		_valuationSet = valuationSetManager.getActiveValuationSet();
	}

	@Override
	public void notifyMethodStateChange(MethodStateChangeEvent event) {}

	@Override
	public MethodImplementation newInstance() {
		return new Unbalanced();
	}

	@Override
	public String isAvailable() {
		sinbad2.domain.linguistic.unbalanced.Unbalanced generateDomain = null;
		String domainName = null;
		
		for(Expert expert : _elementsSet.getExperts()) {
			if(_elementsSet.getElementExpertChildren(expert).size() == 0) {
				for(Criterion criterion : _elementsSet.getCriteria()) {
					if(_elementsSet.getElementCriterionSubcriteria(criterion).size() == 0) {
						for(Alternative alternative : _elementsSet.getAlternatives()) {
							generateDomain = (sinbad2.domain.linguistic.unbalanced.Unbalanced) _valuationSet.getValuation(expert, alternative, criterion).getDomain();
							if(generateDomain != null) {
								domainName = generateDomain.getId();
								if(domainName == null) {
									if(!(generateDomain instanceof sinbad2.domain.linguistic.unbalanced.Unbalanced)) {
										return EVALUATIONS_IN_NOT_LINGUISTIC_DOMAINS;
									} else {
										if (generateDomain.getInfo() == null) {
											return EVALUATIONS_IN_NOT_UNBALANCED_DOMAIN;
										}
									}
								} else {
									if (!domainName.equals((String) generateDomain.getId())) {
										return EVALUATIONS_IN_DIFFERENT_DOMAINS;
									}
								}
							} else {
								return NOT_SET_ALL_ASSIGNMENTS;
							}
						}
					}
				}
			}
		}

		return "";
	}
}
