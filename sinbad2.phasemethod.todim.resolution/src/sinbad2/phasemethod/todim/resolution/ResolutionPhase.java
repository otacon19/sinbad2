package sinbad2.phasemethod.todim.resolution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.rosuda.JRI.Rengine;

import sinbad2.core.utils.Pair;
import sinbad2.domain.Domain;
import sinbad2.domain.linguistic.fuzzy.FuzzySet;
import sinbad2.domain.linguistic.fuzzy.function.types.TrapezoidalFunction;
import sinbad2.element.ProblemElementsManager;
import sinbad2.element.ProblemElementsSet;
import sinbad2.element.alternative.Alternative;
import sinbad2.element.criterion.Criterion;
import sinbad2.element.expert.Expert;
import sinbad2.phasemethod.IPhaseMethod;
import sinbad2.phasemethod.PhasesMethodManager;
import sinbad2.phasemethod.listener.EPhaseMethodStateChange;
import sinbad2.phasemethod.listener.PhaseMethodStateChangeEvent;
import sinbad2.phasemethod.todim.unification.UnificationPhase;
import sinbad2.valuation.Valuation;
import sinbad2.valuation.hesitant.HesitantValuation;
import sinbad2.valuation.linguistic.LinguisticValuation;
import sinbad2.valuation.valuationset.ValuationKey;
import sinbad2.valuation.valuationset.ValuationSet;
import sinbad2.valuation.valuationset.ValuationSetManager;

public class ResolutionPhase implements IPhaseMethod {

	public static final String ID = "flintstones.phasemethod.todim.resolution"; //$NON-NLS-1$

	private static final double P = 2;
	// private static final double M = 2;
	private static final double C = 1.5;

	private int _numAlternatives;
	private int _numCriteria;

	private Map<Pair<Alternative, Criterion>, Valuation> _decisionMatrix;
	private Double[][] _centerOfGravityConsensusMatrix;
	private String[][] _trapezoidalConsensusMatrix;
	private Map<ValuationKey, Double> _distances;
	private Map<Pair<Expert, Criterion>, Double> _thresholdValues;
	private List<Alternative> _realAlternatives;

	private Map<Criterion, Double> _criteriaWeights;
	private Map<String, Double> _relativeWeights;

	private Map<Criterion, Map<Pair<Alternative, Alternative>, Double>> _dominanceDegreeByCriterion;
	private Map<Pair<Alternative, Alternative>, Double> _dominanceDegreeAlternatives;
	private Map<Alternative, Double> _globalDominance;

	private ProblemElementsSet _elementsSet;
	private ValuationSet _valuationSet;

	private UnificationPhase _unificationPhase;

	public LinkedHashMap<Alternative, Double> sortHashMapByValues(HashMap<Alternative, Double> passedMap) {
		List<Alternative> mapKeys = new ArrayList<>(passedMap.keySet());
		List<Double> mapValues = new ArrayList<>(passedMap.values());
		Collections.sort(mapValues);
		Collections.sort(mapKeys);

		LinkedHashMap<Alternative, Double> sortedMap = new LinkedHashMap<>();

		Iterator<Double> valueIt = mapValues.iterator();
		while (valueIt.hasNext()) {
			Double val = valueIt.next();
			Iterator<Alternative> keyIt = mapKeys.iterator();

			while (keyIt.hasNext()) {
				Alternative key = keyIt.next();
				Double comp1 = passedMap.get(key);
				Double comp2 = val;

				if (comp1.equals(comp2)) {
					keyIt.remove();
					sortedMap.put(key, val);
					break;
				}
			}
		}
		return sortedMap;
	}

	public LinkedHashMap<ValuationKey, TrapezoidalFunction> sortHashMapByValuesValuation(
			HashMap<ValuationKey, TrapezoidalFunction> passedMap) {
		List<ValuationKey> mapKeys = new ArrayList<>(passedMap.keySet());
		List<TrapezoidalFunction> mapValues = new ArrayList<>(passedMap.values());
		Collections.sort(mapValues);
		Collections.sort(mapKeys);

		LinkedHashMap<ValuationKey, TrapezoidalFunction> sortedMap = new LinkedHashMap<>();

		Iterator<TrapezoidalFunction> valueIt = mapValues.iterator();
		while (valueIt.hasNext()) {
			TrapezoidalFunction val = valueIt.next();
			Iterator<ValuationKey> keyIt = mapKeys.iterator();

			while (keyIt.hasNext()) {
				ValuationKey key = keyIt.next();
				TrapezoidalFunction comp1 = passedMap.get(key);
				TrapezoidalFunction comp2 = val;

				if (comp1.equals(comp2)) {
					keyIt.remove();
					sortedMap.put(key, val);
					break;
				}
			}
		}
		return sortedMap;
	}

