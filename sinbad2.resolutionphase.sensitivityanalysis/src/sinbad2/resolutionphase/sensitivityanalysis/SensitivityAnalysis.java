package sinbad2.resolutionphase.sensitivityanalysis;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang.builder.HashCodeBuilder;

import sinbad2.core.utils.Pair;
import sinbad2.core.workspace.WorkspaceContentPersistenceException;
import sinbad2.domain.Domain;
import sinbad2.element.ProblemElement;
import sinbad2.element.ProblemElementsManager;
import sinbad2.element.ProblemElementsSet;
import sinbad2.element.alternative.Alternative;
import sinbad2.element.criterion.Criterion;
import sinbad2.method.MethodsManager;
import sinbad2.phasemethod.PhasesMethodManager;
import sinbad2.phasemethod.aggregation.AggregationPhase;
import sinbad2.phasemethod.todim.resolution.ResolutionPhase;
import sinbad2.resolutionphase.IResolutionPhase;
import sinbad2.resolutionphase.io.XMLRead;
import sinbad2.resolutionphase.io.XMLWriter;
import sinbad2.resolutionphase.state.EResolutionPhaseStateChange;
import sinbad2.resolutionphase.state.ResolutionPhaseStateChangeEvent;
import sinbad2.valuation.Valuation;
import sinbad2.valuation.twoTuple.TwoTuple;

public class SensitivityAnalysis implements IResolutionPhase {

	public static final String ID = "flintstones.resolutionphase.sensitivityanalysis"; //$NON-NLS-1$

	private int _numberOfAlternatives;
	private int _numberOfCriteria;

	private Double[] _w;
	private Double[][] _decisionMatrix;

	private Double[] _alternativesFinalPreferences;
	private Double[][] _alternativesRatioFinalPreferences;

	private Integer[] _ranking;
	private Double[][][] _minimumAbsoluteChangeInCriteriaWeights;
	private Double[][][] _minimumPercentChangeInCriteriaWeights;
	private Double[][][] _absoluteThresholdValues;
	private Double[][][] _relativeThresholdValues;
	private List<Integer> _absoluteTop;
	private List<Integer> _absoluteAny;

	private ProblemElementsSet _elementsSet;

	private AggregationPhase _aggregationPhase;

	private EModel _model;
	private EProblem _problem;

	public List<ISensitivityAnalysisChangeListener> _listeners;

	public SensitivityAnalysis() {
		_w = null;

		_absoluteTop = new LinkedList<Integer>();
		_absoluteAny = new LinkedList<Integer>();

		_listeners = new LinkedList<ISensitivityAnalysisChangeListener>();

		_model = EModel.WEIGHTED_SUM;
		_problem = EProblem.MOST_CRITICAL_CRITERION;
	}

	public int getNumAlternatives() {
		return _numberOfAlternatives;
	}

	public void setNumAlternatives(int numberOfAlternatives) {
		_numberOfAlternatives = numberOfAlternatives;
	}

	public int getNumCriteria() {
		return _numberOfCriteria;
	}

	public void setNumCriteria(int numberOfCriteria) {
		_numberOfCriteria = numberOfCriteria;
	}

	public Double[] getWeights() {
		return _w;
	}

	public void setWeights(Double[] w) {
		_w = w;
	}

	public Double[][] getDecisionMatrix() {
		return _decisionMatrix;
	}

	public void setDecisionMatrix(Double[][] dm) {
		_decisionMatrix = dm;
	}

	public EModel getModel() {
		return _model;
	}

	public void setModel(EModel model) {
		_model = model;
	}

	public void setProblem(EProblem problem) {
		_problem = problem;
	}

	public EProblem getProblem() {
		return _problem;
	}

	public Double[] getAlternativesFinalPreferences() {
		return _alternativesFinalPreferences;
	}

	public void setAlternativesFinalPreferences(Double[] alternativesFinalPreferences) {
		_alternativesFinalPreferences = alternativesFinalPreferences;
	}

	public Double[][] getAlternativesRatioFinalPreferences() {
		return _alternativesRatioFinalPreferences;
	}

	public void setAlternativesRatioFinalPreferences(Double[][] alternativesRatioFinalPreferences) {
		_alternativesRatioFinalPreferences = alternativesRatioFinalPreferences;
	}

	public Integer[] getRanking() {
		return _ranking;
	}

	public void setRanking(Integer[] ranking) {
		_ranking = ranking;
	}

	public Double[][][] getMinimumAbsoluteChangeInCriteriaWeights() {
		return _minimumAbsoluteChangeInCriteriaWeights;
	}

	public void setMinimunAbsoluteChangeInCriteriaWeights(Double[][][] minimumAbsoluteChangeInCriteriaWeights) {
		_minimumAbsoluteChangeInCriteriaWeights = minimumAbsoluteChangeInCriteriaWeights;
	}

	public Double[][][] getMinimumPercentChangeInCriteriaWeights() {
		return _minimumPercentChangeInCriteriaWeights;
	}

	public void setMinimunPercentChangeInCriteriaWeights(Double[][][] minimumPercentChangeInCriteriaWeights) {
		_minimumPercentChangeInCriteriaWeights = minimumPercentChangeInCriteriaWeights;
	}

	public Double[][][] getAbsoluteThresholdValues() {
		return _absoluteThresholdValues;
	}

	public void setAbsoluteThresholdValues(Double[][][] absoluteThresholdValues) {
		_absoluteThresholdValues = absoluteThresholdValues;
	}

	public Double[][][] getRelativeThresholdValues() {
		return _relativeThresholdValues;
	}

	public void setRelativeThresholdValues(Double[][][] relativeThresholdValues) {
		_relativeThresholdValues = relativeThresholdValues;
	}

	public List<Integer> getAbsoluteTop() {
		return _absoluteTop;
	}

	public void setAbsoluteTop(List<Integer> absoluteTop) {
		_absoluteTop = absoluteTop;
	}

	public List<Integer> getAbsoluteAny() {
		return _absoluteAny;
	}

	public void setAbsoluteAny(List<Integer> absoluteAny) {
		_absoluteAny = absoluteAny;
	}

	public String[] getAlternativesIds() {
		String[] alternativesIds = new String[_elementsSet.getAlternatives().size()];

		int cont = 0;
		for (Alternative a : _elementsSet.getAlternatives()) {
			alternativesIds[cont] = a.getId();
			cont++;
		}

		return alternativesIds;
	}

