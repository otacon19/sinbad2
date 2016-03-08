package sinbad2.resolutionphase.rating.ui.view.dialog;

import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

import sinbad2.method.MethodsManager;

public class AlgorithmDialog extends Dialog {
	
	private final static String ALGORITHM = "# Require values #\nedNum = <edNum>\nedInt = <edInt>\nedLinUnb = <edLinUnb>\ntamEdLinLis = <tamEdLinLis>\nedLinList = <edLinList>\nedLin = <edLin>\n\n# Algorithm to select the suitable CWW methodology #\n 1: if (edLin[1].2T=true) and (tamEdLinLis=1) then\n 2:     return <1>\n 3: else if (edNum=true) or (edInt=true) then\n 4:     return <5>\n 5: else if (edLinUnb=true) then\n 6:     return <6>\n 7: else\n 8:     edLinListShortCard <-- short(edLinList,edLinList.card)\n 9:     i <-- 1\n10:     while i<tamEdLinLis do\n11:         if (edLinListShortCard.edLin[i].2T=false) then\n12:             return <2>\n13:         else if (edLinListShortCard[i+1].card != ((edLinListShortCard[i].card)-1)�2+1) then\n14:             return <4>\n15:         else\n16:             i <-- i+1\n17:         end if\n18:     end while\n19:     return <3>\n20: end if";

	private String _recommendedMethod;
	private Composite _container;
	private StyledText _algorithmText;
	private StyledText _algorithmInstantationText;
	
	private MethodsManager _methodsManager;

	public AlgorithmDialog(Shell parentShell) {
		super(parentShell);
		
		_methodsManager = MethodsManager.getInstance();
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		_container = (Composite) super.createDialogArea(parent);
		GridLayout gl__container = new GridLayout(2, true);
		gl__container.marginRight = 10;
		gl__container.marginTop = 10;
		gl__container.marginLeft = 10;
		_container.setLayout(gl__container);

		Label algorithmLabel = new Label(_container, SWT.NONE);
		algorithmLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		algorithmLabel.setFont(SWTResourceManager.getFont("Cantarell", 11, SWT.BOLD));
		algorithmLabel.setText("Algorithm");

		Label algorithmInstantationLabel = new Label(_container, SWT.NONE);
		algorithmInstantationLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		algorithmInstantationLabel.setFont(SWTResourceManager.getFont("Cantarell", 11, SWT.BOLD));
		algorithmInstantationLabel.setText("Algorithm instantation");

		_algorithmText = new StyledText(_container, SWT.BORDER | SWT.READ_ONLY | SWT.MULTI | SWT.WRAP);
		GridData gridData = new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1);
		gridData.verticalIndent = 0;
		_algorithmText.setFont(SWTResourceManager.getFont("Cantarell", 10, SWT.NONE));
		_algorithmText.setLayoutData(gridData);

