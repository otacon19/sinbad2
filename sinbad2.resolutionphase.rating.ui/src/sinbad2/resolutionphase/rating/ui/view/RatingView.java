package sinbad2.resolutionphase.rating.ui.view;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.SWTResourceManager;

import sinbad2.method.Method;
import sinbad2.method.MethodsManager;
import sinbad2.method.ui.MethodUI;
import sinbad2.method.ui.MethodsUIManager;
import sinbad2.phasemethod.PhasesMethodManager;
import sinbad2.phasemethod.ui.PhaseMethodUI;
import sinbad2.phasemethod.ui.PhaseMethodUIManager;
import sinbad2.resolutionphase.rating.ui.Images;
import sinbad2.resolutionphase.rating.ui.listener.IStepStateListener;
import sinbad2.resolutionphase.rating.ui.nls.Messages;
import sinbad2.resolutionphase.rating.ui.view.dialog.AlgorithmDialog;

public class RatingView extends ViewPart {
	
	public static final String ID = "flintstones.resolutionphase.rating.ui.view"; //$NON-NLS-1$
	
	private Composite _ratingEditorPanel;
	private Composite _ratingEditorFooter;
	private Composite _ratingEditorContainer;
	private Composite _buttonsBar;
	private Composite _parent;
	
	private Text _descriptionText;
	private Text _stepsText;
	
	private Label _methodNameFooterText;
	private Label _stepValue;
	private CLabel _warningLabel;
	private CLabel _methodLabelSelected;
	
	private ExpandBar _methodsCategoriesBar; 

	private MethodUI _methodUISelected;

	private Button _backButton;
	private Button _nextButton;
	private Button _resetButton;
	
	private CTabFolder _tabFolder;
	
	private int _numStep;
	private int _numPhase;
	
	private String _recommendedMethod;
	
	private Map<CLabel, Method> _methods;
	
	private MethodsUIManager _methodsUIManager;
	private PhasesMethodManager _phasesMethodManager;
	private PhaseMethodUIManager _phasesMethodUIManager;
	
	private List<IStepStateListener> _listeners;

	public RatingView() {}
	
	@Override
	public void createPartControl(Composite parent) {	
		_numPhase = 0;
		_numStep = 0;
		
		_methods = new HashMap<CLabel, Method>();
		
		_methodUISelected = null;
		
		_listeners = new LinkedList<IStepStateListener>();
		
		_parent = parent;
		
		_methodsUIManager = MethodsUIManager.getInstance();
		_phasesMethodManager = PhasesMethodManager.getInstance();
		_phasesMethodUIManager = PhaseMethodUIManager.getInstance();
		
		createRatingEditorPanel();
		createMethodSelectionStep();
		createContent();
	}