	public String[] getCriteriaIds() {
		String[] criteriaIds = new String[_elementsSet.getAllSubcriteria().size()];

		int cont = 0;
		for (Criterion c : _elementsSet.getAllSubcriteria()) {
			criteriaIds[cont] = c.getCanonicalId();
			cont++;
		}

		return criteriaIds;
	}

	@SuppressWarnings("unchecked")
	public void calculateDecisionMatrix(List<Double> weights) {
		_numberOfAlternatives = _elementsSet.getAlternatives().size();
		_numberOfCriteria = _elementsSet.getAllSubcriteria().size();
		
		if (MethodsManager.getInstance().getActiveMethod().getId().contains("todim")) {
			if(weights != null) {
				assignWeights(weights);
			}
			
			computeTODIM();
			computeMulticriteriaProblem();
		} else {
			if (weights != null) {
				assignWeights(weights);
			} else if (_w == null) {
				Map<ProblemElement, Object> criteriaOperatorWeights = _aggregationPhase.getCriteriaOperatorWeights();
				if (!criteriaOperatorWeights.isEmpty()) {
					if (criteriaOperatorWeights.get(null) == null) {
						createDefaultWeights();
					} else if (criteriaOperatorWeights.size() == 1) {
						assignWeights((List<Double>) criteriaOperatorWeights.get(null));
					} else {
						assignWeights(getSubcriteriaWeights());
					}
				} else {
					createDefaultWeights();
				}
			}

			compute();
		}
	}

	private void computeTODIM() {
		int numWeight = 0;

		ResolutionPhase todimPhase = (ResolutionPhase) PhasesMethodManager.getInstance().getPhaseMethod(ResolutionPhase.ID).getImplementation();
		todimPhase.setConsensusMatrix(_decisionMatrix);

		Map<Criterion, Double> criteriaWeights = new HashMap<Criterion, Double>();
		for (Criterion c : _elementsSet.getAllCriteria()) {
			criteriaWeights.put(c, _w[numWeight]);
			numWeight++;
		}

		todimPhase.setCriteriaWeights(criteriaWeights);
		todimPhase.calculateRelativeWeights();
		todimPhase.calculateDominanceDegreeByCriterionCenterOfGravity();
		todimPhase.calculateDominaceDegreeAlternatives();
		Map<Alternative, Double> globalDominance = todimPhase.calculateGlobalDominance();
		int alternative = 0;
		for (Alternative a : _elementsSet.getAlternatives()) {
			_alternativesFinalPreferences[alternative] = globalDominance.get(a);
			alternative++;
		}
	}

	private Double[] computeTODIMInference(double[] weights) {
		int numWeight = 0;

		ResolutionPhase todimPhase = (ResolutionPhase) PhasesMethodManager.getInstance().getPhaseMethod(ResolutionPhase.ID).getImplementation();
		todimPhase.setConsensusMatrix(_decisionMatrix);

		Map<Criterion, Double> criteriaWeights = new HashMap<Criterion, Double>();
		for (Criterion c : _elementsSet.getAllCriteria()) {
			criteriaWeights.put(c, weights[numWeight]);
			numWeight++;
		}

		todimPhase.setCriteriaWeights(criteriaWeights);
		todimPhase.calculateRelativeWeights();
		todimPhase.calculateDominanceDegreeByCriterionCenterOfGravity();
		todimPhase.calculateDominaceDegreeAlternatives();
		Map<Alternative, Double> globalDominance = todimPhase.calculateGlobalDominance();
		int alternative = 0;
		Double[] alternativesFinalPreferences = new Double[_numberOfAlternatives];
		for (Alternative a : _elementsSet.getAlternatives()) {
			alternativesFinalPreferences[alternative] = globalDominance.get(a);
			alternative++;
		}

		return alternativesFinalPreferences;
	}

	@SuppressWarnings("unchecked")
	private List<Double> getSubcriteriaWeights() {
		List<Double> globalWeights = new LinkedList<Double>();
		Map<ProblemElement, Object> criteriaWeights = new HashMap<ProblemElement, Object>();

		criteriaWeights = _aggregationPhase.getCriteriaOperatorWeights();
		globalWeights = ((Map<Object, List<Double>>) criteriaWeights.get(null)).get(null);

		return calculateWeightsSubcriteria(globalWeights, criteriaWeights);
	}

	@SuppressWarnings("unchecked")
	private List<Double> calculateWeightsSubcriteria(List<Double> globalWeights,
			Map<ProblemElement, Object> elementWeights) {
		List<Double> subcriteriaWeights = new LinkedList<Double>();
		int numC = -1, cont = 0;
		double result;

		for (Criterion c : _elementsSet.getCriteria()) {
			numC++;
			if (c.hasSubcriteria()) {
				for (Criterion sc : c.getSubcriteria()) {
					result = globalWeights.get(numC);
					result *= ((Map<Object, List<Double>>) elementWeights.get(c)).get(null).get(cont);
					result *= recursiveWeightCriterion(sc, cont);
					cont++;

					subcriteriaWeights.add(Math.round(result * 1000d) / 1000d);
				}
			} else {
				result = globalWeights.get(numC);
				subcriteriaWeights.add(Math.round(result * 1000d) / 1000d);
			}
		}

		return subcriteriaWeights;
	}

	@SuppressWarnings("unchecked")
	private double recursiveWeightCriterion(Criterion c, int cont) {

		if (c.hasSubcriteria()) {
			for (Criterion sc : c.getSubcriteria()) {
				recursiveWeightCriterion(sc, cont);
			}
		}

		List<Double> weightsCriterion = null;
		if (_aggregationPhase.getCriteriaOperatorWeights().get(c) != null) {
			weightsCriterion = ((Map<Object, List<Double>>) _aggregationPhase.getCriteriaOperatorWeights().get(c))
					.get(null);
		}

		if (weightsCriterion != null) {
			return weightsCriterion.get(cont);
		} else {
			return 1;
		}
	}

	private void assignWeights(List<Double> weights) {
		_w = new Double[_numberOfCriteria];
		for (int i = 0; i < weights.size(); ++i) {
			_w[i] = weights.get(i);
		}
	}

	private void createDefaultWeights() {
		_w = new Double[_numberOfCriteria];
		double tempW = 1d / (double) _numberOfCriteria;
		for (int i = 0; i < _numberOfCriteria; i++) {
			_w[i] = tempW;
		}
	}

