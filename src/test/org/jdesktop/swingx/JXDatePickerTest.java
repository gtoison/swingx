/*
 * $Id$
 * 
 * Copyright 2006 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
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
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.jdesktop.swingx;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;

import junit.framework.TestCase;

import org.jdesktop.swingx.calendar.JXMonthView;
import org.jdesktop.swingx.test.XTestUtils;
import org.jdesktop.test.ActionReport;
import org.jdesktop.test.PropertyChangeReport;
import org.jdesktop.test.TestUtils;

/**
 * Created by IntelliJ IDEA.
 * User: joutwate
 * Date: Jun 1, 2006
 * Time: 6:58:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class JXDatePickerTest extends TestCase {
    private static final Logger LOG = Logger.getLogger(JXDatePickerTest.class
            .getName());
    
    private Calendar cal;

    public void setUp() {
        cal = Calendar.getInstance();
    }

    public void teardown() {
    }

    /**
     * Enhanced commit/cancel.
     * 
     * test the actionEvents produced by the monthview trigger
     * commit cancel in the picker.
     *
     */
    public void testCommitCancelFromMonthView() {
        JXDatePicker picker = new JXDatePicker();
        Action commitAction = picker.getMonthView().getActionMap().get(JXMonthView.COMMIT_KEY);
        ActionReport report = new ActionReport();
        picker.addActionListener(report);
        commitAction.actionPerformed(null);
        assertEquals("must have receive 1 event after monthView commit", 
                1, report.getEventCount());
        assertEquals(JXDatePicker.COMMIT_KEY, report.getLastActionCommand());
        report.clear();
        Action cancelAction = picker.getMonthView().getActionMap().get(JXMonthView.CANCEL_KEY);
        cancelAction.actionPerformed(null);
        assertEquals("must have receive 1 event after monthView cancel", 
                1, report.getEventCount());
        assertEquals(JXDatePicker.CANCEL_KEY, report.getLastActionCommand());
        
    }
    
    /**
     * Enhanced commit/cancel.
     * 
     * test that the ui installed a listener.
     */
    public void testCommitCancelListeningToMonthView() {
        JXMonthView monthView = new JXMonthView();
        int standalone = monthView.getListeners(ActionListener.class).length;
        assertEquals(0, standalone);
        JXDatePicker picker = new JXDatePicker();
        int contained = picker.getMonthView().getListeners(ActionListener.class).length;
        assertEquals(standalone + 1, contained);
    }

    /**
     * Enhanced commit/cancel.
     * 
     * test that cancel action reverts silently: date related
     * state unchanged and no events fired (except the actionEvent).
     * @throws ParseException 
     *
     */
    public void testCancelEditRevertsSilently() {
        JXDatePicker picker = new JXDatePicker();
        String text = picker.getEditor().getText();
        // manipulate the text, not entirely safe ...
        String changed = text.replace('0', '1');
        picker.getEditor().setText(changed);
        ActionReport actionReport = new ActionReport();
        picker.addActionListener(actionReport);
        picker.getEditor().addActionListener(actionReport);
        picker.getMonthView().addActionListener(actionReport);
        PropertyChangeReport propertyReport = new PropertyChangeReport();
        picker.addPropertyChangeListener(propertyReport);
        picker.getEditor().addPropertyChangeListener(propertyReport);
        picker.cancelEdit();
        assertEquals(0, propertyReport.getEventCount());
        assertEquals(1, actionReport.getEventCount());
    }
    /**
     * Enhanced commit/cancel.
     * 
     * test that actions fire as expected.
     *
     */
    public void testCommitCancelActionsFire() {
        JXDatePicker picker = new JXDatePicker();
        Action commitAction = picker.getActionMap().get(JXDatePicker.COMMIT_KEY);
        ActionReport report = new ActionReport();
        picker.addActionListener(report);
        commitAction.actionPerformed(null);
        assertEquals(1, report.getEventCount());
        assertEquals(JXDatePicker.COMMIT_KEY, report.getLastActionCommand());
        report.clear();
        Action cancelAction = picker.getActionMap().get(JXDatePicker.CANCEL_KEY);
        cancelAction.actionPerformed(null);
        assertEquals(1, report.getEventCount());
        assertEquals(JXDatePicker.CANCEL_KEY, report.getLastActionCommand());
    }

    
    /**
     * Enhanced commit/cancel.
     * 
     * test that actions are registered.
     *
     */
    public void testCommitCancelActionExist() {
        JXDatePicker picker = new JXDatePicker();
        assertNotNull(picker.getActionMap().get(JXDatePicker.CANCEL_KEY));
        assertNotNull(picker.getActionMap().get(JXDatePicker.COMMIT_KEY));
    }
    
    /**
     * for now keep the old postAction.
     *
     */
    public void testCommitCancelPreserveOld() {
        JXDatePicker picker = new JXDatePicker();
        ActionReport report = new ActionReport();
        picker.addActionListener(report);
        picker.postActionEvent();
        assertEquals(picker.getActionCommand(), report.getLastActionCommand());
    }
    /**
     * Issue #554-swingx: timezone of formats and picker must be synched.
     * 
     * Here: set the timezone in the monthView.
     */
    public void testSynchTimeZoneModifiedInMonthView() {
        JXDatePicker picker = new JXDatePicker();
        TimeZone defaultZone = picker.getTimeZone();
        TimeZone alternative = TimeZone.getTimeZone("GMT-6");
        // sanity
        assertNotNull(alternative);
        if (alternative.equals(defaultZone)) {
            alternative = TimeZone.getTimeZone("GMT-7");
            // paranoid ... but shit happens
            assertNotNull(alternative);
            assertFalse(alternative.equals(defaultZone));
        }
        picker.getMonthView().setTimeZone(alternative);
        assertEquals(alternative, picker.getTimeZone());
        for (DateFormat format : picker.getFormats()) {
            assertEquals("timezone must be synched", picker.getTimeZone(), format.getTimeZone());
        }
    }
 
    /**
     * Issue #554-swingx: timezone of formats and picker must be synched.
     * 
     * Here: set the timezone in the picker.
     */
    public void testSynchTimeZoneModifiedInPicker() {
        JXDatePicker picker = new JXDatePicker();
        TimeZone defaultZone = picker.getTimeZone();
        TimeZone alternative = TimeZone.getTimeZone("GMT-6");
        // sanity
        assertNotNull(alternative);
        if (alternative.equals(defaultZone)) {
            alternative = TimeZone.getTimeZone("GMT-7");
            // paranoid ... but shit happens
            assertNotNull(alternative);
            assertFalse(alternative.equals(defaultZone));
        }
        picker.setTimeZone(alternative);
        assertEquals(alternative, picker.getTimeZone());
        for (DateFormat format : picker.getFormats()) {
            assertEquals("timezone must be synched", picker.getTimeZone(), format.getTimeZone());
        }
    }

    /**
     * Issue #554-swingx: timezone of formats and picker must be synched.
     * 
     * Here: set the timezone in the picker.
     */
    public void testSynchTimeZoneOnSetMonthView() {
        JXDatePicker picker = new JXDatePicker();
        TimeZone defaultZone = picker.getTimeZone();
        TimeZone alternative = TimeZone.getTimeZone("GMT-6");
        // sanity
        assertNotNull(alternative);
        if (alternative.equals(defaultZone)) {
            alternative = TimeZone.getTimeZone("GMT-7");
            // paranoid ... but shit happens
            assertNotNull(alternative);
            assertFalse(alternative.equals(defaultZone));
        }
        JXMonthView monthView = new JXMonthView();
        monthView.setTimeZone(alternative);
        picker.setMonthView(monthView);
        assertEquals(alternative, picker.getTimeZone());
        for (DateFormat format : picker.getFormats()) {
            assertEquals("timezone must be synched", picker.getTimeZone(), format.getTimeZone());
        }
    }

    /**
     * Issue #554-swingx: timezone of formats and picker must be synched.
     * 
     * Here: initialize the formats with the pickers timezone on setting.
     */
    public void testSynchTimeZoneOnSetFormats() {
        JXDatePicker picker = new JXDatePicker();
        TimeZone defaultZone = picker.getTimeZone();
        TimeZone alternative = TimeZone.getTimeZone("GMT-6");
        // sanity
        assertNotNull(alternative);
        if (alternative.equals(defaultZone)) {
            alternative = TimeZone.getTimeZone("GMT-7");
            // paranoid ... but shit happens
            assertNotNull(alternative);
            assertFalse(alternative.equals(defaultZone));
        }
        picker.setTimeZone(alternative);
        assertEquals(alternative, picker.getTimeZone());
        picker.setFormats(DateFormat.getDateInstance());
        for (DateFormat format : picker.getFormats()) {
            assertEquals("timezone must be synched", picker.getTimeZone(), format.getTimeZone());
        }
    }

    /**
     * Issue #554-swingx: timezone of formats and picker must be synched.
     */
    public void testSynchTimeZoneInitial() {
        JXDatePicker picker = new JXDatePicker();
        assertNotNull(picker.getTimeZone());
        for (DateFormat format : picker.getFormats()) {
            assertEquals("timezone must be synched", picker.getTimeZone(), format.getTimeZone());
        }
    }

    /**
     * setFormats should fire a propertyChange.
     *
     */
    public void testFormatsProperty() {
        JXDatePicker picker = new JXDatePicker();
        PropertyChangeReport report = new PropertyChangeReport();
        picker.addPropertyChangeListener(report);
        DateFormat[] oldFormats = picker.getFormats();
        DateFormat[] newFormats = new DateFormat[] {DateFormat.getDateInstance()};
        picker.setFormats(newFormats);
        TestUtils.assertPropertyChangeEvent(report, "formats", oldFormats, newFormats);
        
    }

    /**
     * Test doc'ed behaviour: editor must not be null.
     */
    public void testEditorNull() {
        JXDatePicker picker = new JXDatePicker();
        assertNotNull(picker.getEditor());

        try {
            picker.setEditor(null);
            fail("picker must throw NPE if editor is null");
        } catch (NullPointerException e) {
            // nothing to do - doc'ed behaviour
        }
    }

    /**
     * Test doc'ed behaviour: editor must not be null.
     */
    public void testMonthViewNull() {
        JXDatePicker picker = new JXDatePicker();
        assertNotNull(picker.getMonthView());

        try {
            picker.setMonthView(null);
            fail("picker must throw NPE if monthView is null");
        } catch (NullPointerException e) {
            // nothing to do - doc'ed behaviour
        }
    }
    /**
     * picker has cleaned date, clarified doc.
     * The test is not exactly true, the details 
     * are up for the DatePickerUI to decide. 
     * 
     */
    public void testSetDateCleansDate() {
        JXDatePicker picker = new JXDatePicker();
        picker.setDate(null);
        Date date = cal.getTime();
        Date clean = XTestUtils.getCleanedDate(cal);
        picker.setDate(date);
        assertEquals(clean, picker.getDate());
    }

    /**
     *  date is a bound property of DatePicker.
     */
    public void testDateProperty() {
        JXDatePicker picker = new JXDatePicker();
        picker.setDate(null);
        Date date = XTestUtils.getCleanedToday(5);
        PropertyChangeReport report = new PropertyChangeReport();
        picker.addPropertyChangeListener(report);
        picker.setDate(date);
        TestUtils.assertPropertyChangeEvent(report, "date", null, date);
    }

    /**
     *  date is a bound property of DatePicker. 
     *  test indirect event firing: changed editor value
     */
    public void testDatePropertyThroughEditor() {
        JXDatePicker picker = new JXDatePicker();
        picker.setDate(null);
        Date date = XTestUtils.getCleanedToday(5);
        PropertyChangeReport report = new PropertyChangeReport();
        picker.addPropertyChangeListener(report);
        picker.getEditor().setValue(date);
        TestUtils.assertPropertyChangeEvent(report, "date", null, date);
    }

    /**
     *  date is a bound property of DatePicker.
     *  test indirect event firing: changed monthView selection 
     */
    public void testDatePropertyThroughSelection() {
        JXDatePicker picker = new JXDatePicker();
        picker.setDate(null);
        Date date = XTestUtils.getCleanedToday(5);
        PropertyChangeReport report = new PropertyChangeReport();
        picker.addPropertyChangeListener(report);
        picker.getMonthView().setSelectionInterval(date, date);
        TestUtils.assertPropertyChangeEvent(report, "date", null, date);
    }

    /**
     *  date is a bound property of DatePicker.
     *  test indirect event firing: commit edited value 
     * @throws ParseException 
     *
     */
    public void testDatePropertyThroughCommit() throws ParseException {
        JXDatePicker picker = new JXDatePicker();
        Date initialDate = picker.getDate();
        String text = picker.getEditor().getText();
        Format[] formats = picker.getFormats();
        assertEquals(picker.getDate(), formats[0].parseObject(text));
        // manipulate the text, not entirely safe ...
        String changed = text.replace('0', '1');
        picker.getEditor().setText(changed);
        Date date;
        try {
            date = (Date) formats[0].parseObject(changed);
        } catch (ParseException e) {
            LOG.info("cannot run DatePropertyThroughCommit - parseException in manipulated text");
            return;
        }
        // sanity ...
        assertFalse("", date.equals(picker.getDate()));
        PropertyChangeReport report = new PropertyChangeReport();
        picker.addPropertyChangeListener(report);
        picker.commitEdit();
        TestUtils.assertPropertyChangeEvent(report, "date", initialDate, date);
    }

    /**
     * last piece: removed synch control from picker.commit. 
     * @throws ParseException 
     *
     */
    public void testSynchAllAfterCommit() throws ParseException {
        JXDatePicker picker = new JXDatePicker();
        String text = picker.getEditor().getText();
        Format[] formats = picker.getFormats();
        assertEquals(picker.getDate(), formats[0].parseObject(text));
        // manipulate the text, not entirely safe ...
        String changed = text.replace('0', '1');
        picker.getEditor().setText(changed);
        Date date;
        try {
            date = (Date) formats[0].parseObject(changed);
        } catch (ParseException e) {
            LOG.info("cannot run testSynchAllAfterCommit - parseException in manipulated text");
            return;
        }
        // sanity ...
        assertFalse("", date.equals(picker.getDate()));
        picker.commitEdit();
        assertSynchAll(picker, date);
    }
    
    /**
     * Issue #559-swingX: date must be synched in all parts.
     * here: initial. 
     * 
     */
    public void testSynchAllInitialDate() {
        Date date = XTestUtils.getCleanedToday(5);
        JXDatePicker picker = new JXDatePicker(date.getTime());
        assertSynchAll(picker, date);
    } 
    
    /**
     * Issue #559-swingX: date must be synched in all parts.
     * here: set date in picker
     * 
     * Note: test uses a cleaned date, do same with uncleaned.
     */
    public void testSynchAllOnDateModified() {
        JXDatePicker picker = new JXDatePicker();
        Date date = XTestUtils.getCleanedToday(5);
        picker.setDate(date);
        assertSynchAll(picker, date);
    } 

    /**
     * Issue #559-swingX: date must be synched in all parts.
     * here: set selected date in monthview
     * Note: test uses a cleaned date, do same with uncleaned.
     */
    public void testSynchAllOnSelectionChange()  {
        JXDatePicker picker = new JXDatePicker();
        Date date = XTestUtils.getCleanedToday(5);
        picker.getMonthView().setSelectionInterval(date, date);
        assertSynchAll(picker, date);
    }
    
    /**
     * Issue #559-swingX: date must be synched in all parts.
     * here: set value in editor.
     * 
     * Note: test uses a cleaned date, do same with uncleaned.
     */
    public void testSynchAllOnEditorSetValue() {
        JXDatePicker picker = new JXDatePicker();
        Date date = XTestUtils.getCleanedToday(5);
        picker.getEditor().setValue(date);
        assertSynchAll(picker, date);
    } 
    
    /**
     * Issue #559-swingX: date must be synched in all parts.
     * here: modify value must work after changing the editor.
     * 
     * Note: this started to fail during listener cleanup.
     */
    public void testSynchAllOnEditorSetValueAfterSetEditor() {
        JXDatePicker picker = new JXDatePicker();
        picker.setEditor(new JFormattedTextField(DateFormat.getInstance()));
        Date date = XTestUtils.getCleanedToday(5);
        picker.getEditor().setValue(date);
        assertSynchAll(picker, date);
    }

    /**
     * Issue #559-swingX: date must be synched in all parts.
     * here: set selection must work after changing the monthView.
     * 
     * Note: this started to fail during listener cleanup.
     */
    public void testSynchAllOnSelectionChangeAfterSetMonthView() {
        JXDatePicker picker = new JXDatePicker();
        picker.setMonthView(new JXMonthView());
        Date date = XTestUtils.getCleanedToday(5);
        picker.getMonthView().setSelectionInterval(date, date);
        assertSynchAll(picker, date);
    }

    /**
     * Issue #559-swingX: date must be synched in all parts.
     * <p>
     * 
     * here: set selection must work after changing the monthView's selection
     * model.
     * 
     * Note: this started to fail during listener cleanup.
     */
    public void testSynchAllOnSelectionChangeAfterSetSelectionModel() {
        JXDatePicker picker = new JXDatePicker();
        picker.getMonthView().setSelectionModel(new DefaultDateSelectionModel());
        Date date = XTestUtils.getCleanedToday(5);
        picker.getMonthView().setSelectionInterval(date, date);
        assertSynchAll(picker, date);
    }

    /**
     * Asserts that all date related values in the picker are synched.
     * 
     * @param picker the picker to 
     * @param date the common date
     */
    private void assertSynchAll(JXDatePicker picker, Date date) {
        assertEquals(date, picker.getEditor().getValue());
        assertEquals(date, picker.getDate());
        assertEquals(date, picker.getMonthView().getSelectedDate());
    } 


    /**
     * test that input of unselectable dates reverts editors value.
     */
    public void testRejectSetValueUnselectable() {
        JXDatePicker picker = new JXDatePicker();
        Date upperBound = XTestUtils.getCleanedToday(1);
        picker.getMonthView().setUpperBound(upperBound.getTime());
        Date future = XTestUtils.getCleanedToday(2);
        // sanity
        assertTrue(picker.getMonthView().isUnselectableDate(future.getTime()));
        Date current = picker.getDate();
        // sanity: 
        assertEquals(current, picker.getEditor().getValue());
        // set the editors value to something invalid
        picker.getEditor().setValue(future);
        // ui must not allow an invalid value in the editor
        assertEquals(current, picker.getEditor().getValue());
        // okay ..
        assertEquals(current, picker.getDate());
    }

    /**
     * PickerUI listened to editable (meant: datePicker) and resets
     * the editors property. Accidentally? Even if meant to, it's 
     * brittle because done during the notification. 
     * Changed to use dedicated listener.
     */
    public void testSpuriousEditableListening() {
        JXDatePicker picker = new JXDatePicker();
        picker.getEditor().setEditable(false);
        // sanity - that at least the other views are uneffected
        assertTrue(picker.isEditable());
        assertTrue(picker.getMonthView().isEnabled());
        assertFalse("Do not change the state of the sender during notification processing", 
                picker.getEditor().isEditable());
    }

    /**
     * PickerUI listened to enabled of button (meant: datePicker) and resets
     * the buttons property. 
     */
    public void testSpuriousEnabledListening() {
        JXDatePicker picker = new JXDatePicker();
        Component button = null;
        for (int i = 0; i < picker.getComponentCount(); i++) {
            if (picker.getComponent(i) instanceof JButton) {
                button = picker.getComponent(i);
            }
        }
        if (button == null) {
            LOG.info("cannot run testEnabledListening - no button found");
        }
        button.setEnabled(false);
        // sanity - that at least the other views are uneffected
        assertTrue(picker.isEnabled());
        assertTrue(picker.getEditor().isEnabled());
        assertFalse("Do not change the state of the sender during notification processing", 
                button.isEnabled());
    }

    /**
     * Sanity during revision: be sure we don't loose the 
     * ui listening to picker property changes.
     *
     */
    public void testDatePickerPropertyListening() {
        JXDatePicker picker = new JXDatePicker();
        picker.setEnabled(false);
        assertFalse(picker.getEditor().isEnabled());
        picker.setEditable(false);
        assertFalse(picker.getEditor().isEditable());
        picker.setToolTipText("dummy");
        assertEquals("dummy", picker.getEditor().getToolTipText());
    }
    /**
     * Issue ??-swingx: uninstallUI does not release propertyChangeListener
     * to editor. Reason is that the de-install was not done in 
     * uninstallListeners but later in uninstallComponents - at that time
     * the handler is already nulled, removing will actually create a new one.
     */
    public void testEditorListeners() {
        JFormattedTextField field = new JFormattedTextField(DateFormat.getInstance());
        JXDatePicker picker = new JXDatePicker();
        int defaultListenerCount = field.getPropertyChangeListeners().length;
        // sanity: we added one listener ...
        assertEquals(defaultListenerCount + 1, 
                picker.getEditor().getPropertyChangeListeners().length);
        picker.getUI().uninstallUI(picker);
        assertEquals("the ui installe listener must be removed", 
                defaultListenerCount, 
                // right now we can access the editor even after uninstall
                // because the picker keeps a reference
                // TODO: after cleanup, this will be done through the ui
                picker.getEditor().getPropertyChangeListeners().length);
    }

    /**
     * Issue #551-swingX: editor value not updated after setMonthView.
     * 
     * quick&dirty fix: let the picker manually update.
     *
     */
    public void testEditorValueOnSetMonthView() {
        JXDatePicker picker = new JXDatePicker();
        // set unselected monthView
        picker.setMonthView(new JXMonthView());
        // sanity: picker takes it
        assertNull(picker.getDate());
        assertEquals(picker.getDate(), picker.getEditor().getValue());
        
    }
    /**
     * Issue #551-swingx: editor value not updated after setEditor. 
     * 
     * quick&dirty fix: let the picker manually update.
     * 
     * who should set it? ui-delegate when listening to editor property change?
     * or picker in setEditor?
     * 
     * Compare to JComboBox: BasicComboUI listens to editor change, does internal
     * wiring to editor and call's comboBox configureEditor with the value of the 
     * old editor.
     * 
     * 
     */
    public void testEditorValueOnSetEditor() {
        JXDatePicker picker = new JXDatePicker();
        Object value = picker.getEditor().getValue();
        picker.setEditor(new JFormattedTextField(new JXDatePickerFormatter()));
        assertEquals(value, picker.getEditor().getValue());
    }
    
    /**
     * Issue #551-swingx: editor value must preserve value on LF switch.
     * 
     * This is a side-effect of picker not updating the editor's value
     * on setEditor.
     *
     * @see #testEditorValueOnSetEditor
     */
    public void testEditorUpdateOnLF() {
        JXDatePicker picker = new JXDatePicker();
        Object date = picker.getEditor().getValue();
        picker.updateUI();
        assertEquals(date, picker.getEditor().getValue());
    }



    /**
     * Characterization: setting the monthview's selection model updates
     * the datePicker's date to the monthView's current
     * selection.
     * 
     * Here: model with selection.
     *
     */
    public void testSynchAllAfterSetSelectionModelNotEmpty() {
        JXDatePicker picker = new JXDatePicker();
        Date date = XTestUtils.getCleanedToday(5);
        DateSelectionModel model = new DefaultDateSelectionModel();
        model.setSelectionInterval(date, date);
        // sanity
        assertFalse(date.equals(picker.getDate()));
        picker.getMonthView().setSelectionModel(model);
        assertSynchAll(picker, date);
    }
    

    /**
     * Characterization: setting the monthview's selection model updates
     * the datePicker's date to the monthView's current
     * selection.
     * 
     * Here: model with empty selection.
     *
     */
    public void testSynchAllAfterSetSelectionModelEmpty() {
        JXDatePicker picker = new JXDatePicker();
        assertNotNull(picker.getDate());
        DateSelectionModel model = new DefaultDateSelectionModel();
        assertTrue(model.isSelectionEmpty());
        picker.getMonthView().setSelectionModel(model);
        assertSynchAll(picker, null);
    }
    
    /**
     * Characterization: setting the monthview updates
     * the datePicker's date to the monthView's current
     * selection.
     * Here: monthview with selection.
     *
     */
    public void testSynchAllSetMonthViewWithSelection() {
        JXDatePicker picker = new JXDatePicker();
        JXMonthView monthView = new JXMonthView();
        Date date = XTestUtils.getCleanedToday(5);
        monthView.setSelectionInterval(date, date);
        // sanity
        assertFalse(date.equals(picker.getDate()));
        picker.setMonthView(monthView);
        assertSynchAll(picker, date);
    }

    /**
     * Characterization: setting the monthview updates
     * the datePicker's date to the monthView's current
     * selection.
     * Here: monthview with empty selection.
     *
     */
    public void testSynchAllSetMonthViewWithEmptySelection() {
        JXDatePicker picker = new JXDatePicker();
        // sanity
        assertNotNull(picker.getDate());
        JXMonthView monthView = new JXMonthView();
        Date selectedDate = monthView.getSelectedDate();
        assertNull(selectedDate);
        picker.setMonthView(monthView);
        assertSynchAll(picker, selectedDate);
    }
    

    /**
     * PrefSize should be independent of empty/filled picker. 
     * If not, the initial size might appear kind of collapsed.
     *
     */
    public void testPrefSizeEmptyEditor() {
        JXDatePicker picker = new JXDatePicker();
        // sanity 
        assertNotNull(picker.getDate());
        Dimension filled = picker.getPreferredSize();
        picker.setDate(null);
        Dimension empty = picker.getPreferredSize();
        assertEquals("pref width must be same for empty/filled", filled.width, empty.width);
    }
    
    public void testDefaultConstructor() {
        JXDatePicker datePicker = new JXDatePicker();
        cal.setTimeInMillis(System.currentTimeMillis());
        Date today = cleanupDate(cal);
        assertTrue(today.equals(datePicker.getDate()));
        assertTrue(today.getTime() == datePicker.getDateInMillis());
    }

    public void testConstructor() {
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.add(Calendar.DAY_OF_MONTH, 5);
        Date expectedDate = cleanupDate(cal);
        JXDatePicker datePicker = new JXDatePicker(cal.getTimeInMillis());
        assertTrue(expectedDate.equals(datePicker.getDate()));
        assertTrue(expectedDate.getTime() == datePicker.getDateInMillis());
    }

    public void testNullSelection() {
        JXDatePicker datePicker = new JXDatePicker();
        datePicker.setDate(null);
        assertTrue(null == datePicker.getDate());
        assertTrue(-1 == datePicker.getDateInMillis());
    }

    public void testSetDate() {
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.add(Calendar.DAY_OF_MONTH, 5);
        Date expectedDate = cleanupDate(cal);
        JXDatePicker datePicker = new JXDatePicker();
        datePicker.setDate(cal.getTime());
        assertTrue(expectedDate.equals(datePicker.getDate()));
        assertTrue(expectedDate.equals(datePicker.getEditor().getValue()));

        datePicker.setDate(null);
        assertTrue(null == datePicker.getDate());
        assertTrue(null == datePicker.getEditor().getValue());
    }

    private Date cleanupDate(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}