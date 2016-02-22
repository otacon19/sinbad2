package sinbad2.phasemethod.unbalanced.methodology.unification;

import java.util.HashMap;
import java.util.Map;

import sinbad2.domain.DomainSet;
import sinbad2.domain.DomainsManager;
import sinbad2.domain.linguistic.fuzzy.FuzzySet;
import sinbad2.domain.linguistic.fuzzy.label.LabelLinguisticDomain;
import sinbad2.domain.linguistic.unbalanced.Unbalanced;
import sinbad2.domain.numeric.real.NumericRealDomain;
import sinbad2.element.alternative.Alternative;
import sinbad2.element.criterion.Criterion;
import sinbad2.phasemethod.IPhaseMethod;
import sinbad2.phasemethod.listener.EPhaseMethodStateChange;
import sinbad2.phasemethod.listener.PhaseMethodStateChangeEvent;
import sinbad2.valuation.Valuation;
import sinbad2.valuation.linguistic.LinguisticValuation;
import sinbad2.valuation.twoTuple.TwoTuple;
import sinbad2.valuation.valuationset.ValuationKey;
import sinbad2.valuation.valuationset.ValuationSet;
import sinbad2.valuation.valuationset.ValuationSetManager;

public class UnificationPhase implements IPhaseMethod {

	public static final String ID = "flintstones.phasemethod.unbalanced.methodology.unification";
	
	private static final int LEFT = 0;
	private static final int RIGHT = 1;

	private Unbalanced _hgls;
	
	private Map<ValuationKey, Valuation> _unifiedEvaluationsResult;
	private Map<Alternative, Valuation> _unifiedEvaluationsResultAlternative;
	
	private ValuationSetManager _valuationSetManager;
	private ValuationSet _valutationSet;
	
	private DomainsManager _domainsManager;
	private DomainSet _domainSet;
	
	private static UnificationPhase _instance;
	
	public UnificationPhase() {
		_valuationSetManager = ValuationSetManager.getInstance();
		_valutationSet = _valuationSetManager.getActiveValuationSet();
		
		_domainsManager = DomainsManager.getInstance();
		_domainSet = _domainsManager.getActiveDomainSet();
	}
	
	public static UnificationPhase getInstance() {
		if(_instance == null) {
			_instance = new UnificationPhase();
		}
		
		return _instance;
	}
	
	public DomainSet getDomainSet() {
		return _domainSet;
	}
	
	public ValuationSet getValuationSet() {
		return _valutationSet;
	}
	
	public Unbalanced getDomainLH() {
		return (Unbalanced) _domainSet.getDomains().get(0);
	}
	
	@Override
	public IPhaseMethod copyStructure() {
		return new UnificationPhase();
	}

	@Override
	public void copyData(IPhaseMethod iPhaseMethod) {
		UnificationPhase unification = (UnificationPhase) iPhaseMethod;
		
		clear();
		
		_valutationSet.setValuations(unification.getValuationSet().getValuations());
		_domainSet.setDomains(unification.getDomainSet().getDomains());
	}
	
	public Map<ValuationKey, Valuation> unification(Unbalanced domain) {

		_unifiedEvaluationsResult = new HashMap<ValuationKey, Valuation>();
		_unifiedEvaluationsResultAlternative = new HashMap<Alternative, Valuation>();

		if(domain != null) {
			int[] lh = domain.getLh();
			Map<Integer, Unbalanced> lhDomains = new HashMap<Integer, Unbalanced>();
			_hgls = null;

			for(int i = 0; i < lh.length; i++) {
				lhDomains.put(lh[i], createDomain(lh[i]));

				if(i == (lh.length - 1)) {
					_hgls = lhDomains.get(lh[i]);
				}
			}
			
			Criterion criterion;
			Valuation valuation;
			Boolean isCost;
			Map<ValuationKey, Valuation> unifiedEvaluations = _valutationSet.getValuations();
			for(ValuationKey vk: unifiedEvaluations.keySet()) {
				criterion = vk.getCriterion();
				valuation = unifiedEvaluations.get(vk);
				isCost = criterion.getCost();
	
				LabelLinguisticDomain label = null;
				double alpha = 0;
				
				if(valuation instanceof TwoTuple) {
					label = ((TwoTuple) valuation).getLabel();
					alpha = ((TwoTuple) valuation).getAlpha();
				} else if (valuation instanceof LinguisticValuation) {
					label = ((LinguisticValuation) valuation).getLabel();
				} else {
					throw new IllegalArgumentException();
				}
				
				int pos = domain.getLabelSet().getPos(label);
				Map<Integer, Integer> domains = domain.getLabel(pos);

				int size = domains.size();
				Unbalanced[] auxDomains = new Unbalanced[size];
				LabelLinguisticDomain[] labels = new LabelLinguisticDomain[size];
				int[] sizes = new int[size];

				int i = 0;
				for (Integer domainSize : domains.keySet()) {
					auxDomains[i] = lhDomains.get(domainSize);
					labels[i] = auxDomains[i].getLabelSet().getLabel(domains.get(domainSize));
					sizes[i] = domainSize;
					i++;
				}
				
				valuation = transform(label, alpha, (size > 1), auxDomains, labels, sizes);

				if (isCost) {
					valuation = ((TwoTuple) valuation).negateValuation();
				}
				
				_unifiedEvaluationsResult.put(vk, valuation);
				_unifiedEvaluationsResultAlternative.put(vk.getAlternative(), valuation);
			}
		}

		return _unifiedEvaluationsResult;
	}
	
