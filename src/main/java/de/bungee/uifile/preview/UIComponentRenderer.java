package de.bungee.uifile.preview;

import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import javax.swing.*;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.image.VolatileImage;
import java.util.*;
import java.util.List;

public class UIComponentRenderer extends JPanel {
    private static final int DEFAULT_WIDTH = 500;
    private static final int DEFAULT_HEIGHT = 320;
    private static final int DEFAULT_COMPONENT_WIDTH = 100;
    private static final int DEFAULT_COMPONENT_HEIGHT = 30;
    private static final float SHADOW_BASE_ALPHA = 0.3f;
    private static final int DEFAULT_ICON_SIZE = 16;
    private static final int DEFAULT_IMAGE_ICON_SIZE = 48;
    private static final int DEFAULT_CHECKBOX_SIZE = 20;

    private UIModel model;
    private double scale = 1.0;
    private VolatileImage backBuffer;
    private long lastModelHash = 0;
    private boolean needsRedraw = true;

    public UIComponentRenderer() {
        super();
        setOpaque(true);
        setBackground(new JBColor(Gray._24, Gray._24));
    }

    public void setModel(UIModel model) {
        this.model = model;
        long newHash = model != null ? model.hashCode() : 0;
        if (newHash != lastModelHash) {
            lastModelHash = newHash;
            needsRedraw = true;
        }
        updatePreferredSize();
    }

    private VolatileImage createBackBuffer() {
        GraphicsConfiguration gc = getGraphicsConfiguration();
        if (gc == null) {
            return null;
        }

        int width = Math.max(1, getWidth());
        int height = Math.max(1, getHeight());

        if (backBuffer == null ||
            backBuffer.validate(gc) == VolatileImage.IMAGE_INCOMPATIBLE ||
            backBuffer.getWidth() != width ||
            backBuffer.getHeight() != height) {

            if (backBuffer != null) {
                backBuffer.flush();
            }
            backBuffer = gc.createCompatibleVolatileImage(width, height, Transparency.TRANSLUCENT);
            needsRedraw = true;
        }
        return backBuffer;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (model == null) {
            return;
        }

        VolatileImage vImg = createBackBuffer();

        if (vImg != null) {
            do {
                int returnCode = vImg.validate(getGraphicsConfiguration());
                if (returnCode == VolatileImage.IMAGE_RESTORED || needsRedraw) {
                    Graphics2D g2 = vImg.createGraphics();
                    try {
                        renderToGraphics(g2);
                    } finally {
                        g2.dispose();
                    }
                    needsRedraw = false;
                }
                g.drawImage(vImg, 0, 0, this);
            } while (vImg.contentsLost());
        } else {
            renderToGraphics((Graphics2D) g);
        }
    }

    private void renderToGraphics(Graphics2D g2) {
        g2.setColor(new JBColor(Gray._24, Gray._24));
        g2.fillRect(0, 0, getWidth(), getHeight());

        setupRenderingHints(g2);
        g2.scale(scale, scale);

        List<UIModel.Component> sortedComponents = new ArrayList<>(model.getTopLevelComponents());
        sortedComponents.sort(Comparator.comparingInt(UIModel.Component::getZIndex));

        for (UIModel.Component component : sortedComponents) {
            Dimension dim = calculateComponentDimensions(component);
            layoutComponent(component, 0, 0, dim.width, dim.height, null);
            drawComponent(g2, component);
        }
    }

    private void setupRenderingHints(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }

    private Dimension calculateComponentDimensions(UIModel.Component component) {
        int width = component.getPreferredWidth();
        int height = component.getPreferredHeight();

        UIModel.Anchor anchor = component.getAnchor();
        if (anchor != null) {
            if (anchor.width != null && anchor.width > 0) {
                width = anchor.width;
            }
            if (anchor.height != null && anchor.height > 0) {
                height = anchor.height;
            }
        }

        if (width == 0 && height == 0) {
            width = DEFAULT_WIDTH;
            height = DEFAULT_HEIGHT;
        } else if (width == 0) {
            width = DEFAULT_WIDTH;
        } else if (height == 0) {
            height = DEFAULT_HEIGHT;
        }

        return new Dimension(width, height);
    }

