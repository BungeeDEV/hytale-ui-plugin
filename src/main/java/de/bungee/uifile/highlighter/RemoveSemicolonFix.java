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
 * Quick fix to remove an unnecessary semicolon after a component block
 */
public class RemoveSemicolonFix extends PsiElementBaseIntentionAction implements IntentionAction {

    private final PsiElement semicolonElement;

    public RemoveSemicolonFix(PsiElement semicolonElement) {
        this.semicolonElement = semicolonElement;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element)
        throws IncorrectOperationException {
        Document document = editor.getDocument();
        int startOffset = semicolonElement.getTextRange().getStartOffset();
        int endOffset = semicolonElement.getTextRange().getEndOffset();
        document.deleteString(startOffset, endOffset);
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        return semicolonElement.isValid();
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return "Remove unnecessary semicolon";
    }

    @NotNull
    @Override
    public String getText() {
        return "Remove semicolon";
    }
}

