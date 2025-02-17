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
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.PsiTypeElementImpl;
import com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl;
import com.intellij.psi.impl.source.tree.java.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * @author codewithyou365
 */
public class CheckState extends PostfixTemplateWithExpressionSelector {
    //private final static String START_WITH = "PsiNewExpression:new A<SFunction<";
    private final static String START_WITH = "PsiReferenceExpression:_checkState";

    protected CheckState(@Nullable PostfixTemplateProvider provider) {
        super(null, ".autoCheckState", "automatic state checking", JavaPostfixTemplatesUtils.selectorAllExpressionsWithCurrentOffset(element -> {
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
        Document doc = null;
        {
            Project project = currNode.getPsi().getProject();
            PsiFile psiFile = currNode.getPsi().getContainingFile();
            PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);

            if (psiFile != null) {
                psiDocumentManager.commitAllDocuments();
                doc = psiDocumentManager.getDocument(psiFile);
            }
        }

        List<String> result = new ArrayList<>();
        // find data
        {
            ASTNode topClassNode = null;
            AtomicReference<String> enumType = new AtomicReference<>();
            while (true) {
                if (currNode == null) {
                    break;
                }
                if (currNode instanceof ClassElement) {
                    topClassNode = currNode;
                }

                if (currNode instanceof PsiCodeBlockImpl && enumType.get() == null) {
                    walk(currNode, (nodeInBlock) -> {
                        if (nodeInBlock instanceof PsiTypeElementImpl && enumType.get() == null) {
                            enumType.set(nodeInBlock.getText());
                            return false;
                        }
                        return true;
                    });
                }
                currNode = currNode.getTreeParent();
                //System.out.println("--------");
                //walk(currNode);
                //System.out.println("--------");
                //currNode = currNode.getTreeParent();

            }
            String finalEnumType = enumType.get();
            Document finalDoc = doc;
            walk(topClassNode, (node) -> {
                List<String> fromStates = new ArrayList<>();
                List<String> toStates = new ArrayList<>();
                List<String> line = new ArrayList<>();
                if (node instanceof PsiIfStatementImpl p) {
                    PsiElement[] children = p.getChildren();
                    if (children.length > 6 &&
                            children[0] instanceof PsiKeywordImpl && children[0].getText().equals("if") &&
                            children[1] instanceof PsiWhiteSpaceImpl &&
                            children[2] instanceof PsiJavaTokenImpl && children[2].getText().equals("(") &&
                            children[4] instanceof PsiJavaTokenImpl && children[4].getText().equals(")") &&
                            children[5] instanceof PsiWhiteSpaceImpl &&
                            children[6] instanceof PsiBlockStatementImpl
                    ) {
                        walk(children[3].getNode(), (nodeInIfArg) -> {
                            tryAdd(finalDoc, line, fromStates, finalEnumType, nodeInIfArg);
                            return true;
                        });

                        if (children[6] instanceof PsiBlockStatementImpl ifBlock) {
                            walk(ifBlock, (nodeInIfBlock) -> {
                                tryAdd(finalDoc, line, toStates, finalEnumType, nodeInIfBlock);
                                return true;
                            });
                        }
                    }
                } else if (node instanceof PsiMethodCallExpressionImpl callNode) {
                    if (callNode.getChildren().length > 1) {
                        if (callNode.getChildren()[1] instanceof PsiExpressionListImpl) {
                            PsiElement[] children = callNode.getChildren()[1].getChildren();
                            for (PsiElement child : children) {
                                if (child instanceof PsiMethodCallExpressionImpl fromCall && child.getFirstChild().getText().equals("from")) {
                                    walk(fromCall, (nodeInFromCall) -> {
                                        tryAdd(finalDoc, line, fromStates, finalEnumType, nodeInFromCall);
                                        return true;
                                    });
                                } else {
                                    walk(child.getNode(), (nodeInFromCall) -> {
                                        tryAdd(finalDoc, line, toStates, finalEnumType, nodeInFromCall);
                                        return true;
                                    });
                                }
                            }
                        }
                    }
                }

                if (fromStates.size() != 0 && toStates.size() != 0 && line.size() != 0) {
                    result.add(String.join(",", fromStates));
                    result.add(String.join(",", toStates));
                    result.add(line.get(0));
                    return false;
                }
                return true;
            });
        }
        // fill
        {
            Project project = expression.getProject();
            TemplateManager manager = TemplateManager.getInstance(project);
            Template template = manager.createTemplate(getId(), "", ".checkState(\"" + String.join("\",\"", result) + "\");");
            manager.startTemplate(editor, template);
        }

    }


    private void tryAdd(Document doc, List<String> line, List<String> out, String enumType, ASTNode node) {
        if (node instanceof PsiReferenceExpressionImpl) {
            String s = node.getText();
            boolean matches = s.matches(enumType + "\\.[A-Z_][A-Z0-9_]*");
            if (matches) {
                out.add(s.split("\\.")[1]);
                if (doc != null) {
                    int lineNumber = doc.getLineNumber(node.getStartOffset()) + 1;
                    line.add(String.valueOf(lineNumber));
                } else {
                    line.add(String.valueOf(0));
                }
            }
        }
    }

    private void walk(ASTNode node, Function<ASTNode, Boolean> func) {
        Boolean next = func.apply(node);
        if (!next) {
            return;
        }
        ASTNode[] children = node.getChildren(null);
        for (ASTNode child : children) {
            walk(child, func);
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

