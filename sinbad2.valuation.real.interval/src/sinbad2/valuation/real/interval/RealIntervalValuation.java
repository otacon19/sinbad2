package sinbad2.valuation.real.interval;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import sinbad2.core.validator.Validator;
import sinbad2.domain.Domain;
import sinbad2.domain.DomainsManager;
import sinbad2.domain.linguistic.fuzzy.FuzzySet;
import sinbad2.domain.linguistic.fuzzy.semantic.IMembershipFunction;
import sinbad2.domain.numeric.real.NumericRealDomain;
import sinbad2.resolutionphase.io.XMLRead;
import sinbad2.valuation.Valuation;
import sinbad2.valuation.ValuationsManager;
import sinbad2.valuation.real.interval.nls.Messages;

public class RealIntervalValuation extends Valuation {
	
	public static final String ID = "flintstones.valuation.real.interval"; //$NON-NLS-1$

	public double _min;
	public double _max;
	
	public RealIntervalValuation() {
		super();
		_min = 0;
		_max = 0;
	}
	
	public RealIntervalValuation(NumericRealDomain domain, double min, double max) {
		super();
		_domain = domain;
		_min = min;
		_max = max;
	}
	
	public void setMin(Double min) {
		_min = min;
	}
	
	public double getMin() {
		return _min;
	}
	
	public void setMax(Double max) {
		_max = max;
	}
	
	public double getMax() {
		return _max;
	}
	
	public void setMinMax(Double min, Double max) {
		Validator.notNull(_domain);
		Validator.notDisorder(new double[] { min, max }, false);
		
		_min = min;
		_max = max;
	}
	
	public Valuation normalize() {
		ValuationsManager valuationsManager = ValuationsManager.getInstance();
		RealIntervalValuation result = (RealIntervalValuation) valuationsManager.copyValuation(ID);
		
		DomainsManager domainsManager = DomainsManager.getInstance();
		NumericRealDomain domain = (NumericRealDomain) domainsManager.copyDomain(NumericRealDomain.ID);
		
		domain.setMinMax(((NumericRealDomain) _domain).getMin(), ((NumericRealDomain) _domain).getMax());
		domain.setInRange(((NumericRealDomain) _domain).getInRange());
		
		result.setDomain(domain);
		result.setMinMax(_min, _max);
		
		return normalizeInterval(result);
	}
	
	
	public Valuation normalizeInterval(RealIntervalValuation result) {
		double min, max, intervalSize;
		
		min = ((NumericRealDomain) _domain).getMin();
		max = ((NumericRealDomain) _domain).getMax();
		intervalSize = max - min;
		
		double maxNormalized = (_max - min) / intervalSize;
		double minNormalized = (_min - min) / intervalSize;
		
		((NumericRealDomain) result._domain).setMinMax(0d, 1d);
		
		result._min = minNormalized;
		result._max = maxNormalized;
		
		return result;
	}
	
	@Override
	public Valuation negateValuation() {
		RealIntervalValuation result = (RealIntervalValuation) clone();
		
		double aux = ((NumericRealDomain) _domain).getMin() + ((NumericRealDomain) _domain).getMax();
		result.setMinMax(aux - _max, aux - _min);
		
		return result;
	}
	
	@Override
	public FuzzySet unification(Domain fuzzySet) {

		Validator.notNull(fuzzySet);

		if (!((FuzzySet) fuzzySet).isBLTS()) {
			throw new IllegalArgumentException(Messages.RealIntervalValuation_Not_BLTS_fuzzy_set);
		}
		
		int cardinality;
		RealIntervalValuation normalized;
		IMembershipFunction function;

		FuzzySet result = (FuzzySet) fuzzySet.clone();
		cardinality = ((FuzzySet) fuzzySet).getLabelSet().getCardinality();
		normalized = (RealIntervalValuation) normalize();

		for (int i = 0; i < cardinality; i++) {
			function = result.getLabelSet().getLabel(i).getSemantic();
			result.setValue(i, function.maxMin(normalized.getMax(), normalized.getMin()));
		}

		return result;
	}
	
	@Override
	public String toString() {
		return ("Real interval[" + _min + "," + _max + "] in" + _domain.toString()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if(this == obj) {
			return true;
		}
		
		if(obj == null) {
			return false;
		}
			
		if(obj.getClass() != this.getClass()) {
			return false;
		}
		
		final RealIntervalValuation other = (RealIntervalValuation) obj;
		
		EqualsBuilder eb = new EqualsBuilder();
		eb.append(_max, other._max);
		eb.append(_min, other._min);
		eb.append(_domain, other._domain);
		
		return eb.isEquals();
	}
	
	@Override
	public int hashCode() {
		HashCodeBuilder hcb = new HashCodeBuilder(17, 31);
		hcb.append(_max);
		hcb.append(_min);
		hcb.append(_domain);
		return hcb.toHashCode();
	}
	
	@Override
	public int compareTo(Valuation other) {
		Validator.notNull(other);
		Validator.notIllegalElementType(other, new String[] {Integer.class.toString()});
		
		if(_domain.equals(other.getDomain())) {
			double middle = (_max + _min) / 2l;
			double otherMidle = (((RealIntervalValuation) other)._max + ((RealIntervalValuation) other)._min) / 2d;
			return Double.compare(middle, otherMidle);	
		} else {
			throw new IllegalArgumentException(Messages.RealInterval_Different_domains);
		}
	}
	
	@Override
	public Object clone() {
		RealIntervalValuation result = null;
		result = (RealIntervalValuation) super.clone();
		result._min = new Double(_min);
		result._max = new Double(_max);
		
		return result;
	}

	@Override
	public String changeFormatValuationToString() {
		return "[" + Double.toString(Math.round(_min * 100d) / 100d) + ", " + Double.toString(Math.round(_max * 100d) / 100d) + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@Override
	public void save(XMLStreamWriter writer) throws XMLStreamException {
		writer.writeAttribute("min", Double.toString(_min)); //$NON-NLS-1$
		writer.writeAttribute("max", Double.toString(_max));	 //$NON-NLS-1$
	}

	@Override
	public void read(XMLRead reader) throws XMLStreamException {
		_min = Double.parseDouble(reader.getStartElementAttribute("min")); //$NON-NLS-1$
		_max = Double.parseDouble(reader.getStartElementAttribute("max")); //$NON-NLS-1$
	}

}
