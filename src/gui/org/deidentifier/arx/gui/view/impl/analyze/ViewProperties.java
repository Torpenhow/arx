/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.arx.gui.view.impl.analyze;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXLattice.Anonymity;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.DataTypeWithFormat;
import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.criteria.DistinctLDiversity;
import org.deidentifier.arx.criteria.EntropyLDiversity;
import org.deidentifier.arx.criteria.EqualDistanceTCloseness;
import org.deidentifier.arx.criteria.HierarchicalDistanceTCloseness;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.criteria.RecursiveCLDiversity;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelConfiguration;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.metric.InformationLoss;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * This view displays basic properties about input or output data
 * TODO: Split into at least two views
 * 
 * @author Fabian Prasser
 */
public class ViewProperties implements IView {

    /**
     * A content provider
     * @author Fabian Prasser
     */
    private class InputContentProvider implements ITreeContentProvider {

        @Override
        public void dispose() {
            // Nothing to do
        }

        @Override
        public Object[] getChildren(final Object arg0) {
            return ((Property) arg0).children.toArray();
        }

        @Override
        public Object[] getElements(final Object arg0) {
            return roots.toArray();
        }

        @Override
        public Object getParent(final Object arg0) {
            return ((Property) arg0).parent;
        }

        @Override
        public boolean hasChildren(final Object arg0) {
            return !((Property) arg0).children.isEmpty();
        }

        @Override
        public void inputChanged(final Viewer arg0,
                                 final Object arg1,
                                 final Object arg2) {
            // Nothing to do
        }

    }

    /**
     * A label provider
     * @author Fabian Prasser
     *
     */
    private class InputLabelProvider implements ITableLabelProvider {

        @Override
        public void addListener(final ILabelProviderListener listener) {
            // Nothing to do
        }

        @Override
        public void dispose() {
            // Nothing to do
        }

        @Override
        public Image
                getColumnImage(final Object element, final int columnIndex) {
            return null;
        }

        @Override
        public String
                getColumnText(final Object element, final int columnIndex) {
            switch (columnIndex) {
            case 0:
                return ((Property) element).property;
            default:
                if ((columnIndex - 1) >= ((Property) element).values.length) {
                    return null;
                } else {
                    return ((Property) element).values[columnIndex - 1];
                }
            }
        }

        @Override
        public boolean isLabelProperty(final Object element,
                                       final String property) {
            return false;
        }

        @Override
        public void removeListener(final ILabelProviderListener listener) {
            // Nothing to do
        }
    }

    /**
     * A content provider
     * @author Fabian Prasser
     *
     */
    private class OutputContentProvider implements ITreeContentProvider {

        @Override
        public void dispose() {
            // Nothing to do
        }

        @Override
        public Object[] getChildren(final Object arg0) {
            return ((Property) arg0).children.toArray();
        }

        @Override
        public Object[] getElements(final Object arg0) {
            return roots.toArray();
        }

        @Override
        public Object getParent(final Object arg0) {
            return ((Property) arg0).parent;
        }

        @Override
        public boolean hasChildren(final Object arg0) {
            return !((Property) arg0).children.isEmpty();
        }

        @Override
        public void inputChanged(final Viewer arg0,
                                 final Object arg1,
                                 final Object arg2) {
            // Nothing to do
        }

    }

    /**
     * A content provider
     * @author Fabian Prasser
     */
    private class OutputLabelProvider implements ITableLabelProvider {

        @Override
        public void addListener(final ILabelProviderListener listener) {
            // Nothing to do
        }

        @Override
        public void dispose() {
            // Nothing to do
        }

        @Override
        public Image
                getColumnImage(final Object element, final int columnIndex) {
            return null;
        }

        @Override
        public String
                getColumnText(final Object element, final int columnIndex) {
            switch (columnIndex) {
            case 0:
                return ((Property) element).property;
            case 1:
                return ((Property) element).values[0];
            }
            return null;
        }

        @Override
        public boolean isLabelProperty(final Object element,
                                       final String property) {
            return false;
        }

        @Override
        public void removeListener(final ILabelProviderListener listener) {
            // Nothing to do
        }
    }

    /**
     * A class for properties displayed in the tree view
     * @author Fabian Prasser
     */
    private class Property {
        public Property       parent;
        public List<Property> children = new ArrayList<Property>();
        public String         property;
        public String[]       values;

        public Property(final Property father,
                        final String property,
                        final String[] values) {
            father.add(this);
            this.property = property;
            this.values = values;
        }

        public Property(final String property, final String[] values) {
            roots.add(this);
            this.property = property;
            this.values = values;
        }