	public ResolutionPhase() {
		_unificationPhase = (UnificationPhase) PhasesMethodManager.getInstance().getPhaseMethod(UnificationPhase.ID)
				.getImplementation();

		initializeConsesusMatrix();

		_decisionMatrix = new HashMap<Pair<Alternative, Criterion>, Valuation>();
		_trapezoidalConsensusMatrix = new String[_numAlternatives][_numCriteria];
		_distances = new HashMap<ValuationKey, Double>();

		_criteriaWeights = new HashMap<Criterion, Double>();
		_relativeWeights = new HashMap<String, Double>();

		_dominanceDegreeByCriterion = new HashMap<Criterion, Map<Pair<Alternative, Alternative>, Double>>();
		_dominanceDegreeAlternatives = new HashMap<Pair<Alternative, Alternative>, Double>();
		_globalDominance = new HashMap<Alternative, Double>();
		_thresholdValues = new HashMap<Pair<Expert, Criterion>, Double>();
	}

	private void initializeConsesusMatrix() {
		_elementsSet = ProblemElementsManager.getInstance().getActiveElementSet();
		_valuationSet = ValuationSetManager.getInstance().getActiveValuationSet();

		_realAlternatives = new LinkedList<Alternative>();
		_realAlternatives.addAll(_elementsSet.getAlternatives());
		
		if (_elementsSet.getAlternative("criterion_importance") != null && _elementsSet.getAlternative("expert_knowledge") == null) {
			_realAlternatives.remove(_elementsSet.getAlternative("criterion_importance"));
		} else if (_elementsSet.getAlternative("criterion_importance") == null && _elementsSet.getAlternative("expert_knowledge") != null) {
			_realAlternatives.remove(_elementsSet.getAlternative("expert_knowledge"));
		} else if (_elementsSet.getAlternative("criterion_importance") != null && _elementsSet.getAlternative("expert_knowledge") != null) {
			_realAlternatives.remove(_elementsSet.getAlternative("criterion_importance"));
			_realAlternatives.remove(_elementsSet.getAlternative("expert_knowledge"));
		} else {
			_realAlternatives = _elementsSet.getAlternatives();
		}

		_numAlternatives = _realAlternatives.size();
		_numCriteria = _elementsSet.getAllCriteria().size();

		_centerOfGravityConsensusMatrix = new Double[_numAlternatives][_numCriteria];
		_trapezoidalConsensusMatrix = new String[_numAlternatives][_numCriteria];

		for (int a = 0; a < _numAlternatives; ++a) {
			for (int c = 0; c < _numCriteria; ++c) {
				_trapezoidalConsensusMatrix[a][c] = "(a,b,c,d)"; //$NON-NLS-1$
			}
		}
	}

	@Override
	public Map<ValuationKey, Valuation> getTwoTupleValuations() {
		return null;
	}

	public Map<ValuationKey, TrapezoidalFunction> getFuzzyValuations() {
		return _unificationPhase.getFuzzyValuations();
	}

	public void setDecisionMatrix(Map<Pair<Alternative, Criterion>, Valuation> decisionMatrix) {
		_decisionMatrix = decisionMatrix;
	}

	public Map<Pair<Alternative, Criterion>, Valuation> getDecisionMatrix() {
		return _decisionMatrix;
	}

	public void setDistances(Map<ValuationKey, Double> distances) {
		_distances = distances;
	}

	public Map<ValuationKey, Double> getDistances() {
		return _distances;
	}

	public void setCenterOfGravityConsensusMatrix(Double[][] consensusMatrix) {
		_centerOfGravityConsensusMatrix = consensusMatrix;
	}

	public Double[][] getCenterOfGravityConsesusMatrix() {
		return _centerOfGravityConsensusMatrix;
	}

	public void setTrapezoidalConsensusMatrix(String[][] consensusMatrix) {
		_trapezoidalConsensusMatrix = consensusMatrix;
	}

	public String[][] getTrapezoidalConsensusMatrix() {
		return _trapezoidalConsensusMatrix;
	}

	public Map<Criterion, Double> getCriteriaWeights() {
		return _criteriaWeights;
	}

	public void setCriteriaWeights(Map<Criterion, Double> criteriaWeights) {
		_criteriaWeights = criteriaWeights;
	}

	public Map<Pair<Expert, Criterion>, Double> getThresholdValues() {
		return _thresholdValues;
	}

	public void setThresholdValues(Map<Pair<Expert, Criterion>, Double> thresholdValues) {
		_thresholdValues = thresholdValues;
	}

