/*****************************************************************************
 *                      J3D.org Copyright (c) 2000
 *                              Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom;

// Standard imports
import javax.media.j3d.*;

import javax.vecmath.Color3f;
import javax.vecmath.Vector3f;
import javax.vecmath.AxisAngle4f;

// Application specific imports

/**
 * Representation of a set of axis around the coordinates.
 * <p>
 *
 * Each axis is color coordinated and the length can be adjusted.
 * <p>
 * X axis: Red<br>
 * Y axis: Green<br>
 * Z axis: Blue
 * <p>
 *
 *
 * @author Jason Taylor, based on the work by Justin Couch
 * @version $Revision: 1.7 $
 */
public class Axis extends Group
{
    /** The default length of the axis */
    private static final float DEFAULT_AXIS_LENGTH = 5;

    /** The size of the box shape on the end */
    private static final float DEFAULT_X_SIZE = 0.05f;

    /**
     * Create a default axis object with each item length 5 from the origin
     */
    public Axis()
    {
        this(DEFAULT_AXIS_LENGTH, 1);
    }
    /**
     * Create an axis object with the given axis length from the origin.
     *
     * @param length The length to use. Must be positive
     */
    public Axis(float length)
    {
        this(length, 1);
    }
    /**
     * Create an axis object with the given axis length from the origin.
     * The transparency of the axis can be controlled through the use of the
     * second parameter. It follows the standard alpha values. A value of
     * 0 is not visible, a value of 1 is completely visible.
     *
     * @param length The length to use. Must be positive
     * @param transparency The amount of alpha channel in the axis
     */
    public Axis(float length, float transparency)
    {
        if(length <= 0)
            throw new IllegalArgumentException("Axis length is not positive");

        float X_SIZE = DEFAULT_X_SIZE;

        // Increases the thickness in propotion to the length if longer then default
        if(length > DEFAULT_AXIS_LENGTH)
            X_SIZE = length * 0.04f;

        // Create a single item of geometry and then share/rotate as needed
        BoxGenerator box_gen = new BoxGenerator(X_SIZE, length, X_SIZE);

        int format = GeometryArray.COORDINATES | GeometryArray.NORMALS;
        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLE_STRIPS;
        data.geometryComponents = GeometryData.NORMAL_DATA;

        box_gen.generate(data);

        TriangleStripArray axis_array =
            new TriangleStripArray(data.vertexCount, format, data.stripCounts);

        axis_array.setCoordinates(0, data.coordinates);
        axis_array.setNormals(0, data.normals);

        ConeGenerator cone_gen = new ConeGenerator(X_SIZE * 4, X_SIZE * 2, 4);
        data.geometryType = GeometryData.INDEXED_TRIANGLE_FANS;
        data.vertexCount = 0;
        data.coordinates = null;
        data.normals = null;
        data.stripCounts = null;

        cone_gen.generate(data);

        CoordinateUtils cu = new CoordinateUtils();
        cu.translate(data.coordinates,
                     data.vertexCount,
                     0,
                     length * 0.5f + (X_SIZE * 2),
                     0);

        IndexedTriangleStripArray cone_array =
            new IndexedTriangleStripArray(data.vertexCount,
                                          format,
                                          data.indexesCount,
                                          data.stripCounts);

        cone_array.setCoordinates(0, data.coordinates);
        cone_array.setNormals(0, data.normals);
        cone_array.setCoordinateIndices(0, data.indexes);
        cone_array.setNormalIndices(0, data.indexes);

        Color3f blue = new Color3f(0, 0, 0.8f);
        Material blue_material = new Material();
        blue_material.setDiffuseColor(blue);
        blue_material.setLightingEnable(true);

        Color3f red = new Color3f(0.8f,0, 0);
        Material red_material = new Material();
        red_material.setDiffuseColor(red);
        red_material.setLightingEnable(true);

        Color3f green = new Color3f(0, 0.8f, 0);
        Material green_material = new Material();
        green_material.setDiffuseColor(green);
        green_material.setLightingEnable(true);

        Appearance x_app = new Appearance();
        x_app.setMaterial(red_material);

        Appearance y_app = new Appearance();
        y_app.setMaterial(green_material);

        Appearance z_app = new Appearance();
        z_app.setMaterial(blue_material);

        if(transparency != 1)
        {
            TransparencyAttributes attr =
                new TransparencyAttributes(TransparencyAttributes.FASTEST,
                                           transparency);

            x_app.setTransparencyAttributes(attr);
            y_app.setTransparencyAttributes(attr);
            z_app.setTransparencyAttributes(attr);
        }

        Shape3D x_shape = new Shape3D();
        x_shape.setAppearance(x_app);
        x_shape.addGeometry(axis_array);
        x_shape.addGeometry(cone_array);

        Shape3D y_shape = new Shape3D();
        y_shape.setAppearance(y_app);
        y_shape.addGeometry(axis_array);
        y_shape.addGeometry(cone_array);

        Shape3D z_shape = new Shape3D();
        z_shape.setAppearance(z_app);
        z_shape.addGeometry(axis_array);
        z_shape.addGeometry(cone_array);

        // The three axis values are all pointing up along the Y axis. Apply a
        // transform to X and Z to move them to the correct position.

        Transform3D tx = new Transform3D();
        AxisAngle4f angle = new AxisAngle4f();

        // X Axis first
        angle.set(0, 0, 1, -(float)(Math.PI * 0.5f));
        tx.setRotation(angle);

        TransformGroup x_tg = new TransformGroup(tx);
        x_tg.addChild(x_shape);

        angle.set(1, 0, 0, (float)(Math.PI * 0.5f));
        tx.setRotation(angle);

        TransformGroup z_tg = new TransformGroup(tx);
        z_tg.addChild(z_shape);

        addChild(x_tg);
        addChild(y_shape);
        addChild(z_tg);
    }
}
