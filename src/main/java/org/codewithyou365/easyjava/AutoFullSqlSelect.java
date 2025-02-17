package org.codewithyou365.easyjava;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateWithExpressionSelector;
import com.intellij.codeInsight.template.postfix.util.JavaPostfixTemplatesUtils;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.java.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author codewithyou365
 */
public class AutoFullSqlSelect extends PostfixTemplateWithExpressionSelector {

    private final static String START_WITH = "PsiReferenceExpression:QuerySelect";

    protected AutoFullSqlSelect(@Nullable PostfixTemplateProvider provider) {
        super(null, ".full", "full select", JavaPostfixTemplatesUtils.selectorAllExpressionsWithCurrentOffset(element -> {
                    return element.toString().startsWith(START_WITH);
                }
        ), provider);
    }

    @Override
    public boolean isBuiltin() {
        return false;
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    protected void expandForChooseExpression(@NotNull PsiElement expression, @NotNull Editor editor) {
        ASTNode currNode = expression.getNode();
        StringBuilder argumentList = new StringBuilder();
        // find data
        {
            String valueName = null;
            int startLineNumber = 0;
            List<String> getCallers = new ArrayList<>();
            while (true) {
                if (currNode == null) {
                    break;
                }
                if (currNode instanceof PsiLocalVariableImpl || currNode instanceof FieldElement) {
                    final PsiJavaTokenImpl[] equal = {null};
                    final PsiIdentifierImpl[] valueId = {null};
                    walk(currNode, (node) -> {
                        if (node instanceof PsiIdentifierImpl p) {
                            if (equal[0] == null) {
                                valueId[0] = p;
                            }
                        }
                        if (node instanceof PsiJavaTokenImpl p) {
                            if (equal[0] == null && "=".equals(p.getText())) {
                                equal[0] = p;
                            }
                        }
                    });
                    valueName = getName(valueId[0].getText());
                    startLineNumber = valueId[0].getStartOffset();
                }
                if (currNode instanceof PsiCodeBlockImpl || currNode instanceof ClassElement) {
                    String finalValueName = "PsiMethodCallExpression:" + valueName + ".";
                    //walk(currNode);
                    int finalStartLineNumber = startLineNumber;
                    walk(currNode, (node) -> {
                        if (node instanceof PsiMethodCallExpressionImpl n) {
                            String str = n.toString();
                            if (str.startsWith(finalValueName)) {
                                if (str.contains("().")) {
                                    return;
                                }
                                if (finalStartLineNumber >= n.getStartOffset()) {
                                    return;
                                }
                                String functionName = str.substring(finalValueName.length());
                                if (functionName.startsWith("is") || functionName.startsWith("get")) {
                                    getCallers.add(str.substring(finalValueName.length(), str.length() - 2));
                                }
                            }
                        }
                    });
                    break;
                }
                currNode = currNode.getTreeParent();
                //System.out.println("--------");
                //walk(currNode);
                //System.out.println("--------");
            }
            HashMap<String, Boolean> m = new HashMap<>();
            for (int i = 0; i < getCallers.size(); i++) {
                String getCaller = getCallers.get(i);
                if (getCaller.startsWith("is")) {
                    getCaller = convertPascalToSnake(getCaller.substring(2));
                } else if (getCaller.startsWith("get")) {
                    getCaller = convertPascalToSnake(getCaller.substring(3));
                }
                if (m.getOrDefault(getCaller, false)) {
                    continue;
                }
                m.put(getCaller, true);
                if (i != 0) {
                    argumentList.append(",");
                }
                argumentList.append("`").append(getCaller).append("`");
            }
        }

        // check
        {

        }

        final String prefix = "";

        // fill
        {
            Project project = expression.getProject();
            TemplateManager manager = TemplateManager.getInstance(project);
            Template template = manager.createTemplate(getId(), "", ".from(\"" + argumentList + "\");");
            manager.startTemplate(editor, template);
        }
    }
    //private final static String START_WITH = "PsiNewExpression:new A<SFunction<";
    protected void expandForChooseExpression1(@NotNull PsiElement expression, @NotNull Editor editor) {
        ASTNode currNode = expression.getNode();

        final PsiMethodCallExpressionImpl[] newCallExpr = {null};
        final PsiNewExpressionImpl[] newExpr = {null};
        final String[] selectName = {null};
        StringBuilder argumentList = new StringBuilder();

        // find data
        {
            String valueName = null;
            List<String> getCallers = new ArrayList<>();
            while (true) {
                if (currNode == null) {
                    break;
                }
                if (currNode instanceof PsiNewExpressionImpl n) {
                    walk(n, (node) -> {
                        if (node instanceof PsiIdentifierImpl id) {
                            selectName[0] = id.toString().substring("PsiIdentifier:".length());
                        }
                    });
                }
                if (currNode instanceof PsiDeclarationStatementImpl || currNode instanceof FieldElement) {
                    {
                        if (currNode instanceof PsiDeclarationStatementImpl n) {
                            PsiElement[] elements = n.getDeclaredElements();
                            if (elements.length == 1) {
                                valueName = elements[0].toString().substring("PsiLocalVariable:".length());
                                valueName = getName(valueName);
                            }
                        }

                        final PsiJavaTokenImpl[] equal = {null};
                        final PsiIdentifierImpl[] valueId = {null};
                        if (currNode instanceof FieldElement) {
                            walk(currNode, (node) -> {
                                if (node instanceof PsiIdentifierImpl p) {
                                    if (equal[0] == null) {
                                        valueId[0] = p;
                                    }
                                }
                                if (node instanceof PsiJavaTokenImpl p) {
                                    if (equal[0] == null && "=".equals(p.getText())) {
                                        equal[0] = p;
                                    }
                                }
                            });
                            valueName = getName(valueId[0].getText());
                        }
                    }
                    walk(currNode, (node) -> {
                        if (node instanceof PsiMethodCallExpressionImpl p) {
                            if (newCallExpr[0] == null) {
                                newCallExpr[0] = p;
                            }
                        }
                        if (node instanceof PsiNewExpressionImpl p) {
                            if (newExpr[0] == null) {
                                newExpr[0] = p;
                            }
                        }
                    });
                }
                if (currNode instanceof PsiCodeBlockImpl || currNode instanceof ClassElement) {
                    String finalValueName = "PsiMethodCallExpression:" + valueName + ".";
                    walk(currNode);
                    walk(currNode, (node) -> {
                        if (node instanceof PsiMethodCallExpressionImpl n) {
                            String str = n.toString();
                            if (str.startsWith(finalValueName)) {
                                if (str.contains("().")) {
                                    return;
                                }
                                String functionName = str.substring(finalValueName.length());
                                if (functionName.startsWith("is") || functionName.startsWith("get")) {
                                    getCallers.add(str.substring(finalValueName.length(), str.length() - 2));
                                }
                            }
                        }
                    });
                    break;
                }
                currNode = currNode.getTreeParent();
                System.out.println("--------");
                walk(currNode);
                System.out.println("--------");
            }
            HashMap<String, Boolean> m = new HashMap<>();
            for (int i = 0; i < getCallers.size(); i++) {
                String getCaller = getCallers.get(i);
                if (m.getOrDefault(getCaller, false)) {
                    continue;
                }
                m.put(getCaller, true);
                if (i == 0) {
                    argumentList.append(selectName[0]).append("::").append(getCaller);
                } else {
                    argumentList.append(", ").append(selectName[0]).append("::").append(getCaller);

                }
            }
        }

        // check
        {
            if (newExpr[0] == null) {
                return;
            }
            if (selectName[0] == null) {
                return;
            }
        }

        final String prefix;
        // cut
        if (newCallExpr[0] != null) {
            Document document = editor.getDocument();
            document.deleteString(newCallExpr[0].getTextRange().getStartOffset(), newCallExpr[0].getTextRange().getEndOffset());
            prefix = newExpr[0].getText();
        } else {
            prefix = "";
        }

        // fill
        {
            Project project = expression.getProject();
            TemplateManager manager = TemplateManager.getInstance(project);
            Template template = manager.createTemplate(getId(), "", prefix + ".a(" + argumentList + ")");
            manager.startTemplate(editor, template);
        }
    }

    private String getName(String name) {
        if (name == null || name.length() <= 1) {
            return null;
        }
        name = name.substring(1);
        char firstChar = name.charAt(0);
        char lowerFirstChar = Character.toLowerCase(firstChar);

        return lowerFirstChar + name.substring(1);
    }

    private void walk(ASTNode node, Consumer<ASTNode> consumer) {
        consumer.accept(node);
        ASTNode[] children = node.getChildren(null);
        for (ASTNode child : children) {
            walk(child, consumer);
        }
    }

    private void walk(ASTNode node) {
        System.out.println(": " + node.getText() + "\t\t,Token Type:" + node.getClass().getSimpleName());
        ASTNode[] children = node.getChildren(null);
        for (ASTNode child : children) {
            walk(child);
        }
    }

    public static String convertPascalToSnake(String pascalCase) {
        if (pascalCase == null || pascalCase.isEmpty()) {
            return pascalCase;
        }

        StringBuilder snakeCase = new StringBuilder();
        char[] chars = pascalCase.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char currentChar = chars[i];

            if (Character.isUpperCase(currentChar) && i > 0) {
                snakeCase.append('_');
            }

            snakeCase.append(Character.toLowerCase(currentChar));
        }

        return snakeCase.toString();
    }
}