	/*
	 * private void normalize(Double[] values) {
	 * 
	 * double sum = 0;
	 * 
	 * for (double value : values) { sum += value; }
	 * 
	 * if (sum != 0) { for (int i = 0; i < values.length; i++) { values[i] /=
	 * sum; } } }
	 */

	private void computeFinalPreferences() {

		_alternativesFinalPreferences = new Double[_numberOfAlternatives];

		for (int alternative = 0; alternative < _numberOfAlternatives; alternative++) {
			_alternativesFinalPreferences[alternative] = 0d;
			for (int criterion = 0; criterion < _numberOfCriteria; criterion++) {
				_alternativesFinalPreferences[alternative] += _decisionMatrix[criterion][alternative] * _w[criterion];
			}
		}

		computeRanking();
	}

	private void computeRanking() {

		_ranking = new Integer[_numberOfAlternatives];

		List<Double> preferences = new LinkedList<Double>();
		for (double preference : _alternativesFinalPreferences) {
			preferences.add(new Double(preference));
		}

		Collections.sort(preferences);
		Collections.reverse(preferences);

		int rankingPos = 0;
		double previousPreference = 0;

		for (double preference : preferences) {
			if (preference != previousPreference) {
				rankingPos++;
				for (int alternative = 0; alternative < _numberOfAlternatives; alternative++) {
					if (_alternativesFinalPreferences[alternative] == preference) {
						_ranking[alternative] = rankingPos;
					}
				}
				previousPreference = preference;
			}
		}
	}

	private void computeFinalPreferencesWeightedProduct() {

		_alternativesRatioFinalPreferences = new Double[_numberOfAlternatives][_numberOfAlternatives];

		for (int alternative1 = 0; alternative1 < _numberOfAlternatives - 1; alternative1++) {
			for (int alternative2 = (alternative1 + 1); alternative2 < _numberOfAlternatives; alternative2++) {
				_alternativesRatioFinalPreferences[alternative1][alternative2] = 1d;
				for (int criterion = 0; criterion < _numberOfCriteria; criterion++) {
					_alternativesRatioFinalPreferences[alternative1][alternative2] *= Math.pow(
							(_decisionMatrix[criterion][alternative1] / _decisionMatrix[criterion][alternative2]),
							_w[criterion]);
				}
			}
		}

		computeRankingWeightedProductModel();
	}

	private void computeRankingWeightedProductModel() {

		_ranking = new Integer[_numberOfAlternatives];

		for (int i = 0; i < _ranking.length; ++i) {
			_ranking[i] = i + 1;
		}

		double ratio = 0;
		for (int alternative1 = 0; alternative1 < _numberOfAlternatives - 1; ++alternative1) {
			for (int alternative2 = (alternative1 + 1); alternative2 < _numberOfAlternatives; ++alternative2) {
				if (alternative2 > alternative1) {
					ratio = _alternativesRatioFinalPreferences[alternative1][alternative2];
					if (ratio < 1) {
						if (_ranking[alternative2] > _ranking[alternative1]) {
							if (_ranking[alternative2] - 1 == _ranking[alternative1]) {
								_ranking[alternative2] = _ranking[alternative1];
								_ranking[alternative1] += 1;
							} else {
								_ranking[alternative2] = _ranking[alternative1];
								incrementPosRanking(alternative2);
							}
						}
					}
				}
			}
		}
	}

	private void incrementPosRanking(int alternative2) {
		int rank = _ranking[alternative2];

		for (int alternative = 0; alternative < _ranking.length; ++alternative) {
			if (_ranking[alternative] >= rank && _ranking[alternative] < _ranking.length) {
				if (alternative != alternative2) {
					_ranking[alternative] += 1;
				}
			}
		}
	}
	
	private void computeMinimumAbsoluteChangeInCriteriaWeights() {
		_minimumAbsoluteChangeInCriteriaWeights = new Double[_numberOfAlternatives][_numberOfAlternatives][_numberOfCriteria];

		for (int i = 0; i < _numberOfAlternatives; i++) {
			for (int j = 0; j < _numberOfAlternatives; j++) {
				for (int k = 0; k < _numberOfCriteria; k++) {
					_minimumAbsoluteChangeInCriteriaWeights[i][j][k] = null;
				}
			}
		}

		for (int i = 0; i < _numberOfAlternatives - 1; i++) {
			for (int j = (i + 1); j < _numberOfAlternatives; j++) {
				for (int k = 0; k < _numberOfCriteria; k++) {
					if (_decisionMatrix[k][j] - _decisionMatrix[k][i] == 0) {
						_minimumAbsoluteChangeInCriteriaWeights[i][j][k] = null;
					} else {
						_minimumAbsoluteChangeInCriteriaWeights[i][j][k] = (_alternativesFinalPreferences[j] - _alternativesFinalPreferences[i]) 
								/ (_decisionMatrix[k][j] - _decisionMatrix[k][i]);
						if (_minimumAbsoluteChangeInCriteriaWeights[i][j][k] > _w[k]) {
							_minimumAbsoluteChangeInCriteriaWeights[i][j][k] = null;
						}
					}
				}
			}
		}
	}

	private void computeMinimumPercentChangeInCriteriaWeights() {
		_minimumPercentChangeInCriteriaWeights = new Double[_numberOfAlternatives][_numberOfAlternatives][_numberOfCriteria];

		for (int i = 0; i < _numberOfAlternatives; i++) {
			for (int j = 0; j < _numberOfAlternatives; j++) {
				for (int k = 0; k < _numberOfCriteria; k++) {
					if (_minimumAbsoluteChangeInCriteriaWeights[i][j][k] != null) {
						if (_w[k] == 0) {
							_minimumPercentChangeInCriteriaWeights[i][j][k] = null;
						} else {
							_minimumPercentChangeInCriteriaWeights[i][j][k] = _minimumAbsoluteChangeInCriteriaWeights[i][j][k]
									* (100d / _w[k]);
						}
					} else {
						_minimumPercentChangeInCriteriaWeights[i][j][k] = null;
					}
				}
			}
		}
	}

