package sinbad2.aggregationoperator.weightedmean.valuation;

import java.util.LinkedList;
import java.util.List;

import sinbad2.aggregationoperator.weightedmean.nls.Messages;
import sinbad2.core.validator.Validator;
import sinbad2.domain.numeric.integer.NumericIntegerDomain;
import sinbad2.valuation.Valuation;
import sinbad2.valuation.integer.IntegerValuation;

public class IntegerOperator {
	
	private IntegerOperator() {}
	
	public static Valuation aggregate(List<Valuation> valuations, List<Double> weights) {
		IntegerValuation result = null;
		double measure = 0;
		List<Double> measures = new LinkedList<Double>();
		NumericIntegerDomain domain = null;

		for(Valuation valuation : valuations) {

			Validator.notIllegalElementType(valuation, new String[] { IntegerValuation.class.toString() });

			if (domain == null) {
				domain = (NumericIntegerDomain) valuation.getDomain();
			} else if (!domain.equals(valuation.getDomain())) {
				throw new IllegalArgumentException(Messages.IntegerOperator_Invalid_domain);
			}

			measures.add((double) ((IntegerValuation) valuation).getValue());
		}

		if (domain != null) {
			int size = measures.size();
			for (int i = 0; i < size; i++) {
				measure += weights.get(i) * measures.get(i);
			}

			result = (IntegerValuation) valuations.get(0).clone();
			result.setValue(measure);
		}

		return result;

	}
	
}
