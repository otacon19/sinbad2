package sinbad2.method.linguistic.elicit;

import java.util.List;

import sinbad2.domain.Domain;
import sinbad2.domain.DomainSet;
import sinbad2.domain.DomainsManager;
import sinbad2.domain.linguistic.fuzzy.FuzzySet;
import sinbad2.method.MethodImplementation;
import sinbad2.method.state.MethodStateChangeEvent;
import sinbad2.valuation.Valuation;
import sinbad2.valuation.hesitant.HesitantValuation;
import sinbad2.valuation.valuationset.ValuationKey;
import sinbad2.valuation.valuationset.ValuationSet;
import sinbad2.valuation.valuationset.ValuationSetManager;

public class HesitantTwoTuple extends MethodImplementation {

public static final String ID = "flintstones.method.linguistic.hesitant.twotuple"; //$NON-NLS-1$
	
	private static final String NOT_SUPPORTED_DOMAINS = "Not supported domains";
	private static final String NOT_HESITANT_VALUATIONS = "Not hesitant evaluations";
	
	private DomainSet _domainSet;
	private ValuationSet _valuationSet;

	public HesitantTwoTuple() {
		DomainsManager domainsManager = DomainsManager.getInstance();
		_domainSet = domainsManager.getActiveDomainSet();
		
		ValuationSetManager valuationSetManager = ValuationSetManager.getInstance();
		_valuationSet = valuationSetManager.getActiveValuationSet();
	}
	
	@Override
	public MethodImplementation newInstance() {
		return new HesitantTwoTuple();
	}

	@Override
	public String isAvailable() {
		List<Domain> domains = _domainSet.getDomains();
	
		for(Domain d: domains) {
			if(!(d instanceof FuzzySet)) {
				return NOT_SUPPORTED_DOMAINS;
			} else {
				Valuation v;
				for(ValuationKey vk: _valuationSet.getValuations().keySet()) {
					v = _valuationSet.getValuations().get(vk);
					if(!(v instanceof HesitantValuation)) {
						return NOT_HESITANT_VALUATIONS;
					}
				}
			}
		}
		
		return ""; //$NON-NLS-1$
	}
	
	@Override
	public void notifyMethodStateChange(MethodStateChangeEvent event) {}
	
}