	private Unbalanced createDomain(int cardinality) {
		String[] labels = new String[cardinality];
		for(int i = 0; i < cardinality; i++) {
			labels[i] = Integer.toString(i);
		}
		
		Unbalanced domain = new Unbalanced();
		
		domain.createTrapezoidalFunction(labels);
		
		return domain;
	}
	
	private Valuation transform(LabelLinguisticDomain label, double alpha, boolean brid, Unbalanced[] domains, LabelLinguisticDomain[] labels, int[] sizes) {
		
		Valuation result;
		
		if((!brid) || (alpha == 0)) {
			if(brid) {
				if (sizes[0] > sizes[1]) {
					result = transformInDomain(domains[0], labels[0], alpha);
				} else {
					result = transformInDomain(domains[1], labels[1], alpha);
				}
			} else {
				result = transformInDomain(domains[0], labels[0], alpha);
			}
		} else {
			if(alpha > 0) {
				if(smallSide(label) == RIGHT) {
					if(sizes[0] > sizes[1]) {
						result = transformInDomain(domains[0], labels[0], alpha);
					} else {
						result = transformInDomain(domains[1], labels[1], alpha);
					}
				} else {
					if(sizes[0] > sizes[1]) {
						result = transformInDomain(domains[1], labels[1], alpha);
					} else {
						result = transformInDomain(domains[0], labels[0], alpha);
					}
				}
			} else {
				if(smallSide(label) == RIGHT) {
					if(sizes[0] > sizes[1]) {
						result = transformInDomain(domains[1], labels[1], alpha);
					} else {
						result = transformInDomain(domains[0], labels[0], alpha);
					}
				} else {
					if(sizes[0] > sizes[1]) {
						result = transformInDomain(domains[0], labels[0], alpha);
					} else {
						result = transformInDomain(domains[1], labels[1], alpha);
					}
				}
			}
		}
		
		return result;
	}
	
	private Valuation transformInDomain(Unbalanced domain, LabelLinguisticDomain label, double alpha) {
		TwoTuple result = new TwoTuple((FuzzySet) domain, label, alpha);
		if(domain != _hgls) {
			result = result.transform(_hgls);
		}

		return result;
	}
	
	private int smallSide(LabelLinguisticDomain l) {

		NumericRealDomain center = l.getSemantic().getCenter();
		NumericRealDomain coverage = l.getSemantic().getCoverage();

		double left = center.getMin() - coverage.getMin();
		double right = coverage.getMax() - center.getMax();

		if(left > right) {
			return RIGHT;
		} else {
			return LEFT;
		}
	}

	public Map<ValuationKey, Valuation> getValuationsResult() {
		return _unifiedEvaluationsResult;
	}
	
	public Map<Alternative, Valuation> getValuationsAlternativeResult() {
		return _unifiedEvaluationsResultAlternative;
	}
	
	@Override
	public void clear() {
		_domainSet.clear();
		_valutationSet.clear();
	}

	@Override
	public void activate() {
		_domainsManager.setActiveDomainSet(_domainSet);
		_valuationSetManager.setActiveValuationSet(_valutationSet);
	}

	@Override
	public boolean validate() {
		if(_domainSet.getDomains().isEmpty()) {
			return false;
		}
		
		if(_valutationSet.getValuations().isEmpty()) {
			return false;
		}
		
		return true;
	}

	@Override
	public IPhaseMethod clone() {
		UnificationPhase result = null;

		try {
			result = (UnificationPhase) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		return result;
	}
	
	@Override
	public void notifyPhaseMethodStateChange(PhaseMethodStateChangeEvent event) {
		if (event.getChange().equals(EPhaseMethodStateChange.ACTIVATED)) {
			activate();
		}
	}

}