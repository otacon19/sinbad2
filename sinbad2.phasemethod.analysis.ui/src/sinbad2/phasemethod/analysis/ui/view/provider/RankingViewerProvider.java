package sinbad2.phasemethod.analysis.ui.view.provider;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import sinbad2.domain.linguistic.fuzzy.FuzzySet;
import sinbad2.element.ProblemElement;
import sinbad2.valuation.Valuation;
import sinbad2.valuation.elicit.ELICIT;
import sinbad2.valuation.hesitant.HesitantValuation;
import sinbad2.valuation.twoTuple.TwoTuple;
import sinbad2.valuation.unifiedValuation.UnifiedValuation;

public class RankingViewerProvider implements IStructuredContentProvider {
	
	private Map<ProblemElement, Valuation> _results;
	
	private class MyComparator implements Comparator<Object[]> {
		@Override
		public int compare(Object[] o1, Object[] o2) {
			if ((o1[0] instanceof HesitantValuation)) {
				return ((HesitantValuation) o1[0]).compareTo((HesitantValuation) o2[0]);
			} else if(o1[0] instanceof ELICIT) {
				return ((ELICIT) o1[0]).compareTo((ELICIT) o2[0]);
		    } else {
		    	return Double.compare((Double) o1[0], (Double) o2[0]);
		    }
		}
	}
	
	@Override
	public void dispose() {}

	@SuppressWarnings("unchecked")
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		_results = (Map<ProblemElement, Valuation>) newInput;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		int size = _results.size();
		if(size != 0) {
			Object[][] input = new Object[size][2];
			int pos = 0;
			for(ProblemElement alternative : _results.keySet()) {
				input[pos][0] = alternative.getId();
				input[pos][1] = _results.get(alternative);
				pos++;
			}
			
			List<Object[]> result = new LinkedList<Object[]>();
			if(input[0][1] != null) {
				boolean hesitant2Tuple = false;
				String alternativeName;
				Valuation valuation;
				TwoTuple twoTuple = null;
				Object[] listEntry = null;
				for (int i = 0; i < size; i++) {
					alternativeName = (String) input[i][0];
					valuation = (Valuation) input[i][1];
					if (valuation != null) {
						if (valuation instanceof UnifiedValuation) {
							twoTuple = ((UnifiedValuation) valuation).disunification((FuzzySet) valuation.getDomain());
						} else if(valuation instanceof TwoTuple) {
							twoTuple = (TwoTuple) valuation;
							listEntry = new Object[] {((TwoTuple) twoTuple).calculateInverseDelta(), alternativeName, twoTuple };
						} else if(valuation instanceof ELICIT) {
							hesitant2Tuple = true;
							listEntry = new Object[] {valuation, alternativeName, valuation };
						} else {
							listEntry = new Object[] {valuation, alternativeName, valuation };
						}
					} else {
						listEntry = new Object[] { 0d, alternativeName, null };
					}
					result.add(listEntry);
				}
		
				if(!hesitant2Tuple) {
					Collections.sort(result, new MyComparator());
					Collections.reverse(result);
				} else {
					result = ELICIT.rankingTrapezoidalFuzzyNumbers(result);
				}
				
				int ranking = 0;
				double previous = -1;
				for (Object[] element : result) {
					if ((element[0] instanceof HesitantValuation || element[0] instanceof ELICIT)) {
						element[0] = Integer.valueOf(++ranking);
				    } else if ((Double) element[0] == previous) {
						element[0] = ranking;
					} else {
						ranking++;
						previous = (Double) element[0];
						element[0] = ranking;
					}
				}
			}
			return result.toArray(new Object[0][0]);
		} else {
			return new Object[0];
		}
	}
}

