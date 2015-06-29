package sinbad2.element;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.lang.builder.HashCodeBuilder;

import sinbad2.core.validator.Validator;
import sinbad2.core.workspace.Workspace;
import sinbad2.element.alternative.Alternative;
import sinbad2.element.alternative.listener.AlternativesChangeEvent;
import sinbad2.element.alternative.listener.EAlternativesChange;
import sinbad2.element.alternative.listener.IAlternativesChangeListener;
import sinbad2.element.criterion.Criterion;
import sinbad2.element.criterion.listener.CriteriaChangeEvent;
import sinbad2.element.criterion.listener.ECriteriaChange;
import sinbad2.element.criterion.listener.ICriteriaChangeListener;
import sinbad2.element.expert.Expert;
import sinbad2.element.expert.listener.EExpertsChange;
import sinbad2.element.expert.listener.ExpertsChangeEvent;
import sinbad2.element.expert.listener.IExpertsChangeListener;
import sinbad2.resolutionphase.io.XMLRead;

public class ProblemElementsSet implements Cloneable {
	
	private List<Expert> _experts;
	private List<Alternative> _alternatives;
	private List<Criterion> _criteria;
	
	private List<IExpertsChangeListener> _expertsListener;
	private List<IAlternativesChangeListener> _alternativesListener;
	private List<ICriteriaChangeListener> _criteriaListener;

	
	public ProblemElementsSet(){
		_experts = new LinkedList<Expert>();
		_alternatives = new LinkedList<Alternative>();
		_criteria = new LinkedList<Criterion>();
		
		_expertsListener = new LinkedList<IExpertsChangeListener>();
		_alternativesListener = new LinkedList<IAlternativesChangeListener>();
		_criteriaListener = new LinkedList<ICriteriaChangeListener>();
	}

	public List<Expert> getExperts() {
		return _experts;
	}
	
	public List<Alternative> getAlternatives() {
		return _alternatives;
	}
	
	public List<Criterion> getCriteria() {
		return _criteria;
	}
	
	public List<Criterion> getElements() {
		return _criteria;
	}
	
	public void setExperts(List<Expert> experts) {
		Validator.notNull(experts);
		
		_experts = experts;
		
		notifyExpertsChanges(new ExpertsChangeEvent(EExpertsChange.EXPERTS_CHANGES, null, _experts, false));
	}
	
	public void setAlternatives(List<Alternative> alternatives) {
		Validator.notNull(alternatives);
		
		_alternatives = alternatives;
		
		notifyAlternativesChanges(new AlternativesChangeEvent(EAlternativesChange.ALTERNATIVES_CHANGES, null, _alternatives, false));
	}
	
	public void setCriteria(List<Criterion> criteria) {
		Validator.notNull(criteria);
		
		_criteria = criteria;
		
		notifyCriteriaChanges(new CriteriaChangeEvent(ECriteriaChange.CRITERIA_CHANGES, null, _criteria, false));
		
	}

	public void addExpert(Expert expert, Boolean hasParent) {
		
		if(!hasParent) {
			_experts.add(expert);
			Collections.sort(_experts);
		}
		
		notifyExpertsChanges(new ExpertsChangeEvent(EExpertsChange.ADD_EXPERT, null, expert, false));
		
	}
	
	public void addAlternative(Alternative alternative) {
		
		_alternatives.add(alternative);
		Collections.sort(_alternatives);
		
		notifyAlternativesChanges(new AlternativesChangeEvent(EAlternativesChange.ADD_ALTERNATIVE, null, alternative, false));
		
	}
	
	public void addCriterion(Criterion criterion, Boolean hasParent) {
		
		if(!hasParent) {
			_criteria.add(criterion);
			Collections.sort(_criteria);
		}
		
		notifyCriteriaChanges(new CriteriaChangeEvent(ECriteriaChange.ADD_CRITERION, null, criterion, false));
		
	}
	
