package de.bungee.uifile.highlighter;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

/**
 * Quick fix to add a missing semicolon at the end of a statement
 */
public class AddSemicolonFix extends PsiElementBaseIntentionAction implements IntentionAction {

    private final int insertOffset;

    public AddSemicolonFix(int insertOffset) {
        this.insertOffset = insertOffset;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element)
        throws IncorrectOperationException {
        Document document = editor.getDocument();
        document.insertString(insertOffset, ";");
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        return true;
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return "Add missing semicolon";
    }

    @NotNull
    @Override
    public String getText() {
        return "Add semicolon";
    }
}