	private void computeWeightedProductMinimumAbsoluteChangeInCriteriaWeights() {
		_minimumAbsoluteChangeInCriteriaWeights = new Double[_numberOfAlternatives][_numberOfAlternatives][_numberOfCriteria];

		for (int i = 0; i < _numberOfAlternatives; i++) {
			for (int j = 0; j < _numberOfAlternatives; j++) {
				for (int k = 0; k < _numberOfCriteria; k++) {
					_minimumAbsoluteChangeInCriteriaWeights[i][j][k] = null;
				}
			}
		}

		double numerator, denominator, total;
		for (int i = 0; i < _numberOfAlternatives - 1; i++) {
			for (int j = i + 1; j < _numberOfAlternatives; j++) {
				for (int k = 0; k < _numberOfCriteria; k++) {
					numerator = Math.log(_alternativesRatioFinalPreferences[i][j]);
					if (_decisionMatrix[k][j] == 0) {
						_minimumAbsoluteChangeInCriteriaWeights[i][j][k] = null;
					} else {
						denominator = Math.log(_decisionMatrix[k][i] / _decisionMatrix[k][j]);
						if (denominator != 0) {
							total = numerator / denominator;
							if (total > _w[k]) {
								_minimumAbsoluteChangeInCriteriaWeights[i][j][k] = null;
							} else {
								_minimumAbsoluteChangeInCriteriaWeights[i][j][k] = total;
								if (_minimumAbsoluteChangeInCriteriaWeights[i][j][k] > _w[k]) {
									_minimumAbsoluteChangeInCriteriaWeights[i][j][k] = null;
								}
							}
						} else {
							_minimumAbsoluteChangeInCriteriaWeights[i][j][k] = null;
						}
					}
				}
			}
		}
	}

	private void computeWeightedProductMinimumPercentChangeInCriteriaWeights() {
		_minimumPercentChangeInCriteriaWeights = new Double[_numberOfAlternatives][_numberOfAlternatives][_numberOfCriteria];

		for (int i = 0; i < _numberOfAlternatives; i++) {
			for (int j = 0; j < _numberOfAlternatives; j++) {
				for (int k = 0; k < _numberOfCriteria; k++) {
					if (_minimumAbsoluteChangeInCriteriaWeights[i][j][k] != null) {
						if (_w[k] == 0) {
							_minimumPercentChangeInCriteriaWeights[i][j][k] = null;
						} else {
							_minimumPercentChangeInCriteriaWeights[i][j][k] = _minimumAbsoluteChangeInCriteriaWeights[i][j][k]
									* (1000d / _w[k]);
						}
					} else {
						_minimumPercentChangeInCriteriaWeights[i][j][k] = null;
					}
				}
			}
		}
	}

	private void computeAbsoluteThresholdValuesWeightedSumModel() {
		_absoluteThresholdValues = new Double[_numberOfAlternatives][_numberOfAlternatives][_numberOfCriteria];

		for (int i = 0; i < _numberOfAlternatives; i++) {
			for (int j = 0; j < _numberOfAlternatives; j++) {
				for (int k = 0; k < _numberOfCriteria; k++) {
					if (i != j) {
						if (_w[k] == 0) {
							_absoluteThresholdValues[i][j][k] = null;
						} else {
							_absoluteThresholdValues[i][j][k] = (_alternativesFinalPreferences[i]
									- _alternativesFinalPreferences[j]) / _w[k];

							if (_absoluteThresholdValues[i][j][k] > _w[k]) {
								_absoluteThresholdValues[i][j][k] = null;
							}
						}
					}
				}
			}
		}
	}

	private void computeRelativeThresholdValuesWeightedSumModel() {
		_relativeThresholdValues = new Double[_numberOfAlternatives][_numberOfAlternatives][_numberOfCriteria];

		for (int i = 0; i < _numberOfAlternatives; i++) {
			for (int j = 0; j < _numberOfAlternatives; j++) {
				for (int k = 0; k < _numberOfCriteria; k++) {
					if (_absoluteThresholdValues[i][j][k] != null) {
						if (_decisionMatrix[k][i] == 0) {
							_relativeThresholdValues[i][j][k] = null;
						} else {
							_relativeThresholdValues[i][j][k] = _absoluteThresholdValues[i][j][k]
									* (1000d / _decisionMatrix[k][i]);
						}
					} else {
						_relativeThresholdValues[i][j][k] = null;
					}
				}
			}
		}
	}

	private void computeAbsoluteThresholdValuesWeightedProductModel() {
		Double[][] auxAlternativesRatioPreferences = new Double[_numberOfAlternatives][_numberOfAlternatives];

		for (int alternative1 = 0; alternative1 < _numberOfAlternatives; alternative1++) {
			for (int alternative2 = 0; alternative2 < _numberOfAlternatives; alternative2++) {
				if (alternative1 != alternative2) {
					auxAlternativesRatioPreferences[alternative1][alternative2] = 1d;
					for (int criterion = 0; criterion < _numberOfCriteria; criterion++) {
						auxAlternativesRatioPreferences[alternative1][alternative2] *= Math.pow(
								(_decisionMatrix[criterion][alternative1] / _decisionMatrix[criterion][alternative2]),
								_w[criterion]);
					}
				}
			}
		}

		_absoluteThresholdValues = new Double[_numberOfAlternatives][_numberOfAlternatives][_numberOfCriteria];

		for (int i = 0; i < _numberOfAlternatives; i++) {
			for (int j = 0; j < _numberOfAlternatives; j++) {
				for (int k = 0; k < _numberOfCriteria; k++) {
					if (i != j) {
						if (_w[k] == 0) {
							_absoluteThresholdValues[i][j][k] = null;
						} else {
							_absoluteThresholdValues[i][j][k] = 1
									- Math.pow(auxAlternativesRatioPreferences[j][i], 1d / _w[k]);

							if (_absoluteThresholdValues[i][j][k] > _w[k]) {
								_absoluteThresholdValues[i][j][k] = null;
							}
						}
					}
				}
			}
		}
	}

	private void computeRelativeThresholdValuesWeightedProductModel() {
		_relativeThresholdValues = new Double[_numberOfAlternatives][_numberOfAlternatives][_numberOfCriteria];

		for (int i = 0; i < _numberOfAlternatives; i++) {
			for (int j = 0; j < _numberOfAlternatives; j++) {
				for (int k = 0; k < _numberOfCriteria; k++) {
					if (_absoluteThresholdValues[i][j][k] == null) {
						_relativeThresholdValues[i][j][k] = null;
					} else {
						_relativeThresholdValues[i][j][k] = _absoluteThresholdValues[i][j][k] * 1000d;
					}
				}
			}
		}
	}