	public void moveExpert(Expert moveExpert, Expert newParent, Expert oldParent) {
		
		if(oldParent == null) {
			_experts.remove(moveExpert);
			newParent.addChildren(moveExpert);
		} else {
			oldParent.removeChildren(moveExpert);
			if(newParent == null) {
				addExpert(moveExpert, false);
			} else {
				newParent.addChildren(moveExpert);
			}
		}
		
		notifyExpertsChanges(new ExpertsChangeEvent(EExpertsChange.MOVE_EXPERT, oldParent, moveExpert, false));
	}
	
	public void moveCriterion(Criterion moveCriterion, Criterion newParent, Criterion oldParent) {
		
		if(oldParent == null) {
			_criteria.remove(moveCriterion);
			newParent.addSubcriterion(moveCriterion);
		} else {
			oldParent.removeSubcriterion(moveCriterion);
			if(newParent == null) {
				addCriterion(moveCriterion, false);
			} else {
				newParent.addSubcriterion(moveCriterion);
			}
		}
		
		notifyCriteriaChanges(new CriteriaChangeEvent(ECriteriaChange.MOVE_CRITERION, oldParent, moveCriterion, false));
	}
	
	public void addMultipleExperts(List<Expert> insertExperts, Boolean hasParent) {
		Expert parent = insertExperts.get(0).getParent();
		
		for(Expert expert: insertExperts) {	
			if(!hasParent) {
				_experts.add(expert);
			} else {
				parent.addChildren(expert);
			}
		}
		
		notifyExpertsChanges(new ExpertsChangeEvent(EExpertsChange.REMOVE_MULTIPLE_EXPERTS, null, insertExperts, false));
		
	}
	
	public void addMultipleAlternatives(List<Alternative> insertAlternatives) {
		
		for(Alternative alternative: insertAlternatives) {	
			_alternatives.add(alternative);
		
		}
		
		Collections.sort(_alternatives);
		
		notifyAlternativesChanges(new AlternativesChangeEvent(EAlternativesChange.ADD_MULTIPLE_ALTERNATIVES, null, insertAlternatives, false));
		
	}
	
	public void addMultipleCriteria(List<Criterion> insertCriteria, Boolean hasParent) {
		Criterion parent = insertCriteria.get(0).getParent();
		
		for(Criterion criterion: insertCriteria) {	
			if(!hasParent) {
				_criteria.add(criterion);
			} else {
				parent.addSubcriterion(criterion);
			}
		}
		
		
		notifyCriteriaChanges(new CriteriaChangeEvent(ECriteriaChange.ADD_CRITERIA, null, insertCriteria, false));
		
	}
	
	public void removeExpert(Expert expert, Boolean hasParent) {
		
		if(!hasParent) {
			_experts.remove(expert);
			Collections.sort(_experts);
		}
		
		notifyExpertsChanges(new ExpertsChangeEvent(EExpertsChange.REMOVE_EXPERT, expert, null, false));
		
	}
	
	public void removeAlternative(Alternative alternative) {
		
		_alternatives.remove(alternative);
		Collections.sort(_alternatives);
		
		notifyAlternativesChanges(new AlternativesChangeEvent(EAlternativesChange.REMOVE_ALTERNATIVE, alternative, null, false));
		
	}
	
	public void removeCriterion(Criterion criterion, Boolean hasParent) {
		
		if(!hasParent) {
			_criteria.remove(criterion);
			Collections.sort(_criteria);
		}
		
		notifyCriteriaChanges(new CriteriaChangeEvent(ECriteriaChange.REMOVE_CRITERION, criterion, null, false));
	}
	
	public void removeMultipleExperts(List<Expert> removeExperts, Boolean hasParent) {
		Expert parent = removeExperts.get(0).getParent();
		
		for(Expert expert: removeExperts) {	
			if(!hasParent) {
				_experts.remove(expert);
			} else {
				parent.removeChildren(expert);
				expert.setParent(parent);
			}
		}
		
		notifyExpertsChanges(new ExpertsChangeEvent(EExpertsChange.REMOVE_MULTIPLE_EXPERTS, removeExperts, null, false));
		
	}
	
