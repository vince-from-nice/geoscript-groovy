package geoscript.style

import org.geotools.styling.Rule
import org.geotools.styling.Mark
import org.geotools.styling.PointSymbolizer
import org.geotools.styling.PolygonSymbolizer
import org.geotools.styling.LineSymbolizer
import org.geotools.styling.TextSymbolizer
import org.geotools.styling.Symbolizer as GtSymbolizer
import org.geotools.styling.Graphic

/**
 * A Symbolizer for point geometries that consists of a color and size.
 * <p>You can create a new Shape with a color, size, type, opacity, and rotation angle:</p>
 * <p><code>def shape = new Shape("#ff0000", 8, "circle", 0.55, 0)</code></p>
 * Or with named parameters:
 * <p><code>def shape = new Shape(type: "star", size: 4, color: "#ff00ff")</code></p>
 * @author Jared Erickson
 */
class Shape extends Symbolizer {

    /**
     * The color (#ff000, blue, [255,255,0])
     */
    String color

    /**
     * The size (6, 10, 12, ect...)
     */
    double size = 6

    /**
     * The type (circle, square, triangle, star, cross, or x).
     */
    String type = "circle"

    /**
     * The Stroke
     */
    Stroke stroke

    /**
     * The rotation angle (0-360 or a geoscript.filter.Function)
     */
    def rotation

    /**
     * The opacity (0: transparent - 1 opaque)
     */
    double opacity

    /**
     * Create a new Shape
     */
    Shape() {
        super()
    }

    /**
     * Create a new Shape with named parameters.
     * <p><code>def shape = new Shape(type: "star", size: 4, color: "#ff00ff")</code></p>
     * @param map A Map of named parameters.
     */
    Shape(Map map) {
        super()
        map.each{k,v->
            if(this.hasProperty(k)){
                this."$k" = v
            }
        }
    }

    /**
     * Create a new Shape.
     * <p><code>def shape = new Shape("#ff0000", 8, "circle", 0.55, 0)</code></p>
     * @param color The color
     * @param size The size
     * @param type The type
     * @param opacity The opacity (0-1)
     * @param angle The angle or rotation (0-360)
     */
    Shape(def color, double size = 6, String type = "circle", double opacity = 1.0, def angle = 0) {
        super()
        this.color = ColorUtil.toHex(color)
        this.opacity = opacity
        this.size = size
        this.type = type
        this.rotation = angle
    }

    /**
     * Set the color
     * @param color  The color (#ffffff, red)
     */
    void setColor(def color) {
        this.color = ColorUtil.toHex(color)
    }

    /**
     * Add a Stroke to this Shape
     * @param color The color
     * @param width The width
     * @param dash The dash pattern
     * @param cap The line cap (round, butt, square)
     * @param join The line join (mitre, round, bevel)
     * @return This Shape
     */
    Shape stroke(def color = "#000000", double width = 1, def dash = null, def cap = null, def join = null) {
        this.stroke = new Stroke(color, width, dash, cap, join)
        this
    }

    /**
     * Prepare the GeoTools Rule by applying this Symbolizer
     * @param rule The GeoTools Rule
     */
    @Override
    protected void prepare(Rule rule) {
        super.prepare(rule)
        getGeoToolsSymbolizers(rule, PointSymbolizer).each{s ->
            apply(s)
        }
    }

    /**
     * Apply this Symbolizer to the GeoTools Symbolizer
     * @param sym The GeoTools Symbolizer
     */
    @Override
    protected void apply(GtSymbolizer sym) {
        super.apply(sym)
        Graphic graphic = createGraphic(sym)
        graphic.size = filterFactory.literal(size)
        if (rotation != null) {
            if (rotation instanceof geoscript.filter.Function) {
                graphic.rotation = rotation.function
            } else if (rotation > 0) {
                graphic.rotation = filterFactory.literal(rotation)
            }
        }
        graphic.graphicalSymbols().clear()
        graphic.graphicalSymbols().add(createMark())
    }

    /**
     * Create a GeoTools Mark from this Shape
     * @return A GeoTools Mark
     */
    protected Mark createMark() {
        Mark mark = styleFactory.createMark()
        if (color != null) {
            mark.fill = new Fill(color, opacity).createFill()
        } else {
            mark.fill = null
        }
        if (stroke) {
            mark.stroke = stroke.createStroke()
        } else {
            mark.stroke = null
        }
        mark.wellKnownName = filterFactory.literal(type)
        return mark
    }

    /**
     * Create a GeoTools Graphic from The GeoTools Symbolizer.
     * @param sym The GeoTools Symbolizer
     * @return A GeoTools Graphic
     */
    protected Graphic createGraphic(GtSymbolizer sym) {
        if (sym instanceof PointSymbolizer || sym instanceof TextSymbolizer) {
            if (!sym.graphic) {
                sym.graphic = styleBuilder.createGraphic()
            }
            return sym.graphic
        } else if (sym instanceof PolygonSymbolizer) {
            if (!sym.fill.graphicFill) {
                sym.fill.graphicFill = styleBuilder.createGraphic()
            }
            return sym.fill.graphicFill
        } else if (sym instanceof LineSymbolizer) {
            if (!sym.stroke.graphicStroke) {
                sym.stroke.graphicStroke = styleBuilder.createGraphic()
            }
            return sym.stroke.graphicStroke
        } else {
            return null;
        }
    }

    /**
     * The string representation
     * @return The string representation
     */
    String toString() {
        buildString("Shape", ['color': color, 'size': size, 'type': type])
    }
}