    private void layoutComponent(UIModel.Component component, int x, int y, int width, int height,
        String parentLayout) {
        x += component.getMarginLeft();
        y += component.getMarginTop();
        width -= (component.getMarginLeft() + component.getMarginRight());
        height -= (component.getMarginTop() + component.getMarginBottom());

        UIModel.Anchor anchor = component.getAnchor();
        if (anchor != null) {
            Dimension adjustedSize = applyAnchorConstraints(anchor, x, y, width, height, component);
            x = (int) adjustedSize.getWidth();
            y = (int) adjustedSize.getHeight();
            width = adjustedSize.width;
            height = adjustedSize.height;
        }

        width = applyWidthConstraints(component, width);
        height = applyHeightConstraints(component, height);

        component.setBounds(x, y, width, height);

        if (component instanceof UIModel.GroupComponent) {
            layoutGroup((UIModel.GroupComponent) component);
        }
    }

    private Dimension applyAnchorConstraints(UIModel.Anchor anchor, int x, int y, int width, int height,
        UIModel.Component component) {
        if (anchor.fillParent != null && anchor.fillParent) {
            anchor.fillHorizontal = true;
            anchor.fillVertical = true;
        }

        if (anchor.fillHorizontal == null || !anchor.fillHorizontal) {
            if (anchor.width != null) {
                width = anchor.width;
            }
        }

        if (anchor.fillVertical == null || !anchor.fillVertical) {
            if (anchor.height != null) {
                height = anchor.height;
            }
        }

        if (anchor.horizontalAlignment != null) {
            int prefWidth = anchor.width != null ? anchor.width : component.getPreferredWidth();
            if (prefWidth > 0 && prefWidth < width) {
                if (isCenter(anchor.horizontalAlignment)) {
                    x += (width - prefWidth) / 2;
                    width = prefWidth;
                } else if ("Right".equalsIgnoreCase(anchor.horizontalAlignment)) {
                    x += width - prefWidth;
                    width = prefWidth;
                }
            }
        }

        if (anchor.verticalAlignment != null) {
            int prefHeight = anchor.height != null ? anchor.height : component.getPreferredHeight();
            if (prefHeight > 0 && prefHeight < height) {
                if (isCenter(anchor.verticalAlignment)) {
                    y += (height - prefHeight) / 2;
                    height = prefHeight;
                } else if ("Bottom".equalsIgnoreCase(anchor.verticalAlignment)) {
                    y += height - prefHeight;
                    height = prefHeight;
                }
            }
        }

        if (anchor.left != null) {
            x += anchor.left;
            width -= anchor.left;
        }
        if (anchor.right != null) {
            width -= anchor.right;
        }
        if (anchor.top != null) {
            y += anchor.top;
            height -= anchor.top;
        }
        if (anchor.bottom != null) {
            height -= anchor.bottom;
        }

        final int finalX = x;
        final int finalY = y;
        return new Dimension(width, height) {
            @Override
            public double getWidth() {
                return finalX;
            }

            @Override
            public double getHeight() {
                return finalY;
            }
        };
    }

    private boolean isCenter(String alignment) {
        return "Center".equalsIgnoreCase(alignment) || "Centre".equalsIgnoreCase(alignment);
    }

    private int applyWidthConstraints(UIModel.Component component, int width) {
        if (component.getMinWidth() > 0 && width < component.getMinWidth()) {
            width = component.getMinWidth();
        }
        if (component.getMaxWidth() > 0 && width > component.getMaxWidth()) {
            width = component.getMaxWidth();
        }
        return width;
    }

    private int applyHeightConstraints(UIModel.Component component, int height) {
        if (component.getMinHeight() > 0 && height < component.getMinHeight()) {
            height = component.getMinHeight();
        }
        if (component.getMaxHeight() > 0 && height > component.getMaxHeight()) {
            height = component.getMaxHeight();
        }
        return height;
    }

