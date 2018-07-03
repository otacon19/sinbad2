package sinbad2.aggregationoperator.fuzzy.min.valuation;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import sinbad2.core.validator.Validator;
import sinbad2.domain.linguistic.fuzzy.FuzzySet;
import sinbad2.domain.linguistic.fuzzy.function.types.TrapezoidalFunction;
import sinbad2.valuation.Valuation;
import sinbad2.valuation.hesitant.twoTuple.HesitantTwoTupleValuation;

public class HesitantTwoTupleOperator {

private HesitantTwoTupleOperator() {}
	
	public static Valuation aggregate(List<Valuation> valuations) {
		HesitantTwoTupleValuation result = null;
		List<Double> a = new LinkedList<Double>(), b = new LinkedList<Double>(), c = new LinkedList<Double>(), d = new LinkedList<Double>();
		double limits[] = new double[4];
		FuzzySet domain = null;
		
		TrapezoidalFunction tpf;

		for(Valuation valuation : valuations) {
			Validator.notIllegalElementType(valuation, new String[] { HesitantTwoTupleValuation.class.toString() });

			if(domain == null) {
				domain = (FuzzySet) valuation.getDomain();
			} else if(!domain.equals(valuation.getDomain())) {
				throw new IllegalArgumentException("Invalid domain");
			}
			
			if(((HesitantTwoTupleValuation) valuation).getBeta() == null) {
				tpf = ((HesitantTwoTupleValuation) valuation).calculateFuzzyEnvelopeEquivalentCLE(domain);
			} else {
				tpf = ((HesitantTwoTupleValuation) valuation).getBeta();
			}
				 
			limits = tpf.getLimits();
			a.add(limits[0]);
			b.add(limits[1]);
			c.add(limits[2]);
			d.add(limits[3]);
		}
		
		Collections.sort(a);
		Collections.sort(b);
		Collections.sort(c);
		Collections.sort(d);
		
		double mina = a.get(0);
		double minb = b.get(0);
		double minc = c.get(0);
		double mind = d.get(0);
		
		result = (HesitantTwoTupleValuation) valuations.get(0).clone();
		result.createRelation(new TrapezoidalFunction(new double[]{mina, minb, minc, mind}));
		

		return result;
	}
	
}
