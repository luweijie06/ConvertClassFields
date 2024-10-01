package com.dev.gear;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ClassChooserUtil {

    public static PsiClass chooseClass(Project project, String instructionText) {
        ClassChooserDialog dialog = new ClassChooserDialog(project, instructionText);
        dialog.show();
        return dialog.getSelectedClass();
    }

    private static List<PsiClass> findMatchingClasses(Project project, String className, boolean fuzzyMatch) {
        AtomicReference<List<PsiClass>> resultRef = new AtomicReference<>(new ArrayList<>());

        ProgressManager.getInstance().run(new Task.Modal(project, "Searching for Classes", true) {
            @Override
            public void run(ProgressIndicator indicator) {
                List<PsiClass> result = new ArrayList<>();
                PsiManager psiManager = PsiManager.getInstance(project);

                VirtualFile[] contentRoots = ProjectRootManager.getInstance(project).getContentRoots();
                Collection<VirtualFile> javaFiles = FilenameIndex.getAllFilesByExt(project, "java", GlobalSearchScope.projectScope(project));

                indicator.setIndeterminate(false);
                indicator.setFraction(0.0);
                int totalFiles = javaFiles.size();
                int processedFiles = 0;

                for (VirtualFile file : javaFiles) {
                    indicator.checkCanceled();
                    indicator.setText("Processing: " + file.getName());

                    ApplicationManager.getApplication().runReadAction(() -> {
                        PsiFile psiFile = psiManager.findFile(file);
                        if (psiFile instanceof PsiJavaFile) {
                            PsiJavaFile javaFile = (PsiJavaFile) psiFile;
                            for (PsiClass psiClass : javaFile.getClasses()) {
                                if (psiClass.getName() != null) {
                                    if (fuzzyMatch) {
                                        if (psiClass.getName().toLowerCase().contains(className.toLowerCase())) {
                                            result.add(psiClass);
                                        }
                                    } else {
                                        if (psiClass.getName().equalsIgnoreCase(className)) {
                                            result.add(psiClass);
                                        }
                                    }
                                }
                            }
                        }
                    });

                    processedFiles++;
                    indicator.setFraction((double) processedFiles / totalFiles);
                }

                resultRef.set(result);
            }
        });

        return resultRef.get();
    }

    private static class ClassChooserDialog extends DialogWrapper {
        private final String instructionText;
        private JTextField searchField;
        private JBList<PsiClass> classList;
        private DefaultListModel<PsiClass> listModel;
        private Project project;
        private JComboBox<String> matchTypeComboBox;

        protected ClassChooserDialog(Project project, String instructionText) {
            super(project);
            this.project = project;
            this.instructionText = instructionText;
            init();
            setTitle("Choose Class");
        }

        @Override
        protected JComponent createCenterPanel() {
            JPanel panel = new JPanel(new BorderLayout());

            searchField = new JTextField();
            searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void changedUpdate(javax.swing.event.DocumentEvent e) { updateClassList(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { updateClassList(); }
                public void insertUpdate(javax.swing.event.DocumentEvent e) { updateClassList(); }
            });

            matchTypeComboBox = new JComboBox<>(new String[]{"Fuzzy", "Exact"});
            matchTypeComboBox.addActionListener(e -> updateClassList());

            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.add(searchField, BorderLayout.CENTER);
            topPanel.add(matchTypeComboBox, BorderLayout.EAST);

            panel.add(topPanel, BorderLayout.NORTH);

            listModel = new DefaultListModel<>();
            classList = new JBList<>(listModel);
            classList.setCellRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof PsiClass) {
                        PsiClass psiClass = (PsiClass) value;
                        setText(psiClass.getQualifiedName());
                    }
                    return c;
                }
            });

            panel.add(new JBScrollPane(classList), BorderLayout.CENTER);

            // Add a label to show instructions
            JLabel instructionLabel = new JLabel(instructionText);
            instructionLabel.setHorizontalAlignment(JLabel.CENTER);
            panel.add(instructionLabel, BorderLayout.SOUTH);

            return panel;
        }

        private void updateClassList() {
            String searchText = searchField.getText();
            if (searchText.isEmpty()) {
                listModel.clear();
                return;
            }

            boolean fuzzyMatch = matchTypeComboBox.getSelectedItem().equals("Fuzzy");
            List<PsiClass> matchingClasses = findMatchingClasses(project, searchText, fuzzyMatch);
            listModel.clear();
            for (PsiClass psiClass : matchingClasses) {
                listModel.addElement(psiClass);
            }
        }

        public PsiClass getSelectedClass() {
            return isOK() ? classList.getSelectedValue() : null;
        }
    }
}