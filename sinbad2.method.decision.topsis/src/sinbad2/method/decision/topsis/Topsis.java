package sinbad2.method.decision.topsis;

import sinbad2.domain.Domain;
import sinbad2.domain.DomainSet;
import sinbad2.domain.DomainsManager;
import sinbad2.domain.linguistic.unbalanced.Unbalanced;
import sinbad2.element.ProblemElementsManager;
import sinbad2.element.ProblemElementsSet;
import sinbad2.element.alternative.Alternative;
import sinbad2.element.criterion.Criterion;
import sinbad2.element.expert.Expert;
import sinbad2.method.MethodImplementation;
import sinbad2.method.state.MethodStateChangeEvent;
import sinbad2.valuation.Valuation;
import sinbad2.valuation.valuationset.ValuationSet;
import sinbad2.valuation.valuationset.ValuationSetManager;

public class Topsis extends MethodImplementation {
	
	public static final String ID = "flintstones.method.decision.topsis";
	
	private static final String NOT_SET_ALL_ASSIGNMENTS = "Not set all assignments";
	private static final String NOT_SUPPORTED_DOMAINS = "Not supported domains";
	
	private DomainSet _domainSet;
	private ProblemElementsSet _elementsSet;
	private ValuationSet _valuationSet;

	public Topsis() {
		DomainsManager domainsManager = DomainsManager.getInstance();
		_domainSet = domainsManager.getActiveDomainSet();
		
		ProblemElementsManager elementsManager = ProblemElementsManager.getInstance();
		_elementsSet = elementsManager.getActiveElementSet();
		
		ValuationSetManager valuationSetManager = ValuationSetManager.getInstance();
		_valuationSet = valuationSetManager.getActiveValuationSet();
	}
	
	@Override
	public MethodImplementation newInstance() {
		return new Topsis();
	}

	@Override
	public void notifyMethodStateChange(MethodStateChangeEvent event) {}
	
	@Override
	public String isAvailable() {
		for(Domain d: _domainSet.getDomains()) {
			if(d instanceof Unbalanced) {
				return NOT_SUPPORTED_DOMAINS;
			}
		}
		
		for(Expert expert : _elementsSet.getAllExperts()) {
			if(!expert.hasChildren()) {
				for(Criterion criterion : _elementsSet.getAllCriteria()) {
					if(!criterion.hasSubcriteria()) {
						for(Alternative alternative : _elementsSet.getAlternatives()) {
							Valuation v = _valuationSet.getValuation(expert, alternative, criterion);
							if(v == null) {
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