	private void createRatingEditorPanel() {
		_ratingEditorPanel = new Composite(_parent, SWT.BORDER);
		
		GridLayout ratingEditorPanelLayout = new GridLayout(1, false);
		ratingEditorPanelLayout.marginRight = 0;
		ratingEditorPanelLayout.verticalSpacing = 0;
		ratingEditorPanelLayout.marginWidth = 0;
		ratingEditorPanelLayout.marginHeight = 0;
		_ratingEditorPanel.setLayout(ratingEditorPanelLayout);
		
		_ratingEditorContainer = new Composite(_ratingEditorPanel, SWT.NONE);
		_ratingEditorContainer.setLayout(new GridLayout(1, false));
		_ratingEditorContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		_ratingEditorFooter = new Composite(_ratingEditorPanel, SWT.NONE);
		_ratingEditorFooter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		GridLayout ratingEditorFooterLayout = new GridLayout(4, false);
		ratingEditorFooterLayout.marginRight = 0;
		ratingEditorFooterLayout.verticalSpacing = 0;
		ratingEditorFooterLayout.marginWidth = 0;
		ratingEditorFooterLayout.marginHeight = 0;
		_ratingEditorFooter.setLayout(ratingEditorFooterLayout);
		
		_buttonsBar = new Composite(_ratingEditorFooter, SWT.NONE);
		GridData gridData = new GridData(SWT.CENTER, SWT.CENTER, true, false, 4, 1);
		_buttonsBar.setLayoutData(gridData);
		_buttonsBar.setLayout(new GridLayout(3, true));
		
		_backButton = new Button(_buttonsBar, SWT.PUSH);
		GridData gd_backbutton = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		_backButton.setLayoutData(gd_backbutton);
		_backButton.setText(Messages.RatingView_Back);
		_backButton.setEnabled(false);
		_backButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getPreviousStep();
			}
		});
		
		_nextButton = new Button(_buttonsBar, SWT.PUSH);
		_nextButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		_nextButton.setText(Messages.RatingView_Next);
		_nextButton.setEnabled(false);
		_nextButton.addSelectionListener(new SelectionAdapter() { 
			@Override
			public void widgetSelected(SelectionEvent e) {
				getNextStep();
			}	
		});
		
		_resetButton = new Button(_buttonsBar, SWT.PUSH);
		_resetButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		_resetButton.setText(Messages.RatingView_Reset);
		_resetButton.setEnabled(false);
		_resetButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				resetRating(true);
			}
		});
		
		Label separator = new Label(_ratingEditorFooter, SWT.SEPARATOR | SWT.HORIZONTAL);
		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1);
		gridData.verticalIndent = 5;
		separator.setLayoutData(gridData);
		
		Label method = new Label(_ratingEditorFooter, SWT.NONE);
		method.setLayoutData(new GridData(SWT.LEFT, SWT.LEFT, false, false, 1, 1));
		method.setFont(SWTResourceManager.getFont("Cantarell", 11, SWT.BOLD)); //$NON-NLS-1$
		method.setText(Messages.RatingView_Method_colon);
		
		_methodNameFooterText = new Label(_ratingEditorFooter, SWT.NONE);
		_methodNameFooterText.setLayoutData(new GridData(SWT.LEFT, SWT.LEFT, false, false, 1, 1));
		_methodNameFooterText.setText(Messages.RatingView_Unselected);
		
		Label step = new Label(_ratingEditorFooter, SWT.NONE);
		step.setLayoutData(new GridData(SWT.RIGHT, SWT.LEFT, true, false, 1, 1));
		step.setFont(SWTResourceManager.getFont("Cantarell", 11, SWT.BOLD)); //$NON-NLS-1$
		step.setText(Messages.RatingView_Step);
		
		_stepValue = new Label(_ratingEditorFooter, SWT.NONE);
		_stepValue.setLayoutData(new GridData(SWT.LEFT, SWT.LEFT, false, false, 1, 1));
		_stepValue.setText("(0/0)"); //$NON-NLS-1$
	}
	
	private void getPreviousStep() {
		if(_numStep != 0) {
			_numStep--;
			activateStep();
			_nextButton.setEnabled(true);
		}
		
		if(_numStep == 0) {
			_backButton.setEnabled(false);
		}
		decrementStep();
	}
	
	private void decrementStep() {
		String currentStep = _stepValue.getText().substring(_stepValue.getText().indexOf("(") + 1, _stepValue.getText().indexOf("/")); //$NON-NLS-1$ //$NON-NLS-2$
		String totalSteps = _stepValue.getText().substring(_stepValue.getText().indexOf("/") + 1, _stepValue.getText().indexOf(")")); //$NON-NLS-1$ //$NON-NLS-2$
		int currentStepNum = Integer.parseInt(currentStep);
		_stepValue.setText("(" + Integer.toString(currentStepNum - 1) + "/" + totalSteps +")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	private void getNextStep() {
		if((_numStep + 1) < _tabFolder.getItemCount()) {
			_numStep++;
			activateStep();
			_backButton.setEnabled(true);
		}
		
		if(_numStep + 1 == _tabFolder.getItemCount()) {
			_nextButton.setEnabled(false);
		}
		
		_resetButton.setEnabled(true);
		
		incrementStep();
	}
	
	private void incrementStep() {
		String currentStep = _stepValue.getText().substring(_stepValue.getText().indexOf("(") + 1, _stepValue.getText().indexOf("/")); //$NON-NLS-1$ //$NON-NLS-2$
		String totalSteps = _stepValue.getText().substring(_stepValue.getText().indexOf("/") + 1, _stepValue.getText().indexOf(")")); //$NON-NLS-1$ //$NON-NLS-2$
		int currentStepNum = Integer.parseInt(currentStep);
		_stepValue.setText("(" + Integer.toString(currentStepNum + 1) + "/" + totalSteps +")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void resetRating(boolean confirm) {
		boolean reset = true;
		if(confirm) {
			reset = MessageDialog.openConfirm(this.getSite().getShell(), Messages.RatingView_Cancel_confirm, Messages.RatingView_All_information_will_be_lost);
		}
		if(reset) {
			clearMethodSteps();
			_tabFolder.setSelection(0);
			createContent();
		}
		
		_resetButton.setEnabled(false);
	}

	private void createMethodSelectionStep() {
		if(_tabFolder == null) {
			_tabFolder = new CTabFolder(_ratingEditorContainer, SWT.BORDER | SWT.VERTICAL);
			_tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
	
			Display display = Display.getCurrent();
			_tabFolder.setSelectionBackground(display.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
			_tabFolder.layout();
			  
		    _tabFolder.addSelectionListener(new SelectionAdapter() {
		    	@Override
		    	public void widgetSelected(SelectionEvent e) {
		    		_numStep = _tabFolder.getSelectionIndex();
		    		if(_numStep == 0) {
		    			_backButton.setEnabled(false);
		    		} else if(_numStep == _tabFolder.getItemCount() - 1) {
		    			_nextButton.setEnabled(false);
		    		} else {
		    			_backButton.setEnabled(true);
		    			_nextButton.setEnabled(true);
		    		}
		    		
		    		activateStep();
		    	}
			});
		}
		
		CTabItem item = new CTabItem(_tabFolder, SWT.CLOSE, 0);
	    item.setText(Messages.RatingView_Method_selection);
	    item.setShowClose(false);
	}

	private void createContent() {
		Composite composite = new Composite(_tabFolder, SWT.NONE);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		composite.setLayoutData(gridData);

		GridLayout layout = new GridLayout(2, true);
		layout.horizontalSpacing = 15;
		composite.setLayout(layout);
		
		Composite compositeLeft = new Composite(composite, SWT.NONE);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		compositeLeft.setLayoutData(gridData);
		
		layout = new GridLayout(1, true);
		layout.horizontalSpacing = 15;
		compositeLeft.setLayout(layout);
		
		Label methodsLabel = new Label(compositeLeft, SWT.NONE);
		methodsLabel.setFont(SWTResourceManager.getFont("Cantarell", 10, SWT.BOLD)); //$NON-NLS-1$
		methodsLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		methodsLabel.setText(Messages.RatingView_Method_selection);
		
		_methodsCategoriesBar = new ExpandBar(compositeLeft, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		_methodsCategoriesBar.setLayoutData(gridData);
		
		createInfoPanels(composite);
		createWarningLabel(composite);
		
		String[] ids = _methodsUIManager.getIdsRegisters();
		
		Map<String, List<Method>> categoriesMethods = new HashMap<String, List<Method>>();
		List<Method> methods;
		for(String id: ids) {
			methods = new LinkedList<Method>();
			MethodUI methodUI = _methodsUIManager.getUI(id);
			Method method = methodUI.getMethod();
			String category = method.getCategory();
			if(categoriesMethods.get(category) != null) {
				methods = categoriesMethods.get(category);
			}
			methods.add(method);
			categoriesMethods.put(category, methods);
		}


		MethodsManager methodsManager = MethodsManager.getInstance();
		_recommendedMethod = methodsManager.getRecommendedMethod();
		
		int cont = 0;
		for(String key: categoriesMethods.keySet()) {
			createCategoryBar(key, cont,  categoriesMethods.get(key));
			cont++;
		}
		
		checkRecommendedMethod();
		
		Button showAlgorithmButton = new Button(compositeLeft, SWT.NONE);
		showAlgorithmButton.setVisible(false);
		showAlgorithmButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		showAlgorithmButton.setText(Messages.RatingView_Show_algorithm);
		showAlgorithmButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new AlgorithmDialog(Display.getCurrent().getActiveShell()).open();
			}
		});
		
		_tabFolder.getItem(0).setControl(composite);
	}
	
	private void createCategoryBar(String categoryName, int pos, List<Method> methods) {
		Composite composite = new Composite(_methodsCategoriesBar, SWT.NONE);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		composite.setLayoutData(gridData);
		GridLayout layout = new GridLayout();
		layout.marginLeft = 0;
		layout.marginTop = 0;
		layout.marginRight = 0;
		layout.marginBottom = 0;
		layout.verticalSpacing = 0;
		composite.setLayout(layout);
		
		for (int i = 0; i < methods.size(); i++) {
			createMethod(composite, methods.get(i));
		}

		ExpandItem categoryItem = new ExpandItem(_methodsCategoriesBar, SWT.NONE, pos);
		categoryItem.setText(categoryName);
		categoryItem.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		categoryItem.setControl(composite);
		categoryItem.setImage(Images.category);
	}
	
	private void createMethod(Composite parent, final Method currentMethod) {
		final CLabel label = new CLabel(parent, SWT.LEFT);

		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gridData.horizontalIndent = 15;
		label.setLayoutData(gridData);

		final String test = currentMethod.getImplementation().isAvailable();
		if(test.length() == 0 || test.equals(Messages.RatingView_Not_set_all_assignments)) {
			label.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN));
			label.setImage(Images.signed_yes);
			_nextButton.setEnabled(false);
		} else {	
			label.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED));
			label.setImage(Images.signed_no);
		}
		
		label.setFont(SWTResourceManager.getFont("Cantarell", 10, SWT.NORMAL)); //$NON-NLS-1$
		label.setText(currentMethod.getName());
		
		if(currentMethod.getName().equals(_recommendedMethod) && (!label.getForeground().equals(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED)))) {
			label.setText(label.getText() + Messages.RatingView_SUITABLE);
		}
		
		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {			
				CLabel labelSelected = (CLabel) e.getSource();
				if(_methodLabelSelected != null && _methodLabelSelected != labelSelected) {
					_methodLabelSelected.setFont(SWTResourceManager.getFont("Cantarell", 10, SWT.NORMAL)); //$NON-NLS-1$
				}
				_methodLabelSelected = labelSelected;
				_methodLabelSelected.setFont(SWTResourceManager.getFont("Cantarell", 10, SWT.BOLD)); //$NON-NLS-1$
				_methodNameFooterText.setText(_methodLabelSelected.getText());
				_methodNameFooterText.getParent().layout();
				
				selectMethod(currentMethod, _methodLabelSelected);
			}
		});
		
		label.pack();
		
		_methods.put(label, currentMethod);
	}
	
	private void calculateNumSteps() {
		List<PhaseMethodUI> phasesMethodUI = _methodUISelected.getPhasesUI();
		
		int numSteps = 0;
		for(PhaseMethodUI phase: phasesMethodUI) {
			numSteps += _phasesMethodUIManager.getStepsPhaseMethod(phase.getId()).size();
		}
		_stepValue.setText("(0/" + numSteps + ")"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void loadNextStep() {
		PhaseMethodUI currentPhaseMethod = _phasesMethodUIManager.getActiveResolutionPhasesUI();
		List<PhaseMethodUI> phasesMethodUI = _methodsUIManager.getActivateMethodUI().getPhasesUI();
		
		if(_listeners.size() == 0) {
			registerStepChangeListeners(_phasesMethodUIManager);
		}
		
		ViewPart step = null;
		if(currentPhaseMethod == null) {
			currentPhaseMethod = phasesMethodUI.get(_numPhase);
			if(currentPhaseMethod.getPhaseMethod().getImplementation().validate()) {
				activateCurrentPhaseMethod(currentPhaseMethod);
				step = _phasesMethodUIManager.getStepPhaseMethod(currentPhaseMethod.getId(), 0);
			}
		} else if(_phasesMethodUIManager.getNextStep() == null) {
			_numPhase++;
			if(_numPhase == phasesMethodUI.size()) {
				step = null;
			} else {
				currentPhaseMethod = phasesMethodUI.get(_numPhase);
				if(currentPhaseMethod.getPhaseMethod().getImplementation().validate()) {
					activateCurrentPhaseMethod(currentPhaseMethod);
					step = _phasesMethodUIManager.getStepPhaseMethod(currentPhaseMethod.getId(), 0);
				}
			}
		} else {
			step = _phasesMethodUIManager.getNextStep();
		}
		
		if(step != null) {
			
			_phasesMethodUIManager.activateStep(step);
			
			if(!checkLoadedSteps(step)) {
				
				CTabItem item = new CTabItem(_tabFolder, SWT.CLOSE, _tabFolder.getItemCount());
				item.setText(step.getPartName());
				item.setShowClose(false);
				item.setData("view", step); //$NON-NLS-1$
	
				Composite parent = new Composite(_tabFolder, SWT.NONE);
				
				step.createPartControl(parent);
				if(step instanceof IStepStateListener) {
					setRatingViewToStep((IStepStateListener) step);
				}
				
				item.setControl(parent);
				item.setData(step);
				
				_nextButton.setEnabled(true);
			}
		}
	}
	
	private boolean checkLoadedSteps(ViewPart step) {
		boolean loaded = false;
		for(CTabItem tabItem: _tabFolder.getItems()) {
			if(tabItem.getText().equals(step.getPartName())) {
				loaded = true;
				break;
			}
		}
		return loaded;
	}

	private void clearMethodSteps() {
		
		while(_tabFolder.getItemCount() > 1) {
			((ViewPart) _tabFolder.getItem(1).getData("view")).dispose(); //$NON-NLS-1$
			_tabFolder.getItem(1).dispose();
		}
		
		_listeners.clear();

		_numPhase = 0;
		_numStep = 0;
		
		_methodNameFooterText.setText(Messages.RatingView_Unselected);
		_stepValue.setText("(0/0)"); //$NON-NLS-1$
		
		_phasesMethodManager.clearPhases();
		_methodsUIManager.deactiveCurrentActive();
		
		_methodUISelected = null;
	}
	
	private void createInfoPanels(Composite composite) {
		Composite compositePanels = new Composite(composite, SWT.NONE);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gridData.verticalIndent = 2;
		compositePanels.setLayoutData(gridData);

		GridLayout layout = new GridLayout(1, true);
		layout.horizontalSpacing = 15;
		compositePanels.setLayout(layout);

		Label descriptionLabel = new Label(compositePanels, SWT.NONE);
		descriptionLabel.setFont(SWTResourceManager.getFont("Cantarell", 10, SWT.BOLD)); //$NON-NLS-1$
		descriptionLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		descriptionLabel.setText(Messages.RatingView_Method_description);

		_descriptionText = new Text(compositePanels, SWT.BORDER | SWT.READ_ONLY| SWT.MULTI | SWT.WRAP);
		gridData  = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gridData.verticalIndent = 0;
		gridData.heightHint = 120;
		_descriptionText.setLayoutData(gridData);

		Label stepsLabel = new Label(compositePanels, SWT.NONE);
		stepsLabel.setFont(SWTResourceManager.getFont("Cantarell", 10, SWT.BOLD)); //$NON-NLS-1$
		stepsLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		stepsLabel.setText(Messages.RatingView_Method_phases);

		_stepsText = new Text(compositePanels, SWT.BORDER | SWT.READ_ONLY | SWT.MULTI);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		_stepsText.setLayoutData(gridData);
	}
	
	private void createWarningLabel(Composite composite) {
		Composite compositeWarning = new Composite(composite, SWT.NONE);
		compositeWarning.setLayout(new GridLayout(1, true));
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		compositeWarning.setLayoutData(gridData);
		
		_warningLabel = new CLabel(compositeWarning, SWT.NONE);
		_warningLabel.setFont(SWTResourceManager.getFont("Occidental", 10, SWT.BOLD)); //$NON-NLS-1$
		gridData = new GridData(SWT.CENTER, SWT.FILL, true, true, 1, 1);
		_warningLabel.setLayoutData(gridData);
	}

	public void checkRecommendedMethod() {
		
		for(ExpandItem item: _methodsCategoriesBar.getItems()) {
			item.setExpanded(false);
		}
		
		String methodName;
		CLabel suitableLabel = null;
		for(ExpandItem item: _methodsCategoriesBar.getItems()) {
			Composite control = (Composite) item.getControl();
			Control[] childrens = control.getChildren();
			for(Control methodLabel : childrens) {
				((CLabel) methodLabel).setFont(SWTResourceManager.getFont("Cantarell", 10, SWT.NORMAL)); //$NON-NLS-1$
				((CLabel) methodLabel).pack();
				methodName = ((CLabel) methodLabel).getText();
				if(methodName.contains(Messages.RatingView_SUITABLE)) {
					suitableLabel = (CLabel) methodLabel;
					if(!item.getExpanded()) {
						item.setExpanded(true);
						((CLabel) methodLabel).setFont(SWTResourceManager.getFont("Cantarell", 10, SWT.BOLD)); //$NON-NLS-1$
						((CLabel) methodLabel).pack();
					}
				}
			}
		}

		Method methodToSelect = _methods.get(suitableLabel);
		if(methodToSelect != null) {
			selectMethod(methodToSelect, suitableLabel);
		}
	}
	
	private void selectMethod(Method methodToSelect, CLabel suitableLabel) {
		
		_descriptionText.setText(methodToSelect.getDescription());
		
		if(_methodsUIManager.getActivateMethodUI() != null) {
			clearMethodSteps();
		}

		_methodNameFooterText.setText(suitableLabel.getText().replace(Messages.RatingView_SUITABLE, "")); //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$
		_methodLabelSelected = suitableLabel;
		
		_methodsUIManager.activate(methodToSelect); //$NON-NLS-1$
		_methodUISelected = _methodsUIManager.getActivateMethodUI();
		_stepsText.setText(_methodUISelected.getPhasesFormat());
		
		String test = methodToSelect.getImplementation().isAvailable();
		if(suitableLabel != null) {
			if(!suitableLabel.getForeground().equals(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED))) {
				calculateNumSteps();
				loadNextStep();
				_warningLabel.setImage(null);
				_warningLabel.setText(""); //$NON-NLS-1$
			} else {
				_warningLabel.setImage(Images.error);
				_warningLabel.setText(test);
				_warningLabel.setForeground(new Color(_warningLabel.getParent().getDisplay(), new RGB(205, 65, 65)));
			}
			
			if(test.equals(Messages.RatingView_Not_set_all_assignments)) {
				_warningLabel.setImage(Images.warning);
				_warningLabel.setText(test);
				_warningLabel.setForeground(new Color(_warningLabel.getParent().getDisplay(), new RGB(255, 212, 0)));
			}
		}
		
		_warningLabel.pack();
		_warningLabel.getParent().layout();
	}
		
	private void activateStep() {
		_tabFolder.setSelection(_numStep);
		notifyNewStep();
	}
	
	public void disabledNextStep() {
		_nextButton.setEnabled(false);
	}
	
	@Override
	public void setFocus() {
		_tabFolder.setSelection(0);
	}
	
	public void disposeFollowingPhases(int numPhase) {
		if(_tabFolder.getItemCount() > numPhase) {
			int size = _tabFolder.getItemCount();
			for(int i = numPhase; i < size; ++i) {
				_tabFolder.getItem(_tabFolder.getItemCount() - 1).dispose();
				_numPhase--;
			}
			
			List<PhaseMethodUI> phasesMethodUI = _methodsUIManager.getActivateMethodUI().getPhasesUI();
			PhaseMethodUI currentPhaseMethod = phasesMethodUI.get(_numPhase);
			
			activateCurrentPhaseMethod(currentPhaseMethod);
			
			ViewPart step = _phasesMethodUIManager.getStepPhaseMethod(currentPhaseMethod.getId(), 1);
			_phasesMethodUIManager.activateStep(step);
		}
	}
	
	private void activateCurrentPhaseMethod(PhaseMethodUI phaseUI) {
		_phasesMethodManager.activate(phaseUI.getPhaseMethod().getId());
		_phasesMethodUIManager.activate(phaseUI.getId());
	}
	
	public void registerStepChangeListeners(PhaseMethodUIManager phasesMethodUIManager) {
		Map<String, List<ViewPart>> phasesSteps = phasesMethodUIManager.getPhasesSteps(); 
		for(String id: phasesSteps.keySet()) {
			List<ViewPart> views = phasesSteps.get(id);
			for(ViewPart view: views) {
				if(view instanceof IStepStateListener) {
					_listeners.add((IStepStateListener) view);
				}
			}
		}
	}
	
	public void unregisterStepChangeListener(IStepStateListener listener) {
		_listeners.remove(listener);
	}

	private void notifyNewStep() {
		CTabItem item = _tabFolder.getSelection();
		if(item.getData() instanceof IStepStateListener) {
			((IStepStateListener) item.getData()).notifyStepStateChange();
		}
	}
	
	private void setRatingViewToStep(IStepStateListener step) {
		step.setRatingView(this);
	}	
}
