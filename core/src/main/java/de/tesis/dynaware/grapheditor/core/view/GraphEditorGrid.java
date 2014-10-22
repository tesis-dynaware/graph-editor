/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.view;

import com.sun.javafx.css.converters.PaintConverter;
import com.sun.javafx.css.converters.SizeConverter;
import de.tesis.dynaware.grapheditor.core.DefaultGraphEditor;
import de.tesis.dynaware.grapheditor.utils.GraphEditorProperties;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;

/**
 * The alignment grid that appears in the background of the editor.
 */
public class GraphEditorGrid extends Pane {

    // This is to make the stroke be drawn 'on pixel'.
    private static final double HALF_PIXEL_OFFSET = -0.5;
    private static final String GRID_STYLE_CLASS = "graph-grid";

    private GraphEditorProperties editorProperties;
    private final Canvas canvas = new Canvas();
    private boolean needsLayout = false;
    
    private final StyleableObjectProperty<Paint> gridColor = new StyleableObjectProperty<Paint>(GraphEditorProperties.DEFAULT_GRID_COLOR) {

        @Override
        public CssMetaData<? extends Styleable, Paint> getCssMetaData() {
            return StyleableProperties.GRID_COLOR;
        }

        @Override
        public Object getBean() {
            return GraphEditorGrid.this;
        }

        @Override
        public String getName() {
            return "gridColor";
        }

        @Override
        protected void invalidated() {
            needsLayout = true;
            requestLayout();
        }
    };
		
    private final StyleableObjectProperty<Number> gridSpacing = new StyleableObjectProperty<Number>(GraphEditorProperties.DEFAULT_GRID_SPACING) {

        @Override
        public CssMetaData<? extends Styleable, Number> getCssMetaData() {
            return StyleableProperties.GRID_SPACING;
        }

        @Override
        public Object getBean() {
            return GraphEditorGrid.this;
        }

        @Override
        public String getName() {
            return "gridSpacing";
        }

        @Override
        protected void invalidated() {
            needsLayout = true;
            requestLayout();
        }
    };

    /**
     * Creates a new grid manager. Only one instance should exist per
     * {@link DefaultGraphEditor} instance.
     */
    public GraphEditorGrid() {
        getStyleClass().add(GRID_STYLE_CLASS);
        getChildren().add(canvas);
        setMouseTransparent(true);
    }

    /**
     * Sets the editor properties object where the grid spacing is stored.
     *
     * @param editorProperties a {@link GraphEditorProperties} instance
     */
    public void setProperties(final GraphEditorProperties editorProperties) {
        // remove binding to old properties:
        if (this.editorProperties != null) {
            gridColor.unbindBidirectional(this.editorProperties.gridColorProperty());
            gridSpacing.unbindBidirectional(this.editorProperties.gridSpacingProperty());
        }

        this.editorProperties = editorProperties;
        
        // bind to properties: (do not use bind() because the property will then not be settable via CSS)
        if (this.editorProperties != null) {
            gridColor.bindBidirectional(this.editorProperties.gridColorProperty());
            gridSpacing.bindBidirectional(this.editorProperties.gridSpacingProperty());
        }
    }
    
    
    
    @Override
    protected void layoutChildren() {
        final int top = (int) snappedTopInset();
        final int right = (int) snappedRightInset();
        final int bottom = (int) snappedBottomInset();
        final int left = (int) snappedLeftInset();
        final int width = (int) getWidth() - left - right;
        final int height = (int) getHeight() - top - bottom;
        final double spacing = gridSpacing.get().doubleValue();

        canvas.setLayoutX(left);
        canvas.setLayoutY(top);

        if (width != canvas.getWidth() || height != canvas.getHeight() || needsLayout) {
            canvas.setWidth(width);
            canvas.setHeight(height);

            GraphicsContext g = canvas.getGraphicsContext2D();
            g.clearRect(0, 0, width, height);
            g.setStroke(gridColor.get());

            final int hLineCount = (int) Math.floor((height + 1) / spacing);
            final int vLineCount = (int) Math.floor((width + 1) / spacing);

            for (int i = 0; i < hLineCount; i++) {
                g.strokeLine(0, snap((i + 1) * spacing), width, snap((i + 1) * spacing));
            }

            for (int i = 0; i < vLineCount; i++) {
                g.strokeLine(snap((i + 1) * spacing), 0, snap((i + 1) * spacing), height);
            }

            needsLayout = false;
        }
    }

    private static double snap(double y) {
        return ((int) y) + HALF_PIXEL_OFFSET;
    }
    
    /**
     * @return The CssMetaData associated with this class, including the
     * CssMetaData of its super classes.
     */
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return getClassCssMetaData();
    }

    private static class StyleableProperties {

        private static final CssMetaData<GraphEditorGrid, Paint> GRID_COLOR = new CssMetaData<GraphEditorGrid, Paint>(
            "-graph-grid-color", PaintConverter.getInstance()) {

            @Override
            public boolean isSettable(GraphEditorGrid node) {
                return !node.gridColor.isBound();
            }

            @Override
            public StyleableProperty<Paint> getStyleableProperty(GraphEditorGrid node) {
                return node.gridColor;
            }
        };

        private static final CssMetaData<GraphEditorGrid, Number> GRID_SPACING = new CssMetaData<GraphEditorGrid, Number>(
            "-graph-grid-spacing", SizeConverter.getInstance()) {

            @Override
            public boolean isSettable(GraphEditorGrid node) {
                return !node.gridSpacing.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(GraphEditorGrid node) {
                return node.gridSpacing;
            }
        };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Pane.getClassCssMetaData());
            styleables.add(GRID_COLOR);
            styleables.add(GRID_SPACING);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
    
}