	public void setRelativeWeights(Map<String, Double> relativeWeights) {
		_relativeWeights = relativeWeights;
	}

	public Map<String, Double> getRelativeWeights() {
		return _relativeWeights;
	}

	public void setDominanceDegreeByCriterion(
			Map<Criterion, Map<Pair<Alternative, Alternative>, Double>> dominanceDegreeByCriterion) {
		_dominanceDegreeByCriterion = dominanceDegreeByCriterion;
	}

	public Map<Criterion, Map<Pair<Alternative, Alternative>, Double>> getDominanceDegreeByCriterion() {
		return _dominanceDegreeByCriterion;
	}

	public void setDominanceDegreeAlternatives(
			Map<Pair<Alternative, Alternative>, Double> dominanceDegreeAlternatives) {
		_dominanceDegreeAlternatives = dominanceDegreeAlternatives;
	}

	public Map<Pair<Alternative, Alternative>, Double> getDominanceDegreeAlternatives() {
		return _dominanceDegreeAlternatives;
	}

	public void setGlobalDominance(Map<Alternative, Double> globalDominance) {
		_globalDominance = globalDominance;
	}

	public Map<Alternative, Double> getGlobalDominance() {
		return _globalDominance;
	}

	public List<Alternative> getRealAlternatives() {
		return _realAlternatives;
	}
	
	@Override
	public Domain getUnifiedDomain() {
		return _unificationPhase.getUnifiedDomain();
	}

	@Override
	public void setUnifiedDomain(Domain domain) {
	}
	

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<Pair<Expert, Criterion>, Double> calculateThresholdValues() {

		_thresholdValues.clear();

		Map<ValuationKey, Valuation> valuations = _valuationSet.getValuations();
		Map<ValuationKey, TrapezoidalFunction> fuzzyValuations = getFuzzyValuations();
		Alternative aFGC = _elementsSet.getAlternative("expert_knowledge");
		for (ValuationKey vk : fuzzyValuations.keySet()) {

			if (aFGC == null) {
				aFGC = new Alternative("null_threshold");
			}

			ValuationKey vkFGC = new ValuationKey(vk.getExpert(), aFGC, vk.getCriterion());
			LinguisticValuation v = (LinguisticValuation) valuations.get(vkFGC);
			FuzzySet knowledgeDomain = (FuzzySet) v.getDomain();

			double knowledge = 0;
			if (knowledgeDomain.getLabelSet().getPos(v.getLabel()) == 0) {
				knowledge = 1.3;
			} else if (knowledgeDomain.getLabelSet().getPos(v.getLabel()) == 1) {
				knowledge = 1.1;
			} else if (knowledgeDomain.getLabelSet().getPos(v.getLabel()) == 2) {
				knowledge = 0.9;
			} else if (knowledgeDomain.getLabelSet().getPos(v.getLabel()) == 3) {
				knowledge = 0.7;
			} else if (knowledgeDomain.getLabelSet().getPos(v.getLabel()) == 4) {
				knowledge = 0.5;
			} else if (knowledgeDomain.getLabelSet().getPos(v.getLabel()) == 5) {
				knowledge = 0.3;
			} else {
				knowledge = 0.1;
			}

			_thresholdValues.put(new Pair(vk.getExpert(), vk.getCriterion()), knowledge);
		}

		return _thresholdValues;
	}

	public Double[][] calculateConsensusMatrixCenterOfGravity() {
		Double cog;
		String trapezoidalNumber;

		if (_centerOfGravityConsensusMatrix != null && _trapezoidalConsensusMatrix != null) {
			for (int al = 0; al < _numAlternatives; ++al) {
				for (int cr = 0; cr < _numCriteria; ++cr) {
					trapezoidalNumber = (String) _trapezoidalConsensusMatrix[al][cr];
					cog = transformToTrapezoidalFunction(trapezoidalNumber).centroid();
					_centerOfGravityConsensusMatrix[al][cr] = Math.round(cog * 1000d) / 1000d;
				}
			}
		}

		return (Double[][]) _centerOfGravityConsensusMatrix;
	}

	public Double[][] calculateConsensusMatrixCenterOfGravity(Double[][] result) {
		double cog;
		String trapezoidalNumber;

		if (_centerOfGravityConsensusMatrix != null && _trapezoidalConsensusMatrix != null) {
			for (int al = 0; al < _numAlternatives; ++al) {
				for (int cr = 0; cr < _numCriteria; ++cr) {
					trapezoidalNumber = (String) _trapezoidalConsensusMatrix[al][cr];
					cog = transformToTrapezoidalFunction(trapezoidalNumber).centroid();
					result[al][cr] = Math.round(cog * 1000d) / 1000d;
				}
			}
		}

		return (Double[][]) result;
	}

