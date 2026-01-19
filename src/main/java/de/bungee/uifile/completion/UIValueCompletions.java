package de.bungee.uifile.completion;

import java.util.*;

/**
 * Provides value completions for UI file properties
 */
public class UIValueCompletions {

    /**
     * LayoutMode values for Group and Container components
     */
    public static final List<String> LAYOUT_MODES = Arrays.asList(
        "Top",
        "TopScrolling",
        "Middle",
        "MiddleCenter",
        "CenterMiddle",
        "Center",
        "Bottom",
        "BottomScrolling",
        "Left",
        "LeftCenterWrap",
        "Right",
        "Full"
    );

    /**
     * Common boolean values
     */
    public static final List<String> BOOLEAN_VALUES = Arrays.asList(
        "true",
        "false"
    );

    /**
     * Horizontal alignment values
     */
    public static final List<String> HORIZONTAL_ALIGNMENT = Arrays.asList(
        "Start",
        "Center",
        "End"
    );

    /**
     * Vertical alignment values
     */
    public static final List<String> VERTICAL_ALIGNMENT = Arrays.asList(
        "Start",
        "Center",
        "End"
    );

    /**
     * Font name values
     */
    public static final List<String> FONT_NAMES = Arrays.asList(
        "Default",
        "Secondary"
    );

    /**
     * Get value suggestions for a specific property
     */
    public static List<String> getValueSuggestionsForProperty(String propertyName) {
        return switch (propertyName) {
            case "LayoutMode" -> LAYOUT_MODES;
            case "Visible", "Enabled", "RenderBold", "RenderUppercase", "RenderItalics",
                 "Wrap", "HitTestVisible", "AutoScrollDown", "KeepScrollPosition",
                 "AspectRatio", "Checked", "ScrollEnabled", "ScrollbarVisible",
                 "HorizontalScroll", "VerticalScroll", "ReadOnly", "Password",
                 "CloseOnClickOutside", "OnlyVisibleWhenHovered" -> BOOLEAN_VALUES;
            case "HorizontalAlignment" -> HORIZONTAL_ALIGNMENT;
            case "VerticalAlignment" -> VERTICAL_ALIGNMENT;
            case "Alignment" -> Arrays.asList("Center", "Start", "End");
            case "FontName" -> FONT_NAMES;
            default -> Collections.emptyList();
        };
    }

    /**
     * Get description for a specific value
     */
    public static String getValueDescription(String propertyName, String value) {
        if ("LayoutMode".equals(propertyName)) {
            return switch (value) {
                case "Top" -> "Arrange children from top, no scrolling";
                case "TopScrolling" -> "Arrange children from top with vertical scrolling";
                case "Middle" -> "Arrange children in the middle";
                case "MiddleCenter", "CenterMiddle" -> "Center children both horizontally and vertically";
                case "Center" -> "Center children";
                case "Bottom" -> "Arrange children from bottom, no scrolling";
                case "BottomScrolling" -> "Arrange children from bottom with scrolling";
                case "Left" -> "Arrange children from left";
                case "LeftCenterWrap" -> "Arrange children from left, centered, with wrapping";
                case "Right" -> "Arrange children from right";
                case "Full" -> "Fill entire space";
                default -> "";
            };
        }
        return "";
    }

    /**
     * Get example values for Background property
     */
    public static List<String> getBackgroundExamples() {
        return Arrays.asList(
            "\"path/to/image.png\"",
            "#ffffff",
            "#000000(0.5)",
            "(TexturePath:\"image.png\")",
            "(TexturePath:\"image.png\",Border:15)",
            "(TexturePath:\"image.png\",Color:#ffffff)",
            "(TexturePath:\"image.png\",HorizontalBorder:50,VerticalBorder:0)",
            "(Color:#ffffff)",
            "$Common.@DefaultTooltipBackground"
        );
    }

    /**
     * Get example values for Anchor property
     */
    public static List<String> getAnchorExamples() {
        return Arrays.asList(
            "(Width:100,Height:100)",
            "(Top:0,Left:0)",
            "(Full:0)",
            "(Horizontal:8,Vertical:8)",
            "(Width:200,Height:150,Top:10,Left:10)",
            "(MaxWidth:600)",
            "(MinWidth:200)",
            "(Bottom:10,Right:10)"
        );
    }

    /**
     * Get example values for Padding property
     */
    public static List<String> getPaddingExamples() {
        return Arrays.asList(
            "10",
            "(Full:10)",
            "(Horizontal:16,Vertical:8)",
            "(Top:10,Bottom:10)",
            "(Left:10,Right:10)",
            "(Top:10,Left:8,Right:8,Bottom:5)"
        );
    }

    /**
     * Get example values for ScrollbarStyle property
     */
    public static List<String> getScrollbarStyleExamples() {
        return Arrays.asList(
            "$Common.@DefaultScrollbarStyle",
            "(Spacing:0,Size:0)",
            "(...$Common.@DefaultScrollbarStyle,OnlyVisibleWhenHovered:true)",
            "$Common.@DefaultPlaceholderScrollbarStyle",
            "$Common.@DefaultExtraSpacingScrollbarStyle"
        );
    }

    /**
     * Check if a property expects a color value
     */
    public static boolean isColorProperty(String propertyName) {
        return propertyName.equals("Background") ||
               propertyName.equals("TextColor") ||
               propertyName.endsWith("Color");
    }

    /**
     * Check if a property expects a texture/image path
     */
    public static boolean isTextureProperty(String propertyName) {
        return propertyName.equals("MaskTexturePath") ||
               propertyName.equals("TexturePath") ||
               propertyName.equals("Source") ||
               propertyName.endsWith("Path") ||
               propertyName.endsWith("Icon");
    }
}

