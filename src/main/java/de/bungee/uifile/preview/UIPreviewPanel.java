package de.bungee.uifile.preview;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class UIPreviewPanel extends JPanel implements Disposable {
    private static final Logger LOG = Logger.getInstance(UIPreviewPanel.class);
    private static final int SCROLL_UNIT_INCREMENT = 16;
    private static final int DEBOUNCE_DELAY_MS = 300;
    private static final int BUTTON_SIZE = 40;
    private static final float BUTTON_FONT_SIZE = 20f;
    private static final int ZOOM_LABEL_WIDTH = 55;
    private static final int SEPARATOR_HEIGHT = 30;
    private static final JBColor PREVIEW_BG = new JBColor(Gray._24, Gray._24);

    private final UIComponentRenderer renderer;
    private Disposable currentListenerDisposable;
    private JLabel zoomLabel;
    private final Timer debounceTimer;
    private String pendingContent;

    public UIPreviewPanel() {
        super(new BorderLayout());
        this.renderer = new UIComponentRenderer();
        this.debounceTimer = new Timer(DEBOUNCE_DELAY_MS, e -> {
            if (pendingContent != null) {
                renderContent(pendingContent);
                pendingContent = null;
            }
        });
        this.debounceTimer.setRepeats(false);

        add(createScrollPane(), BorderLayout.CENTER);
        add(createToolbar(), BorderLayout.NORTH);

        setBackground(PREVIEW_BG);
        setOpaque(true);
        setupKeyboardShortcuts();
    }

    private JBScrollPane createScrollPane() {
        JBScrollPane scrollPane = new JBScrollPane(renderer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_UNIT_INCREMENT);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(SCROLL_UNIT_INCREMENT);
        scrollPane.setBackground(PREVIEW_BG);
        scrollPane.getViewport().setBackground(PREVIEW_BG);
        scrollPane.setOpaque(true);
        scrollPane.getViewport().setOpaque(true);

        renderer.addMouseWheelListener(e -> {
            if (e.isControlDown()) {
                if (e.getWheelRotation() < 0) {
                    renderer.zoomIn();
                } else {
                    renderer.zoomOut();
                }
                updateZoomLabel();
                e.consume();
            }
        });

        return scrollPane;
    }

    private void setupKeyboardShortcuts() {
        InputMap inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = getActionMap();

        registerZoomAction(inputMap, actionMap, "zoomIn",
            new String[]{"control PLUS", "control EQUALS", "control ADD"},
            () -> {
                renderer.zoomIn();
                updateZoomLabel();
            });

        registerZoomAction(inputMap, actionMap, "zoomOut",
            new String[]{"control MINUS", "control SUBTRACT"},
            () -> {
                renderer.zoomOut();
                updateZoomLabel();
            });

        registerZoomAction(inputMap, actionMap, "resetZoom",
            new String[]{"control 0", "control NUMPAD0"},
            () -> {
                renderer.resetZoom();
                updateZoomLabel();
            });
    }

    private void registerZoomAction(InputMap inputMap, ActionMap actionMap, String actionKey,
        String[] keyStrokes, Runnable action) {
        for (String keyStroke : keyStrokes) {
            inputMap.put(KeyStroke.getKeyStroke(keyStroke), actionKey);
        }
        actionMap.put(actionKey, new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                action.run();
            }
        });
    }

    private JComponent createToolbar() {
        JPanel toolbarPanel = new JPanel(new BorderLayout());
        toolbarPanel.setBorder(JBUI.Borders.customLine(JBColor.border(), 0, 0, 1, 0));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        leftPanel.setBackground(JBColor.background());

        JButton zoomInBtn = createStyledButton("+", "Zoom in (Ctrl + Plus)", e -> {
            renderer.zoomIn();
            updateZoomLabel();
        });

        JButton zoomOutBtn = createStyledButton("−", "Zoom out (Ctrl + Minus)", e -> {
            renderer.zoomOut();
            updateZoomLabel();
        });

        JButton resetBtn = createStyledButton("⟲", "Reset zoom to 100% (Ctrl + 0)", e -> {
            renderer.resetZoom();
            updateZoomLabel();
        });

        zoomLabel = new JLabel("100%");
        zoomLabel.setFont(zoomLabel.getFont().deriveFont(Font.BOLD, 13f));
        zoomLabel.setBorder(JBUI.Borders.empty(0, 12, 0, 8));
        zoomLabel.setForeground(JBColor.foreground());
        zoomLabel.setPreferredSize(JBUI.size(ZOOM_LABEL_WIDTH, BUTTON_SIZE));
        zoomLabel.setHorizontalAlignment(SwingConstants.CENTER);
        zoomLabel.setVerticalAlignment(SwingConstants.CENTER);

        leftPanel.add(zoomInBtn);
        leftPanel.add(zoomOutBtn);
        leftPanel.add(resetBtn);
        leftPanel.add(Box.createHorizontalStrut(5));
        leftPanel.add(createSeparator());
        leftPanel.add(zoomLabel);

        toolbarPanel.add(leftPanel, BorderLayout.WEST);
        return toolbarPanel;
    }

    private JSeparator createSeparator() {
        JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
        separator.setPreferredSize(JBUI.size(1, SEPARATOR_HEIGHT));
        separator.setBackground(JBColor.border());
        return separator;
    }

    private JButton createStyledButton(String text, String tooltip, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.setToolTipText(tooltip);
        button.setFocusable(false);
        button.addActionListener(listener);
        button.setPreferredSize(JBUI.size(BUTTON_SIZE, BUTTON_SIZE));
        button.setMinimumSize(JBUI.size(BUTTON_SIZE, BUTTON_SIZE));
        button.setMaximumSize(JBUI.size(BUTTON_SIZE, BUTTON_SIZE));
        button.setFont(button.getFont().deriveFont(Font.BOLD, BUTTON_FONT_SIZE));
        button.setMargin(JBUI.emptyInsets());
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setVerticalAlignment(SwingConstants.CENTER);
        button.setBorderPainted(true);
        button.setContentAreaFilled(true);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(JBUI.CurrentTheme.ActionButton.hoverBackground());
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(null);
            }
        });

        return button;
    }

    private void updateZoomLabel() {
        zoomLabel.setText(renderer.getZoomPercent() + "%");
    }

    public void updatePreview(@Nullable VirtualFile file) {
        if (file == null) {
            return;
        }

        removeCurrentDocumentListener();

        try {
            Document document = FileDocumentManager.getInstance().getDocument(file);
            String content;
            if (document != null) {
                content = document.getText();
                attachDocumentListener(file);
            } else {
                content = new String(file.contentsToByteArray(), file.getCharset());
            }
            renderContent(content);
        } catch (IOException e) {
            LOG.error("Failed to load preview for file: " + file.getPath(), e);
        }
    }

    private void removeCurrentDocumentListener() {
        if (currentListenerDisposable != null) {
            Disposer.dispose(currentListenerDisposable);
            currentListenerDisposable = null;
        }
    }

    private void attachDocumentListener(@NotNull VirtualFile file) {
        Document document = FileDocumentManager.getInstance().getDocument(file);
        if (document == null) {
            return;
        }

        currentListenerDisposable = Disposer.newDisposable();

        DocumentListener documentListener = new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                ApplicationManager.getApplication().invokeLater(() -> {
                    pendingContent = document.getText();
                    debounceTimer.restart();
                });
            }
        };

        document.addDocumentListener(documentListener, currentListenerDisposable);
    }

    private void renderContent(@NotNull String content) {
        try {
            UIModel model = UIModelParser.parse(content);
            renderer.setModel(model);
            renderer.revalidate();
            renderer.repaint();
        } catch (RuntimeException e) {
            showErrorModel("Parsing Error: " + e.getMessage());
            System.err.println("UI Preview parsing error: " + e.getMessage());
        }
    }

    private void showErrorModel(String errorMessage) {
        UIModel errorModel = new UIModel();
        UIModel.LabelComponent errorLabel = new UIModel.LabelComponent();
        errorLabel.setText(errorMessage);
        errorLabel.setTextColor(com.intellij.ui.JBColor.RED);
        errorLabel.setFontSize(12);
        errorModel.addComponent(errorLabel);

        renderer.setModel(errorModel);
        renderer.revalidate();
        renderer.repaint();
    }

    @Override
    public void dispose() {
        if (debounceTimer != null) {
            debounceTimer.stop();
        }
        removeCurrentDocumentListener();
    }
}