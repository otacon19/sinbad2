package sinbad2.aggregationoperator.median;

import java.util.LinkedList;
import java.util.List;

import sinbad2.aggregationoperator.UnweightedAggregationOperator;
import sinbad2.aggregationoperator.median.nls.Messages;
import sinbad2.aggregationoperator.median.valuation.IntegerOperator;
import sinbad2.aggregationoperator.median.valuation.IntervalIntegerOperator;
import sinbad2.aggregationoperator.median.valuation.IntervalRealOperator;
import sinbad2.aggregationoperator.median.valuation.RealOperator;
import sinbad2.aggregationoperator.median.valuation.TwoTupleOperator;
import sinbad2.aggregationoperator.median.valuation.UnifiedValuationOperator;
import sinbad2.core.validator.Validator;
import sinbad2.valuation.Valuation;
import sinbad2.valuation.integer.IntegerValuation;
import sinbad2.valuation.integer.interval.IntegerIntervalValuation;
import sinbad2.valuation.real.RealValuation;
import sinbad2.valuation.real.interval.RealIntervalValuation;
import sinbad2.valuation.twoTuple.TwoTuple;
import sinbad2.valuation.unifiedValuation.UnifiedValuation;

public class Median extends UnweightedAggregationOperator {

	@Override
	public Valuation aggregate(List<Valuation> valuations) {
		Validator.notNull(valuations);
		
		List<Valuation> auxValuations = new LinkedList<Valuation>();
		for (Valuation valuation : valuations) {
			if (valuation != null) {
				auxValuations.add(valuation);
			}
		}

		if (auxValuations.size() != valuations.size()) {
			valuations = auxValuations;
		}
		
		if(valuations.size() > 0) {
			for(Valuation valuation: valuations) {
				if(valuation instanceof IntegerValuation) {
					return IntegerOperator.aggregate(valuations);
				} else if(valuation instanceof RealValuation) {
					return RealOperator.aggregate(valuations);
				} else if(valuation instanceof IntegerIntervalValuation){
					return IntervalIntegerOperator.aggregate(valuations);
				} else if(valuation instanceof RealIntervalValuation) {
					return IntervalRealOperator.aggregate(valuations);
				} else if(valuation instanceof TwoTuple) {
					return TwoTupleOperator.aggregate(valuations);
				}  else if(valuation instanceof UnifiedValuation) {
					return UnifiedValuationOperator.aggregate(valuations);
				} else {
					throw new IllegalArgumentException(Messages.Median_Not_supported_type);
				}
			}
		}
		
		return null;
			
	}
}