    private void layoutGroup(UIModel.GroupComponent parent) {
        int x = parent.x + parent.getPaddingLeft();
        int y = parent.y + parent.getPaddingTop();
        int width = parent.width - (parent.getPaddingLeft() + parent.getPaddingRight());
        int height = parent.height - (parent.getPaddingTop() + parent.getPaddingBottom());

        String layoutMode = parent.getLayoutMode();

        if ("Absolute".equalsIgnoreCase(layoutMode) || "Stack".equalsIgnoreCase(layoutMode)) {
            layoutAbsoluteGroup(parent, x, y, width, height, layoutMode);
            return;
        }

        boolean isHorizontal = isHorizontalLayout(layoutMode);
        int gap = parent.getGap();
        int totalGap = parent.getChildren().isEmpty() ? 0 : (parent.getChildren().size() - 1) * gap;

        FlexInfo flexInfo = calculateFlexInfo(parent, isHorizontal, width, height, totalGap);
        int currentPos = calculateStartPosition(parent, layoutMode, isHorizontal, width, height, flexInfo);

        layoutChildren(parent, x, y, width, height, isHorizontal, gap, flexInfo, currentPos, layoutMode);
    }

    private void layoutAbsoluteGroup(UIModel.GroupComponent parent, int x, int y, int width, int height,
        String layoutMode) {
        for (UIModel.Component child : parent.getChildren()) {
            int childWidth = child.getPreferredWidth() > 0 ? child.getPreferredWidth() : width;
            int childHeight = child.getPreferredHeight() > 0 ? child.getPreferredHeight() : height;
            layoutComponent(child, x, y, childWidth, childHeight, layoutMode);
        }
    }

    private boolean isHorizontalLayout(String layoutMode) {
        return layoutMode.equals("Left") || layoutMode.equals("Right") ||
               layoutMode.equals("Center") || layoutMode.equals("Centre") ||
               layoutMode.equals("Horizontal") || layoutMode.equals("Row");
    }

    private FlexInfo calculateFlexInfo(UIModel.GroupComponent parent, boolean isHorizontal, int width, int height,
        int totalGap) {
        float totalFlex = 0;
        int fixedSize = 0;

        for (UIModel.Component child : parent.getChildren()) {
            if (!child.isVisible()) {
                continue;
            }

            float childFlex = child.getFlexWeight();
            if (childFlex > 0) {
                totalFlex += childFlex;
            } else {
                int childSize = isHorizontal ?
                    child.getPreferredWidth() + child.getMarginLeft() + child.getMarginRight() :
                    child.getPreferredHeight() + child.getMarginTop() + child.getMarginBottom();

                if (childSize == 0) {
                    childSize = isHorizontal ? DEFAULT_COMPONENT_WIDTH : DEFAULT_COMPONENT_HEIGHT;
                }
                fixedSize += childSize;
            }
        }

        int availableSpace = isHorizontal ? width : height;
        int remainingSpace = Math.max(0, availableSpace - fixedSize - totalGap);

        return new FlexInfo(totalFlex, fixedSize, remainingSpace);
    }

    private int calculateStartPosition(UIModel.GroupComponent parent, String layoutMode, boolean isHorizontal,
        int width, int height, FlexInfo flexInfo) {
        String contentAlign = parent.getContentAlignment();
        int totalContentSize = flexInfo.fixedSize + (flexInfo.totalFlex > 0 ? flexInfo.remainingSpace : 0);
        int totalGap = parent.getChildren().isEmpty() ? 0 : (parent.getChildren().size() - 1) * parent.getGap();

        if (layoutMode.equals("Right") && isHorizontal) {
            return width - flexInfo.fixedSize - flexInfo.remainingSpace;
        } else if (layoutMode.equals("Bottom") && !isHorizontal) {
            return height - flexInfo.fixedSize - flexInfo.remainingSpace;
        } else if (layoutMode.equals("Center") || layoutMode.equals("Centre")) {
            return isHorizontal ?
                (width - totalContentSize - totalGap) / 2 :
                (height - totalContentSize - totalGap) / 2;
        } else if (isCenter(contentAlign)) {
            return isHorizontal ?
                (width - totalContentSize - totalGap) / 2 :
                (height - totalContentSize - totalGap) / 2;
        } else if ("End".equalsIgnoreCase(contentAlign)) {
            return isHorizontal ?
                width - totalContentSize - totalGap :
                height - totalContentSize - totalGap;
        }
        return 0;
    }