	private void computeAbsoluteThresholdValuesAnalyticHierarchyProcess() {
		_absoluteThresholdValues = new Double[_numberOfAlternatives][_numberOfAlternatives][_numberOfCriteria];

		double denominator;
		for (int i = 0; i < _numberOfAlternatives; i++) {
			for (int j = 0; j < _numberOfAlternatives; j++) {
				for (int k = 0; k < _numberOfCriteria; k++) {
					if (i != j) {
						denominator = _alternativesFinalPreferences[i] - _alternativesFinalPreferences[j]
								+ _w[k] * (_decisionMatrix[k][j] - _decisionMatrix[k][i] + 1);
						if (denominator == 0) {
							_absoluteThresholdValues[i][j][k] = null;
						} else {
							_absoluteThresholdValues[i][j][k] = (_alternativesFinalPreferences[i]
									- _alternativesFinalPreferences[j]) / denominator;

							if (_absoluteThresholdValues[i][j][k] > _w[k]) {
								_absoluteThresholdValues[i][j][k] = null;
							}
						}
					}
				}
			}
		}
	}

	private void computeRelativeThresholdValuesAnalyticHierarchyProcess() {
		_relativeThresholdValues = new Double[_numberOfAlternatives][_numberOfAlternatives][_numberOfCriteria];

		for (int i = 0; i < _numberOfAlternatives; i++) {
			for (int j = 0; j < _numberOfAlternatives; j++) {
				for (int k = 0; k < _numberOfCriteria; k++) {
					if (_absoluteThresholdValues[i][j][k] != null) {
						if (_decisionMatrix[k][i] == 0) {
							_relativeThresholdValues[i][j][k] = null;
						} else {
							_relativeThresholdValues[i][j][k] = _absoluteThresholdValues[i][j][k]
									* (1000d / _decisionMatrix[k][i]);
						}
					} else {
						_relativeThresholdValues[i][j][k] = null;
					}
				}
			}
		}
	}

	private void computeAbsoluteTopCriticalCriterion() {
		List<Integer> bestAlternatives = new LinkedList<Integer>();

		for (int i = 0; i < _numberOfAlternatives; i++) {
			if (_ranking[i] == 1) {
				bestAlternatives.add(new Integer(i));
			}
		}

		Double minimum = null;
		Double aux = null;
		for (int i = 0; i < bestAlternatives.size(); i++) {
			for (int j = 0; j < _numberOfAlternatives; j++) {
				for (int k = 0; k < _numberOfCriteria; k++) {
					aux = _minimumAbsoluteChangeInCriteriaWeights[bestAlternatives.get(i)][j][k];
					if (aux != null) {
						aux = Math.abs(aux);
						if (minimum == null) {
							minimum = aux;
							_absoluteTop = new LinkedList<Integer>();
							_absoluteTop.add(new Integer(k));
						} else if (aux < minimum) {
							minimum = aux;
							_absoluteTop = new LinkedList<Integer>();
							_absoluteTop.add(new Integer(k));
						} else if (aux == minimum) {
							_absoluteTop.add(new Integer(k));
						}
					}
				}
			}

			for (int j = 0; j < _numberOfAlternatives; j++) {
				for (int k = 0; k < _numberOfCriteria; k++) {
					aux = _minimumAbsoluteChangeInCriteriaWeights[j][bestAlternatives.get(i)][k];
					if (aux != null) {
						aux = Math.abs(aux);
						if (minimum == null) {
							minimum = aux;
							_absoluteTop = new LinkedList<Integer>();
							_absoluteTop.add(new Integer(k));
						} else if (aux < minimum) {
							minimum = aux;
							_absoluteTop = new LinkedList<Integer>();
							_absoluteTop.add(new Integer(k));
						} else if (aux == minimum) {
							_absoluteTop.add(new Integer(k));
						}
					}
				}
			}
		}
	}

	private void computeAbsoluteAnyCriticalCriterion() {
		Double minimum = null;
		Double aux = null;

		for (int i = 0; i < _numberOfAlternatives; i++) {
			for (int j = 0; j < _numberOfAlternatives; j++) {
				for (int k = 0; k < _numberOfCriteria; k++) {
					aux = _minimumAbsoluteChangeInCriteriaWeights[i][j][k];
					if (aux != null) {
						aux = Math.abs(aux);
						if (minimum == null) {
							minimum = aux;
							_absoluteAny = new LinkedList<Integer>();
							_absoluteAny.add(new Integer(k));
						} else if (aux < minimum) {
							minimum = aux;
							_absoluteAny = new LinkedList<Integer>();
							_absoluteAny.add(new Integer(k));
						} else if (aux == minimum) {
							_absoluteAny.add(new Integer(k));
						}
					}
				}
			}
		}
	}

	public Double[] computeAlternativesFinalPreferenceInferWeights(double[] ws) {
		Double[] alternativeFinalPreferences = new Double[_numberOfAlternatives];

		if (MethodsManager.getInstance().getActiveMethod().getId().contains("todim")) {

			alternativeFinalPreferences = computeTODIMInference(ws);

		} else {

			for (int alternative = 0; alternative < _numberOfAlternatives; ++alternative) {
				alternativeFinalPreferences[alternative] = 0d;
				for (int criterion = 0; criterion < _numberOfCriteria; criterion++) {
					alternativeFinalPreferences[alternative] += _decisionMatrix[criterion][alternative] * ws[criterion];
				}
			}
		}
		
		return alternativeFinalPreferences;
	}

	public double computeAlternativeRatioFinalPreferenceInferWeights(int alternativeIndex1, int alternativeIndex2, double[] w) {
		double alternativeRatioFinalPreference = 1;

		for (int criterion = 0; criterion < _numberOfCriteria; criterion++) {
			alternativeRatioFinalPreference *= Math.pow((_decisionMatrix[criterion][alternativeIndex1] / _decisionMatrix[criterion][alternativeIndex2]),
					w[criterion]);
		}

		return alternativeRatioFinalPreference;
	}

