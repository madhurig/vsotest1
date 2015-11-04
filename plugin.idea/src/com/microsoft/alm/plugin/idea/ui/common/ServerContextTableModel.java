// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.plugin.idea.ui.common;


import com.microsoft.alm.plugin.context.ServerContext;
import com.microsoft.alm.plugin.idea.resources.TfPluginBundle;
import com.microsoft.teamfoundation.core.webapi.model.TeamProjectCollectionReference;
import com.microsoft.teamfoundation.sourcecontrol.webapi.model.GitRepository;
import jersey.repackaged.com.google.common.base.Predicate;
import jersey.repackaged.com.google.common.collect.Collections2;
import jersey.repackaged.com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ServerContextTableModel extends AbstractTableModel {
    public enum Column {REPOSITORY, PROJECT, COLLECTION, ACCOUNT}

    public final static Column[] VSO_REPO_COLUMNS = new Column[]{Column.REPOSITORY, Column.PROJECT, Column.ACCOUNT};
    public final static Column[] TFS_REPO_COLUMNS = new Column[]{Column.REPOSITORY, Column.PROJECT, Column.COLLECTION};
    public final static Column[] VSO_PROJECT_COLUMNS = new Column[]{Column.PROJECT, Column.ACCOUNT};
    public final static Column[] TFS_PROJECT_COLUMNS = new Column[]{Column.PROJECT, Column.COLLECTION};

    /**
     * The default converter simply returns the index given.
     */
    private final static TableModelSelectionConverter DEFAULT_CONVERTER = new TableModelSelectionConverter() {
        @Override
        public int convertRowIndexToModel(int viewRowIndex) {
            return viewRowIndex;
        }
    };

    private ListSelectionModel selectionModel = new DefaultListSelectionModel();
    private List<ServerContext> rows = new ArrayList<ServerContext>(1000);
    private List<ServerContext> filteredRows = null;
    private String filter;
    private final Column[] columns;
    private TableModelSelectionConverter converter;

    public ServerContextTableModel(Column[] columns) {
        assert columns != null;
        this.columns = columns.clone();
        selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    public ListSelectionModel getSelectionModel() {
        return selectionModel;
    }

    public ServerContext getServerContext(final int rowIndex) {
        final List<ServerContext> localRows;
        if (filteredRows != null) {
            localRows = filteredRows;
        } else {
            localRows = rows;
        }

        if (rowIndex >= 0 && rowIndex < localRows.size()) {
            return localRows.get(rowIndex);
        }

        return null;
    }

    public void addServerContexts(final List<ServerContext> contexts) {
        // Remember selection
        final ServerContext selectedContext = getSelectedContext();

        // Add the new rows to the existing list
        rows.addAll(contexts);
        // Sort the rows by the first column
        Collections.sort(rows, new Comparator<ServerContext>() {
            @Override
            public int compare(ServerContext c1, ServerContext c2) {
                final String name1 = getValueFor(c1, 0);
                final String name2 = getValueFor(c2, 0);
                return String.CASE_INSENSITIVE_ORDER.compare(name1, name2);
            }
        });

        if (hasFilter()) {
            // re-apply the filter, this will fire its own event
            applyFilter();
        } else {
            // Fire an event letting callers know
            super.fireTableDataChanged();
        }

        // Attempt to restore the selection
        select(selectedContext);
    }

    public void setSelectionConverter(TableModelSelectionConverter converter) {
        this.converter = converter;
    }

    public TableModelSelectionConverter getSelectionConverter() {
        if (converter == null) {
            return DEFAULT_CONVERTER;
        } else {
            return converter;
        }
    }

    public int getSelectedIndex() {
        final int viewSelectedIndex;
        // Check both the max and min selected indexes to see which one is really selected
        if (selectionModel.isSelectedIndex(selectionModel.getMinSelectionIndex())) {
            viewSelectedIndex = selectionModel.getMinSelectionIndex();
        } else {
            viewSelectedIndex = selectionModel.getMaxSelectionIndex();
        }
        final int selectedIndex = getSelectionConverter().convertRowIndexToModel(viewSelectedIndex);
        return selectedIndex;
    }

    public ServerContext getSelectedContext() {
        final ServerContext selectedContext = getServerContext(getSelectedIndex());
        return selectedContext;
    }

    private void select(final ServerContext context) {
        final List<ServerContext> localRows;
        if (filteredRows != null) {
            localRows = filteredRows;
        } else {
            localRows = rows;
        }

        final int index = localRows.indexOf(context);
        if (index >= 0) {
            selectionModel.setSelectionInterval(index, index);
        }
    }

    public void clearRows() {
        filteredRows = null;
        rows.clear();
        super.fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        if (filteredRows != null) {
            return filteredRows.size();
        }
        return rows.size();
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        final ServerContext serverContext = getServerContext(rowIndex);
        return getValueFor(serverContext, columnIndex);
    }

    private String getValueFor(final ServerContext serverContext, final int columnIndex) {
        if (serverContext == null) {
            return "";
        }

        // The following might throw index out of bounds, but that is the appropriate error
        Column column = columns[columnIndex];

        switch (column) {
            case REPOSITORY: {
                final GitRepository repository = serverContext.getGitRepository();
                return repository != null ? repository.getName() : "";
            }
            case PROJECT: {
                final GitRepository repository = serverContext.getGitRepository();
                return (repository != null && repository.getProjectReference() != null) ? repository.getProjectReference().getName() : "";
            }
            case COLLECTION: {
                final TeamProjectCollectionReference collection = serverContext.getTeamProjectCollectionReference();
                return collection != null ? collection.getName() : "";
            }
            case ACCOUNT:
                return serverContext.getUri().getHost();
            default:
                return "";
        }
    }

    @Override
    public String getColumnName(final int columnIndex) {
        // The following might throw index out of bounds, but that is the appropriate error
        Column column = columns[columnIndex];

        switch (column) {
            case REPOSITORY:
                return TfPluginBundle.message(TfPluginBundle.KEY_SERVER_CONTEXT_TABLE_REPO_COLUMN);
            case PROJECT:
                return TfPluginBundle.message(TfPluginBundle.KEY_SERVER_CONTEXT_TABLE_PROJECT_COLUMN);
            case COLLECTION:
                return TfPluginBundle.message(TfPluginBundle.KEY_SERVER_CONTEXT_TABLE_COLLECTION_COLUMN);
            case ACCOUNT:
                return TfPluginBundle.message(TfPluginBundle.KEY_SERVER_CONTEXT_TABLE_ACCOUNT_COLUMN);
            default:
                return "";
        }
    }

    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return false;
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    public boolean hasFilter() {
        return StringUtils.isNotEmpty(this.filter);
    }

    public void setFilter(final String filter) {
        this.filter = filter;

        // Remember selection
        final ServerContext selectedContext = getSelectedContext();

        applyFilter();

        // Attempt to restore the selection
        select(selectedContext);

    }

    private void applyFilter() {
        if (!hasFilter()) {
            filteredRows = null;
        } else {
            filteredRows = Lists.newArrayList(Collections2.filter(rows, new Predicate<ServerContext>() {
                @Override
                public boolean apply(ServerContext repositoryRow) {
                    return rowContains(repositoryRow);
                }
            }));
        }
        super.fireTableDataChanged();
    }

    private boolean rowContains(ServerContext repositoryRow) {
        // search for the string in a case insensitive way
        // check each column for a match, if any column contains the string the result is true
        for (int c = 0; c < columns.length; c++) {
            if (StringUtils.containsIgnoreCase(getValueFor(repositoryRow, c), filter)) {
                return true;
            }
        }

        return false;
    }
}