    private void layoutChildren(UIModel.GroupComponent parent, int x, int y, int width, int height,
        boolean isHorizontal, int gap, FlexInfo flexInfo, int currentPos, String layoutMode) {
        for (UIModel.Component child : parent.getChildren()) {
            if (!child.isVisible()) {
                continue;
            }

            if (isHorizontal) {
                int childWidth = calculateChildSize(child, flexInfo, isHorizontal, DEFAULT_COMPONENT_WIDTH);
                layoutComponent(child, x + currentPos, y, childWidth, height, layoutMode);
                currentPos += child.width + child.getMarginLeft() + child.getMarginRight() + gap;
            } else {
                int childHeight = calculateChildSize(child, flexInfo, isHorizontal, DEFAULT_COMPONENT_HEIGHT);
                layoutComponent(child, x, y + currentPos, width, childHeight, layoutMode);
                currentPos += child.height + child.getMarginTop() + child.getMarginBottom() + gap;
            }
        }
    }

    private int calculateChildSize(UIModel.Component child, FlexInfo flexInfo, boolean isHorizontal, int defaultSize) {
        if (child.getFlexWeight() > 0) {
            return (int) (flexInfo.remainingSpace * (child.getFlexWeight() / flexInfo.totalFlex));
        } else {
            int size = isHorizontal ? child.getPreferredWidth() : child.getPreferredHeight();
            return size == 0 ? defaultSize : size;
        }
    }

    private static class FlexInfo {
        final float totalFlex;
        final int fixedSize;
        final int remainingSpace;

        FlexInfo(float totalFlex, int fixedSize, int remainingSpace) {
            this.totalFlex = totalFlex;
            this.fixedSize = fixedSize;
            this.remainingSpace = remainingSpace;
        }
    }