	public double[] calculateInferWeights(Criterion criterion, double weightCriterionSelected) {
		List<Criterion> criteria = _elementsSet.getAllSubcriteria();
		double[] inferWeights = new double[criteria.size()];

		weightCriterionSelected = Math.round(weightCriterionSelected * 10000d) / 10000d;

		int indexCriterionSelected = criteria.indexOf(criterion);
		inferWeights[indexCriterionSelected] = weightCriterionSelected;
		for (int i = 0; i < criteria.size(); ++i) {
			if (i != indexCriterionSelected) {
				inferWeights[i] = ((1 - weightCriterionSelected) / (1 - _w[indexCriterionSelected])) * _w[i];
			}
		}

		return inferWeights;
	}

	public double[] getMinimumPercentPairAlternatives(int a1, int a2) {
		double[] percents = new double[_numberOfCriteria];

		switch (_problem) {
		case MOST_CRITICAL_CRITERION:
			for (int k = 0; k < _numberOfCriteria; k++) {
				if (_minimumPercentChangeInCriteriaWeights[a1][a2][k] != null) {
					percents[k] = _minimumPercentChangeInCriteriaWeights[a1][a2][k];
				}
			}
			break;
		case MOST_CRITICAL_MEASURE:
			for (int k = 0; k < _numberOfCriteria; k++) {
				if (_relativeThresholdValues[a1][a2][k] != null) {
					percents[k] = _relativeThresholdValues[a1][a2][k];
				}
			}
			break;
		}

		return percents;
	}

	public double[] getMinimumAbsolutePairAlternatives(int a1, int a2) {
		double[] absolute = new double[_numberOfCriteria];

		switch (_problem) {
		case MOST_CRITICAL_CRITERION:
			for (int k = 0; k < _numberOfCriteria; k++) {
				if (_minimumAbsoluteChangeInCriteriaWeights[a1][a2][k] != null) {
					absolute[k] = _minimumAbsoluteChangeInCriteriaWeights[a1][a2][k];
				}
			}
			break;
		case MOST_CRITICAL_MEASURE:
			for (int k = 0; k < _numberOfCriteria; k++) {
				if (_absoluteThresholdValues[a1][a2][k] != null) {
					absolute[k] = _absoluteThresholdValues[a1][a2][k];
				}
			}
			break;
		}

		return absolute;
	}

	public Map<Criterion, Map<Alternative, Double>> getMinimunPercentMCMByCriterion() {
		Map<Criterion, Map<Alternative, Double>> result = new LinkedHashMap<Criterion, Map<Alternative, Double>>();

		List<Alternative> alternatives = _elementsSet.getAlternatives();
		List<Criterion> criteria = _elementsSet.getAllSubcriteria();
		double min, max = Math.round(getMaximunPercentMCM());

		String numberS = Double.toString(max);
		numberS = numberS.substring(1, numberS.indexOf('.'));
		double units = Math.pow(10, numberS.length()) - Double.parseDouble(numberS);
		max += units;

		for (int c = 0; c < _numberOfCriteria; ++c) {
			for (int a1 = 0; a1 < _numberOfAlternatives; ++a1) {
				min = Double.MAX_VALUE;
				for (int a2 = 0; a2 < _numberOfAlternatives; ++a2) {
					if (_relativeThresholdValues[a1][a2][c] != null) {
						if (min > Math.abs(_relativeThresholdValues[a1][a2][c])) {
							min = Math.abs(_relativeThresholdValues[a1][a2][c]);
						}
					} else {
						if (a1 != a2) {
							if (min == Double.MAX_VALUE) {
								min = max;
							}
						}
					}
				}
				if (min != Double.MAX_VALUE) {
					if (result.get(criteria.get(c)) == null) {
						Map<Alternative, Double> minimunAlternative = new LinkedHashMap<Alternative, Double>();
						minimunAlternative.put(alternatives.get(a1), min);
						result.put(criteria.get(c), minimunAlternative);
					} else {
						Map<Alternative, Double> minimunAlternative = result.get(criteria.get(c));
						minimunAlternative.put(alternatives.get(a1), min);
					}
				}
			}
		}

		return result;
	}

	public double getMaximunPercentMCM() {
		double max = Double.MIN_VALUE;
		for (int c = 0; c < _numberOfCriteria; ++c) {
			for (int a1 = 0; a1 < _numberOfAlternatives; ++a1) {
				for (int a2 = 0; a2 < _numberOfAlternatives; ++a2) {
					if (_relativeThresholdValues[a1][a2][c] != null) {
						if (max < Math.abs(_relativeThresholdValues[a1][a2][c])) {
							max = Math.abs(_relativeThresholdValues[a1][a2][c]);
						}
					}
				}
			}
		}

		return max;
	}

	public Map<Criterion, Map<Alternative, Double>> getMinimunPercentMCCByCriterion() {
		Map<Criterion, Map<Alternative, Double>> result = new LinkedHashMap<Criterion, Map<Alternative, Double>>();
		Map<Integer, Double> minimunValueAlternatives = new LinkedHashMap<Integer, Double>();

		List<Alternative> alternatives = _elementsSet.getAlternatives();
		List<Criterion> criteria = _elementsSet.getAllSubcriteria();
		double min, max = Math.round(getMaximunPercentMCC());

		String numberS = Double.toString(max);
		numberS = numberS.substring(1, numberS.indexOf('.'));
		double units = Math.pow(10, numberS.length()) - Double.parseDouble(numberS);
		max += units;

		for (int c = 0; c < _numberOfCriteria; ++c) {
			minimunValueAlternatives.clear();
			for (int a1 = 0; a1 < _numberOfAlternatives; ++a1) {
				min = Double.MAX_VALUE;
				for (int a2 = 0; a2 < _numberOfAlternatives; ++a2) {
					if (_minimumPercentChangeInCriteriaWeights[a1][a2][c] != null) {
						if (min > Math.abs(_minimumPercentChangeInCriteriaWeights[a1][a2][c])) {
							min = Math.abs(_minimumPercentChangeInCriteriaWeights[a1][a2][c]);
						}
					} else {
						if (a1 != a2) {
							if (min == Double.MAX_VALUE) {
								if (_minimumPercentChangeInCriteriaWeights[a2][a1][c] == null) {
									min = max;
								} else {
									if (minimunValueAlternatives.get(a1) != null) {
										if (minimunValueAlternatives.get(a1) > Math
												.abs(_minimumPercentChangeInCriteriaWeights[a2][a1][c])) {
											min = Math.abs(_minimumPercentChangeInCriteriaWeights[a2][a1][c]);
										} else {
											min = minimunValueAlternatives.get(a1);
										}
									} else {
										min = Math.abs(_minimumPercentChangeInCriteriaWeights[a2][a1][c]);
									}
								}
							} else {
								if (_minimumPercentChangeInCriteriaWeights[a2][a1][c] != null) {
									if (Math.abs(_minimumPercentChangeInCriteriaWeights[a2][a1][c]) < min) {
										min = Math.abs(_minimumPercentChangeInCriteriaWeights[a2][a1][c]);
									}
								}
							}
						}
					}
				}

				if (min != Double.MAX_VALUE) {
					if (result.get(criteria.get(c)) == null) {
						Map<Alternative, Double> minimunAlternative = new LinkedHashMap<Alternative, Double>();
						minimunAlternative.put(alternatives.get(a1), min);
						result.put(criteria.get(c), minimunAlternative);
					} else {
						Map<Alternative, Double> minimunAlternative = result.get(criteria.get(c));
						minimunAlternative.put(alternatives.get(a1), min);
					}
					minimunValueAlternatives.put(a1, min);
				}
			}
		}

		return result;
	}