	public Map<Criterion, Double> getImportanceCriteriaWeights() {
		Map<Criterion, List<TrapezoidalFunction>> expertsEnvelopeWeightsForEachCriterion = new HashMap<Criterion, List<TrapezoidalFunction>>();
		List<TrapezoidalFunction> envelopeWeights;

		if (_criteriaWeights.isEmpty()) {
			ValuationSetManager vsm = ValuationSetManager.getInstance();
			ValuationSet vs = vsm.getActiveValuationSet();

			Valuation v = null;
			Map<ValuationKey, Valuation> valuations = vs.getValuations();
			for (ValuationKey vk : valuations.keySet()) {
				if (vk.getAlternative().getId().equals("null_importance")
						|| vk.getAlternative().getId().equals("criterion_importance")) {
					if (expertsEnvelopeWeightsForEachCriterion.get(vk.getCriterion()) != null) {
						envelopeWeights = expertsEnvelopeWeightsForEachCriterion.get(vk.getCriterion());
					} else {
						envelopeWeights = new LinkedList<TrapezoidalFunction>();
					}
					v = valuations.get(vk);
					envelopeWeights.add(((HesitantValuation) v).calculateFuzzyEnvelope((FuzzySet) v.getDomain()));
					expertsEnvelopeWeightsForEachCriterion.put(vk.getCriterion(), envelopeWeights);
				}
			}
		}

		if (expertsEnvelopeWeightsForEachCriterion.size() > 0) {
			calculateWeights(expertsEnvelopeWeightsForEachCriterion);
		} else if (_criteriaWeights.isEmpty()) {
			assignDefaultWeights();
		}

		return _criteriaWeights;
	}

	private void calculateWeights(Map<Criterion, List<TrapezoidalFunction>> expertsEnvelopeWeightsForEachCriterion) {
		double acum;

		for (Criterion c : expertsEnvelopeWeightsForEachCriterion.keySet()) {
			acum = 0;
			List<TrapezoidalFunction> envelopeFunctions = expertsEnvelopeWeightsForEachCriterion.get(c);
			for (TrapezoidalFunction envelope : envelopeFunctions) {
				acum += envelope.centroid();
			}
			_criteriaWeights.put(c, Math.round((acum / envelopeFunctions.size()) * 1000d) / 1000d);
		}

		normalizeWeights();
	}

	private void normalizeWeights() {

		double sum = 0;

		for (Criterion c : _criteriaWeights.keySet()) {
			sum += _criteriaWeights.get(c);
		}

		for (Criterion c : _criteriaWeights.keySet()) {
			_criteriaWeights.put(c, Math.round((_criteriaWeights.get(c) / sum) * 1000d) / 1000d);
		}
	}

	private void assignDefaultWeights() {
		for (Criterion c : _elementsSet.getAllCriteria()) {
			_criteriaWeights.put(c, 1d / _elementsSet.getAllCriteria().size());
		}
	}

	public Map<String, Double> calculateRelativeWeights() {
		_relativeWeights = new HashMap<String, Double>();

		double weightReference = Double.NEGATIVE_INFINITY, weight;
		for (Criterion c : _criteriaWeights.keySet()) {
			weight = _criteriaWeights.get(c);
			if (weight > weightReference) {
				weightReference = weight;
			}
		}

		List<Criterion> criteria = _elementsSet.getAllCriteria();
		for (int i = 0; i < criteria.size(); ++i) {
			_relativeWeights.put(criteria.get(i).getCanonicalId(),
					Math.round((_criteriaWeights.get(criteria.get(i)) / weightReference) * 1000d) / 1000d);
		}

		return _relativeWeights;
	}