        public void add(final Property p) {
            children.add(p);
            p.parent = this;
        }
    }

    private TreeViewer           treeViewer;
    private final List<Property> roots  = new ArrayList<Property>();
    private final Composite      root;
    private final Controller     controller;
    private final ModelPart    target;
    private final ModelPart    reset;
    private Model                model;

    private final NumberFormat   format = new DecimalFormat("##0.000"); //$NON-NLS-1$

    /**
     * Constructor
     * @param parent
     * @param controller
     * @param target
     * @param reset
     */
    public ViewProperties(final Composite parent,
                          final Controller controller,
                          final ModelPart target,
                          final ModelPart reset) {

        // Register
        controller.addListener(ModelPart.SELECTED_ATTRIBUTE, this);
        controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
        controller.addListener(ModelPart.METRIC, this);
        controller.addListener(ModelPart.MAX_OUTLIERS, this);
        controller.addListener(ModelPart.DATA_TYPE, this);
        controller.addListener(ModelPart.MODEL, this);
        controller.addListener(target, this);
        this.controller = controller;
        if (reset != null) {
            controller.addListener(reset, this);
        }
        this.reset = reset;
        this.target = target;
        root = parent;
        create(parent);
        reset();
    }

    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    @Override
    public void reset() {

        root.setRedraw(false);
        roots.clear();
        treeViewer.refresh();
        SWTUtil.disable(root);
        root.setRedraw(true);
    }

    @Override
    public void update(final ModelEvent event) {

        SWTUtil.enable(root);
        update();

        // Handle reset target, i.e., e.g. input has changed
        if (event.part == reset) {
            reset();
        } else if (event.part == target) {
            SWTUtil.enable(root);
            update();
        } else if (event.part == ModelPart.MODEL) {
            model = (Model) event.data;
            reset();
            // Handle selected attribute
        }
    }

    /**
     * Converts an information loss into a relative value in percent 
     * TODO: Code duplicate from NodePropertiesView
     * 
     * @param infoLoss
     * @return
     */
    private double asRelativeValue(final InformationLoss infoLoss,
                                   final ARXResult result) {
        return ((infoLoss.getValue() - result.getLattice()
                                             .getBottom()
                                             .getMinimumInformationLoss()
                                             .getValue()) / result.getLattice()
                                                                  .getTop()
                                                                  .getMaximumInformationLoss()
                                                                  .getValue()) * 100d;
    }

    /**
     * Creates the view
     * @param group
     */
    private void create(final Composite group) {

        final Tree tree = new Tree(group, SWT.BORDER | SWT.H_SCROLL |
                                          SWT.V_SCROLL);
        tree.setHeaderVisible(true);
        treeViewer = new TreeViewer(tree);

        if (target == ModelPart.OUTPUT) {

            final TreeColumn column1 = new TreeColumn(tree, SWT.LEFT);
            tree.setLinesVisible(true);
            column1.setAlignment(SWT.LEFT);
            column1.setText(Resources.getMessage("PropertiesView.1")); //$NON-NLS-1$
            column1.setWidth(160);
            final TreeColumn column2 = new TreeColumn(tree, SWT.RIGHT);
            column2.setAlignment(SWT.LEFT);
            column2.setText(Resources.getMessage("PropertiesView.2")); //$NON-NLS-1$
            column2.setWidth(100);

            treeViewer.setContentProvider(new OutputContentProvider());
            treeViewer.setLabelProvider(new OutputLabelProvider());
        } else {

            final TreeColumn column1 = new TreeColumn(tree, SWT.LEFT);
            tree.setLinesVisible(true);
            column1.setAlignment(SWT.LEFT);
            column1.setText(Resources.getMessage("PropertiesView.3")); //$NON-NLS-1$
            column1.setWidth(160);
            final TreeColumn column2 = new TreeColumn(tree, SWT.RIGHT);
            column2.setAlignment(SWT.LEFT);
            column2.setText(Resources.getMessage("PropertiesView.4")); //$NON-NLS-1$
            column2.setWidth(100);
            final TreeColumn column6 = new TreeColumn(tree, SWT.RIGHT);
            column6.setAlignment(SWT.LEFT);
            column6.setText(Resources.getMessage("PropertiesView.5")); //$NON-NLS-1$
            column6.setWidth(100);
            final TreeColumn column7 = new TreeColumn(tree, SWT.RIGHT);
            column7.setAlignment(SWT.LEFT);
            column7.setText(Resources.getMessage("PropertiesView.101")); //$NON-NLS-1$
            column7.setWidth(80);
            final TreeColumn column3 = new TreeColumn(tree, SWT.RIGHT);
            column3.setAlignment(SWT.LEFT);
            column3.setText(Resources.getMessage("PropertiesView.6")); //$NON-NLS-1$
            column3.setWidth(50);
            final TreeColumn column4 = new TreeColumn(tree, SWT.RIGHT);
            column4.setAlignment(SWT.LEFT);
            column4.setText(Resources.getMessage("PropertiesView.7")); //$NON-NLS-1$
            column4.setWidth(50);
            final TreeColumn column5 = new TreeColumn(tree, SWT.RIGHT);
            column5.setAlignment(SWT.LEFT);
            column5.setText(Resources.getMessage("PropertiesView.8")); //$NON-NLS-1$
            column5.setWidth(50);

            treeViewer.setContentProvider(new InputContentProvider());
            treeViewer.setLabelProvider(new InputLabelProvider());
        }
        treeViewer.setInput(roots);
        treeViewer.expandAll();
    }

