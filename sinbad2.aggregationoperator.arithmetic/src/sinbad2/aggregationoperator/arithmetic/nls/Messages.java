package sinbad2.aggregationoperator.arithmetic.nls;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "sinbad2.aggregationoperator.arithmetic.nls.messages"; //$NON-NLS-1$
	public static String Arithmetic_Not_supported_type;
	public static String IntegerOperator_Invalid_domain;
	public static String RealOperator_Invalid_domain;
	public static String TwoTupleOperator_Invalid_domain;
	public static String UnifiedValuationOperator_Invalid_domain;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
