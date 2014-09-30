/* ProWritingAid, a natural language style checker 
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */

package com.prowritingaid.openoffice;

/** OpenOffice 3.x Integration
 * 
 * @author Orpheus Technology
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.sun.star.lang.*;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XDesktop;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.linguistic2.ProofreadingResult;
import com.sun.star.linguistic2.SingleProofreadingError;
import com.sun.star.linguistic2.XLinguServiceEventBroadcaster;
import com.sun.star.linguistic2.XLinguServiceEventListener;
import com.sun.star.linguistic2.XProofreader;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.task.XJobExecutor;
import com.sun.star.text.XTextViewCursor;
import com.sun.star.text.XTextViewCursorSupplier;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

public class Main extends WeakBase implements XJobExecutor,
    XServiceDisplayName, XServiceInfo, XProofreader,
    XLinguServiceEventBroadcaster {

  // Service name required by the OOo API && our own name.
  private static final String[] SERVICE_NAMES = {
          "com.sun.star.linguistic2.Proofreader",
          "com.prowritingaid.openoffice.Main" };

  // use a different name than the stand-alone version to avoid conflicts:
  private static final String CONFIG_FILE = ".prowritingaid-ooo.cfg";

  // LibreOffice (since 4.2.0) special tag for locale with variant 
  // e.g. language ="qlt" country="ES" variant="ca-ES-valencia":
  private static final String LIBREOFFICE_SPECIAL_LANGUAGE_TAG = "qlt";

  private Configuration config;
  private String docID;

  // Rules disabled using the config dialog box rather than Spelling dialog box
  // or the context menu.
  private Set<String> disabledRules;
  private Set<String> disabledRulesUI;

  private List<XLinguServiceEventListener> xEventListeners;

  private boolean recheck;

  /**
   * Sentence tokenization-related members.
   */
  private String currentPara;
  private List<String> tokenizedSentences;
  private int position;
  private XComponentContext xContext;

  public static void showMessage(String msg) {
		final DialogThread dt = new DialogThread(msg);
		dt.start();
  }
  
  public Main(final XComponentContext xCompContext) throws FileNotFoundException, UnsupportedEncodingException {
    xEventListeners = new ArrayList<>();
  }

  public final void changeContext(final XComponentContext xCompContext) {
    xContext = xCompContext;
  }

  private XComponent getXComponent() {
    try {
      final XMultiComponentFactory xMCF = xContext.getServiceManager();
      final Object desktop = xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", xContext);
      final XDesktop xDesktop = UnoRuntime.queryInterface(XDesktop.class, desktop);
      return xDesktop.getCurrentComponent();
    } catch (final Throwable t) {
      showError(t);
      return null;
    }
  }

  /**
   * Runs the grammar checker on paragraph text.
   * 
   * @param docID document ID
   * @param paraText paragraph text
   * @param locale Locale the text Locale
   * @param startOfSentencePos start of sentence position
   * @param nSuggestedBehindEndOfSentencePosition end of sentence position
   * @return ProofreadingResult containing the results of the check.
   */
  @Override
  public final ProofreadingResult doProofreading(final String docID,
      final String paraText, final Locale locale, final int startOfSentencePos,
      final int nSuggestedBehindEndOfSentencePosition,
      final PropertyValue[] propertyValues) {
	    final ProofreadingResult paRes = new ProofreadingResult();
	   // showMessage("hello proofing");
	    try {	    	
	      paRes.nStartOfSentencePosition = startOfSentencePos;
	      paRes.xProofreader = this;
	      paRes.aLocale = locale;
	      paRes.aDocumentIdentifier = docID;
	      paRes.aText = paraText;
	      paRes.aProperties = propertyValues;
	      //int[] footnotePositions = getPropertyValues("FootnotePositions", propertyValues);  // since LO 4.3
	      showMessage("Not happening");
	      List<SingleProofreadingError> errors = new ArrayList<SingleProofreadingError>();
	      if (paraText.indexOf("mistake")>=0){
	    	  SingleProofreadingError aError = new SingleProofreadingError();
	    	  aError.nErrorType = com.sun.star.text.TextMarkupType.PROOFREADING;
	    	    aError.aFullComment = "This is a mistake";
	    	    aError.aShortComment = "This is a mistake";
	    	    aError.aSuggestions = new String[1];
	    	    aError.aSuggestions[0] = "msitake";
	    	    aError.nErrorStart = paraText.indexOf("mistake") + paRes.nStartOfSentencePosition;
	    	    aError.nErrorLength = aError.nErrorStart+7;
	    	    aError.aRuleIdentifier = "MISTAKE";
	    	  errors.add(aError);
	      }
	      
        final SingleProofreadingError[] errorArray = 
                new SingleProofreadingError[errors.size()];

	      for(int i = 0; i < errors.size(); i++){
	    	  errorArray[i] = errors.get(i);
	      }
	      paRes.aErrors=errorArray;
	      return paRes;
	    } catch (final Throwable t) {
	      //showError(t);
	      return paRes;
	    }
  }

  private int[] getPropertyValues(String propName, PropertyValue[] propertyValues) {
    for (PropertyValue propertyValue : propertyValues) {
      if (propName.equals(propertyValue.Name)) {
        if (propertyValue.Value instanceof int[]) {
          return (int[]) propertyValue.Value;
        } else {
          System.err.println("Not of expected type int[]: " + propertyValue.Name + ": " + propertyValue.Value.getClass());
        }
      }
    }
    return new int[]{};  // e.g. for LO/OO < 4.3 and the 'FootnotePositions' property
  }
  
  /**
   * We leave spell checking to OpenOffice/LibreOffice.
   * @return false
   */
  @Override
  public final boolean isSpellChecker() {
    return false;
  }

  /**
   * Runs LT options dialog box.
   */
  public final void runOptionsDialog() {
    //final Language lang = getLanguage();
    /*if (lang == null) {
      return;
    }
    prepareConfig(lang);
    final ConfigThread configThread = new ConfigThread(lang, config, this);
    configThread.start();*/
  }

  /**
   * @return An array of Locales supported by LT
   */
  @Override
  public final Locale[] getLocales() {
		return new Locale[] { 
				new Locale("en", "us", "en_US"), 
				new Locale("en", "ca", "en_CA"), 
				new Locale("en", "uk", "en_UK"), 
				new Locale("en", "gb", "en_GB"), 
				new Locale("en", "in", "en_IN"), 
				new Locale("en", "au", "en_AU"), 
				new Locale("en", "nz", "en_NZ"), 
				new Locale("en", "za", "en_ZA"), 
				new Locale("en", "zw", "en_ZW"), 
				new Locale("en", "ph", "en_PH"), 
				new Locale("en", "be", "en_BE"), 
				new Locale("en", "bw", "en_BW"), 
				new Locale("en", "bz", "en_BZ"), 
				new Locale("en", "jm", "en_JM"), 
				new Locale("en", "be", "en_BE"), 
				new Locale("en", "gu", "en_GU"), 
				new Locale("en", "hk", "en_HK"), 
				new Locale("en", "mh", "en_MH"), 
				new Locale("en", "mp", "en_MP"), 
				new Locale("en", "pk", "en_PK"), 
				new Locale("en", "sg", "en_SG"), 
				new Locale("en", "tt", "en_TT"), 
				new Locale("en", "um", "en_UM"), 
				new Locale("en", "vi", "en_VI")
			};
  }

  /**
   * @return true if PWA supports the language of a given locale
   * @param locale The Locale to check
   */
  @Override
  public final boolean hasLocale(final Locale locale) {
		return "en".equals( locale.Language );
  }

  /**
   * Add a listener that allow re-checking the document after changing the
   * options in the configuration dialog box.
   * 
   * @param eventListener the listener to be added
   * @return true if listener is non-null and has been added, false otherwise
   */
  @Override
  public final boolean addLinguServiceEventListener(
      final XLinguServiceEventListener eventListener) {
    if (eventListener == null) {
      return false;
    }
    xEventListeners.add(eventListener);
    return true;
  }

  /**
   * Remove a listener from the event listeners list.
   * 
   * @param eventListener the listener to be removed
   * @return true if listener is non-null and has been removed, false otherwise
   */
  @Override
  public final boolean removeLinguServiceEventListener(
      final XLinguServiceEventListener eventListener) {
    if (eventListener == null) {
      return false;
    }
    if (xEventListeners.contains(eventListener)) {
      xEventListeners.remove(eventListener);
      return true;
    }
    return false;
  }

  /**
   * Inform listener (grammar checking iterator) that options have changed and
   * the doc should be rechecked.
   */
  public final void resetDocument() {
    if (!xEventListeners.isEmpty()) {
      for (final XLinguServiceEventListener xEvLis : xEventListeners) {
        if (xEvLis != null) {
          final com.sun.star.linguistic2.LinguServiceEvent xEvent = new com.sun.star.linguistic2.LinguServiceEvent();
          xEvent.nEvent = com.sun.star.linguistic2.LinguServiceEventFlags.PROOFREAD_AGAIN;
          xEvLis.processLinguServiceEvent(xEvent);
        }
      }
      recheck = true;
      //disabledRules = config.getDisabledRuleIds();
      if (disabledRules == null) {
        disabledRules = new HashSet<>();
      }
    }
  }

  @Override
  public String[] getSupportedServiceNames() {
    return getServiceNames();
  }

  public static String[] getServiceNames() {
    return SERVICE_NAMES;
  }

  @Override
  public boolean supportsService(final String sServiceName) {
    for (final String sName : SERVICE_NAMES) {
      if (sServiceName.equals(sName)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String getImplementationName() {
    return Main.class.getName();
  }

  public static XSingleComponentFactory __getComponentFactory(
      final String sImplName) {
    SingletonFactory xFactory = null;
    if (sImplName.equals(Main.class.getName())) {
      xFactory = new SingletonFactory();
    }
    return xFactory;
  }

  public static boolean __writeRegistryServiceInfo(final XRegistryKey regKey) {
    return Factory.writeRegistryServiceInfo(Main.class.getName(), Main.getServiceNames(), regKey);
  }

  @Override
  public void trigger(final String sEvent) {
    if (Thread.currentThread().getContextClassLoader() == null) {
      Thread.currentThread().setContextClassLoader(Main.class.getClassLoader());
    }
    if (!javaVersionOkay()) {
      return;
    }
    try {
      if ("configure".equals(sEvent)) {
        //runOptionsDialog();
  	    showMessage("Config");
      } else if ("about".equals(sEvent)) {
    	  ResourceBundle messages = ResourceBundle.getBundle("MessagesBundle");
        final AboutDialogThread aboutThread = new AboutDialogThread(messages);
        aboutThread.start();
      } else {
        System.err.println("Sorry, don't know what to do, sEvent = " + sEvent);
      }
    } catch (final Throwable e) {
      showError(e);
    }
  }

  private boolean javaVersionOkay() {
    showMessage("Check Java");
	  final String version = System.getProperty("java.version");
    if (version != null
        && (version.startsWith("1.0") || version.startsWith("1.1")
            || version.startsWith("1.2") || version.startsWith("1.3")
            || version.startsWith("1.4") || version.startsWith("1.5")
            || version.startsWith("1.6"))) {
      final DialogThread dt = new DialogThread(
          "Error: ProWritingAid requires Java 7.0 or later. Current version: " + version);
      dt.start();
      return false;
    }
    try {
      // do not set look and feel for on Mac OS X as it causes the following error:
      // soffice[2149:2703] Apple AWT Java VM was loaded on first thread -- can't start AWT.
      if (!System.getProperty("os.name").contains("OS X")) {
        for (UIManager.LookAndFeelInfo info : UIManager
            .getInstalledLookAndFeels()) {
          if ("Nimbus".equals(info.getName())) {
            UIManager.setLookAndFeel(info.getClassName());
            break;
          }
        }
      }
    } catch (Exception ignored) {
      // Well, what can we do...
    }
    return true;
  }

  static void showError(final Throwable e) {
    final String metaInfo = "OS: " + System.getProperty("os.name") + " on "
        + System.getProperty("os.arch") + ", Java version "
        + System.getProperty("java.version") + " from "
        + System.getProperty("java.vm.vendor");
    e.printStackTrace();  // without this, we see no exception if a test case fails
  }

  private File getHomeDir() {
    final String homeDir = System.getProperty("user.home");
    if (homeDir == null) {
      @SuppressWarnings("ThrowableInstanceNeverThrown")
      final RuntimeException ex = new RuntimeException("Could not get home directory");
      showError(ex);
    }
    return new File(homeDir);
  }

  private class AboutDialogThread extends Thread {

    private final ResourceBundle messages;

    AboutDialogThread(final ResourceBundle messages) {
      this.messages = messages;
    }

    @Override
    public void run() {
      // TODO: null can cause the dialog to appear on the wrong screen in a
      // multi-monitor setup, but we just don't have a proper java.awt.Component
      // here which we could use instead:
      final AboutDialog about = new AboutDialog(messages, null);
      about.show();
    }
  }

  /**
   * Called when "Ignore" is selected e.g. in the context menu for an error.
   */
  @Override
  public void ignoreRule(final String ruleId, final Locale locale)
      throws IllegalArgumentException {
    // TODO: config should be locale-dependent
    disabledRulesUI.add(ruleId);
    //config.setDisabledRuleIds(disabledRulesUI);
    try {
      //config.saveConfiguration(langTool.getLanguage());
    } catch (final Throwable t) {
      showError(t);
    }
    recheck = true;
  }

  /**
   * Called on rechecking the document - resets the ignore status for rules that
   * was set in the spelling dialog box or in the context menu.
   * 
   * The rules disabled in the config dialog box are left as intact.
   */
  @Override
  public void resetIgnoreRules() {
    //config.setDisabledRuleIds(disabledRules);
    try {
      //config.saveConfiguration(langTool.getLanguage());
    } catch (final Throwable t) {
      showError(t);
    }
    recheck = true;
  }

  @Override
  public String getServiceDisplayName(Locale locale) {
    return "ProWritingAid";
  }

}

/**
 * A simple comparator for sorting errors by their position.
 */
class ErrorPositionComparator implements Comparator<SingleProofreadingError> {

  @Override
  public int compare(final SingleProofreadingError match1,
      final SingleProofreadingError match2) {
    if (match1.aSuggestions.length == 0 && match2.aSuggestions.length > 0) {
      return 1;
    }
    if (match2.aSuggestions.length == 0 && match1.aSuggestions.length > 0) {
      return -1;
    }
    final int error1pos = match1.nErrorStart;
    final int error2pos = match2.nErrorStart;
    if (error1pos > error2pos) {
      return 1;
    } else if (error1pos < error2pos) {
      return -1;
    } else {
      if (match1.aSuggestions.length != 0 && match2.aSuggestions.length != 0
          && match1.aSuggestions.length != match2.aSuggestions.length) {
        return ((Integer) (match1.aSuggestions.length))
            .compareTo(match2.aSuggestions.length);
      }
    }
    return match1.aRuleIdentifier.compareTo(match2.aRuleIdentifier);
  }
  
}

class DialogThread extends Thread {
  private final String text;

  DialogThread(final String text) {
    this.text = text;
  }

  @Override
  public void run() {
    JOptionPane.showMessageDialog(null, text);
  }
}