    /**
     * Update the view
     */
    private void update() {

        if (model == null) { return; }

        // Obtain the right config
        ModelConfiguration config = model.getOutputConfig();
        if (config == null) {
            config = model.getInputConfig();
        }

        // Obtain the right handle
        final DataHandle data;
        if (target == ModelPart.INPUT) {

            if (config.getInput() == null) {
                reset();
                return;
            } else {
                data = config.getInput().getHandle();
            }
        } else {
            data = model.getOutput();
        }

        // Clear if nothing to draw
        if ((config == null) || (data == null)) {
            reset();
            return;
        }

        root.setRedraw(false);
        if (target == ModelPart.INPUT) {
            updateInput(config, 
                        data);
        } else {
            updateOutput(config,
                         model.getResult(),
                         model.getSelectedNode(),
                         data);
        }
        root.setRedraw(true);
    }

    /**
     * Update the input properties
     * @param config
     * @param data
     */
    private void updateInput(final ModelConfiguration config, final DataHandle data) {

        roots.clear();
        new Property(Resources.getMessage("PropertiesView.9"), new String[] { String.valueOf(data.getNumRows()) }); //$NON-NLS-1$
        new Property(Resources.getMessage("PropertiesView.10"), new String[] { String.valueOf(config.getAllowedOutliers() * 100d) + Resources.getMessage("PropertiesView.11") }); //$NON-NLS-1$ //$NON-NLS-2$
        final Property attributes = new Property(Resources.getMessage("PropertiesView.12"), new String[] { String.valueOf(data.getNumColumns()) }); //$NON-NLS-1$
        final Property identifying = new Property(attributes,
                                                  Resources.getMessage("PropertiesView.13"), new String[] { String.valueOf(data.getDefinition().getIdentifyingAttributes().size()) }); //$NON-NLS-1$

        int index = 0;
        for (int i = 0; i < data.getNumColumns(); i++) {
            final String s = data.getAttributeName(i);
            if (data.getDefinition().getIdentifyingAttributes().contains(s)) {
                final String[] values = new String[] { "", "", "", "", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                values[0] = s;
                values[1] = data.getDefinition().getDataType(s).toString();
                new Property(identifying, Resources.getMessage("PropertiesView.19") + (index++), values); //$NON-NLS-1$
            }
        }

        final Property quasiIdentifying = new Property(attributes,
                                                       Resources.getMessage("PropertiesView.20"), new String[] { String.valueOf(data.getDefinition().getQuasiIdentifyingAttributes().size()) }); //$NON-NLS-1$

        index = 0;
        for (int i = 0; i < data.getNumColumns(); i++) {
            final String s = data.getAttributeName(i);
            if (data.getDefinition()
                    .getQuasiIdentifyingAttributes()
                    .contains(s)) {
                final String[] values = new String[] { "", "", "", "", "" , ""}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                values[0] = s;
                if (data.getDefinition().getHierarchy(s) != null) {
                    DataType<?> type = data.getDefinition().getDataType(s);
                    values[1] = type.getDescription().getLabel();
                    if (type.getDescription().hasFormat() && 
                        ((DataTypeWithFormat)type).getFormat() != null){
                        values[2] = ((DataTypeWithFormat)type).getFormat();
                    }
                    values[3] = String.valueOf(data.getDefinition()
                                                   .getHierarchyHeight(s));
                    values[4] = String.valueOf(data.getDefinition()
                                                   .getMinimumGeneralization(s));
                    values[5] = String.valueOf(data.getDefinition()
                                                   .getMaximumGeneralization(s));
                }
                new Property(quasiIdentifying,
                             Resources.getMessage("PropertiesView.26") + (index++), values); //$NON-NLS-1$
            }
        }
        final Property sensitive = new Property(attributes,
                                                Resources.getMessage("PropertiesView.27"), new String[] { String.valueOf(data.getDefinition().getSensitiveAttributes().size()) }); //$NON-NLS-1$

        index = 0;
        for (int i = 0; i < data.getNumColumns(); i++) {
            final String s = data.getAttributeName(i);
            if (data.getDefinition().getSensitiveAttributes().contains(s)) {
                final String[] values = new String[] { "", "", "", "", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                values[0] = s;
                if (config.getHierarchy(s) != null) {
                    int height = 0;
                    if (config.getHierarchy(s).getHierarchy().length > 0) {
                        height = config.getHierarchy(s).getHierarchy()[0].length;
                    }
                    values[1] = data.getDefinition().getDataType(s).toString();
                    values[2] = String.valueOf(height);
                }
                new Property(sensitive,
                             Resources.getMessage("PropertiesView.33") + (index++), values); //$NON-NLS-1$
            }
        }

        final Property insensitive = new Property(attributes,
                                                  Resources.getMessage("PropertiesView.34"), new String[] { String.valueOf(data.getDefinition().getInsensitiveAttributes().size()) }); //$NON-NLS-1$

        index = 0;
        for (int i = 0; i < data.getNumColumns(); i++) {
            final String s = data.getAttributeName(i);
            if (data.getDefinition().getInsensitiveAttributes().contains(s)) {
                final String[] values = new String[] { "", "", "", "", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                values[0] = s;
                values[1] = data.getDefinition().getDataType(s).toString();
                new Property(insensitive,
                             Resources.getMessage("PropertiesView.40") + (index++), values); //$NON-NLS-1$
            }
        }

        treeViewer.refresh();
        treeViewer.expandAll();

    }

    /**
     * Update the output
     * @param config
     * @param result
     * @param node
     * @param data
     */
    private void updateOutput(final ModelConfiguration config,
                              final ARXResult result,
                              final ARXNode node,
                              final DataHandle data) {

        // Clear
        roots.clear();
        
        // Print basic info on outliers
        new Property(Resources.getMessage("PropertiesView.41"), new String[] { String.valueOf(result.getNumberOfOutlyingGroups()) }); //$NON-NLS-1$
        new Property(Resources.getMessage("PropertiesView.42"), new String[] { String.valueOf(result.getNumberOfGroups()) }); //$NON-NLS-1$
        new Property(Resources.getMessage("PropertiesView.43"), new String[] { String.valueOf(result.getNumberOfOutlyingGroups()) }); //$NON-NLS-1$

        // Print information loss
        if (node.getMaximumInformationLoss().getValue() == 
            node.getMinimumInformationLoss().getValue()) {
            
            final String infoloss = String.valueOf(node.getMinimumInformationLoss().getValue()) +
                                    " [" + format.format(asRelativeValue(node.getMinimumInformationLoss(), result)) + "%]"; //$NON-NLS-1$ //$NON-NLS-2$
            new Property(Resources.getMessage("PropertiesView.46"), new String[] { infoloss }); //$NON-NLS-1$

        } else {
            controller.getResources()
                      .getLogger()
                      .warn("Differing minimum and maximum information loss"); //$NON-NLS-1$
        }
        
        // Print basic info on neighbouring nodes
        new Property(Resources.getMessage("PropertiesView.48"), new String[] { String.valueOf(node.getSuccessors().length) }); //$NON-NLS-1$
        new Property(Resources.getMessage("PropertiesView.49"), new String[] { String.valueOf(node.getPredecessors().length) }); //$NON-NLS-1$
        new Property(Resources.getMessage("PropertiesView.50"), new String[] { Arrays.toString(node.getTransformation()) }); //$NON-NLS-1$

        // If the node is anonymous
        if (node.isAnonymous() == Anonymity.ANONYMOUS) {

            // Print info about d-presence
            if (config.containsCriterion(DPresence.class)) {
                DPresence criterion = config.getCriterion(DPresence.class);
                // only if its not an auto-generated criterion
                if (!(criterion.getDMin()==0d && criterion.getDMax()==1d)){
                    Property n = new Property(Resources.getMessage("PropertiesView.92"), new String[] { Resources.getMessage("PropertiesView.93") }); //$NON-NLS-1$ //$NON-NLS-2$
                    new Property(n, Resources.getMessage("PropertiesView.94"), new String[] { String.valueOf(criterion.getDMin())}); //$NON-NLS-1$
                    new Property(n, Resources.getMessage("PropertiesView.95"), new String[] { String.valueOf(criterion.getDMax())}); //$NON-NLS-1$
                }
            }
            // Print info about k-anonymity
            if (config.containsCriterion(KAnonymity.class)) {
                KAnonymity criterion = config.getCriterion(KAnonymity.class);
                Property n = new Property(Resources.getMessage("PropertiesView.51"), new String[] { Resources.getMessage("PropertiesView.52") }); //$NON-NLS-1$ //$NON-NLS-2$
                new Property(n, Resources.getMessage("PropertiesView.53"), new String[] { String.valueOf(criterion.getK())}); //$NON-NLS-1$
            }
            
            // Print info about l-diversity or t-closeness
            int index = 0;
            for (PrivacyCriterion c : config.getCriteria()) {
                if (c instanceof DistinctLDiversity){
                    DistinctLDiversity criterion = (DistinctLDiversity)c;
                    Property n = new Property(Resources.getMessage("PropertiesView.57"), new String[] { Resources.getMessage("PropertiesView.58") }); //$NON-NLS-1$ //$NON-NLS-2$
                    new Property(n, Resources.getMessage("PropertiesView.59"), new String[] { String.valueOf(criterion.getL()) }); //$NON-NLS-1$
                    new Property(n, Resources.getMessage("PropertiesView.100"), new String[] { criterion.getAttribute() }); //$NON-NLS-1$
                } else if (c instanceof EntropyLDiversity){
                    EntropyLDiversity criterion = (EntropyLDiversity)c;
                    Property n = new Property(Resources.getMessage("PropertiesView.63"), new String[] { Resources.getMessage("PropertiesView.64") }); //$NON-NLS-1$ //$NON-NLS-2$
                    new Property(n, Resources.getMessage("PropertiesView.65"), new String[] { String.valueOf(criterion.getL()) }); //$NON-NLS-1$
                    new Property(n, Resources.getMessage("PropertiesView.100"), new String[] { criterion.getAttribute() }); //$NON-NLS-1$
                } else if (c instanceof RecursiveCLDiversity){
                    RecursiveCLDiversity criterion = (RecursiveCLDiversity)c;
                    Property n = new Property(Resources.getMessage("PropertiesView.69"), new String[] { Resources.getMessage("PropertiesView.70") }); //$NON-NLS-1$ //$NON-NLS-2$
                    new Property(n, Resources.getMessage("PropertiesView.71"), new String[] { String.valueOf(criterion.getC()) }); //$NON-NLS-1$
                    new Property(n, Resources.getMessage("PropertiesView.72"), new String[] { String.valueOf(criterion.getL()) }); //$NON-NLS-1$
                    new Property(n, Resources.getMessage("PropertiesView.100"), new String[] { criterion.getAttribute() }); //$NON-NLS-1$
                } else if (c instanceof EqualDistanceTCloseness){
                    EqualDistanceTCloseness criterion = (EqualDistanceTCloseness)c;
                    Property n = new Property(Resources.getMessage("PropertiesView.77"), new String[] { Resources.getMessage("PropertiesView.78") }); //$NON-NLS-1$ //$NON-NLS-2$
                    new Property(n, Resources.getMessage("PropertiesView.79"), new String[] { String.valueOf(criterion.getT()) }); //$NON-NLS-1$
                    new Property(n, Resources.getMessage("PropertiesView.100"), new String[] { criterion.getAttribute() }); //$NON-NLS-1$
                } else if (c instanceof HierarchicalDistanceTCloseness){
                    HierarchicalDistanceTCloseness criterion = (HierarchicalDistanceTCloseness)c;
                    Property n = new Property(Resources.getMessage("PropertiesView.83"), new String[] { Resources.getMessage("PropertiesView.84") }); //$NON-NLS-1$ //$NON-NLS-2$
                    new Property(n, Resources.getMessage("PropertiesView.85"), new String[] { String.valueOf(criterion.getT()) }); //$NON-NLS-1$
                    new Property(n, Resources.getMessage("PropertiesView.100"), new String[] { criterion.getAttribute() }); //$NON-NLS-1$
                    final int height = config.getHierarchy(criterion.getAttribute()).getHierarchy()[0].length;
                    new Property(n, "SE-"+(index++), new String[] { Resources.getMessage("PropertiesView.87") + String.valueOf(height) }); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        } else {
            new Property(Resources.getMessage("PropertiesView.90"), new String[] { Resources.getMessage("PropertiesView.91") }); //$NON-NLS-1$ //$NON-NLS-2$
        }

        treeViewer.refresh();
        treeViewer.expandAll();
    }
}