	public double getMaximunPercentMCC() {
		double max = Double.MIN_VALUE;
		for (int c = 0; c < _numberOfCriteria; ++c) {
			for (int a1 = 0; a1 < _numberOfAlternatives; ++a1) {
				for (int a2 = 0; a2 < _numberOfAlternatives; ++a2) {
					if (_minimumPercentChangeInCriteriaWeights[a1][a2][c] != null) {
						if (max < Math.abs(_minimumPercentChangeInCriteriaWeights[a1][a2][c])) {
							max = Math.abs(_minimumPercentChangeInCriteriaWeights[a1][a2][c]);
						}
					}
				}
			}
		}

		return max;
	}

	public void computeMulticriteriaProblem() {

		if (_problem == EProblem.MOST_CRITICAL_CRITERION) {
			computeMulticriteriaProblemCriticalCriterion();
		} else {
			computeMulticriteriaProblemCriticalMeasure();
		}
	}

	public void compute() {

		readDecisionMatrix();

		switch (_model) {
		case WEIGHTED_SUM:
			if (_problem == EProblem.MOST_CRITICAL_CRITERION) {
				computeWeightedSumModelCriticalCriterion();
			} else {
				computeWeightedSumModelCriticalMeasure();
			}
			break;
		case WEIGHTED_PRODUCT:
			if (_problem == EProblem.MOST_CRITICAL_CRITERION) {
				computeWeightedProductModelCriticalCriterion();
			} else {
				computeWeightedProductModelCriticalMeasure();
			}
			break;
		case ANALYTIC_HIERARCHY_PROCESS:
			if (_problem == EProblem.MOST_CRITICAL_CRITERION) {
				computeAnalyticHierarchyProcessModelCriticalCriterion();
			} else {
				computeAnalyticHierarchyProcessModelCriticalMeasure();
			}
			break;
		}
	}

	public void computeWeightedSumModelCriticalCriterion() {
		normalizeDecisionMatrix();

		computeFinalPreferences();
		computeMinimumAbsoluteChangeInCriteriaWeights();
		computeMinimumPercentChangeInCriteriaWeights();
		computeAbsoluteTopCriticalCriterion();
		computeAbsoluteAnyCriticalCriterion();

		notifySensitivityAnalysisChange();
	}

	public void computeMulticriteriaProblemCriticalCriterion() {
		normalizeDecisionMatrix();

		computeRanking();
		computeMinimumAbsoluteChangeInCriteriaWeights();
		computeMinimumPercentChangeInCriteriaWeights();
		computeAbsoluteTopCriticalCriterion();
		computeAbsoluteAnyCriticalCriterion();

		notifySensitivityAnalysisChange();
	}

	public void computeWeightedSumModelCriticalMeasure() {
		normalizeDecisionMatrix();

		computeFinalPreferences();
		computeAbsoluteThresholdValuesWeightedSumModel();
		computeRelativeThresholdValuesWeightedSumModel();

		notifySensitivityAnalysisChange();
	}

	public void computeMulticriteriaProblemCriticalMeasure() {
		normalizeDecisionMatrix();

		computeRanking();
		computeAbsoluteThresholdValuesWeightedSumModel();
		computeRelativeThresholdValuesWeightedSumModel();

		notifySensitivityAnalysisChange();
	}

	public void computeAnalyticHierarchyProcessModelCriticalCriterion() {
		normalizeDecisionMatrixSumToOne();

		computeFinalPreferences();
		computeMinimumAbsoluteChangeInCriteriaWeights();
		computeMinimumPercentChangeInCriteriaWeights();
		computeAbsoluteTopCriticalCriterion();
		computeAbsoluteAnyCriticalCriterion();

		notifySensitivityAnalysisChange();
	}

	public void computeAnalyticHierarchyProcessModelCriticalMeasure() {
		normalizeDecisionMatrixSumToOne();

		computeFinalPreferences();
		computeAbsoluteThresholdValuesAnalyticHierarchyProcess();
		computeRelativeThresholdValuesAnalyticHierarchyProcess();

		notifySensitivityAnalysisChange();
	}

	public void computeWeightedProductModelCriticalCriterion() {
		normalizeDecisionMatrix();

		computeFinalPreferencesWeightedProduct();
		computeWeightedProductMinimumAbsoluteChangeInCriteriaWeights();
		computeWeightedProductMinimumPercentChangeInCriteriaWeights();
		computeAbsoluteTopCriticalCriterion();
		computeAbsoluteAnyCriticalCriterion();

		notifySensitivityAnalysisChange();
	}