	public void removeMultipleAlternatives(List<Alternative> removeAlternatives) {
		
		for(Alternative alternative: removeAlternatives) {
			_alternatives.remove(alternative);
			
		}
		
		Collections.sort(_alternatives);
		
		notifyAlternativesChanges(new AlternativesChangeEvent(EAlternativesChange.REMOVE_MULTIPLE_ALTERNATIVES, removeAlternatives, null, false));	
	
	}
	
	public void removeMultipleCriteria(List<Criterion> removeCriteria, Boolean hasParent) {
		Criterion parent = removeCriteria.get(0).getParent();
		
		for(Criterion criterion: removeCriteria) {
			if(!hasParent) {
				_criteria.remove(criterion);
			} else {
				parent.removeSubcriterion(criterion);
				criterion.setParent(parent);
			}
		}
		
		notifyCriteriaChanges(new CriteriaChangeEvent(ECriteriaChange.REMOVE_CRITERIA, removeCriteria, null, false));
		
	}
	
	public void modifyExpert(Expert modifyExpert, String id) {
		Expert oldExpert = (Expert) modifyExpert.clone();
		modifyExpert.setId(id);
		
		
		notifyExpertsChanges(new ExpertsChangeEvent(EExpertsChange.MODIFY_EXPERT, oldExpert, modifyExpert, false));
		
	}
	
	public void modifyAlternative(Alternative modifyAlternative, String id) {
		Alternative oldAlternative = (Alternative) modifyAlternative.clone();
		modifyAlternative.setId(id);
		
		Collections.sort(_alternatives);
		
		notifyAlternativesChanges(new AlternativesChangeEvent(EAlternativesChange.MODIFY_ALTERNATIVE, oldAlternative, modifyAlternative, false));
		
	}
	
	public void modifyCriterion(Criterion modifyCriterion, String id, Boolean newCost) {
		Criterion oldCriterion = (Criterion) modifyCriterion.clone();
		modifyCriterion.setId(id);
		modifyCriterion.setCost(newCost);
		
		notifyCriteriaChanges(new CriteriaChangeEvent(ECriteriaChange.MODIFY_CRITERION, oldCriterion, modifyCriterion, false));
	}
	
	public void registerExpertsChangesListener(IExpertsChangeListener listener) {
		_expertsListener.add(listener);
	}
	
	public void unregisterExpertsChangeListener(IExpertsChangeListener listener) {
		_expertsListener.remove(listener);
	}
	
	public void registerAlternativesChangesListener(IAlternativesChangeListener listener) {
		_alternativesListener.add(listener);
	}
	
	public void unregisterAlternativesChangeListener(IAlternativesChangeListener listener) {
		_alternativesListener.remove(listener);
	}
	
	public void registerCriteriaChangeListener(ICriteriaChangeListener listener) {
		_criteriaListener.add(listener);
	}
	
	public void unregisterCriteriaChangeListener(ICriteriaChangeListener listener) {
		_criteriaListener.remove(listener);
	}
	
	public void notifyExpertsChanges(ExpertsChangeEvent event) {
		for(IExpertsChangeListener listener: _expertsListener) {
			listener.notifyExpertsChange(event);
		}
		
		Workspace.getWorkspace().updateHashCode();
	}
	
	public void notifyAlternativesChanges(AlternativesChangeEvent event) {
		for(IAlternativesChangeListener listener: _alternativesListener) {
			listener.notifyAlternativesChange(event);
		}
		
		Workspace.getWorkspace().updateHashCode();
	}
	
	public void notifyCriteriaChanges(CriteriaChangeEvent event) {
		for(ICriteriaChangeListener listener: _criteriaListener) {
			listener.notifyCriteriaChange(event);
		}
		
		Workspace.getWorkspace().updateHashCode();
	}
	