	public Map<Criterion, Map<Pair<Alternative, Alternative>, Double>> calculateDominanceDegreeByCriterionCenterOfGravity(
			double attenuationFactor) {
		_dominanceDegreeByCriterion = new HashMap<Criterion, Map<Pair<Alternative, Alternative>, Double>>();

		double acumSumRelativeWeights = getAcumSumRelativeWeights();

		int criterionIndex = 0, a1Index = 0, a2Index = 0;
		double dominance = 0, condition = 0;
		for (Criterion c : _elementsSet.getAllCriteria()) {
			a1Index = 0;
			for (Alternative a1 : _realAlternatives) {
				a2Index = 0;
				for (Alternative a2 : _realAlternatives) {
					if (a1 != a2) {
						condition = (Double) _centerOfGravityConsensusMatrix[a1Index][criterionIndex]
								- (Double) _centerOfGravityConsensusMatrix[a2Index][criterionIndex];
						if (c.isCost()) {
							if (condition > 0) {
								double inverse = (Double) _centerOfGravityConsensusMatrix[a2Index][criterionIndex]
										- (Double) _centerOfGravityConsensusMatrix[a1Index][criterionIndex];
								dominance = (-1d / attenuationFactor);
								dominance *= Math.sqrt(
										(inverse * acumSumRelativeWeights) / _relativeWeights.get(c.getCanonicalId()));
							} else if (condition < 0) {
								dominance = Math.sqrt((condition * _relativeWeights.get(c.getCanonicalId()))
										/ acumSumRelativeWeights);
							} else {
								dominance = 0;
							}
						} else {
							if (condition > 0) {
								dominance = Math.sqrt((condition * _relativeWeights.get(c.getCanonicalId()))
										/ acumSumRelativeWeights);
							} else if (condition < 0) {
								double inverse = (Double) _centerOfGravityConsensusMatrix[a2Index][criterionIndex]
										- (Double) _centerOfGravityConsensusMatrix[a1Index][criterionIndex];
								dominance = (-1d / attenuationFactor);
								dominance *= Math.sqrt(
										(inverse * acumSumRelativeWeights) / _relativeWeights.get(c.getCanonicalId()));
							} else {
								dominance = 0;
							}
						}

						Pair<Alternative, Alternative> pairAlternatives = new Pair<Alternative, Alternative>(a1, a2);
						Map<Pair<Alternative, Alternative>, Double> pairAlternativesDominance;
						if (_dominanceDegreeByCriterion.get(c) != null) {
							pairAlternativesDominance = _dominanceDegreeByCriterion.get(c);
						} else {
							pairAlternativesDominance = new HashMap<Pair<Alternative, Alternative>, Double>();
						}
						pairAlternativesDominance.put(pairAlternatives, dominance);
						_dominanceDegreeByCriterion.put(c, pairAlternativesDominance);

					}
					a2Index++;
				}
				a1Index++;
			}
			criterionIndex++;
		}

		return _dominanceDegreeByCriterion;
	}

	private double getAcumSumRelativeWeights() {
		double acum = 0;

		for (Criterion c : _elementsSet.getAllCriteria()) {
			acum += _relativeWeights.get(c.getCanonicalId());
		}

		return acum;
	}

	public Map<Criterion, Map<Pair<Alternative, Alternative>, Double>> calculateDominanceDegreeByCriterionFuzzyNumber(
			double attenuationFactor) {
		_dominanceDegreeByCriterion = new HashMap<Criterion, Map<Pair<Alternative, Alternative>, Double>>();

		double acumSumRelativeWeights = getAcumSumRelativeWeights();

		int criterionIndex = 0, a1Index = 0, a2Index = 0;
		double dominance = 0, condition = 0;
		String trapezoidalNumber1, trapezoidalNumber2;
		TrapezoidalFunction tpf1, tpf2;

		for (Criterion c : _elementsSet.getAllCriteria()) {

			a1Index = 0;

			for (Alternative a1 : _realAlternatives) {

				a2Index = 0;

				for (Alternative a2 : _realAlternatives) {

					if (a1 != a2) {

						trapezoidalNumber1 = (String) _trapezoidalConsensusMatrix[a1Index][criterionIndex];
						trapezoidalNumber2 = (String) _trapezoidalConsensusMatrix[a2Index][criterionIndex];

						tpf1 = transformToTrapezoidalFunction(trapezoidalNumber1);
						tpf2 = transformToTrapezoidalFunction(trapezoidalNumber2);

						condition = tpf1.getSimpleDefuzzifiedValue() - tpf2.getSimpleDefuzzifiedValue();
						if (c.isCost()) {
							if (condition > 0) {
								dominance = (-1d / attenuationFactor);
								dominance *= Math.sqrt((tpf1.distance(tpf2, P) * acumSumRelativeWeights)
										/ _relativeWeights.get(c.getCanonicalId()));
							} else if (condition <= 0) {
								dominance = Math
										.sqrt((tpf1.distance(tpf2, P) * _relativeWeights.get(c.getCanonicalId()))
												/ acumSumRelativeWeights);
							}
						} else {
							if (condition >= 0) {
								dominance = Math
										.sqrt((tpf1.distance(tpf2, P) * _relativeWeights.get(c.getCanonicalId()))
												/ acumSumRelativeWeights);
							} else if (condition < 0) {
								dominance = (-1d / attenuationFactor);
								dominance *= Math.sqrt((tpf1.distance(tpf2, P) * acumSumRelativeWeights)
										/ _relativeWeights.get(c.getCanonicalId()));
							}
						}

						Pair<Alternative, Alternative> pairAlternatives = new Pair<Alternative, Alternative>(a1, a2);
						Map<Pair<Alternative, Alternative>, Double> pairAlternativesDominance;

						if (_dominanceDegreeByCriterion.get(c) != null) {
							pairAlternativesDominance = _dominanceDegreeByCriterion.get(c);
						} else {
							pairAlternativesDominance = new HashMap<Pair<Alternative, Alternative>, Double>();
						}

						pairAlternativesDominance.put(pairAlternatives, dominance);
						_dominanceDegreeByCriterion.put(c, pairAlternativesDominance);
					}
					a2Index++;
				}
				a1Index++;
			}
			criterionIndex++;
		}

		return _dominanceDegreeByCriterion;
	}