	public void computeWeightedProductModelCriticalMeasure() {
		normalizeDecisionMatrix();

		computeFinalPreferencesWeightedProduct();
		computeAbsoluteThresholdValuesWeightedProductModel();
		computeRelativeThresholdValuesWeightedProductModel();

		notifySensitivityAnalysisChange();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void readDecisionMatrix() {

		_decisionMatrix = new Double[_numberOfCriteria][_numberOfAlternatives];

		int i = 0, j = 0;

		Map<Pair<Alternative, Criterion>, Valuation> decisionMatrix = _aggregationPhase.getDecisionMatrix();
		for (Alternative a : _elementsSet.getAlternatives()) {
			j = 0;
			for (Criterion c : _elementsSet.getAllCriteria()) {
				_decisionMatrix[j][i] = ((TwoTuple) decisionMatrix.get(new Pair(a, c))).calculateInverseDelta();
				j++;
			}
			i++;
		}
	}

	private void normalizeDecisionMatrix() {
		if (!checkNormalizedMatrix()) {
			double acum, noStandarizedValue;
			for (int i = 0; i < _numberOfCriteria; ++i) {
				acum = sumCriteria(i);
				for (int j = 0; j < _numberOfAlternatives; ++j) {
					noStandarizedValue = _decisionMatrix[i][j];
					_decisionMatrix[i][j] = (double) Math.round((noStandarizedValue / acum) * 10000d) / 10000d;
				}
			}
		}
	}

	private boolean checkNormalizedMatrix() {
		for (int i = 0; i < _numberOfCriteria; ++i) {
			for (int j = 0; j < _numberOfAlternatives; ++j) {
				if (_decisionMatrix[i][j] > 1) {
					return false;
				}
			}
		}
		return true;
	}

	private double sumCriteria(int numCriterion) {
		double value = 0;
		for (int j = 0; j < _numberOfAlternatives; ++j) {
			value += Math.pow(_decisionMatrix[numCriterion][j], 2);
		}
		return Math.sqrt(value);
	}

	private void normalizeDecisionMatrixSumToOne() {
		double rowAcum;

		for (int c = 0; c < _numberOfCriteria; ++c) {
			rowAcum = 0;
			for (int a = 0; a < _numberOfAlternatives; ++a) {
				rowAcum += _decisionMatrix[c][a];
			}
			if (rowAcum != 1) {
				normalizeRow(c, Math.round(rowAcum * 10000d) / 10000d);
			}
		}
	}

	private void normalizeRow(int criterion, double rowAcum) {
		double value;
		for (int a = 0; a < _numberOfAlternatives; ++a) {
			value = Math.round((_decisionMatrix[criterion][a] / rowAcum) * 10000d) / 10000d;
			_decisionMatrix[criterion][a] = value;
		}
	}

	public Domain getDomain() {
		return _aggregationPhase.getUnifiedDomain();
	}

	public Object[] getAggregatedValuationsPosAndAlpha() {
		return _aggregationPhase.getAggregatedValuationsPosAndAlpha();
	}

	public void registerSensitivityAnalysisChangeListener(ISensitivityAnalysisChangeListener listener) {
		_listeners.add(listener);
	}

	public void unregisterSensitivityAnalysisChangeListener(ISensitivityAnalysisChangeListener listener) {
		_listeners.remove(listener);
	}

	public void notifySensitivityAnalysisChange() {
		for (ISensitivityAnalysisChangeListener listener : _listeners) {
			listener.notifySensitivityAnalysisChange();
		}
	}

	@Override
	public void notifyResolutionPhaseStateChange(ResolutionPhaseStateChangeEvent event) {

		if (event.getChange().equals(EResolutionPhaseStateChange.ACTIVATED)) {
			activate();
		}
	}

	@Override
	public IResolutionPhase copyStructure() {
		return new SensitivityAnalysis();
	}

	@Override
	public void copyData(IResolutionPhase iResolutionPhase) {
		SensitivityAnalysis sa = (SensitivityAnalysis) iResolutionPhase;

		clear();

		_absoluteAny = sa.getAbsoluteAny();
		_absoluteTop = sa.getAbsoluteTop();
		_alternativesFinalPreferences = sa.getAlternativesFinalPreferences();
		_decisionMatrix = sa.getDecisionMatrix();
		_minimumAbsoluteChangeInCriteriaWeights = sa.getMinimumAbsoluteChangeInCriteriaWeights();
		_minimumPercentChangeInCriteriaWeights = sa.getMinimumPercentChangeInCriteriaWeights();
		_numberOfAlternatives = sa.getNumAlternatives();
		_numberOfCriteria = sa.getNumCriteria();
		_ranking = sa.getRanking();
		_w = sa.getWeights();
	}

	@Override
	public void clear() {
		_absoluteAny.clear();
		_absoluteTop.clear();
		_alternativesFinalPreferences = null;
		_decisionMatrix = null;
		_minimumAbsoluteChangeInCriteriaWeights = null;
		_minimumPercentChangeInCriteriaWeights = null;
		_numberOfAlternatives = -1;
		_numberOfCriteria = -1;
		_ranking = null;
		_w = null;
	}

	@Override
	public void save(XMLWriter writer) throws WorkspaceContentPersistenceException {
		@SuppressWarnings("unused")
		XMLStreamWriter streamWriter = writer.getStreamWriter();
	}

	@Override
	public void read(XMLRead reader, Map<String, IResolutionPhase> buffer) throws WorkspaceContentPersistenceException {
	}

	@Override
	public int hashCode() {
		HashCodeBuilder hcb = new HashCodeBuilder(17, 31);

		return hcb.toHashCode();
	}

	@Override
	public IResolutionPhase clone() {
		SensitivityAnalysis result = null;

		try {
			result = (SensitivityAnalysis) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		return result;
	}

	@Override
	public boolean validate() {
		PhasesMethodManager pmm = PhasesMethodManager.getInstance();
		_aggregationPhase = (AggregationPhase) pmm.getPhaseMethod(AggregationPhase.ID).getImplementation();

		if (_elementsSet.getAlternatives().isEmpty()) {
			return false;
		}

		if (_elementsSet.getCriteria().isEmpty()) {
			return false;
		}

		if (_elementsSet.getExperts().isEmpty()) {
			return false;
		}

		if (_aggregationPhase.getCriteriaOperators().isEmpty() && _aggregationPhase.getExpertsOperators().isEmpty()) {
			return false;
		}

		Map<Pair<Alternative, Criterion>, Valuation> decisionMatrixAggregation = _aggregationPhase.getDecisionMatrix();
		for (Pair<Alternative, Criterion> pair : decisionMatrixAggregation.keySet()) {
			if (!(decisionMatrixAggregation.get(pair) instanceof TwoTuple)) {
				return false;
			}
		}

		if (MethodsManager.getInstance().getActiveMethod().getId().contains("todim")) {
			if (_alternativesFinalPreferences == null) {
				return false;
			}
		}

		return true;
	}

	@Override
	public void activate() {
		_elementsSet = ProblemElementsManager.getInstance().getActiveElementSet();
	}
}
