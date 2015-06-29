package sinbad2.resolutionphase.frameworkstructuring.domainassignments;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import sinbad2.element.alternative.Alternative;
import sinbad2.element.criterion.Criterion;
import sinbad2.element.expert.Expert;

public class DomainAssignmentKey implements Cloneable, Comparable<DomainAssignmentKey> {
	
	private Expert _expert;
	private Alternative _alternative;
	private Criterion _criterion;
	
	private DomainAssignmentKey() {
		_expert = null;
		_alternative = null;
		_criterion = null;
	}
	
	public DomainAssignmentKey(Expert expert, Alternative alternative, Criterion criterion) {
		this();
		
		_expert = expert;
		_alternative = alternative;
		_criterion = null;
	}
	
	public Expert getExpert() {
		return _expert;
	}
	
	public Alternative getAlternative() {
		return _alternative;
	}
	
	public Criterion getCriterion() {
		return _criterion;
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
		
		final DomainAssignmentKey other = (DomainAssignmentKey) obj;
		
		EqualsBuilder eb = new EqualsBuilder();
		eb.append(_expert, other._expert);
		eb.append(_alternative, other._alternative);
		eb.append(_criterion, other._criterion);
		
		return eb.isEquals();
	}
	
	@Override
	public int hashCode() {
		HashCodeBuilder hcb = new HashCodeBuilder(17, 31);
		hcb.append(_expert);
		hcb.append(_alternative);
		hcb.append(_criterion);
		
		return hcb.hashCode();
	}
	
	@Override
	public Object clone() {
		DomainAssignmentKey result = null;
		
		try {
			result = (DomainAssignmentKey) super.clone();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		result._expert = (_expert != null) ? (Expert) _expert.clone() : null;
		result._alternative = (_alternative != null) ? (Alternative) _alternative.clone() : null;
		result._criterion = (_criterion != null) ? (Criterion) _criterion.clone() : null;
		
		return result;
	}
	
	@Override
	public String toString() {
		String result = null;
		
		result += (_expert != null) ? _expert.getPathId() : "null";
		result += ":";
		result += (_alternative != null) ? _alternative.getPathId() : "null";
		result += ":";
		result += (_criterion != null) ? _criterion.getPathId() : "null";
		
		return result;
	}
	
	@Override
	public int compareTo(DomainAssignmentKey other) {
		return toString().compareTo(other.toString());
	}

}