	private TrapezoidalFunction transformToTrapezoidalFunction(String trapezoidalNumber1) {
		trapezoidalNumber1 = trapezoidalNumber1.replace("(", ""); //$NON-NLS-1$ //$NON-NLS-2$
		trapezoidalNumber1 = trapezoidalNumber1.replace(")", ""); //$NON-NLS-1$ //$NON-NLS-2$
		String[] limits = trapezoidalNumber1.split(","); //$NON-NLS-1$
		double aT1 = Double.parseDouble(limits[0]);
		double bT1 = Double.parseDouble(limits[1]);
		double cT1 = Double.parseDouble(limits[2]);
		double dT1 = Double.parseDouble(limits[3]);
		double[] limits1 = new double[] { aT1, bT1, cT1, dT1 };

		return new TrapezoidalFunction(limits1);
	}

	public Map<Pair<Alternative, Alternative>, Double> calculateDominaceDegreeOverAlternatives() {

		_dominanceDegreeAlternatives = new HashMap<Pair<Alternative, Alternative>, Double>();

		double acum;
		for (Alternative a1 : _realAlternatives) {
			for (Alternative a2 : _realAlternatives) {
				acum = 0;
				if (a1 != a2) {
					for (Criterion c : _dominanceDegreeByCriterion.keySet()) {
						Map<Pair<Alternative, Alternative>, Double> pairAlternativesDominanceByCriterion = _dominanceDegreeByCriterion
								.get(c);
						for (Pair<Alternative, Alternative> pair : pairAlternativesDominanceByCriterion.keySet()) {
							if (a1.equals(pair.getLeft()) && a2.equals(pair.getRight())) {
								acum += pairAlternativesDominanceByCriterion.get(pair);
							}
						}
					}
					_dominanceDegreeAlternatives.put(new Pair<Alternative, Alternative>(a1, a2), acum);
				}
			}
		}

		return _dominanceDegreeAlternatives;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public LinkedHashMap<Alternative, Double> calculateGlobalDominance() {

		Map<Alternative, Double> acumDominanceDegreeAlternatives = getAcumDominanceAlternatives();
		double max = getMaxDominace(acumDominanceDegreeAlternatives);
		double min = getMinDominance(acumDominanceDegreeAlternatives);
		double dominance;

		for (Alternative a : acumDominanceDegreeAlternatives.keySet()) {
			dominance = (acumDominanceDegreeAlternatives.get(a) - min) / (max - min);
			_globalDominance.put(a, dominance);
		}

		return sortHashMapByValues((HashMap) _globalDominance);
	}

	private Map<Alternative, Double> getAcumDominanceAlternatives() {
		Map<Alternative, Double> acumDominanceDegreeAlternatives = new HashMap<Alternative, Double>();
		double acum;

		for (Alternative a : _realAlternatives) {
			acum = 0;
			for (Pair<Alternative, Alternative> pairAlternatives : _dominanceDegreeAlternatives.keySet()) {
				if (a.equals(pairAlternatives.getLeft())) {
					acum += _dominanceDegreeAlternatives.get(pairAlternatives);
				}
			}
			acumDominanceDegreeAlternatives.put(a, acum);
		}

		return acumDominanceDegreeAlternatives;
	}

	private double getMaxDominace(Map<Alternative, Double> acumDominanceDegreeAlternatives) {
		double max = acumDominanceDegreeAlternatives.get(_realAlternatives.get(0));
		for (Alternative a : acumDominanceDegreeAlternatives.keySet()) {
			if (acumDominanceDegreeAlternatives.get(a) > max) {
				max = acumDominanceDegreeAlternatives.get(a);
			}
		}
		return max;
	}

	private double getMinDominance(Map<Alternative, Double> acumDominanceDegreeAlternatives) {
		double min = acumDominanceDegreeAlternatives.get(_realAlternatives.get(0));
		for (Alternative a : acumDominanceDegreeAlternatives.keySet()) {
			if (acumDominanceDegreeAlternatives.get(a) < min) {
				min = acumDominanceDegreeAlternatives.get(a);
			}
		}
		return min;
	}

	public List<Double> transformWeightsToList() {
		List<Double> weights = new LinkedList<Double>();
		for (Criterion c : _elementsSet.getAllCriteria()) {
			weights.add(_criteriaWeights.get(c));
		}
		return weights;
	}

	@Override
	public IPhaseMethod copyStructure() {
		return new ResolutionPhase();
	}

	@Override
	public void copyData(IPhaseMethod iPhaseMethod) {
		ResolutionPhase resolution = (ResolutionPhase) iPhaseMethod;

		clear();

		_decisionMatrix = resolution.getDecisionMatrix();
		_distances = resolution.getDistances();
		_centerOfGravityConsensusMatrix = resolution.getCenterOfGravityConsesusMatrix();
		_criteriaWeights = resolution.getImportanceCriteriaWeights();
		_relativeWeights = resolution.getRelativeWeights();
		_dominanceDegreeByCriterion = resolution.getDominanceDegreeByCriterion();
		_dominanceDegreeAlternatives = resolution.getDominanceDegreeAlternatives();
		_globalDominance = resolution.getGlobalDominance();
		_thresholdValues = resolution.getThresholdValues();
		_trapezoidalConsensusMatrix = resolution.getTrapezoidalConsensusMatrix();
	}

	@Override
	public void clear() {
		_decisionMatrix.clear();
		_distances.clear();
		_centerOfGravityConsensusMatrix = new Double[_numAlternatives][_numCriteria];
		_trapezoidalConsensusMatrix = new String[_numAlternatives][_numCriteria];
		initializeConsesusMatrix();

		_criteriaWeights.clear();
		_relativeWeights.clear();
		_dominanceDegreeByCriterion.clear();
		_dominanceDegreeAlternatives.clear();
		_globalDominance.clear();
		_thresholdValues.clear();
	}

	@Override
	public boolean validate() {
		return true;
	}

	@Override
	public void notifyPhaseMethodStateChange(PhaseMethodStateChangeEvent event) {
		if (event.getChange().equals(EPhaseMethodStateChange.ACTIVATED)) {
			activate();
		}
	}

	@Override
	public void activate() {
	}

	@Override
	public IPhaseMethod clone() {
		ResolutionPhase result = null;

		try {
			result = (ResolutionPhase) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		return result;
	}

	public void computeLinearProgramming() {
		List<Alternative> alternatives = _elementsSet.getAlternatives();
		List<Criterion> criteria = _elementsSet.getAllSubcriteria();
		List<Expert> experts = _elementsSet.getOnlyExpertChildren();

		// Experts weights
		double[] expertsWeights = computeExpertsWeights();

		// Initial fuzzy valuations
		Map<ValuationKey, TrapezoidalFunction> fuzzyValuations = getFuzzyValuations();

		// Compute overall opinion
		TrapezoidalFunction[][] O = new TrapezoidalFunction[alternatives.size()][criteria.size()];
		for (int i = 0; i < alternatives.size(); ++i) {
			for (int j = 0; j < criteria.size(); ++j) {
				for (int k = 0; k < experts.size(); ++k) {
					TrapezoidalFunction r = fuzzyValuations
							.get(new ValuationKey(experts.get(k), alternatives.get(i), criteria.get(j)));
					if (O[i][j] == null) {
						O[i][j] = r.multiplicationScalar(expertsWeights[k]);
					} else {
						O[i][j] = O[i][j].addition(r.multiplicationScalar(expertsWeights[k]));
					}
				}
			}
		}

		// Compute distances and similarities
		double u = computeUValue(fuzzyValuations);
		u = Math.pow(u, P);
		double[][][] distances = new double[experts.size()][alternatives.size()][criteria.size()];
		double[][][] similarities = new double[experts.size()][alternatives.size()][criteria.size()];
		for (int i = 0; i < alternatives.size(); ++i) {
			for (int j = 0; j < criteria.size(); ++j) {
				for (int k = 0; k < experts.size(); ++k) {
					distances[k][i][j] = O[i][j].distance(
							fuzzyValuations.get(new ValuationKey(experts.get(k), alternatives.get(i), criteria.get(j))),
							P);
					similarities[k][i][j] = 1d - ((1d / (4 * u)) * distances[k][i][j]);
				}
			}
		}

		// Overall opinion R
		String overall = "c(";
		double[] limits;
		for (int i = 0; i < alternatives.size(); ++i) {
			for (int j = 0; j < criteria.size(); ++j) {
				limits = O[i][j].getLimits();
				for (int f = 0; f < 4; f++) {
					overall += Double.toString(limits[f]) + ",";
				}
			}
		}

		overall = overall.substring(0, overall.length() - 1) + ")";

		// Individual opinion R
		String individual = "c(";
		TrapezoidalFunction r;
		for (int i = 0; i < alternatives.size(); ++i) {
			for (int j = 0; j < criteria.size(); ++j) {
				for (int k = 0; k < experts.size(); ++k) {
					r = fuzzyValuations.get(new ValuationKey(experts.get(k), alternatives.get(i), criteria.get(j)));
					limits = r.getLimits();
					for (int f = 0; f < 4; f++) {
						individual += Double.toString(limits[f]) + ",";
					}
				}
			}
		}

		individual = individual.substring(0, individual.length() - 1) + ")";

		String w = "c(";
		for (double we : expertsWeights) {
			w += Double.toString(we) + ",";
		}

		w = w.substring(0, w.length() - 1) + ")";

		String alternativesSize = Integer.toString(alternatives.size());
		String criteriaSize = Integer.toString(criteria.size());
		String expertsSize = Integer.toString(experts.size());

		// Start Rengine.
		Rengine engine = new Rengine(new String[] { "--no-save" }, false, null);

		engine.eval("overall=" + overall);
		engine.eval("individual=" + individual);
		engine.eval("w=" + w);

		engine.eval("alternativesSize=" + alternativesSize);
		engine.eval("criteriaSize=" + criteriaSize);
		engine.eval("expertsSize=" + expertsSize);

		engine.eval("C=" + C);
		engine.eval("u=" + u);
		engine.eval("P=" + P);

		// objective function
		engine.eval("result <- function(overall, individual) {" + "acum <- 0.0;" + "for (i in 1: alternativesSize) {"
				+ "for (j in 1: criteriaSize) {" + "for (k in 1: expertsSize) {"
				+ "acum <- sum((w[k] * (C - (1 - (1 / (4 * u)))) * ("
				+ "abs(overall[(i-1) * criteriaSize * 4 + (j-1) * 4] - individual[((k-1) * criteriaSize * 4 * alternativesSize + 1) + ((i-1) * criteriaSize * 4) + ((j-1) * 4)])^(P) +"
				+ "abs(overall[(i-1) * criteriaSize * 4 + (j-1) * 4 + 1] - individual[((k-1) * criteriaSize * 4 * alternativesSize + 1) + ((i-1) * criteriaSize * 4) + ((j-1) * 4) + 1])^(P) +"
				+ "abs(overall[(i-1) * criteriaSize * 4 + (j-1) * 4 + 2] - individual[((k-1) * criteriaSize * 4 * alternativesSize + 1) + ((i-1) * criteriaSize * 4) + ((j-1) * 4) + 2])^(P) +"
				+ "abs(overall[(i-1) * criteriaSize * 4 + (j-1) * 4 + 3] - individual[((k-1) * criteriaSize * 4 * alternativesSize + 1) + ((i-1) * criteriaSize * 4) + ((j-1) * 4) + 3])^(P))"
				+ "))" + "};" + "};" + "};" + "acum" + "}");

		// Constraint functions
		engine.eval("eval_g0 <- function(overall, individual) { " + "h <- numeric(6)"
				+ "for (i in 1: alternativesSize) {" + "for (j in 1: criteriaSize) {" + "for (k in 1: expertsSize) {"
				+ "h[1] <- " + "}" + "}" + "}");

		engine.eval("res1 <- nloptr(overall, eval_f=result, lb = c(0,0), ub = c(1,1), eval_g_ineq = eval_g0,"
				+ "opts = list(algorithm=NLOPT_LN_COBYLA, xtol_rel=1.0e-8), a = a, b = b )");

		engine.end();
	}

	private double[] computeExpertsWeights() {
		double[] weights = new double[_elementsSet.getOnlyExpertChildren().size()];
		for (int k = 0; k < _elementsSet.getOnlyExpertChildren().size(); ++k) {
			weights[k] = 1d / weights.length;
		}
		return weights;
	}

	private double computeUValue(Map<ValuationKey, TrapezoidalFunction> fuzzyValuations) {
		double max = Double.NEGATIVE_INFINITY, min = Double.POSITIVE_INFINITY;
		for (ValuationKey vk : fuzzyValuations.keySet()) {
			TrapezoidalFunction fuzzy = fuzzyValuations.get(vk);
			for (int i = 0; i < 4; ++i) {
				if (max < fuzzy.getLimits()[i]) {
					max = fuzzy.getLimits()[i];
				}

				if (min > fuzzy.getLimits()[i]) {
					min = fuzzy.getLimits()[i];
				}
			}
		}
		return max - min;
	}
}
