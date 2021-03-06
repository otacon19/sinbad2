package sinbad2.aggregationoperator.harmonic.valuation;

import java.util.List;

import sinbad2.core.validator.Validator;
import sinbad2.domain.linguistic.fuzzy.FuzzySet;
import sinbad2.valuation.Valuation;
import sinbad2.valuation.twoTuple.TwoTuple;

public class TwoTupleOperator {

	private TwoTupleOperator() {}
	
	public static Valuation aggregate(List<Valuation> valuations) {
		TwoTuple result = null;
		double beta = 0, aux =0;
		FuzzySet domain = null;
	
		for(Valuation valuation : valuations) {
			Validator.notIllegalElementType(valuation, new String[] { TwoTuple.class.toString() });

			if(domain == null) {
				domain = (FuzzySet) valuation.getDomain();
			} else if(!domain.equals(valuation.getDomain())) {
				throw new IllegalArgumentException("Invalid domain");
			}

			beta += 1d / ((TwoTuple) valuation).calculateInverseDelta();
		}

		aux = valuations.size() / beta;
		beta = aux;
		
		if (domain != null) {
			result = (TwoTuple) valuations.get(0).clone();
			result.calculateDelta(beta);
		}

		return result;
	}
}