	public void clear() {
		if(!_experts.isEmpty()) {
			_experts.clear();
			notifyExpertsChanges(new ExpertsChangeEvent(EExpertsChange.EXPERTS_CHANGES, null, _experts, false));
		}
		
		if(!_alternatives.isEmpty()) {
			_alternatives.clear();
			notifyAlternativesChanges(new AlternativesChangeEvent(EAlternativesChange.ALTERNATIVES_CHANGES, null, _alternatives, false));
		}
		
		if(!_criteria.isEmpty()) {
			_criteria.clear();
			notifyCriteriaChanges(new CriteriaChangeEvent(ECriteriaChange.CRITERIA_CHANGES, null, _criteria, false));
		}
	}
	
	public void save(XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement("elements"); //$NON-NLS-1$

		writer.writeStartElement("experts"); //$NON-NLS-1$
		saveExperts(_experts, writer);
		writer.writeEndElement();

		writer.writeStartElement("alternatives"); //$NON-NLS-1$
		saveAlternatives(_alternatives, writer);
		writer.writeEndElement();

		writer.writeStartElement("criteria"); //$NON-NLS-1$
		saveCriteria(_criteria, writer);
		writer.writeEndElement();

		writer.writeEndElement();
	}

	public void read(XMLRead reader) throws XMLStreamException {
		reader.goToStartElement("elements"); //$NON-NLS-1$
		readExperts(reader);
		readAlternatives(reader);
		readCriteria(reader);
		reader.goToEndElement("elements"); //$NON-NLS-1$
	}

	public void readExperts(XMLRead reader) throws XMLStreamException {
		reader.goToStartElement("experts"); //$NON-NLS-1$

		XMLEvent event;
		String id;
		Expert expert = null;
		Expert parent = null;
		boolean end = false;
		while (reader.hasNext() && !end) {
			event = reader.next();

			if (event.isStartElement()) {
				if ("expert".equals(reader.getStartElementLocalPart())) { //$NON-NLS-1$
					id = reader.getStartElementAttribute("id"); //$NON-NLS-1$
					expert = new Expert(id);
					if (parent == null) {
						_experts.add(expert);
						parent = expert;
					} else {
						parent.addChildren(expert);
						parent = expert;
					}
				}
			} else if (event.isEndElement()) {
				if ("expert".equals(reader.getEndElementLocalPart())) { //$NON-NLS-1$
					parent = expert.getParent();
					expert = parent;
				} else if ("experts".equals(reader.getEndElementLocalPart())) { //$NON-NLS-1$
					end = true;
					Collections.sort(_experts);
				}
			}
		}
	}

	public void readAlternatives(XMLRead reader) throws XMLStreamException {
		reader.goToStartElement("alternatives"); //$NON-NLS-1$

		XMLEvent event;
		boolean end = false;
		while (reader.hasNext() && !end) {
			event = reader.next();

			if (event.isStartElement()) {
				if ("alternative".equals(reader.getStartElementLocalPart())) { //$NON-NLS-1$
					_alternatives.add(new Alternative(reader
							.getStartElementAttribute("id"))); //$NON-NLS-1$
				}
			} else if (event.isEndElement()) {
				if ("alternatives".equals(reader.getEndElementLocalPart())) { //$NON-NLS-1$
					end = true;
					Collections.sort(_alternatives);
				}
			}
		}
	}

