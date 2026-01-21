package de.bungee.uifile.preview;

import com.intellij.ui.JBColor;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UIModel {
    private final List<Component> topLevelComponents = new ArrayList<>();
    private final Map<String, Map<String, Object>> globalStyles = new HashMap<>();
    private final Map<String, Object> variables = new HashMap<>();

    public void addComponent(Component component) {
        topLevelComponents.add(component);
    }

    public List<Component> getTopLevelComponents() {
        return topLevelComponents;
    }

    public void addStyle(String name, Map<String, Object> style) {
        globalStyles.put(name, style);
    }

    public Map<String, Object> getStyle(String name) {
        return globalStyles.get(name);
    }

    public void setVariable(String name, Object value) {
        variables.put(name, value);
    }

    public Object getVariable(String name) {
        return variables.get(name);
    }

    @Deprecated
    public List<GroupComponent> getTopLevelGroups() {
        List<GroupComponent> groups = new ArrayList<>();
        for (Component component : topLevelComponents) {
            if (component instanceof GroupComponent) {
                groups.add((GroupComponent) component);
            }
        }
        return groups;
    }

    public static class Anchor {
        public Integer width, height, full;
        public Float flexWeight;
        public Integer top, bottom, left, right;
        public String horizontalAlignment;
        public String verticalAlignment;
        public Boolean fillHorizontal;
        public Boolean fillVertical;
        public Boolean fillParent;
    }

    public static class Component {
        protected int x, y, width, height;
        protected int prefWidth, prefHeight;
        protected int minWidth, minHeight, maxWidth, maxHeight;
        protected float flexWeight = 0;
        protected Color background;
        protected Anchor anchor;
        protected int marginTop, marginBottom, marginLeft, marginRight;
        protected int paddingTop, paddingBottom, paddingLeft, paddingRight;
        protected Map<String, Object> properties = new HashMap<>();
        protected String id;
        protected String styleRef;
        protected Color borderColor;
        protected int borderWidth = 0;
        protected int borderRadius = 0;
        protected float opacity = 1.0f;
        protected boolean visible = true;
        protected int zIndex = 0;
        protected boolean hasShadow = false;
        protected Color shadowColor;
        protected int shadowOffsetX = 0;
        protected int shadowOffsetY = 2;
        protected int shadowBlur = 4;
        protected String horizontalAlignment = "Left";
        protected String verticalAlignment = "Top";

        // Source code position for navigation
        protected int sourceLineNumber = -1;
        protected int sourceColumnNumber = -1;

        public void setDimensions(int w, int h) {
            this.prefWidth = w;
            this.prefHeight = h;
        }

        public int getPreferredWidth() {
            return prefWidth;
        }

        public int getPreferredHeight() {
            return prefHeight;
        }

        public void setFlexWeight(float f) {
            this.flexWeight = f;
        }

        public float getFlexWeight() {
            return flexWeight;
        }

        public void setBackground(Color c) {
            this.background = c;
        }

        public Color getBackground() {
            return background;
        }

        public void setAnchor(Anchor a) {
            this.anchor = a;
        }

        public Anchor getAnchor() {
            return anchor;
        }

        public void setMargin(int top, int right, int bottom, int left) {
            this.marginTop = top;
            this.marginRight = right;
            this.marginBottom = bottom;
            this.marginLeft = left;
        }

        public void setPadding(int top, int right, int bottom, int left) {
            this.paddingTop = top;
            this.paddingRight = right;
            this.paddingBottom = bottom;
            this.paddingLeft = left;
        }

        public int getMarginTop() {
            return marginTop;
        }

        public int getMarginBottom() {
            return marginBottom;
        }

        public int getMarginLeft() {
            return marginLeft;
        }

        public int getMarginRight() {
            return marginRight;
        }

        public int getPaddingTop() {
            return paddingTop;
        }

        public int getPaddingBottom() {
            return paddingBottom;
        }

        public int getPaddingLeft() {
            return paddingLeft;
        }

        public int getPaddingRight() {
            return paddingRight;
        }

        public Rectangle getBounds() {
            return new Rectangle(x, y, width, height);
        }

        public void setBounds(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
        }

        public void setProperty(String key, Object value) {
            properties.put(key, value);
        }

        public Object getProperty(String key) {
            return properties.get(key);
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public void setSourcePosition(int lineNumber, int columnNumber) {
            this.sourceLineNumber = lineNumber;
            this.sourceColumnNumber = columnNumber;
        }

        public int getSourceLineNumber() {
            return sourceLineNumber;
        }

        public int getSourceColumnNumber() {
            return sourceColumnNumber;
        }

        public void setStyleRef(String styleRef) {
            this.styleRef = styleRef;
        }

        public String getStyleRef() {
            return styleRef;
        }

        public void setMinWidth(int w) {
            this.minWidth = w;
        }

        public int getMinWidth() {
            return minWidth;
        }

        public void setMinHeight(int h) {
            this.minHeight = h;
        }

        public int getMinHeight() {
            return minHeight;
        }

        public void setMaxWidth(int w) {
            this.maxWidth = w;
        }

        public int getMaxWidth() {
            return maxWidth;
        }

        public void setMaxHeight(int h) {
            this.maxHeight = h;
        }

        public int getMaxHeight() {
            return maxHeight;
        }

        public void setBorder(Color color, int width) {
            this.borderColor = color;
            this.borderWidth = width;
        }

        public Color getBorderColor() {
            return borderColor;
        }

        public int getBorderWidth() {
            return borderWidth;
        }

        public void setBorderRadius(int radius) {
            this.borderRadius = radius;
        }

        public int getBorderRadius() {
            return borderRadius;
        }

        public void setOpacity(float opacity) {
            this.opacity = Math.max(0f, Math.min(1f, opacity));
        }

        public float getOpacity() {
            return opacity;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }

        public boolean isVisible() {
            return visible;
        }

        public void setZIndex(int zIndex) {
            this.zIndex = zIndex;
        }

        public int getZIndex() {
            return zIndex;
        }

        public void setHorizontalAlignment(String alignment) {
            this.horizontalAlignment = alignment;
        }

        public String getHorizontalAlignment() {
            return horizontalAlignment;
        }

        public void setVerticalAlignment(String alignment) {
            this.verticalAlignment = alignment;
        }

        public String getVerticalAlignment() {
            return verticalAlignment;
        }

        public void setShadow(Color color, int offsetX, int offsetY, int blur) {
            this.hasShadow = true;
            this.shadowColor = color;
            this.shadowOffsetX = offsetX;
            this.shadowOffsetY = offsetY;
            this.shadowBlur = blur;
        }

        public boolean hasShadow() {
            return hasShadow;
        }

        public Color getShadowColor() {
            return shadowColor;
        }

        public int getShadowOffsetX() {
            return shadowOffsetX;
        }

        public int getShadowOffsetY() {
            return shadowOffsetY;
        }

        public int getShadowBlur() {
            return shadowBlur;
        }
    }

    public static class GroupComponent extends Component {
        private String layoutMode = "Top";
        private int padding = 0;
        private int gap = 0;
        private final List<Component> children = new ArrayList<>();
        private boolean wrapContent = false;
        private String contentAlignment = "Start";

        public void addChild(Component child) {
            children.add(child);
        }

        public List<Component> getChildren() {
            return children;
        }

        public void setLayoutMode(String mode) {
            this.layoutMode = mode;
        }

        public String getLayoutMode() {
            return layoutMode;
        }

        public void setPadding(int p) {
            this.padding = p;
        }

        public int getPadding() {
            return padding;
        }

        public void setGap(int gap) {
            this.gap = gap;
        }

        public int getGap() {
            return gap;
        }

        public void setWrapContent(boolean wrap) {
            this.wrapContent = wrap;
        }

        public boolean isWrapContent() {
            return wrapContent;
        }

        public void setContentAlignment(String alignment) {
            this.contentAlignment = alignment;
        }

        public String getContentAlignment() {
            return contentAlignment;
        }
    }

    public static class LabelComponent extends Component {
        private String text = "";
        private int fontSize = 12;
        private Color textColor = JBColor.WHITE;
        private boolean bold = false;
        private boolean uppercase = false;
        private float letterSpacing = 0;
        private String horizontalAlignment = "Left";
        private String verticalAlignment = "Top";

        public void setText(String t) {
            this.text = t;
        }

        public String getText() {
            return uppercase ? text.toUpperCase() : text;
        }

        public String getRawText() {
            return text;
        }

        public void setFontSize(int s) {
            this.fontSize = s;
        }

        public int getFontSize() {
            return fontSize;
        }

        public void setTextColor(Color c) {
            this.textColor = c;
        }

        public Color getTextColor() {
            return textColor;
        }

        public void setBold(boolean b) {
            this.bold = b;
        }

        public boolean isBold() {
            return bold;
        }

        public void setUppercase(boolean u) {
            this.uppercase = u;
        }

        public void setLetterSpacing(float s) {
            this.letterSpacing = s;
        }

        public float getLetterSpacing() {
            return letterSpacing;
        }

        public void setAlignment(String a) {
            this.horizontalAlignment = a;
        }

        public String getAlignment() {
            return horizontalAlignment;
        }

        public void setHorizontalAlignment(String a) {
            this.horizontalAlignment = a;
        }

        public String getHorizontalAlignment() {
            return horizontalAlignment;
        }

        public void setVerticalAlignment(String a) {
            this.verticalAlignment = a;
        }

        public String getVerticalAlignment() {
            return verticalAlignment;
        }
    }

    public static class ButtonComponent extends Component {
        private String text = "";
        private LabelComponent labelStyle;
        private boolean hasIcon = false;

        public void setText(String t) {
            this.text = t;
        }

        public String getText() {
            return text;
        }

        public void setLabelStyle(LabelComponent style) {
            this.labelStyle = style;
        }

        public LabelComponent getLabelStyle() {
            return labelStyle;
        }

        public void setHasIcon(boolean hasIcon) {
            this.hasIcon = hasIcon;
        }

        public boolean hasIcon() {
            return hasIcon;
        }
    }

    public static class TextFieldComponent extends Component {
        private String placeholder = "";

        public void setPlaceholder(String p) {
            this.placeholder = p;
        }

        public String getPlaceholder() {
            return placeholder;
        }
    }

    public static class CheckBoxComponent extends Component {
        private String text = "";
        private boolean checked = false;

        public void setText(String t) {
            this.text = t;
        }

        public String getText() {
            return text;
        }

        public void setChecked(boolean c) {
            this.checked = c;
        }

        public boolean isChecked() {
            return checked;
        }
    }

    public static class ImageComponent extends Component {
        private String texturePath = "";

        public void setTexturePath(String path) {
            this.texturePath = path;
        }

        public String getTexturePath() {
            return texturePath;
        }
    }

    public static class ProgressBarComponent extends Component {
        private float value = 0.5f;

        public void setValue(float v) {
            this.value = Math.max(0f, Math.min(1f, v));
        }

        public float getValue() {
            return value;
        }
    }

    public static class DividerComponent extends Component {
        private boolean vertical = false;

        public void setVertical(boolean v) {
            this.vertical = v;
        }

        public boolean isVertical() {
            return vertical;
        }
    }
}