		_algorithmInstantationText = new StyledText(_container, SWT.BORDER | SWT.READ_ONLY | SWT.MULTI | SWT.WRAP);
		gridData = new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1);
		gridData.verticalIndent = 0;
		_algorithmInstantationText.setFont(SWTResourceManager.getFont("Cantarell", 10, SWT.NONE));
		_algorithmInstantationText.setLayoutData(gridData);

		setAlgorithm();
		setAlgorithmInstantation();
		_algorithmText.pack();
		_algorithmInstantationText.pack();
		return _container;
	}

	private void setAlgorithm() {

		String algorithm = ALGORITHM;

		algorithm = algorithm.replace("<1>", "2-Tuple linguistic computational model");
		algorithm = algorithm.replace("<2>", "Fusion approach for managing multi-granular linguistic information");
		algorithm = algorithm.replace("<3>", "Linguistic Hierarchies");
		algorithm = algorithm.replace("<4>", "Extended Linguistic Hierarchies");
		algorithm = algorithm.replace("<5>", "Fusion approach for managing heterogeneous information");
		algorithm = algorithm.replace("<6>", "Methodology to deal with unbalanced linguistic term sets");

		Color BLACK = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);

		Color[] textColors = new Color[] { BLACK, BLACK, BLACK, BLACK, BLACK,
				BLACK, BLACK, BLACK, BLACK, BLACK, BLACK, BLACK, BLACK, BLACK,
				BLACK, BLACK, BLACK, BLACK, BLACK, BLACK, BLACK, BLACK, BLACK,
				BLACK, BLACK, BLACK, BLACK, BLACK };

		StyleRange[] textRanges = new StyleRange[textColors.length];
		int lineStart = 0;
		int lineLength = 0;
		String lines[] = algorithm.split("\n");
		for(int i = 0; i < textColors.length; i++) {
			lineLength = lines[i].length();
			textRanges[i] = new StyleRange(lineStart, lineLength,
					textColors[i], null);
			lineStart += lineLength + 1;
		}

		_algorithmText.setText(algorithm);

		if(textRanges != null) {

			_algorithmText.setStyleRanges(textRanges);

			int auxPos = algorithm.indexOf(" Require values ");
			_algorithmText.setStyleRange(new StyleRange(auxPos - 1, "# Require values #".length(), null, null, SWT.BOLD));

			auxPos = algorithm.indexOf(" Algorithm to select the suitable CWW methodology ");
			_algorithmText.setStyleRange(new StyleRange(auxPos - 1, "# Algorithm to select the suitable CWW methodology #".length(), null, null, SWT.BOLD));

			// if
			int initPos = 0;
			int newPos = 0;
			while(algorithm.indexOf(" if", initPos) != -1) {
				newPos = algorithm.indexOf(" if", initPos);
				_algorithmText.setStyleRange(new StyleRange(newPos, 3, null, null, SWT.BOLD));
				initPos = newPos + 3;
			}

			// else
			initPos = 0;
			newPos = 0;
			while(algorithm.indexOf(" else", initPos) != -1) {
				newPos = algorithm.indexOf(" else", initPos);
				_algorithmText.setStyleRange(new StyleRange(newPos, 5, null, null, SWT.BOLD));
				initPos = newPos + 5;
			}

			// end
			initPos = 0;
			newPos = 0;
			while(algorithm.indexOf(" end", initPos) != -1) {
				newPos = algorithm.indexOf(" end", initPos);
				_algorithmText.setStyleRange(new StyleRange(newPos, 4, null, null, SWT.BOLD));
				initPos = newPos + 4;
			}

			// while
			initPos = 0;
			newPos = 0;
			while(algorithm.indexOf(" while", initPos) != -1) {
				newPos = algorithm.indexOf(" while", initPos);
				_algorithmText.setStyleRange(new StyleRange(newPos, 6, null, null, SWT.BOLD));
				initPos = newPos + 6;
			}

			// return
			initPos = 0;
			newPos = 0;
			while(algorithm.indexOf(" return", initPos) != -1) {
				newPos = algorithm.indexOf(" return", initPos);
				_algorithmText.setStyleRange(new StyleRange(newPos, 7, null, null, SWT.BOLD));
				initPos = newPos + 7;
			}

			// then
			initPos = 0;
			newPos = 0;
			while(algorithm.indexOf(" then", initPos) != -1) {
				newPos = algorithm.indexOf(" then", initPos);
				_algorithmText.setStyleRange(new StyleRange(newPos, 5, null, null, SWT.BOLD));
				initPos = newPos + 5;
			}

			// do
			initPos = 0;
			newPos = 0;
			while(algorithm.indexOf(" do", initPos) != -1) {
				newPos = algorithm.indexOf(" do", initPos);
				_algorithmText.setStyleRange(new StyleRange(newPos, 3, null, null, SWT.BOLD));
				initPos = newPos + 3;
			}

			// and
			initPos = 0;
			newPos = 0;
			while(algorithm.indexOf(" and ", initPos) != -1) {
				newPos = algorithm.indexOf(" and ", initPos);
				_algorithmText.setStyleRange(new StyleRange(newPos, 5, null, null, SWT.BOLD));
				initPos = newPos + 5;
			}

			// or
			initPos = 0;
			newPos = 0;
			while(algorithm.indexOf(" or ", initPos) != -1) {
				newPos = algorithm.indexOf(" or ", initPos);
				_algorithmText.setStyleRange(new StyleRange(newPos, 4, null, null, SWT.BOLD));
				initPos = newPos + 4;
			}

			// true
			initPos = 0;
			newPos = 0;
			while(algorithm.indexOf("=true", initPos) != -1) {
				newPos = algorithm.indexOf("=true", initPos) + 1;
				_algorithmText.setStyleRange(new StyleRange(newPos, 4, null, null, SWT.BOLD));
				initPos = newPos + 4;
			}

			// false
			initPos = 0;
			newPos = 0;
			while(algorithm.indexOf("=false", initPos) != -1) {
				newPos = algorithm.indexOf("=false", initPos) + 1;
				_algorithmText.setStyleRange(new StyleRange(newPos, 5, null, null, SWT.BOLD));
				initPos = newPos + 4;
			}
		}
	}

	private void setAlgorithmInstantation() {

		String algorithm = ALGORITHM;

		boolean bestConditionsNumeric = _methodsManager.getBestConditionsNumeric();
		boolean edInt = _methodsManager.getBestConditionsNumeric();
		boolean bestConditionsUnbalanced = _methodsManager.getBestConditionsUnbalanced();
		int[] cardinalitiesFuzzySet = _methodsManager.getCardinalitiesFuzzySet();
		int tamEdLinList = cardinalitiesFuzzySet.length;
		Map<Integer, Boolean> edLin = _methodsManager.getBestConditionsLinguistic();

		String edLinValue = "{";

		if(cardinalitiesFuzzySet != null) {
			if(cardinalitiesFuzzySet.length > 0) {
				for(int i = 0; i < cardinalitiesFuzzySet.length; i++) {
					edLinValue += "(" + Integer.toString(cardinalitiesFuzzySet[i]) + "," + edLin.get(cardinalitiesFuzzySet[i]) + ")";
					if((i + 1) < cardinalitiesFuzzySet.length) {
						edLinValue += ",";
					}
				}
			}
		}
		edLinValue += "}";
		
		String edLinListValue = "[";
		if(cardinalitiesFuzzySet != null) {
			if(cardinalitiesFuzzySet.length > 0) {
				for(int i = 0; i < cardinalitiesFuzzySet.length; i++) {
					edLinListValue += cardinalitiesFuzzySet[i];
					if((i + 1) < cardinalitiesFuzzySet.length) {
						edLinListValue += ",";
					}

				}
			}
		}
		edLinListValue += "]";

		algorithm = algorithm.replace("<edNum>", Boolean.toString(bestConditionsNumeric));
		algorithm = algorithm.replace("<edInt>", Boolean.toString(edInt));
		algorithm = algorithm.replace("<edLinUnb>", Boolean.toString(bestConditionsUnbalanced));
		algorithm = algorithm.replace("<tamEdLinLis>", Integer.toString(tamEdLinList));
		algorithm = algorithm.replace("<edLinList>", edLinListValue);
		algorithm = algorithm.replace("<edLin>", edLinValue);

		algorithm = algorithm.replace("<1>", "2-Tuple linguistic computational model");
		algorithm = algorithm.replace("<2>", "Fusion approach for managing multi-granular linguistic information");
		algorithm = algorithm.replace("<3>", "Linguistic Hierarchies");
		algorithm = algorithm.replace("<4>", "Extended Linguistic Hierarchies");
		algorithm = algorithm.replace("<5>", "Fusion approach for managing heterogeneous information");
		algorithm = algorithm.replace("<6>", "Methodology to deal with unbalanced linguistic term sets");

		Color DARK_BLUE = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_BLUE);
		Color GREEN = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN);
		Color MAGENTA = Display.getCurrent().getSystemColor(SWT.COLOR_MAGENTA);
		Color RED = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
		Color BLACK = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);

		Color[] textColors = new Color[] { BLACK, MAGENTA, MAGENTA, MAGENTA, MAGENTA,
				MAGENTA, MAGENTA, BLACK, BLACK, BLACK, BLACK, BLACK, BLACK,
				BLACK, BLACK, BLACK, BLACK, BLACK, BLACK, BLACK, BLACK, BLACK,
				BLACK, BLACK, BLACK, BLACK, BLACK, BLACK, BLACK };

		_recommendedMethod = _methodsManager.getRecommendedMethod();

		int start = 8;
		if("2-Tuple linguistic computational model".equals(_recommendedMethod)) {
			textColors[start + 1] = textColors[start + 2] = GREEN;
		} else if("Fusion approach for managing multi-granular linguistic information".equals(_recommendedMethod)) {
			textColors[start + 1] = textColors[start + 3] = textColors[start + 5] = RED;
			textColors[start + 7] = textColors[start + 8] = textColors[start + 9] = textColors[start + 10] = textColors[start + 11] = textColors[start + 12] = GREEN;
		} else if("Linguistic Hierarchies".equals(_recommendedMethod)) {
			textColors[start + 1] = textColors[start + 3] = textColors[start + 5] = textColors[start + 11] = textColors[start + 13] = RED;
			textColors[start + 7] = textColors[start + 8] = textColors[start + 9] = textColors[start + 10] = textColors[start + 15] = textColors[start + 16] = textColors[start + 17] = textColors[start + 18] = textColors[start + 19] = GREEN;
		} else if("Extended Linguistic Hierarchies".equals(_recommendedMethod)) {
			textColors[start + 1] = textColors[start + 3] = textColors[start + 5] = textColors[start + 11] = RED;
			textColors[start + 7] = textColors[start + 8] = textColors[start + 9] = textColors[start + 10] = textColors[start + 13] = textColors[start + 14] = GREEN;
		} else if("Fusion approach for managing heterogeneous information".equals(_recommendedMethod)) {
			textColors[start + 1] = RED;
			textColors[start + 3] = textColors[start + 4] = GREEN;
		} else if ("Methodology to deal with unbalanced linguistic term sets".equals(_recommendedMethod)) {
			textColors[start + 1] = textColors[start + 3] = RED;
			textColors[start + 5] = textColors[start + 6] = GREEN;
		}

		StyleRange[] textRanges = new StyleRange[textColors.length];
		int lineStart = 0;
		int lineLength = 0;
		String lines[] = algorithm.split("\n");
		for(int i = 0; i < textColors.length; i++) {
			lineLength = lines[i].length();
			textRanges[i] = new StyleRange(lineStart, lineLength,
					textColors[i], null);
			lineStart += lineLength + 1;
		}

		_algorithmInstantationText.setText(algorithm);

		if(textRanges != null) {

			_algorithmInstantationText.setStyleRanges(textRanges);
			_algorithmInstantationText.setStyleRange(new StyleRange(algorithm.indexOf(_recommendedMethod), _recommendedMethod.length(), DARK_BLUE, null, SWT.BOLD));

			// Comments
			int auxPos = algorithm.indexOf(" Require values ");
			_algorithmInstantationText.setStyleRange(new StyleRange(auxPos - 1, "# Require values #".length(), _algorithmInstantationText.getStyleRangeAtOffset(auxPos).foreground, null,SWT.BOLD));

			auxPos = algorithm.indexOf(" Algorithm to select the suitable CWW methodology ");
			_algorithmInstantationText.setStyleRange(new StyleRange(auxPos - 1, "# Algorithm to select the suitable CWW methodology #".length(), _algorithmInstantationText.getStyleRangeAtOffset(auxPos).foreground, null,SWT.BOLD));

			// if
			int initPos = 0;
			int newPos = 0;
			while(algorithm.indexOf(" if", initPos) != -1) {
				newPos = algorithm.indexOf(" if", initPos);
				_algorithmInstantationText.setStyleRange(new StyleRange(newPos, 3, _algorithmInstantationText.getStyleRangeAtOffset(newPos).foreground, null, SWT.BOLD));
				initPos = newPos + 3;
			}

			// else
			initPos = 0;
			newPos = 0;
			while (algorithm.indexOf(" else", initPos) != -1) {
				newPos = algorithm.indexOf(" else", initPos);
				_algorithmInstantationText.setStyleRange(new StyleRange(newPos, 5, _algorithmInstantationText.getStyleRangeAtOffset(newPos).foreground, null, SWT.BOLD));
				initPos = newPos + 5;
			}

			// end
			initPos = 0;
			newPos = 0;
			while (algorithm.indexOf(" end", initPos) != -1) {
				newPos = algorithm.indexOf(" end", initPos);
				_algorithmInstantationText.setStyleRange(new StyleRange(newPos,
						4, _algorithmInstantationText
								.getStyleRangeAtOffset(newPos).foreground,
						null, SWT.BOLD));
				initPos = newPos + 4;
			}

			// while
			initPos = 0;
			newPos = 0;
			while (algorithm.indexOf(" while", initPos) != -1) {
				newPos = algorithm.indexOf(" while", initPos);
				_algorithmInstantationText.setStyleRange(new StyleRange(newPos,
						6, _algorithmInstantationText
								.getStyleRangeAtOffset(newPos).foreground,
						null, SWT.BOLD));
				initPos = newPos + 6;
			}

			// return
			initPos = 0;
			newPos = 0;
			while (algorithm.indexOf(" return", initPos) != -1) {
				newPos = algorithm.indexOf(" return", initPos);
				_algorithmInstantationText.setStyleRange(new StyleRange(newPos,
						7, _algorithmInstantationText
								.getStyleRangeAtOffset(newPos).foreground,
						null, SWT.BOLD));
				initPos = newPos + 7;
			}

			// then
			initPos = 0;
			newPos = 0;
			while (algorithm.indexOf(" then", initPos) != -1) {
				newPos = algorithm.indexOf(" then", initPos);
				_algorithmInstantationText.setStyleRange(new StyleRange(newPos,
						5, _algorithmInstantationText
								.getStyleRangeAtOffset(newPos).foreground,
						null, SWT.BOLD));
				initPos = newPos + 5;
			}

			// do
			initPos = 0;
			newPos = 0;
			while (algorithm.indexOf(" do", initPos) != -1) {
				newPos = algorithm.indexOf(" do", initPos);
				_algorithmInstantationText.setStyleRange(new StyleRange(newPos,
						3, _algorithmInstantationText
								.getStyleRangeAtOffset(newPos).foreground,
						null, SWT.BOLD));
				initPos = newPos + 3;
			}

			// and
			initPos = 0;
			newPos = 0;
			while (algorithm.indexOf(" and ", initPos) != -1) {
				newPos = algorithm.indexOf(" and ", initPos);
				_algorithmInstantationText.setStyleRange(new StyleRange(newPos,
						5, _algorithmInstantationText
								.getStyleRangeAtOffset(newPos).foreground,
						null, SWT.BOLD));
				initPos = newPos + 5;
			}

			// or
			initPos = 0;
			newPos = 0;
			while (algorithm.indexOf(" or ", initPos) != -1) {
				newPos = algorithm.indexOf(" or ", initPos);
				_algorithmInstantationText.setStyleRange(new StyleRange(newPos,
						4, _algorithmInstantationText
								.getStyleRangeAtOffset(newPos).foreground,
						null, SWT.BOLD));
				initPos = newPos + 4;
			}

			// true
			initPos = 0;
			newPos = 0;
			while (algorithm.indexOf("=true", initPos) != -1) {
				newPos = algorithm.indexOf("=true", initPos) + 1;
				_algorithmInstantationText.setStyleRange(new StyleRange(newPos,
						4, _algorithmInstantationText
								.getStyleRangeAtOffset(newPos).foreground,
						null, SWT.BOLD));
				initPos = newPos + 4;
			}

			// false
			initPos = 0;
			newPos = 0;
			while (algorithm.indexOf("=false", initPos) != -1) {
				newPos = algorithm.indexOf("=false", initPos) + 1;
				_algorithmInstantationText.setStyleRange(new StyleRange(newPos,
						5, _algorithmInstantationText
								.getStyleRangeAtOffset(newPos).foreground,
						null, SWT.BOLD));
				initPos = newPos + 4;
			}
		}
	}
	
	 @Override
	   protected void createButtonsForButtonBar(Composite parent) {
	    super.createButtonsForButtonBar(parent);

	    Button ok = getButton(IDialogConstants.OK_ID);
	    setButtonLayoutData(ok);

	    Button cancel = getButton(IDialogConstants.CANCEL_ID);
	    cancel.setVisible(false);
	    setButtonLayoutData(cancel);
	 }

}