	public void readCriteria(XMLRead reader) throws XMLStreamException {
		reader.goToStartElement("criteria"); //$NON-NLS-1$

		XMLEvent event;
		String id;
		Boolean cost;
		Criterion criterion = null;
		Criterion parent = null;
		boolean end = false;
		while (reader.hasNext() && !end) {
			event = reader.next();

			if (event.isStartElement()) {
				if ("criterion".equals(reader.getStartElementLocalPart())) { //$NON-NLS-1$
					id = reader.getStartElementAttribute("id"); //$NON-NLS-1$
					cost = Boolean.parseBoolean(reader
							.getStartElementAttribute("cost")); //$NON-NLS-1$

					criterion = new Criterion(id);
					if (cost != null) {
						criterion.setCost(cost);
					}

					if (parent == null) {
						_criteria.add(criterion);
						parent = criterion;
					} else {
						parent.addSubcriterion(criterion);
						parent = criterion;
					}
				}
			} else if (event.isEndElement()) {
				if ("criterion".equals(reader.getEndElementLocalPart())) { //$NON-NLS-1$
					parent = criterion.getParent();
					criterion = parent;
				} else if ("criteria".equals(reader.getEndElementLocalPart())) { //$NON-NLS-1$
					end = true;
					Collections.sort(_criteria);
				}
			}
		}
	}

	@Override
	public int hashCode() {
		HashCodeBuilder hcb = new HashCodeBuilder(17, 31);
		for (Expert expert : _experts) {
			hcb.append(expert);
		}
		for (Alternative alternative : _alternatives) {
			hcb.append(alternative);
		}
		for (Criterion criterion : _criteria) {
			hcb.append(criterion);
		}
		return hcb.toHashCode();
	}

	
	@Override
	public Object clone() throws CloneNotSupportedException {
		
		ProblemElementsSet result = null;
		
		result = (ProblemElementsSet) super.clone();
		
		result._experts = new LinkedList<Expert>();
		for(Expert expert: _experts){
			result._experts.add((Expert) expert.clone());
		}
		
		result._alternatives = new LinkedList<Alternative>();
		for(Alternative alternative: _alternatives){
			result._alternatives.add((Alternative) alternative.clone());
		}
		
		result._criteria = new LinkedList<Criterion>();
		for(Criterion criterion: _criteria){
			result._criteria.add((Criterion) criterion.clone());
		}
		
		result._expertsListener = new LinkedList<IExpertsChangeListener>();
		for(IExpertsChangeListener listener: _expertsListener) {
			result._expertsListener.add(listener);
		}
		
		result._alternativesListener = new LinkedList<IAlternativesChangeListener>();
		for(IAlternativesChangeListener listener: _alternativesListener) {
			result._alternativesListener.add(listener);
		}
		
		result._criteriaListener = new LinkedList<ICriteriaChangeListener>();
		for(ICriteriaChangeListener listener: _criteriaListener) {
			result._criteriaListener.add(listener);
		}
		
		return result;
		
	}
	
	private void saveExperts(List<Expert> experts, XMLStreamWriter writer)
			throws XMLStreamException {
		for (Expert expert : experts) {
			writer.writeStartElement("expert"); //$NON-NLS-1$
			writer.writeAttribute("id", expert.getId()); //$NON-NLS-1$
			if (expert.hasChildrens()) {
				saveExperts(expert.getChildrens(), writer);
			}
			writer.writeEndElement();
		}
	}

	private void saveAlternatives(List<Alternative> alternatives,
			XMLStreamWriter writer) throws XMLStreamException {
		for (Alternative alternative : alternatives) {
			writer.writeStartElement("alternative"); //$NON-NLS-1$
			writer.writeAttribute("id", alternative.getId()); //$NON-NLS-1$
			writer.writeEndElement();
		}
	}

	private void saveCriteria(List<Criterion> criteria, XMLStreamWriter writer)
			throws XMLStreamException {
		for (Criterion criterion : criteria) {
			writer.writeStartElement("criterion"); //$NON-NLS-1$
			writer.writeAttribute("id", criterion.getId()); //$NON-NLS-1$
			writer.writeAttribute("cost", Boolean.toString(criterion.getCost())); //$NON-NLS-1$
			if (criterion.hasSubcriteria()) {
				saveCriteria(criterion.getSubcriteria(), writer);
			}
			writer.writeEndElement();
		}
	}
	
}