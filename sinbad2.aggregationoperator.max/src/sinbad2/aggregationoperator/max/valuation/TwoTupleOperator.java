package sinbad2.aggregationoperator.max.valuation;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import sinbad2.aggregationoperator.max.nls.Messages;
import sinbad2.core.validator.Validator;
import sinbad2.domain.linguistic.fuzzy.FuzzySet;
import sinbad2.valuation.Valuation;
import sinbad2.valuation.twoTuple.TwoTuple;

public class TwoTupleOperator {
	
	private TwoTupleOperator() {}
	
	public static Valuation aggregate(List<Valuation> valuations) {
		Valuation result = null;
		FuzzySet domain = null;
		List<Valuation> values = new LinkedList<Valuation>();
		
		for(Valuation valuation: valuations) {
			Validator.notIllegalElementType(valuation, new String[] {TwoTuple.class.toString()});
			
			if(domain == null) {
				domain = (FuzzySet) valuation.getDomain();
			} else if(!domain.equals(valuation.getDomain())) {
				throw new IllegalArgumentException(Messages.TwoTupleOperator_Invalid_domain);
			}
			
			values.add(valuation);
		}
		
		if(domain != null) {
			Collections.sort(values);
			result = values.get(values.size() - 1);
			result = (Valuation) result.clone();
		}
		
		return result;
	} 

}
