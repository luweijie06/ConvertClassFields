package com.dev.gear;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ConvertClassFieldsAction extends AnAction {


    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);

        if (project == null || editor == null || psiFile == null) {
            showErrorMessage(project, "Invalid context", "Unable to perform the action in the current context.");
            return;
        }

        SelectionModel selectionModel = editor.getSelectionModel();
        if (!selectionModel.hasSelection()) {
            showErrorMessage(project, "No Selection", "Please select class to convert.");
            return;
        }

        int startOffset = selectionModel.getSelectionStart();
        PsiElement element = psiFile.findElementAt(startOffset);
        if (element == null) {
            showErrorMessage(project, "Invalid Selection", "Unable to find a valid element at the cursor position.");
            return;
        }

        PsiVariable variable = PsiTreeUtil.getParentOfType(element, PsiVariable.class);
        if (variable == null || !(variable.getType() instanceof PsiClassType)) {
            showErrorMessage(project, "Invalid Selection", "Please select a variable of a class type.");
            return;
        }

        PsiClass sourceClass = ((PsiClassType) variable.getType()).resolve();
        if (sourceClass == null) {
            showErrorMessage(project, "Invalid Class", "Unable to resolve the class of the selected variable.");
            return;
        }

        PsiClass targetClass = ClassChooserUtil.chooseClass(project, "Type to search for classes");
        if (targetClass == null) {
            return; // User cancelled the class selection, no need for error message
        }

        generateSetterWithGetter(project, targetClass, sourceClass, editor);
    }


    private void showErrorMessage(Project project, String title, String message) {
        Messages.showErrorDialog(project, message, title);
    }
    private void generateSetterWithGetter(Project project, PsiClass targetClass, PsiClass sourceClass, Editor editor) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            String defaultClassName = sourceClass != null ? sourceClass.getName() : "";
            String defaultTargetClassName = targetClass.getName();

            if (defaultClassName != null && !defaultClassName.isEmpty()) {
                defaultClassName = Character.toLowerCase(defaultClassName.charAt(0)) + defaultClassName.substring(1);
            }
            if (defaultTargetClassName != null && !defaultTargetClassName.isEmpty()) {
                defaultTargetClassName = Character.toLowerCase(defaultTargetClassName.charAt(0)) + defaultTargetClassName.substring(1);
            }
            InputDialog dialog = new InputDialog(project, defaultClassName, defaultTargetClassName);
            if (dialog.showAndGet()) {
                String variableName = dialog.getVariableName();
                String targetClassName = dialog.getTargetClassName();

                if (variableName.isEmpty() || targetClassName.isEmpty()) {
                    return;
                }

                String newInstanceCode = targetClass.getName() + " " + targetClassName + " = new " + targetClass.getName() + "();";

                List<String> setterCalls = new ArrayList<>();
                setterCalls.add(newInstanceCode);

                for (PsiField targetField : targetClass.getFields()) {
                    PsiMethod setter = findSetter(targetClass, targetField);
                    if (setter != null) {
                        PsiField sourceField = findMatchingField(sourceClass, targetField);
                        if (sourceField != null) {
                            PsiMethod getter = findGetter(targetClass, targetField);
                            if (getter != null) {
                                String setterCall = targetClassName + "." + setter.getName() + "(" +
                                        variableName + "." + getter.getName() + "());";
                                setterCalls.add(setterCall);
                            }
                        }
                    }
                }

                CodeInsertionUtil.insertCodeBelowCursor(project, editor, setterCalls);
            }
        });
    }

    private PsiMethod findSetter(PsiClass psiClass, PsiField field) {
        String setterName = "set" + capitalize(field.getName());
        PsiMethod[] setters = psiClass.findMethodsByName(setterName, false);
        return setters.length > 0 ? setters[0] : null;
    }

    private PsiMethod findGetter(PsiClass psiClass, PsiField field) {
        String getterName = "get" + capitalize(field.getName());
        PsiMethod[] getters = psiClass.findMethodsByName(getterName, false);
        return getters.length > 0 ? getters[0] : null;
    }

    private PsiField findMatchingField(PsiClass sourceClass, PsiField targetField) {
        for (PsiField sourceField : sourceClass.getFields()) {
            if (sourceField.getName().equals(targetField.getName()) && sourceField.getType().equals(targetField.getType())) {
                return sourceField;
            }
        }
        return null;
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private static class InputDialog extends DialogWrapper {
        private JTextField variableNameField;
        private JTextField targetClassNameField;

        protected InputDialog(Project project, String defaultVariableName, String defaultTargetClassName) {
            super(project);
            setTitle("Enter Names");
            init();
            variableNameField.setText(defaultVariableName);
            targetClassNameField.setText(defaultTargetClassName);
        }

        @Nullable
        @Override
        protected JComponent createCenterPanel() {
            JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
            panel.add(new JLabel("Variable Name:"));
            variableNameField = new JTextField();
            panel.add(variableNameField);
            panel.add(new JLabel("Target Class Name:"));
            targetClassNameField = new JTextField();
            panel.add(targetClassNameField);
            return panel;
        }

        public String getVariableName() {
            return variableNameField.getText();
        }

        public String getTargetClassName() {
            return targetClassNameField.getText();
        }
    }
}