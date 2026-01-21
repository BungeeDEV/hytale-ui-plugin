package de.bungee.uifile.preview;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import de.bungee.uifile.UIFileType;
import org.jetbrains.annotations.NotNull;

public class UIPreviewToolWindowFactory implements ToolWindowFactory, DumbAware {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        UIPreviewPanel previewPanel = new UIPreviewPanel(project);
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(previewPanel, "", false);
        toolWindow.getContentManager().addContent(content);

        project.getMessageBus().connect().subscribe(
            FileEditorManagerListener.FILE_EDITOR_MANAGER,
            new FileEditorManagerListener() {
                @Override
                public void selectionChanged(@NotNull FileEditorManagerEvent event) {
                    VirtualFile file = event.getNewFile();
                    if (file != null && file.getFileType() instanceof UIFileType) {
                        previewPanel.updatePreview(file);
                    }
                }
            }
        );

        VirtualFile[] selectedFiles = FileEditorManager.getInstance(project).getSelectedFiles();
        if (selectedFiles.length > 0 && selectedFiles[0].getFileType() instanceof UIFileType) {
            previewPanel.updatePreview(selectedFiles[0]);
        }
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        return true;
    }
}


