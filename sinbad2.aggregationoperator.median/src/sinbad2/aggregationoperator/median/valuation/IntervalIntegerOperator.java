package sinbad2.aggregationoperator.median.valuation;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import sinbad2.core.validator.Validator;
import sinbad2.domain.numeric.integer.NumericIntegerDomain;
import sinbad2.valuation.Valuation;
import sinbad2.valuation.integer.interval.IntegerInterval;

public class IntervalIntegerOperator {
	
	private IntervalIntegerOperator() {}
	
	public static Valuation aggregate(List<Valuation> valuations) {
		Valuation result = null;
		NumericIntegerDomain domain = null;
		List<Valuation> values = new LinkedList<Valuation>();
		
		for(Valuation valuation: valuations) {
			Validator.notIllegalElementType(valuation, new String[] {IntegerInterval.class.toString()});
			
			if(domain == null) {
				domain = (NumericIntegerDomain) valuation.getDomain();
			} else if(!domain.equals(valuation.getDomain())) {
				throw new IllegalArgumentException("Invalid domain");
			}
			
			values.add(valuation);
		}
		
		if(!values.isEmpty()) {
			int median = values.size() / 2;
			Collections.sort(values);
			result = values.get(median);
			result = (Valuation) result.clone();
			
			if((values.size() % 2) == 0) {
				Valuation auxValuation = values.get(median - 1);
				long minValue = (((IntegerInterval) result).getMin()) + (((IntegerInterval) auxValuation).getMin());
				long maxValue = (((IntegerInterval) result).getMax()) + (((IntegerInterval) auxValuation).getMax());
				minValue /= 2;
				maxValue /= 2;
				((IntegerInterval) result).setMinMax(minValue, maxValue);
			}
		}
		
		return result;
	}
}