    private void drawComponent(Graphics2D g, UIModel.Component component) {
        if (!component.isVisible()) {
            return;
        }

        Composite oldComposite = g.getComposite();
        if (component.getOpacity() < 1.0f) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, component.getOpacity()));
        }

        if (component.hasShadow()) {
            drawShadow(g, component);
        }

        if (component.getBackground() != null) {
            drawBackground(g, component);
        }

        if (component.getBorderWidth() > 0 && component.getBorderColor() != null) {
            drawBorder(g, component);
        }

        drawComponentSpecific(g, component);

        g.setComposite(oldComposite);
    }

    private void drawShadow(Graphics2D g, UIModel.Component component) {
        Color shadowColor = component.getShadowColor();
        int shadowX = component.x + component.getShadowOffsetX();
        int shadowY = component.y + component.getShadowOffsetY();
        int blur = component.getShadowBlur();

        for (int i = blur; i > 0; i--) {
            float alpha = (SHADOW_BASE_ALPHA / blur) * (blur - i + 1);
            Color shadowWithAlpha = new Color(
                shadowColor.getRed(),
                shadowColor.getGreen(),
                shadowColor.getBlue(),
                (int) (alpha * 255)
            );
            g.setColor(shadowWithAlpha);

            if (component.getBorderRadius() > 0) {
                g.fillRoundRect(shadowX - i, shadowY - i,
                    component.width + i * 2, component.height + i * 2,
                    component.getBorderRadius(), component.getBorderRadius());
            } else {
                g.fillRect(shadowX - i, shadowY - i,
                    component.width + i * 2, component.height + i * 2);
            }
        }
    }

    private void drawBackground(Graphics2D g, UIModel.Component component) {
        g.setColor(component.getBackground());
        if (component.getBorderRadius() > 0) {
            g.fillRoundRect(component.x, component.y, component.width, component.height,
                component.getBorderRadius(), component.getBorderRadius());
        } else {
            g.fillRect(component.x, component.y, component.width, component.height);
        }
    }

    private void drawBorder(Graphics2D g, UIModel.Component component) {
        g.setColor(component.getBorderColor());
        Stroke oldStroke = g.getStroke();
        g.setStroke(new BasicStroke(component.getBorderWidth()));
        if (component.getBorderRadius() > 0) {
            g.drawRoundRect(component.x, component.y, component.width, component.height,
                component.getBorderRadius(), component.getBorderRadius());
        } else {
            g.drawRect(component.x, component.y, component.width, component.height);
        }
        g.setStroke(oldStroke);
    }

    private void drawComponentSpecific(Graphics2D g, UIModel.Component component) {
        if (component instanceof UIModel.LabelComponent) {
            drawLabel(g, (UIModel.LabelComponent) component);
        } else if (component instanceof UIModel.ButtonComponent) {
            drawButton(g, (UIModel.ButtonComponent) component);
        } else if (component instanceof UIModel.TextFieldComponent) {
            drawTextField(g, (UIModel.TextFieldComponent) component);
        } else if (component instanceof UIModel.CheckBoxComponent) {
            drawCheckBox(g, (UIModel.CheckBoxComponent) component);
        } else if (component instanceof UIModel.ImageComponent) {
            drawImage(g, (UIModel.ImageComponent) component);
        } else if (component instanceof UIModel.ProgressBarComponent) {
            drawProgressBar(g, (UIModel.ProgressBarComponent) component);
        } else if (component instanceof UIModel.DividerComponent) {
            drawDivider(g, (UIModel.DividerComponent) component);
        } else if (component instanceof UIModel.GroupComponent group) {
            drawGroup(g, group);
        }
    }

    private void drawGroup(Graphics2D g, UIModel.GroupComponent group) {
        if (group.getChildren().isEmpty() && group.getBackground() == null) {
            g.setColor(new JBColor(new Color(100, 100, 110, 80), new Color(100, 100, 110, 80)));
            Stroke oldStroke = g.getStroke();
            float[] dashPattern = {5.0f, 5.0f};
            g.setStroke(
                new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dashPattern, 0.0f));
            g.drawRect(group.x, group.y, group.width, group.height);
            g.setStroke(oldStroke);
        }

        for (UIModel.Component child : group.getChildren()) {
            drawComponent(g, child);
        }
    }


    private void drawLabel(Graphics2D g, UIModel.LabelComponent label) {
        String text = label.getText();
        if (text.isEmpty()) {
            return;
        }

        g.setFont(createLabelFont(label));

        Color textColor = label.getTextColor();
        if (textColor == null) {
            textColor = new JBColor(new Color(220, 220, 230), new Color(220, 220, 230));
        }
        g.setColor(textColor);

        FontMetrics fm = g.getFontMetrics();
        Point textPos = calculateTextPosition(label, fm, text);
        g.drawString(text, textPos.x, textPos.y);
    }

    private Font createLabelFont(UIModel.LabelComponent label) {
        Map<TextAttribute, Object> attributes = new HashMap<>();
        attributes.put(TextAttribute.SIZE, (float) label.getFontSize());
        attributes.put(TextAttribute.WEIGHT, label.isBold() ? TextAttribute.WEIGHT_BOLD : TextAttribute.WEIGHT_REGULAR);

        if (label.getLetterSpacing() > 0) {
            attributes.put(TextAttribute.TRACKING, label.getLetterSpacing() * 0.01f);
        }

        Font font = Font.getFont(attributes);
        if (font == null) {
            font = new Font(Font.SANS_SERIF, label.isBold() ? Font.BOLD : Font.PLAIN, label.getFontSize());
            if (label.getLetterSpacing() > 0) {
                attributes.clear();
                attributes.put(TextAttribute.TRACKING, label.getLetterSpacing() * 0.01f);
                font = font.deriveFont(attributes);
            }
        }
        return font;
    }

    private Point calculateTextPosition(UIModel.LabelComponent label, FontMetrics fm, String text) {
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        int textX, textY;

        String hAlign = label.getHorizontalAlignment();
        String vAlign = label.getVerticalAlignment();

        if (isCenter(hAlign)) {
            textX = label.x + (label.width - textWidth) / 2;
        } else if ("Right".equalsIgnoreCase(hAlign)) {
            textX = label.x + label.width - textWidth - label.getPaddingRight();
        } else {
            textX = label.x + label.getPaddingLeft();
        }

        if (isCenter(vAlign) || "Middle".equalsIgnoreCase(vAlign)) {
            textY = label.y + (label.height - textHeight) / 2 + fm.getAscent();
        } else if ("Bottom".equalsIgnoreCase(vAlign)) {
            textY = label.y + label.height - fm.getDescent() - label.getPaddingBottom();
        } else {
            textY = label.y + fm.getAscent() + label.getPaddingTop();
        }

        return new Point(textX, textY);
    }

    private void drawButton(Graphics2D g, UIModel.ButtonComponent button) {
        Color bgColor = button.getBackground();
        if (bgColor == null) {
            bgColor = new JBColor(new Color(58, 123, 213), new Color(58, 123, 213));
        }
        g.setColor(bgColor);

        int radius = button.getBorderRadius();
        fillShape(g, button.x, button.y, button.width, button.height, radius);

        if (button.getBorderWidth() > 0 && button.getBorderColor() != null) {
            drawShapeBorder(g, button.x, button.y, button.width, button.height, radius,
                button.getBorderColor(), button.getBorderWidth());
        }

        int contentX = button.x;
        int availableWidth = button.width;

        if (button.hasIcon()) {
            int iconX = button.x + 8;
            int iconY = button.y + (button.height - DEFAULT_ICON_SIZE) / 2;

            g.setColor(new JBColor(new Color(255, 255, 255, 180), new Color(255, 255, 255, 180)));
            g.fillRect(iconX, iconY, DEFAULT_ICON_SIZE, DEFAULT_ICON_SIZE);

            contentX = iconX + DEFAULT_ICON_SIZE + 4;
            availableWidth = button.width - (contentX - button.x) - 8;
        }

        drawButtonText(g, button, contentX, availableWidth);
    }

    private void drawButtonText(Graphics2D g, UIModel.ButtonComponent button, int contentX, int availableWidth) {
        String text = button.getText();
        if (text.isEmpty()) {
            return;
        }

        UIModel.LabelComponent labelStyle = button.getLabelStyle();
        if (labelStyle != null) {
            g.setFont(
                new Font(Font.SANS_SERIF, labelStyle.isBold() ? Font.BOLD : Font.PLAIN, labelStyle.getFontSize()));
            g.setColor(labelStyle.getTextColor() != null ? labelStyle.getTextColor() : JBColor.WHITE);
        } else {
            g.setFont(g.getFont().deriveFont(Font.BOLD, 14f));
            g.setColor(JBColor.WHITE);
        }

        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textX = contentX + (availableWidth - textWidth) / 2;
        int textY = button.y + (button.height + fm.getAscent() - fm.getDescent()) / 2;

        g.drawString(text, textX, textY);
    }

    private void fillShape(Graphics2D g, int x, int y, int width, int height, int radius) {
        if (radius > 0) {
            g.fillRoundRect(x, y, width, height, radius, radius);
        } else {
            g.fillRect(x, y, width, height);
        }
    }

    private void drawShapeBorder(Graphics2D g, int x, int y, int width, int height, int radius, Color color,
        int borderWidth) {
        g.setColor(color);
        Stroke oldStroke = g.getStroke();
        g.setStroke(new BasicStroke(borderWidth));
        if (radius > 0) {
            g.drawRoundRect(x, y, width, height, radius, radius);
        } else {
            g.drawRect(x, y, width, height);
        }
        g.setStroke(oldStroke);
    }

    private void drawTextField(Graphics2D g, UIModel.TextFieldComponent textField) {
        g.setColor(new JBColor(new Color(30, 30, 40), new Color(50, 50, 60)));
        int radius = textField.getBorderRadius();
        fillShape(g, textField.x, textField.y, textField.width, textField.height, radius);

        drawShapeBorder(g, textField.x, textField.y, textField.width, textField.height, radius,
            new JBColor(new Color(70, 70, 80), new Color(90, 90, 100)), 1);

        String placeholder = textField.getPlaceholder();
        if (!placeholder.isEmpty()) {
            g.setColor(new JBColor(new Color(140, 140, 150), new Color(140, 140, 150)));
            g.setFont(g.getFont().deriveFont(Font.PLAIN, 13f));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(placeholder, textField.x + 12,
                textField.y + (textField.height + fm.getAscent() - fm.getDescent()) / 2);
        }
    }

    private void drawCheckBox(Graphics2D g, UIModel.CheckBoxComponent checkBox) {
        int boxX = checkBox.x + 4;
        int boxY = checkBox.y + (checkBox.height - DEFAULT_CHECKBOX_SIZE) / 2;

        g.setColor(checkBox.isChecked() ?
            new JBColor(new Color(58, 123, 213), new Color(58, 123, 213)) :
            new JBColor(new Color(40, 40, 50), new Color(60, 60, 70)));
        g.fillRect(boxX, boxY, DEFAULT_CHECKBOX_SIZE, DEFAULT_CHECKBOX_SIZE);

        drawShapeBorder(g, boxX, boxY, DEFAULT_CHECKBOX_SIZE, DEFAULT_CHECKBOX_SIZE, 0,
            new JBColor(new Color(90, 90, 100), new Color(110, 110, 120)), 1);

        if (checkBox.isChecked()) {
            drawCheckMark(g, boxX, boxY);
        }

        drawCheckBoxLabel(g, checkBox, boxX);
    }

    private void drawCheckMark(Graphics2D g, int boxX, int boxY) {
        g.setColor(JBColor.WHITE);
        Stroke oldStroke = g.getStroke();
        g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int[] xPoints = {boxX + 5, boxX + 8, boxX + 15};
        int[] yPoints = {boxY + 10, boxY + 14, boxY + 6};
        g.drawPolyline(xPoints, yPoints, 3);
        g.setStroke(oldStroke);
    }

    private void drawCheckBoxLabel(Graphics2D g, UIModel.CheckBoxComponent checkBox, int boxX) {
        String text = checkBox.getText();
        if (text.isEmpty()) {
            return;
        }

        g.setColor(new JBColor(new Color(220, 220, 230), new Color(220, 220, 230)));
        g.setFont(g.getFont().deriveFont(Font.PLAIN, 13f));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, boxX + DEFAULT_CHECKBOX_SIZE + 10,
            checkBox.y + (checkBox.height + fm.getAscent() - fm.getDescent()) / 2);
    }

    private void drawImage(Graphics2D g, UIModel.ImageComponent image) {
        g.setColor(new JBColor(new Color(50, 50, 60), new Color(50, 50, 60)));
        int radius = image.getBorderRadius();
        fillShape(g, image.x, image.y, image.width, image.height, radius);

        drawShapeBorder(g, image.x, image.y, image.width, image.height, radius,
            new JBColor(new Color(80, 80, 90), new Color(100, 100, 110)), 1);

        drawImagePlaceholder(g, image);
    }

    private void drawImagePlaceholder(Graphics2D g, UIModel.ImageComponent image) {
        g.setColor(new JBColor(new Color(255, 255, 255, 100), new Color(255, 255, 255, 100)));
        int iconSize = Math.min(DEFAULT_IMAGE_ICON_SIZE, Math.min(image.width, image.height) / 2);
        int iconX = image.x + (image.width - iconSize) / 2;
        int iconY = image.y + (image.height - iconSize) / 2 - 10;

        g.fillRect(iconX, iconY + 4, iconSize - 4, iconSize - 4);
        g.fillPolygon(new int[]{iconX + iconSize - 4, iconX + iconSize - 4, iconX + iconSize},
            new int[]{iconY + 4, iconY + 10, iconY + 10}, 3);

        g.setColor(new JBColor(new Color(200, 200, 210), new Color(200, 200, 210)));
        g.setFont(g.getFont().deriveFont(Font.PLAIN, 11f));
        FontMetrics fm = g.getFontMetrics();

        String label = "Image";
        int labelWidth = fm.stringWidth(label);
        g.drawString(label, image.x + (image.width - labelWidth) / 2,
            image.y + (image.height) / 2 + iconSize / 2 + 5);

        String path = image.getTexturePath();
        if (!path.isEmpty()) {
            String fileName = path.substring(path.lastIndexOf('/') + 1);
            if (fileName.length() > 20) {
                fileName = fileName.substring(0, 17) + "...";
            }
            g.setFont(g.getFont().deriveFont(Font.PLAIN, 9f));
            g.setColor(new JBColor(new Color(150, 150, 160), new Color(150, 150, 160)));
            fm = g.getFontMetrics();
            int fileNameWidth = fm.stringWidth(fileName);
            g.drawString(fileName, image.x + (image.width - fileNameWidth) / 2,
                image.y + (image.height) / 2 + iconSize / 2 + 20);
        }
    }

    private void drawProgressBar(Graphics2D g, UIModel.ProgressBarComponent progressBar) {
        int radius = progressBar.getBorderRadius();

        g.setColor(new JBColor(new Color(40, 40, 50), new Color(60, 60, 70)));
        fillShape(g, progressBar.x, progressBar.y, progressBar.width, progressBar.height, radius);

        int progressWidth = (int) (progressBar.width * progressBar.getValue());
        if (progressWidth > 0) {
            g.setColor(new JBColor(new Color(76, 175, 80), new Color(76, 175, 80)));
            fillShape(g, progressBar.x, progressBar.y, progressWidth, progressBar.height, radius);
        }

        drawShapeBorder(g, progressBar.x, progressBar.y, progressBar.width, progressBar.height, radius,
            new JBColor(new Color(70, 70, 80), new Color(90, 90, 100)), 1);
    }

    private void drawDivider(Graphics2D g, UIModel.DividerComponent divider) {
        Stroke oldStroke = g.getStroke();
        g.setStroke(new BasicStroke(1.5f));
        g.setColor(new JBColor(new Color(60, 60, 70), new Color(80, 80, 90)));

        if (divider.isVertical()) {
            int x = divider.x + divider.width / 2;
            g.drawLine(x, divider.y, x, divider.y + divider.height);
        } else {
            int y = divider.y + divider.height / 2;
            g.drawLine(divider.x, y, divider.x + divider.width, y);
        }

        g.setStroke(oldStroke);
    }

    public void zoomIn() {
        scale *= 1.1;
        updateAfterZoom();
    }

    public void zoomOut() {
        scale /= 1.1;
        updateAfterZoom();
    }

    public void resetZoom() {
        scale = 1.0;
        updateAfterZoom();
    }

    private void updateAfterZoom() {
        needsRedraw = true;
        updatePreferredSize();
        revalidate();
        repaint();
    }

    public int getZoomPercent() {
        return (int) Math.round(scale * 100);
    }

    private void updatePreferredSize() {
        if (model == null) {
            return;
        }

        int maxWidth = 0;
        int maxHeight = 0;

        for (UIModel.Component component : model.getTopLevelComponents()) {
            Dimension dim = calculateComponentDimensions(component);
            maxWidth = Math.max(maxWidth, (int) (dim.width * scale));
            maxHeight = Math.max(maxHeight, (int) (dim.height * scale));
        }
        setPreferredSize(new Dimension(maxWidth, maxHeight));
    }
}

